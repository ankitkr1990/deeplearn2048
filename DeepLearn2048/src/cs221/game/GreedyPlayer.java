package cs221.game;

import java.util.ArrayList;
import cs221.game.Game2048.Move;

public class GreedyPlayer extends Player {
	public GreedyPlayer(Game2048 _simulator) {
		super(_simulator);
	}

	@Override
	public Move chooseNextMove() {
		ArrayList<Move> moves = simulator.getValidMoves();
		Move bestMove = null;
		float bestMoveScore = -1;
		for (Move move : moves) {
			float moveScore = simulator.evaluateMove(move).getSecond(); 
			if (moveScore > bestMoveScore) {
				bestMoveScore = moveScore;
				bestMove = move;
			}
		}
		return bestMove;
	}
	
	@Override
	public void train() {
		// no training ;)
	}
}