package cs221.game;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import cs221.game.Game2048.GameStatus;
import cs221.game.Game2048.Move;
import cs221.util.Counter;

public class MonteCarloTreeSearchPlayer extends Player {
	private Game2048 personalSimulator;
	private static int TREE_DEPTH = 100;
	private static HashMap<BoardState, Move> movesCache = null;

	public MonteCarloTreeSearchPlayer(Game2048 _simulator) {
		super(_simulator);
		if (movesCache == null) {
			// warm up cache by reading from file.
			warmUpCache(new String("monte-carlo-tree-search-moves-cache.txt"));
		}
	}

	private void warmUpCache(String filename) {
		movesCache = new HashMap<BoardState, Move>();
		// Try to read from cache file first, if not then need to create cache
		// entries.
		try {
			// System.out.println("Warming up cache");
			FileInputStream fis = new FileInputStream(filename);
			// Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("#");
				assert parts.length == 2;
				movesCache.put(new BoardState(parts[0]), Move.valueOf(parts[1]));
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	@Override
	public Move chooseNextMove() {
		if (movesCache != null) {
			// If present in cache
			Move moveFromCache = movesCache.get(simulator.getState());
			if (moveFromCache != null) {
				//System.out.println("Cache hit");
				return moveFromCache;
			}
		}
		// System.out.println("Cache miss");
		// Play TREE_DEPTH moves with each random valid move and keep track of
		// average scores.
		ArrayList<Move> moves = simulator.getValidMoves();
		Counter<Move> move_scores = new Counter<Move>();
		for (Move move : moves) {
			personalSimulator = new Game2048(simulator);
			int reward = personalSimulator.performMove(move).getSecond();
			RandomPlayer randomPlayer = new RandomPlayer(personalSimulator);
			move_scores.setCount(move, randomPlayer.evaluate(TREE_DEPTH).getSecond() + reward);
		}
		return move_scores.argMax();
	}

	@Override
	public void train() {
		// no training ;)
	}

	public static void createMonteCarloTreeSearchMovesCache(String filename) {
		HashMap<BoardState, Move> movesCache = new HashMap<BoardState, Move>();
		for (int i = 0; i < 100; ++i) {
			Game2048 tempSimulator = new Game2048(true, false);
			Player player = new MonteCarloTreeSearchPlayer(tempSimulator);
			if (i % 1 == 0)
				System.out.println("Played " + i + " games by monte carlo tree search.");
			while (tempSimulator.getGameStatus() == GameStatus.NOT_OVER) {
				Move move = player.chooseNextMove();
				BoardState state = new BoardState(tempSimulator.getState());
				movesCache.put(new BoardState(state), move);
				state.rotate(90);
				movesCache.put(new BoardState(state), rotateMove(move, 90));
				state.rotate(90);
				movesCache.put(new BoardState(state), rotateMove(move, 180));
				state.rotate(90);
				movesCache.put(new BoardState(state), rotateMove(move, 270));
				tempSimulator.performMove(move);
			}
			tempSimulator.resetGame();
		}
		// Dump the map to a file
		try {
			PrintWriter networkParamWriters = new PrintWriter(filename, "UTF-8");
			for (BoardState state : movesCache.keySet()) {
				networkParamWriters.write(state.toStringCompact() + "#" + movesCache.get(state) + "\n");
			}
			networkParamWriters.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static Move rotateMove(Move move, int angle) {
		assert angle == 90 || angle == 180 || angle == 270;
		if (move == Move.UP) {
			if (angle == 90)
				return Move.RIGHT;
			if (angle == 180)
				return Move.DOWN;
			if (angle == 270)
				return Move.LEFT;
		}
		if (move == Move.DOWN) {
			if (angle == 90)
				return Move.LEFT;
			if (angle == 180)
				return Move.UP;
			if (angle == 270)
				return Move.RIGHT;
		}
		if (move == Move.LEFT) {
			if (angle == 90)
				return Move.UP;
			if (angle == 180)
				return Move.RIGHT;
			if (angle == 270)
				return Move.DOWN;
		}
		if (move == Move.RIGHT) {
			if (angle == 90)
				return Move.DOWN;
			if (angle == 180)
				return Move.LEFT;
			if (angle == 270)
				return Move.UP;
		}
		return null;
	}
}