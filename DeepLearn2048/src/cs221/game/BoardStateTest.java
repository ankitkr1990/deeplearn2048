package cs221.game;

import java.util.Arrays;
import cs221.game.Game2048.Move;
import cs221.util.Triplet;

public class BoardStateTest extends junit.framework.TestCase {

	public void testGetValidMoves() {
		String strState = 
				new String(
						"|2|0|0|0|\n" +
				    "|4|2|4|2|\n" +
						"|4|2|0|0|\n" +
				    "|2|0|0|0|\n");
		BoardState state = new BoardState(strState);
		assertEquals(strState, state.toString());
		assertEquals(Arrays.asList(Move.RIGHT, Move.UP, Move.DOWN), state.getValidMoves());
	}
	
	public void testCompactToString() {
		String strState = 
				new String(
						"|2|0|0|0|\n" +
				    "|4|2|4|2|\n" +
						"|4|2|0|0|\n" +
				    "|2|0|0|0|\n");
		String compactStrState = 
				new String(
						"|2|0|0|0|4|2|4|2|4|2|0|0|2|0|0|0|");
		BoardState state = new BoardState(strState);
		BoardState compactState = new BoardState(compactStrState);
		assertEquals(state, compactState);
		assertEquals(state.toStringCompact(), compactStrState);
	}
	
	public void testEvaluateMove() {
		BoardState state = new BoardState(
				new String(
						"|4|2|0|2|\n" +
						"|4|2|0|0|\n" +						
						"|4|2|0|0|\n" +
						"|4|2|0|0|\n"));
		BoardState afterState = new BoardState(
				new String(
						"|4|4|0|0|\n" +
						"|4|2|0|0|\n" +						
						"|4|2|0|0|\n" +
						"|4|2|0|0|\n"));
		BoardState copy = new BoardState(state); 
		Triplet<BoardState, Integer, Boolean> output = state.evaluateMove(Move.LEFT);
		assertTrue(output.getThird());  // move was made
		assertEquals(copy, state); // state is unchanged on evaluating a move
		assertEquals(afterState, output.getFirst());
		assertEquals(new Integer(4), output.getSecond());  // expected reward
	}
}
