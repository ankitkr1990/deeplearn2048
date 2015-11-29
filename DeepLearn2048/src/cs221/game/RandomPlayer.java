package cs221.game;

import java.util.ArrayList;
import java.util.Random;
import cs221.game.Game2048.Move;

public class RandomPlayer extends Player {
	public RandomPlayer(Game2048 _simulator) {
		super(_simulator);
	}

	@Override
	public Move chooseNextMove() {
		ArrayList<Move> moves = simulator.getValidMoves(); 
		return moves.get(new Random().nextInt(moves.size()));
	}

	@Override
	public void train() {
		// no training ;)
	}
}