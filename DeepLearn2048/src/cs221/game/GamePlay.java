package cs221.game;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class GamePlay {
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
		*/
		
	  // Evaluate player trained by deep learning
		gameEngine.resetGame();
		Player networkPlayer = new NetworkPlayer(gameEngine);
		networkPlayer.train();
		System.out.println("NetworkPlayer performance : " + networkPlayer.evaluate(1000));
	}
}