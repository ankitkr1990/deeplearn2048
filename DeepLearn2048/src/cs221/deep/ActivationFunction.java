package cs221.deep;

import org.ejml.simple.SimpleMatrix;

public interface ActivationFunction {
	public SimpleMatrix valueAt(SimpleMatrix z);
	public SimpleMatrix derivativeValueAt(SimpleMatrix z);
	
	public class Sigmoid implements ActivationFunction {
		@Override
		public SimpleMatrix valueAt(SimpleMatrix z) {
			SimpleMatrix value = new SimpleMatrix(z.numRows(), z.numCols());
			for (int i = 0; i < z.numRows(); ++i)
				value.set(i, 0, 1.0/(1.0+Math.exp(-1*z.get(i, 0))));
			assert !value.hasUncountable();
			return value;
		}
		
		@Override
		public SimpleMatrix derivativeValueAt(SimpleMatrix z) {
			SimpleMatrix val = valueAt(z);
			SimpleMatrix derivative = val.elementMult(Utils.OnesMatrix(z.numRows(), z.numCols()).minus(val));
			assert !derivative.hasUncountable();
			return derivative;
		}
	}

	public class TanH implements ActivationFunction {
		@Override
		public SimpleMatrix valueAt(SimpleMatrix z) {
			SimpleMatrix value = new SimpleMatrix(z.numRows(), z.numCols());
			for (int i = 0; i < z.numRows(); ++i)
				value.set(i, 0, Math.tanh(z.get(i, 0)));
			assert !value.hasUncountable();
			return value;
		}
		
		@Override
		public SimpleMatrix derivativeValueAt(SimpleMatrix z) {
			SimpleMatrix val = valueAt(z);
			SimpleMatrix derivative = Utils.OnesMatrix(z.numRows(), z.numCols()).minus(val.elementMult(val));
			assert !derivative.hasUncountable();
			return derivative;
		}
	}
	
	public class ReLU implements ActivationFunction {
		@Override
		public SimpleMatrix valueAt(SimpleMatrix z) {
			SimpleMatrix value = new SimpleMatrix(z.numRows(), z.numCols());
			for (int i = 0; i < z.numRows(); ++i)
				if (z.get(i, 0) > 0)
					value.set(i, 0, z.get(i, 0));
			assert !value.hasUncountable();
			return value;
		}
		
		@Override
		public SimpleMatrix derivativeValueAt(SimpleMatrix z) {
			SimpleMatrix derivative = new SimpleMatrix(z.numRows(), z.numCols());
			for (int i = 0; i < z.numRows(); ++i)
				if (z.get(i, 0) > 0)
					derivative.set(i, 0, 1);
			assert !derivative.hasUncountable();
			return derivative;
		}
	}
	
	public class LinearUnit implements ActivationFunction {
		@Override
		public SimpleMatrix valueAt(SimpleMatrix z) {
			assert !z.hasUncountable();
			return z;
		}
		
		@Override
		public SimpleMatrix derivativeValueAt(SimpleMatrix z) {
			SimpleMatrix derivative = Utils.OnesMatrix(z.numRows(), z.numCols());
			return derivative;
		}
	}
}