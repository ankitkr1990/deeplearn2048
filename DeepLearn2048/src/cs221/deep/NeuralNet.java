package cs221.deep;

import cs221.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import org.ejml.simple.*;

public class NeuralNet {

	static Boolean do_gradient_check = true;

	public int mini_batch_size, numLayers;
	public double learningRate, lambda;
	public List<Integer> sizes;
	public ObjectiveFunction costFn;
	public List<ActivationFunction> activnFns;

	public List<SimpleMatrix> biases, weights;

	public NeuralNet(double _lr, double _lambda, int _mini_batch_size, int _numlayers, List<Integer> _sizes,
			ObjectiveFunction _costFn, List<ActivationFunction> _activnFns) {
		learningRate = _lr;
		lambda = _lambda;
		mini_batch_size = _mini_batch_size;
		numLayers = _numlayers;
		sizes = _sizes;
		costFn = _costFn;
		activnFns = _activnFns;
		initWeights();
	}

	public NeuralNet(NeuralNet other) {
		this.learningRate = other.learningRate;
		lambda = other.lambda;
		mini_batch_size = other.mini_batch_size;
		numLayers = other.numLayers;
		sizes = other.sizes;
		costFn = other.costFn;
		activnFns = other.activnFns;
		// deep copy weights and biases
		this.weights = new ArrayList<SimpleMatrix>(this.numLayers - 1);
		this.biases = new ArrayList<SimpleMatrix>(this.numLayers - 1);
		for (SimpleMatrix w : other.weights) {
			SimpleMatrix copy = new SimpleMatrix(w);
			//assert Utils.SimpleMatrixEquals(copy, w);
			this.weights.add(copy);
		}
		for (SimpleMatrix b : other.biases) {
			this.biases.add(new SimpleMatrix(b));
		}
	}
	
	public boolean equals(NeuralNet other) {
		if (this.learningRate != other.learningRate ||
				this.lambda != other.lambda ||
				this.mini_batch_size != other.mini_batch_size ||
				this.numLayers != other.numLayers ||
				!this.sizes.equals(other.sizes) ||
				this.weights.size() != other.weights.size() ||
				this.biases.size() != other.biases.size()) {
			return false;
		}
		for (int i = 0; i < this.weights.size(); ++i)
			if (!Utils.SimpleMatrixEquals(this.weights.get(i), other.weights.get(i))) {
				return false;
			}
		for (int i = 0; i < this.biases.size(); ++i)
			if (!Utils.SimpleMatrixEquals(this.biases.get(i), other.biases.get(i))) {
				return false;
			}
		return true;
	}

	/**
	 * Initializes the weights randomly.
	 */
	public void initWeights() {
		this.biases = new ArrayList<SimpleMatrix>(this.numLayers - 1);
		this.weights = new ArrayList<SimpleMatrix>(this.numLayers - 1);
		for (int i = 1; i < this.numLayers; ++i) {
			this.biases.add(new SimpleMatrix(this.sizes.get(i), 1)); // Careful in
																																// indices
			double epsilon = Math.sqrt(6) / Math.sqrt(this.sizes.get(i - 1) + this.sizes.get(i));
			this.weights.add(
					SimpleMatrix.random(this.sizes.get(i), this.sizes.get(i - 1),  1e-2, epsilon, new Random(System.nanoTime())));
		}
	}

	public void DumpNetworkParams() {
		try {
			// Dump network parameters.
			PrintWriter networkParamWriters = new PrintWriter("network-params.txt", "UTF-8");
			for (int l = 0; l < this.numLayers - 1; ++l) {
				networkParamWriters.println("Stats for layer : " + (l+1));
				networkParamWriters.println("W : " + this.weights.get(l));
				networkParamWriters.println("b : " + this.biases.get(l));
			}
			networkParamWriters.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void save() {
		try {
			for (int i = 0; i < this.weights.size(); ++i) {
				this.weights.get(i).saveToFileBinary("weights_layer_" + (i + 1) + ".data");
				SimpleMatrix temp = SimpleMatrix.loadBinary("weights_layer_" + (i + 1) + ".data");
			}
			for (int i = 0; i < this.weights.size(); ++i)
				this.biases.get(i).saveToFileBinary("biases_layer_" + (i + 1) + ".data");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		DumpNetworkParams();  // dump a human readable version of weights.
	}

	public void loadFromFile() {
		try {
			for (int i = 0; i < this.numLayers - 1; ++i)
				this.weights.set(i, SimpleMatrix.loadBinary("weights_layer_" + (i + 1) + ".data"));
			for (int i = 0; i < this.numLayers - 1; ++i)
				this.biases.set(i, SimpleMatrix.loadBinary("biases_layer_" + (i + 1) + ".data"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public SimpleMatrix feedForward(SimpleMatrix featureVector) {
		for (int i = 1; i < this.numLayers; ++i) {
			SimpleMatrix b = this.biases.get(i - 1);
			SimpleMatrix w = this.weights.get(i - 1);
			featureVector = this.activnFns.get(i - 1).valueAt(w.mult(featureVector).plus(b));
		}
		return featureVector;
	}

	public void SGDWithMiniBatch(List<Pair<SimpleMatrix, SimpleMatrix>> mini_batch) {
		List<SimpleMatrix> delta_b = new ArrayList<SimpleMatrix>(this.biases.size());
		List<SimpleMatrix> delta_w = new ArrayList<SimpleMatrix>(this.weights.size());
		// Initialization
		for (int i = 0; i < this.biases.size(); ++i) {
			delta_b.add(new SimpleMatrix(this.biases.get(i).numRows(), this.biases.get(i).numCols()));
		}
		for (int i = 0; i < this.weights.size(); ++i) {
			delta_w.add(new SimpleMatrix(this.weights.get(i).numRows(), this.weights.get(i).numCols()));
		}
		// Update with BackProp
		for (int i = 0; i < mini_batch.size(); ++i) {
			SimpleMatrix x = mini_batch.get(i).getFirst();
			Pair<List<SimpleMatrix>, List<SimpleMatrix>> deltas = backProp(x, mini_batch.get(i).getSecond());
			UpdateListWithSum(delta_b, deltas.getFirst());
			UpdateListWithSum(delta_w, deltas.getSecond());
		}
		assert this.weights.size() == delta_w.size();
		assert this.biases.size() == delta_b.size();
		// Update weights
		for (int i = 0; i < this.weights.size(); ++i) {
			SimpleMatrix w = this.weights.get(i);
			SimpleMatrix d_w = delta_w.get(i);
			assert !d_w.hasUncountable();
			SimpleMatrix new_w = w.scale(1 - learningRate * (lambda)).minus(d_w.scale(learningRate / mini_batch.size()));
			assert !new_w.hasUncountable();
			//System.out.println("Previous: " + w.elementSum() + "  New = " + new_w.elementSum());
			assert new_w.elementMaxAbs() > 0;
			this.weights.set(i, new_w);
		}
		// Update biases
		for (int i = 0; i < this.biases.size(); ++i) {
			SimpleMatrix b = this.biases.get(i);
			SimpleMatrix d_b = delta_b.get(i);
			assert !d_b.hasUncountable();
			SimpleMatrix new_b = b.minus(d_b.scale(learningRate / mini_batch.size()));
			assert !new_b.hasUncountable();
			this.biases.set(i, new_b);
		}
	}

	public Pair<List<SimpleMatrix>, List<SimpleMatrix>> backProp(SimpleMatrix x, SimpleMatrix y) {
		List<SimpleMatrix> delta_b = new ArrayList<SimpleMatrix>(this.biases.size());
		for (int i = 0; i < this.biases.size(); ++i)
			delta_b.add(null);
		List<SimpleMatrix> delta_w = new ArrayList<SimpleMatrix>(this.weights.size());
		for (int i = 0; i < this.weights.size(); ++i)
			delta_w.add(null);
		// feedforward first
		List<SimpleMatrix> activations = new ArrayList<SimpleMatrix>(); // Store
																																		// layer by
																																		// layer
																																		// activations
		SimpleMatrix activation = x;
		activations.add(activation);
		List<SimpleMatrix> zs = new ArrayList<SimpleMatrix>(); // Store layer by
																														// layer z values
		for (int i = 1; i < this.numLayers; ++i) {
			SimpleMatrix b = this.biases.get(i - 1);
			SimpleMatrix w = this.weights.get(i - 1);
			SimpleMatrix z = w.mult(activation).plus(b);
			assert !z.hasUncountable();
			zs.add(z);
			activation = this.activnFns.get(i - 1).valueAt(z);
			assert !activation.hasUncountable();
			activations.add(activation);
		}
		// backward pass
		SimpleMatrix delta = this.costFn.delta(zs.get(zs.size() - 1), activations.get(activations.size() - 1), y,
				this.activnFns.get(this.activnFns.size() - 1));
		delta_b.set(delta_b.size() - 1, delta);
		delta_w.set(delta_w.size() - 1, delta.mult(activations.get(activations.size() - 2).transpose()));
		for (int l = 2; l < this.numLayers; ++l) {
			SimpleMatrix z = zs.get(zs.size() - l);
			SimpleMatrix derivativeValue = this.activnFns.get(this.activnFns.size() - l).derivativeValueAt(z);
			delta = this.weights.get(this.weights.size() - l + 1).transpose().mult(delta).elementMult(derivativeValue);
			// assert delta.elementMaxAbs() > 0;  // the gradient must not vanish, otherwise learning will stall.
			assert !delta.hasUncountable();
			delta_b.set(delta_b.size() - l, delta);
			delta_w.set(delta_w.size() - l, delta.mult(activations.get(activations.size() - l - 1).transpose()));
		}
		if (this.do_gradient_check) {
			// Perform gradient check.
			//GradientCheck.check(x, y, this.weights, this.biases, this.activnFns, this.costFn, this.weights, delta_w);
			//GradientCheck.check(x, y, this.weights, this.biases, this.activnFns, this.costFn, this.biases, delta_b);
		}
		return new Pair<List<SimpleMatrix>, List<SimpleMatrix>>(delta_b, delta_w);
	}

	public float fractionOfDeadNeurons(List<Pair<SimpleMatrix, SimpleMatrix>> batch) {
		int numNeurons = 0, numDeadNeurons = 0;
		for (int i = 1; i < this.numLayers; ++i)
			numNeurons += this.sizes.get(i);
		boolean neuronActive[] = new boolean[numNeurons];
		for (int i = 0; i < numNeurons; ++i)
			neuronActive[i] = false;

		for (Pair<SimpleMatrix, SimpleMatrix> datapoint : batch) {
			SimpleMatrix featureVector = datapoint.getFirst();
			int index = 0;
			for (int i = 1; i < this.numLayers; ++i) {
				SimpleMatrix b = this.biases.get(i - 1);
				SimpleMatrix w = this.weights.get(i - 1);
				featureVector = this.activnFns.get(i - 1).valueAt(w.mult(featureVector).plus(b));
				for (int j = 0; j < featureVector.numRows(); ++j)
					if (featureVector.get(j, 0) != 0)
						neuronActive[index + j] = true;
				index += this.sizes.get(i);
			}
		}
		for (int i = 0; i < numNeurons; ++i)
			if (!neuronActive[i])
				++numDeadNeurons;
		return (float)numDeadNeurons/numNeurons;
	}

	// target += valueToAdd
	private void UpdateListWithSum(List<SimpleMatrix> target, List<SimpleMatrix> valueToAdd) {
		assert target.size() == valueToAdd.size();
		for (int i = 0; i < target.size(); ++i) {
			target.set(i, target.get(i).plus(valueToAdd.get(i)));
		}
	}
}
