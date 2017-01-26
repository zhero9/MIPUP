import ilog.concert.IloException;

import java.util.Vector;

public class RowSplit {

	private boolean[][] matrix; // / given matrix
	public boolean[][] rowSplitM; // row split of matrix without duplicated
									// columns
	public boolean[][] solution; // final solution
	private int m;
	private int n, t;
	public int[] rows;
	private Digraph optB;
	private Vector<Vector<Integer>> out;
	public Vector<Vector<Integer>> columnsCopies;

	public RowSplit(boolean[][] mat, String alg) {
		matrix = filterDoubleColumns(mat);
		m = matrix.length; // number of rows
		n = matrix[0].length; // number of columns

		if (alg.equals("ip")) {
			System.out.println("RowSplit: Started ILP for min num of rows...");
			MinILP lp = new MinILP(matrix);
			optB = lp.solveOpt();
			t = lp.value;
			System.out.println("Optimal value for |U(B)| = "+t);
			System.out.println("RowSplit: ILP for MCRS finished succesfully.");

		} else if (alg.equals("greedy")) {
			System.out.println("RowSplit: Greedy started...");
			SequentialGreedy sg = new SequentialGreedy(matrix);
			optB = sg.solve();
			t = sg.value();
			System.out.println("RowSplit: Greedy algorithm finished.");

		} else if (alg.equals("ls")) {
			System.out
					.println("RowSplit: Local Search started on random branching...");
			LocalSearch ls = new LocalSearch(matrix);
			optB = ls.branching;
			t = evaluateBranching(optB);
			System.out.println("RowSplit: Local Search finished.");

		} else if (alg.equals("ipd")) {
			System.out.println("RowSplit: Started ILP (min num of DISTINCT rows)...");
			MinDistinctILP distinct = new MinDistinctILP(matrix);
			optB = distinct.solveOpt();
			int optI = distinct.objValue;
			t = evaluateBranching(optB);
			System.out.println("Optimal value for |I(B)| = " + optI);
			System.out.println("with total number of rows |U(B)| = " + t);
			System.out.println("RowSplit: ILP for MCDRS finished succesfully.");
		} else {
			System.out.println("Algotihm "+alg+" does NOT exists!");
		}

		rows = new int[t];
		recoverRowSplit();
		solution = new boolean[rowSplitM.length][mat[0].length];
		solve();

	}

	boolean[][] originalMatrix;

	public RowSplit(int[][] mat, String alg) throws IloException {
		System.out.println("Starting ILP for extended MCRS ...");
		ExtensionILP ext = new ExtensionILP(mat, alg);
		originalMatrix = ext.getOriginalMatrix(); ///optimal given matrix for realized *'s.
		matrix = ext.getMatrix();
		m = matrix.length;
		n = matrix[0].length;
		optB = ext.branching;
		int tmp = ext.objValue;
		if(alg == "ext"){
			t = tmp;
			System.out.println("Minimum number of rows, |U(B)| = "+t);
		}else{
			t = evaluateBranching(optB);
			System.out.println("Optimal value, |I(B)| = " + tmp );
			System.out.println("Number of rows, |U(B)| = "+t);
		}
		
		columnsCopies = ext.getCC();
		rows = new int[t];
		recoverRowSplit();
		solution = new boolean[rowSplitM.length][mat[0].length];
		solve();
	}

	private void solve() {
		for (int i = 0; i < rowSplitM.length; i++) {
			for (int j = 0; j < columnsCopies.size(); j++) {
				for (int k = 0; k < columnsCopies.elementAt(j).size(); k++) {
					solution[i][columnsCopies.elementAt(j).elementAt(k)] = rowSplitM[i][j];
				}
			}
		}
	}

	private void recoverRowSplit() {
		out = new Vector<Vector<Integer>>(); // calcualte B^+(v) for all v\in V
		for (int i = 0; i < optB.outEdges.size(); i++) {
			out.addElement(optB.reachable(i));
		}
		rowSplitM = new boolean[t][matrix[0].length];
		int i = 0;
		for (int r = 0; r < m; r++) {
			for (int v = 0; v < n; v++) {
				if (!r_covered_v(r, v) && matrix[r][v]) {
					// System.out.println("r:"+r+" v:"+v);
					for (int j = 0; j < n; j++) {
						if (out.elementAt(v).contains(j) || v == j) {
							// System.out.println("     i:"+i+" j:"+j);
							rowSplitM[i][j] = true;
						}
					}
					rows[i] = r;
					i++;
				}
			}
		}
	}

	private boolean r_covered_v(int r, int v) {
		for (int i = 0; i < optB.inEdges.elementAt(v).size(); i++) {
			if (matrix[r][optB.inEdges.elementAt(v).elementAt(i)])
				return true;
		}
		return false;
	}

	private boolean[][] filterDoubleColumns(boolean[][] m) {
		// check for two equal columns
		boolean[] tmp = new boolean[m[0].length];

		columnsCopies = new Vector<Vector<Integer>>();
		for (int s1 = 0; s1 < m[0].length; s1++) {
			if (!tmp[s1]) { // s1 is not added
				Vector<Integer> copies = new Vector<Integer>();
				copies.add(s1);
				tmp[s1] = true;
				for (int s2 = s1 + 1; s2 < m[0].length; s2++) {
					boolean tmp1 = true;
					int i = 0;
					while (tmp1 && i < m.length && !tmp[s2]) { // is s1 = s2
						if (m[i][s1] != m[i][s2])
							tmp1 = false;
						i++;
					}
					if (tmp1 && !tmp[s2]) {
						copies.add(s2);
						tmp[s2] = true;
					}
				}
				columnsCopies.add(copies);
			}
		}

		boolean[][] filteredM = new boolean[m.length][columnsCopies.size()];
		for (int i = 0; i < filteredM[0].length; i++) {
			for (int k = 0; k < m.length; k++) {
				filteredM[k][i] = m[k][columnsCopies.elementAt(i).elementAt(0)];
			}
		}
		return filteredM;
	}

	public int evaluateBranching(Digraph b) {
		int[] uncovered = new int[n];
		for (int i = 0; i < n; i++) {
			if (b.inEdges.elementAt(i).size() == 0) {
				uncovered[i] = sizeOfSet(i);
			} else {
				uncovered[i] = sizeOfSet(i)
						- sizeOfSet(disjunctionOfInEdges(i, b));
				// System.out.println(" "+sizeOfSet(disjunctionOfInEdges(i,b)));
			}
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
			sum += uncovered[i];
		}
		return sum;
		// return Arrays.stream(uncovered).sum();
	}

	public boolean[] disjunctionOfInEdges(int s, Digraph b) {
		boolean[] tmp = new boolean[m];
		for (int j = 0; j < b.inEdges.elementAt(s).size(); j++) {
			for (int k = 0; k < m; k++) {
				tmp[k] = tmp[k]
						|| matrix[k][b.inEdges.elementAt(s).elementAt(j)];
			}
		}
		return tmp;
	}

	public int sizeOfSet(int s) {
		int size = 0;
		for (int i = 0; i < m; i++) {
			if (matrix[i][s])
				size++;
		}
		return size;
	}

	public int sizeOfSet(boolean[] s) {
		int size = 0;
		for (int i = 0; i < s.length; i++) {
			if (s[i])
				size++;
		}
		return size;
	}

	public Vector<Vector<Integer>> getCC() {
		return columnsCopies;
	}
}
