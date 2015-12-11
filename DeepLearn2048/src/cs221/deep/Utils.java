package cs221.deep;

import java.util.Arrays;
import java.util.List;
import org.ejml.simple.SimpleMatrix;

public class Utils {
	public static SimpleMatrix OnesMatrix(int numRows, int numCols) {
		SimpleMatrix A = new SimpleMatrix(numRows, numCols);
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				A.set(i, j, 1);
		return A;
	}
	
	public static List<String> classes = Arrays.asList("O", "LOC", "MISC", "ORG", "PER");
	
	public static SimpleMatrix getClassVector(String label) {
		assert classes.contains(label);
		SimpleMatrix vector = new SimpleMatrix(classes.size(), 1);
		vector.set(classes.indexOf(label), 0, 1);
		return vector;
	}
	
	public static String getLabelFromClassVector(SimpleMatrix vector) {
		assert vector.isVector();
		assert !vector.hasUncountable();
		assert vector.numRows() == classes.size();
		// find the element with highest value
		int index = 0; double maxVal = -1.0;
		for (int i = 0; i < vector.numRows(); ++i) {
			if (vector.get(i, 0) > maxVal) {
				maxVal = vector.get(i, 0);
				index = i;
			}
		}
		return classes.get(index);
	}
	
	public static boolean SimpleMatrixEquals(SimpleMatrix first, SimpleMatrix second) {
		if (first.numRows() != second.numRows() || first.numCols() != second.numCols())
			return false;
		for (int i = 0; i < first.numRows(); ++i)
			for (int j = 0; j < first.numCols(); ++j)
				if (first.get(i, j) != second.get(i, j))
					return false;
		return true;
	}
	
	public static int log2nlz (int num) {
		if (num == 0) 
			return 0;
		return 31 - Integer.numberOfLeadingZeros(num);
	}

	public static float squash(int num) {
		return (float)log2nlz(num)/11;
	}
}