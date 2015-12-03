package cs221.game;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
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
			if (moveFromCache != null)
				return moveFromCache;
		}
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
}