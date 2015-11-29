package cs221.game;

import cs221.game.Game2048.GameStatus;
import cs221.game.Game2048.Move;
import cs221.util.*;

public abstract class Player {
	protected Game2048 simulator;
	public Player(Game2048 _simulator) {
		simulator = _simulator;
	}
	
	public void resetSimulator() {
		simulator.resetGame();
	}
	
	public abstract Move chooseNextMove();  // abstract
	public abstract void train();  // abstract

	public void play() {
		while (simulator.getGameStatus().equals(GameStatus.NOT_OVER)) {
			Move move = chooseNextMove();
			simulator.performMove(move);
		}
	}
	
	public Pair<Float, Float> evaluate(int numTrials) {
    float scores = 0;
    float wins = 0;
    for (int i = 0; i < numTrials; ++i) {
    	resetSimulator();
    	play();
      scores += simulator.getScore();
      assert simulator.getGameStatus().equals(GameStatus.WIN) || 
      simulator.getGameStatus().equals(GameStatus.LOSE);
      if (simulator.getGameStatus().equals(GameStatus.WIN)) wins += 1;
    }
    return new Pair<Float, Float>(wins/numTrials, scores/numTrials);
	}
}