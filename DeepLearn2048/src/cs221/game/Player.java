package cs221.game;

import cs221.game.Game2048.GameStatus;
import cs221.game.Game2048.Move;
import cs221.util.*;

public abstract class Player {
	protected Game2048 simulator;
	protected Game2048 startingSimulator;  // a backup copy of the simulator with which game starts
	public Player(Game2048 _simulator) {
		simulator = _simulator;
		startingSimulator = new Game2048(simulator);
	}
	
	public void resetSimulator() {
		simulator.copyStateFrom(startingSimulator);
		//simulator.resetGame();
	}
	
	public abstract Move chooseNextMove();  // abstract
	public abstract void train();  // abstract

	public void play() {
		while (simulator.getGameStatus().equals(GameStatus.NOT_OVER)) {
			//System.out.println();
			//System.out.println();
			//System.out.println();
			//System.out.println("!-------------PLAY --------------!");
			//System.out.println("State before choosing next move : " + simulator.getState());
			//BoardState temp = new BoardState(simulator.getState());
			Move move = chooseNextMove();
			//System.out.println("State after choosing next move : " + simulator.getState());
			//assert temp.equals(simulator.getState());  // state should be unchanged
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
    return new Pair<Float, Float>(wins/numTrials * 100, scores/numTrials);
	}
}