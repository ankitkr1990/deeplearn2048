package cs221.deep;

import java.util.List;
import org.ejml.simple.SimpleMatrix;

/**
 * An objective function takes an input vector and computes the error for a
 * given example.
 * 
 * @author Chris Billovits
 */
public interface ObjectiveFunction {
	/**
	 * Evaluates an objective function on top of a Matrix vector input, against a
	 * label.
	 */
	public double valueAt(SimpleMatrix y, SimpleMatrix a);
	public double valueAt(SimpleMatrix y, SimpleMatrix x, List<SimpleMatrix> weights,
			List<SimpleMatrix> biases, List<ActivationFunction> activnFns);
	// The error from the final layer.
	public SimpleMatrix delta(SimpleMatrix y, SimpleMatrix a, SimpleMatrix z, ActivationFunction activnFn);
	
	// CrossEntropyObjectiveFunction
	public class CrossEntropyCost implements ObjectiveFunction {
		@Override
		public double valueAt(SimpleMatrix y, SimpleMatrix a) {
			assert y.isVector() && a.isVector();
			double value = 0;
			for (int i = 0; i < y.numRows(); ++i) {
				value -= 1.0 * y.get(i, 0) * Math.log(a.get(i, 0)) + (1-y.get(i, 0)) * Math.log(1-a.get(i, 0));
			}
			return value;
		}
		
		@Override
		public double valueAt(SimpleMatrix y, SimpleMatrix x, List<SimpleMatrix> weights, 
				List<SimpleMatrix> biases, List<ActivationFunction> activnFns) {
			SimpleMatrix activation = x;
			assert weights.size() == biases.size();
			assert weights.size() == activnFns.size();
			for (int i = 0; i < weights.size(); ++i) {
				SimpleMatrix b = biases.get(i);
				SimpleMatrix w = weights.get(i);
				activation = activnFns.get(i).valueAt(w.mult(activation).plus(b));
			}
			return valueAt(y, activation);
		}
		
		@Override
		public SimpleMatrix delta(SimpleMatrix z, SimpleMatrix a, SimpleMatrix y, ActivationFunction activnFn) {
			assert y.isVector() && a.isVector() && z.isVector();
			SimpleMatrix value = a.minus(y);
			assert !value.hasUncountable();
			return value;
		}
	}
	
	// MeanSquaredError
	public class MeanSquaredError implements ObjectiveFunction {
		@Override
		public double valueAt(SimpleMatrix y, SimpleMatrix a) {
			assert y.isVector() && a.isVector();
			double value = 0;
			for (int i = 0; i < y.numRows(); ++i) {
				value += Math.pow(a.get(i, 0) - y.get(i, 0), 2);
			}
			return value/2;
		}
		
		@Override
		public double valueAt(SimpleMatrix y, SimpleMatrix x, List<SimpleMatrix> weights, 
				List<SimpleMatrix> biases, List<ActivationFunction> activnFns) {
			SimpleMatrix activation = x;
			assert weights.size() == biases.size();
			assert weights.size() == activnFns.size();
			for (int i = 0; i < weights.size(); ++i) {
				SimpleMatrix b = biases.get(i);
				SimpleMatrix w = weights.get(i);
				activation = activnFns.get(i).valueAt(w.mult(activation).plus(b));
			}
			return valueAt(y, activation);
		}
		
		@Override
		public SimpleMatrix delta(SimpleMatrix z, SimpleMatrix a, SimpleMatrix y, ActivationFunction activnFn) {
			assert y.isVector() && a.isVector() && z.isVector();
			SimpleMatrix value = a.minus(y).elementMult(activnFn.derivativeValueAt(z));
			assert !value.hasUncountable();
			return value;
		}
	}
}