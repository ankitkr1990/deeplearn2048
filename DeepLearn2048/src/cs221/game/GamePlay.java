package cs221.game;

import java.io.PrintWriter;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import cs221.game.Game2048.GameStatus;
import cs221.game.Game2048.Move;

public class GamePlay {

	private static Move rotateMove(Move move, int angle) {
		assert angle == 90 || angle == 180 || angle == 270;
		if (move == Move.UP) {
			if (angle == 90) return Move.RIGHT;
			if (angle == 180) return Move.DOWN;
			if (angle == 270) return Move.LEFT;
		}
		if (move == Move.DOWN) {
			if (angle == 90) return Move.LEFT;
			if (angle == 180) return Move.UP;
			if (angle == 270) return Move.RIGHT;
		}
		if (move == Move.LEFT) {
			if (angle == 90) return Move.UP;
			if (angle == 180) return Move.RIGHT;
			if (angle == 270) return Move.DOWN;
		}
		if (move == Move.RIGHT) {
			if (angle == 90) return Move.DOWN;
			if (angle == 180) return Move.LEFT;
			if (angle == 270) return Move.UP;
		}
		return null;
	}
	
	public static void crateMonteCarloTreeSearchMovesCache(String filename) {
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
				networkParamWriters.write(state.toStringCompact() + "#" + movesCache.get(state)+ "\n");
			}
			networkParamWriters.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) {
		boolean displayActive = false;
		Game2048 gameEngine = new Game2048(true /* automated */, displayActive);
		if (displayActive) {
			JFrame game = new JFrame();
			game.setTitle("2048 Game");
			game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			game.setSize(340, 400);
			game.setResizable(false);
			game.add(gameEngine.display);
			game.setLocationRelativeTo(null);
			game.setVisible(true);
		}

		/*
		// Evaluate random player
		gameEngine.resetGame();
		Player randomPlayer = new RandomPlayer(gameEngine);
		System.out.println("RandomPlayer performance : " + randomPlayer.evaluate(1000));

		// Evaluate greedy player
		gameEngine.resetGame();
		Player greedyPlayer = new GreedyPlayer(gameEngine);
		System.out.println("GreedyPlayer performance : " + greedyPlayer.evaluate(1000));

		// Evaluate monte carlo tree search player
		gameEngine.resetGame();
		Player treeSearchPlayer = new MonteCarloTreeSearchPlayer(gameEngine);
		System.out.println("RandomPlayer performance : " + treeSearchPlayer.evaluate(10));
		*/
		
		crateMonteCarloTreeSearchMovesCache("monte-carlo-tree-search-moves-cache.txt");
		System.exit(0);
	  // Evaluate player trained by deep learning
		gameEngine.resetGame();
		NetworkPlayer networkPlayer = new NetworkPlayer(gameEngine);
	  //networkPlayer.loadFromFile();
		networkPlayer.train();
	  //networkPlayer.play();
		System.out.println("NetworkPlayer performance : " + networkPlayer.evaluate(1000));
	}
}