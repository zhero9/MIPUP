import java.util.Random;
import java.util.Vector;

public class RandMatrix {

	private boolean[][] allColumns;

	public RandMatrix() {

	}

	public RandMatrix(int k) {
		this.allColumns = completePoset(k);
	}

	static Random random = new Random();

	public static boolean[][] randMatrix(int m, int n) {
		boolean[][] matrix = new boolean[m][n];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = random.nextBoolean() && random.nextBoolean();
			}
		}
		return filterDoubleColumns(filterZeroRows(matrix));
	}

	public static boolean[][] randMatrix() {

		int m = random.nextInt(5) + 7;
		int n = random.nextInt(30) + 80;
		boolean[][] matrix = new boolean[m][n];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = random.nextBoolean();
			}
		}
		return filterDoubleColumns(filterZeroRows(matrix));
	}

	public boolean[][] completePoset(int k) {
		boolean[][] full = new boolean[k][(int) Math.pow(2, k)];

		for (int i = 0; i < full.length; i++) { // complete poset
			for (int j = 0; j < full[0].length; j++) {
				if ((Math.floor((j) / (Math.pow(2, k - i - 1)))) % 2 == 0) {
					full[i][j] = false;
				} else {
					full[i][j] = true;
				}
			}
		}

		return full;
	}

	public static boolean[][] filterZeroRows(boolean[][] m) {
		// check for zero rows
		Vector<Integer> zeroRows = new Vector<Integer>();
		for (int i = 0; i < m.length; i++) {
			boolean tmp = true; // it is zero row, until we prove different
			int j = 0;
			while (tmp && j < m[0].length) { // try to disprove that i is zero
												// row
				if (m[i][j] == true)
					tmp = false;
				j++;
			}
			if (tmp) {
				// System.out.println("Row "+ i +" is zero row!");
				zeroRows.add(new Integer(i));
			}
		}
		// make same matrix with excluded zero rows
		boolean[][] filteredM = new boolean[m.length - zeroRows.size()][m[0].length];
		int j = 0;
		for (int i = 0; i < m.length; i++) {
			if (!zeroRows.contains(new Integer(i))) {
				filteredM[j] = m[i];
				j++;
			}
		}

		return filteredM;
	}

	public static boolean[][] filterDoubleColumns(boolean[][] m) {
		// check for two equal columns
		Vector<Integer> columnsCopies = new Vector<Integer>();
		for (int s1 = 0; s1 < m[0].length; s1++) {
			for (int s2 = s1 + 1; s2 < m[0].length; s2++) {
				boolean tmp = true;
				int i = 0;
				while (tmp && i < m.length) { // is s1 = s2
					if (m[i][s1] != m[i][s2])
						tmp = false;
					i++;
				}
				if (tmp && !columnsCopies.contains(new Integer(s2))) {
					// System.out.println("Column "+s2+" is double of "+s1+"!");
					columnsCopies.add(new Integer(s2));
				}
			}
		}

		boolean[][] filteredM = new boolean[m.length][m[0].length
				- columnsCopies.size()];
		int j = 0;
		for (int i = 0; i < m[0].length; i++) {
			if (!columnsCopies.contains(new Integer(i))) {
				for (int k = 0; k < m.length; k++) {
					filteredM[k][j] = m[k][i];
				}
				j++;
			}
		}
		return filteredM;
	}

	public static void printMatrix(boolean[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				System.out.print("" + (m[i][j] ? 1 : 0));
			}
			System.out.println();
		}
	}

	public static void print(int[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				System.out.print(" " + m[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}

	public Vector<Integer> makeSubset(int j) {
		Vector<Integer> vector = new Vector<Integer>();
		int k = allColumns[0].length;
		for (int i = 0; i < allColumns[0].length; i++) {
			if ((Math.floor((j) / (Math.pow(2, k - i - 1)))) % 2 != 0) {
				vector.add(new Integer(i));
				// System.out.println("Added "+i+" in the subset "+ j);
			}
		}
		return vector;
	}

	public boolean[][] makeMatrix(int j) {
		Vector<Integer> subset = makeSubset(j);
		boolean[][] matrix = new boolean[allColumns.length][subset.size()];
		int k = 0;
		for (int i = 0; i < allColumns[0].length; i++) {
			if (subset.contains(new Integer(i))) {
				for (int t = 0; t < allColumns.length; t++) {
					matrix[t][k] = allColumns[t][i];
				}
				k++;
			}
		}
		return matrix;

	}
}
