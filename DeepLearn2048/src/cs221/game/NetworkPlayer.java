package cs221.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.ejml.simple.SimpleMatrix;
import cs221.game.Game2048.GameStatus;
import cs221.game.Game2048.Move;
import cs221.deep.*;
import cs221.util.*;

class NetworkPlayer extends Player {
	private Game2048 trainSimulator;
	public NeuralNet network, fixedNetwork;

	// static parameters.
	private static boolean squashNums = true;
	private static int replayMemoryLimit = 100000;
	private static int minibatchSize = 10;
	private static int numTrialsToTrain = 50000;
	private static int numMovesToFixTarget = 10;
	private static float explorationProbability = 0.3f;
	private static float discount = 0.2f;
	
	private TransitionRecord[] replayMemory;
	private int numRecordsInreplayMemory;
	private Random randomizer;

	public NetworkPlayer(Game2048 _simulator) {
		super(_simulator);
		trainSimulator = new Game2048(true /* automated */, false /* displayActive */);
		network = new NeuralNet(
				0.01 /* lr */, 
				0 /* lambda */,
				minibatchSize,
				4 /* numlayers */,
				Arrays.asList(16, 100, 100, 1) /* sizes */,
				new ObjectiveFunction.MeanSquaredError() /* costFn */,
				Arrays.asList(new ActivationFunction.ReLU(), new ActivationFunction.ReLU(), new ActivationFunction.LinearUnit()) /* activnFns */);
		fixedNetwork = new NeuralNet(network);
		replayMemory = new TransitionRecord[replayMemoryLimit];
		numRecordsInreplayMemory = 0;
		randomizer = new Random(100);
		warmUpReplayMemory();
	}

	public void loadFromFile() {
		this.network.loadFromFile();
		this.fixedNetwork = new NeuralNet(this.network);
	}
	
	private int log2nlz (int num) {
		if (num == 0) 
			return 0;
		return 31 - Integer.numberOfLeadingZeros(num);
	}

	private float squash(int num) {
		if (squashNums)
			return (float)log2nlz(num)/11;
		return num;
	}

	private SimpleMatrix getNetworkInput(BoardState state) {
		SimpleMatrix output = new SimpleMatrix(16, 1);
		for (int i = 0; i < 4; ++i)
			for (int j = 0; j < 4; ++j)
				output.set(i + 4 *j, squash(state.tileAt(i, j).value));
		return output;
	}

	static class TransitionRecord {
		public BoardState stateBeforeMove;
		public Move move;
		public BoardState afterState;
		public BoardState stateAfterMove;
		public float reward;
		
		public TransitionRecord(BoardState _stateBeforeMove, 
				Move _move, BoardState _afterState, 
				BoardState _stateAfterMove, float _reward) {
			this.stateAfterMove = _stateBeforeMove;
			this.move = _move;
			this.afterState = _afterState;
			this.stateAfterMove = _stateAfterMove;
			this.reward = _reward;
		}
	}

  private TransitionRecord generateNewMoveAndRecord() {
  	Move move = generateEpsilonGreedyMove();
  	assert move != null;
  	BoardState stateBeforeMove = trainSimulator.getState();
  	Pair<BoardState, Integer> t = trainSimulator.performMove(move);
  	BoardState afterState = t.getFirst();
  	BoardState stateAfterMove = trainSimulator.getState();
  	float reward = squash(t.getSecond());  	
  	if (trainSimulator.getGameStatus() == GameStatus.WIN) {
  		reward = 5;
  	} else if (trainSimulator.getGameStatus() == GameStatus.LOSE) {
  		reward = -5;
  	}
  	TransitionRecord record = new TransitionRecord(stateBeforeMove, move, afterState, stateAfterMove, reward);
    addToReplayMemory(record);
    return record;
  }
  
  private void addToReplayMemory(TransitionRecord record) {
  	if (numRecordsInreplayMemory >= replayMemoryLimit) {
  		int index = new Random().nextInt(numRecordsInreplayMemory);
  		replayMemory[index] = record;
  	} else {
  		replayMemory[numRecordsInreplayMemory] = record;
  		++numRecordsInreplayMemory;
  	}
  }
  
  private void warmUpReplayMemory() {
  	System.out.println("Warming up record memory");
  	while (numRecordsInreplayMemory < 2 * minibatchSize) {
  		if (trainSimulator.getGameStatus() == GameStatus.NOT_OVER) {
  			generateNewMoveAndRecord();
  		} else {
  			trainSimulator.resetGame();
  		}
  	}
  	System.out.println("Warming up complete");
  }
  
  private void getBatchOfSize(int size, List<TransitionRecord> batch) {
  	for (int i = 0; i < size; ++i)
  		batch.add(replayMemory[randomizer.nextInt(numRecordsInreplayMemory)]);
  }
  
  private List<Pair<SimpleMatrix, SimpleMatrix>> prepareMiniBatch(List<TransitionRecord> records) {
  	List<Pair<SimpleMatrix, SimpleMatrix>> batch = new ArrayList<Pair<SimpleMatrix, SimpleMatrix>>();
  	for (TransitionRecord record : records) {
  		double target = record.reward + this.discount * getV(record.stateAfterMove).getSecond();
  		SimpleMatrix y = new SimpleMatrix(1, 1);
  		y.set(0, 0, target);
  		batch.add(new Pair<SimpleMatrix, SimpleMatrix>(getNetworkInput(record.afterState), y));
  	}
  	return batch;
  }

  private double getQ(BoardState state, Move move) {
    // create the afterstate, neural net learns on afterstate
  	Triplet<BoardState, Integer, Boolean> t = state.evaluateMove(move);
    BoardState afterState = t.getFirst();
    SimpleMatrix y = fixedNetwork.feedForward(getNetworkInput(afterState));
    assert y.isVector();
    assert !y.hasUncountable();
    return y.get(0, 0);
  }
  
  private Pair<Move, Double> getV(BoardState state) {
    if (state.getValidMoves().size() == 0) {
    	return new Pair<Move, Double>(null, new Double(0));
    }
    Move bestMove = null; double bestMoveScore = - 1 * Double.MAX_VALUE;
    for (Move move : state.getValidMoves()) {
    	double moveScore = getQ(state, move);
    	if (moveScore > bestMoveScore) {
    		bestMove = move;
    		bestMoveScore = moveScore; 
    	}
    }
    return new Pair<Move, Double>(bestMove, bestMoveScore);
  }
  
  private Move generateEpsilonGreedyMove() {
  	ArrayList<Move> moves = trainSimulator.getValidMoves();
  	assert moves.size() > 0;
  	if (randomizer.nextFloat() < this.explorationProbability) {
  		//return moves.get(randomizer.nextInt(moves.size()));
  		// take the monte carlo tree search move from this state to be the guided exploration.
  		MonteCarloTreeSearchPlayer treeSearchPlayer = new MonteCarloTreeSearchPlayer(trainSimulator);
  		return treeSearchPlayer.chooseNextMove();
  	}
  	// Choose the optimal move.
  	Move bestMove = getV(trainSimulator.getState()).getFirst();
  	assert bestMove != null;
  	return bestMove;
  }
  
	@Override
	public Move chooseNextMove() {
		// The move would be the one with the best V function.
		//System.out.println("!--ChooseNextMove--!");
		//System.out.println("State " + simulator.getState());
		//System.out.println("Simulator available moves : " + simulator.getValidMoves());
		//System.out.println("State available moves : " + simulator.getState().getValidMoves());
		Move move = getV(simulator.getState()).getFirst();
		//System.out.println("Chosen move : " + move);
		return move;
	}

	@Override
	public void train() {
		for (int i = 0; i < this.numTrialsToTrain; ++i) {
			trainSimulator.resetGame();
			int numMoves = 0;
			while (trainSimulator.getGameStatus() == GameStatus.NOT_OVER) {
				if (numMoves % numMovesToFixTarget == 0) {
					fixedNetwork = new NeuralNet(network);
				}
				TransitionRecord record = generateNewMoveAndRecord();
				List<TransitionRecord> sample = new ArrayList<TransitionRecord>(minibatchSize);
				if (minibatchSize > 1) {
					getBatchOfSize(this.minibatchSize - 1, sample);
				}
				sample.add(record);
				Collections.shuffle(sample); // shuffle
				List<Pair<SimpleMatrix, SimpleMatrix>> mini_batch = prepareMiniBatch(sample);
				network.SGDWithMiniBatch(mini_batch);
				++numMoves;
			}
			if (i % 250 == 0) {
				System.out.println("Trial " + i + " finished in " + numMoves + " moves");
				network.save();
				// Compute the number of dead units in the network if using non leaky reLUs
				/*List<TransitionRecord> sample = new ArrayList<TransitionRecord>(100);
				getBatchOfSize(100, sample);
				List<Pair<SimpleMatrix, SimpleMatrix>> batch = prepareMiniBatch(sample);
				System.out.println("% of dead neurons : " + network.fractionOfDeadNeurons(batch) * 100);*/
			}
		}
		// training done - for good or worse.
		fixedNetwork = new NeuralNet(network);  // predictions are done by fixednetwork.
		network.save();
	}
}