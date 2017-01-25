import java.util.Vector;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class ExtensionILP {

	private int m, n, nf;

	private boolean[][] mat; 	// starting matrix i.e. M', * realized, 
								// but with no column copies
	Digraph branching; 			// opt branching of D_M'
	Vector<Vector<Integer>> sameColumns; 	// columns that become same after realizing *
	Vector<Vector<Integer>> columnsCopies;	// columns that are the same as vectors in {0,1,*}^m
	int objValue = 0;

	public ExtensionILP(int[][] matrix, String alg) throws IloException {
		m = matrix.length; 			// number of rows
		nf = matrix[0].length; 		// number of all columns
		int[][] filteredM = findEqualColumns(matrix);
		n = filteredM[0].length; 	// number of DIFFERENT columns
		solve(filteredM, alg);
	}
	
	private int[][] findEqualColumns(int[][] matrix) {
		/* Return matrix with one representative of each set of equal columns.
		 * */
		boolean[] tmp = new boolean[nf];
		columnsCopies = new Vector<Vector<Integer>>();
		for (int s1 = 0; s1 < nf; s1++) {
			if (!tmp[s1]) {
				// s1 is not added in some set of equal columns.
				Vector<Integer> copies = new Vector<Integer>();
				copies.add(s1);
				tmp[s1] = true;
				for (int s2 = s1 + 1; s2 < nf; s2++) {
					boolean tmp1 = true;
					int i = 0;
					while (tmp1 && i < m && !tmp[s2]) { // is s1 == s2
						if (matrix[i][s1] != matrix[i][s2])
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

		int[][] filteredM = new int[m][columnsCopies.size()];
		for (int i = 0; i < filteredM[0].length; i++) {
			for (int k = 0; k < m; k++) {
				filteredM[k][i] = matrix[k][columnsCopies.elementAt(i).elementAt(0)];
			}
		}
		return filteredM;
	}
	
	private void appendNewEqualColumns(){
		/* Some columns become equal after realizing *. 
		 * The sets of equal columns are found by ILP. 
		 * This function merges these new sets with sets of same columns
		 * that were the same before realizing *'s. 
		 */
		boolean[] tmp = new boolean[nf];

		for (int i = sameColumns.size() -1;  i >= 0; i--){
			for(int j = sameColumns.elementAt(i).size()-1; j >= 1; j--){
				for(int k : columnsCopies.elementAt(sameColumns.elementAt(i).elementAt(j)))
					columnsCopies.elementAt(sameColumns.elementAt(i).elementAt(0)).add(k);
			tmp[sameColumns.elementAt(i).elementAt(j)] = true;
			}
		}
		
		for(int i = nf-1; i>0; i-- ){
			if(tmp[i])	columnsCopies.remove(i);
		}
		//printVV(columnsCopies);
	}
	
	public boolean[][] getOriginalMatrix(){
		/* Return matrix starting data matrix, where each of * (missing data)
		 * is realized into 0 or 1. This is used later to find statistics.
		 */
		boolean[][] originalMatrix = new boolean[m][nf];
		appendNewEqualColumns();
		for(int i = 0; i< columnsCopies.size(); i++){
			for(int u:columnsCopies.elementAt(i)){
				for(int r = 0; r < m ; r++){
					originalMatrix[r][u] = mat[r][i];
				}
			}
		}
		return originalMatrix;
	}
	
	public void printVV(Vector<Vector<Integer>> vec){
		for(int i = 0; i< vec.size(); i++){
			for(int j : vec.elementAt(i)){
				System.out.print(" "+j);
			}
			System.out.println();
		}
	}

	public void print(int[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				System.out.print(" " + m[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}

	public void solve(int[][] matrix, String alg) throws IloException {
		System.out.println("Finding set A of all potential arcs ...");
		Digraph A = findA(matrix);

		System.out.println("Setting up extended ILP.");
		IloCplex cplex = new IloCplex();

		/* Variables */
		IloNumVar[][] t = new IloNumVar[m][];
		for (int i = 0; i < m; i++) {
			t[i] = cplex.boolVarArray(n);
		}

		IloNumVar[] p = cplex.boolVarArray(n);

		IloNumVar[][] q = new IloNumVar[n][];
		for (int i = 0; i < n; i++) {
			q[i] = cplex.boolVarArray(n);
		}

		IloNumVar[][] x = new IloNumVar[n][];
		for (int i = 0; i < n; i++) {
			x[i] = cplex.boolVarArray(A.outEdges.elementAt(i).size());
		}

		// Additional variables used to linearize program:
		// tt[r][u][v] = t[r][u]*t[r][v]
		IloNumVar[][][] tt = new IloNumVar[m][][];
		for (int r = 0; r < m; r++) {
			tt[r] = new IloNumVar[n][];
			for (int v = 0; v < n; v++) {
				tt[r][v] = cplex.boolVarArray(n);
			}
		}

		// qp[u][v] = q[u][v]*p[v]
		IloNumVar[][] qp = new IloNumVar[n][];
		for (int u = 0; u < n; u++) {
			qp[u] = cplex.boolVarArray(n);
		}

		// pt[r][v] = p[v] * t[r][v]
		IloNumVar[][] pt = new IloNumVar[m][];
		for (int r = 0; r < m; r++) {
			pt[r] = cplex.boolVarArray(n);
		}

		// tx[r][u][v] = t[r][u] * x[u][v]
		IloNumVar[][][] tx = new IloNumVar[m][][];
		for (int r = 0; r < m; r++) {
			tx[r] = new IloNumVar[n][];
			for (int u = 0; u < n; u++) {
				tx[r][u] = cplex.boolVarArray(A.outEdges.elementAt(u).size());
			}
		}

		// expression for constraints
		IloLinearNumExpr num_expr = cplex.linearNumExpr();

		// /////////////////////////////////////////
		// block of things that differ two models //
		// /////////////////////////////////////////
		if (alg == "ext") { // MCRS
			IloNumVar[][] y = new IloNumVar[m][];
			for (int i = 0; i < m; i++) {
				y[i] = cplex.boolVarArray(n);
			}
			// Objective function
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int r = 0; r < m; r++) {
				for (int v = 0; v < n; v++) {
					if (matrix[r][v] != 0) {
						objective.addTerm(1, y[r][v]);
					}
				}
			}
			cplex.addMinimize(objective);

			// /// 8
			for (int r = 0; r < m; r++) {
				for (int v = 0; v < n; v++) {
					if (matrix[r][v] > 0) {
						num_expr = cplex.linearNumExpr();
						num_expr.addTerm(1, y[r][v]);
						num_expr.addTerm(-1, pt[r][v]);
						for (int u : A.inEdges.elementAt(v)) {
							num_expr.addTerm(1, tx[r][u][A.outEdges
									.elementAt(u).indexOf(v)]);
						}
						cplex.addGe(num_expr, 0);
					}
				}
			}
		} else if (alg == "extd") { // MCDRS
			IloNumVar[] z = cplex.boolVarArray(n);

			// Objective function
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int v = 0; v < n; v++) {
				objective.addTerm(1, z[v]);
			}
			cplex.addMinimize(objective);

			// 8
			for (int r = 0; r < m; r++) {
				for (int v = 0; v < n; v++) {
					if (matrix[r][v] > 0) {
						num_expr = cplex.linearNumExpr();
						num_expr.addTerm(1, z[v]);
						num_expr.addTerm(-1, pt[r][v]);
						for (int u : A.inEdges.elementAt(v)) {
							num_expr.addTerm(1, tx[r][u][A.outEdges
									.elementAt(u).indexOf(v)]);
						}
						cplex.addGe(num_expr, 0);
					}
				}
			}
		}
		///////////// end of block!

		// Constraints  that are same in both models.(All except 8)
		// 1 
		for (int r = 0; r < m; r++) {
			for (int v = 0; v < n; v++) {
				if (matrix[r][v] < 2) {
					cplex.addEq(t[r][v], matrix[r][v]);
				}
			}
		}

		// 2
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				num_expr = cplex.linearNumExpr();
				num_expr.addTerm(1, q[u][v]);
				for (int r = 0; r < m; r++) {
					num_expr.addTerm(1, t[r][u]);
					num_expr.addTerm(1, t[r][v]);
					num_expr.addTerm(-2, tt[r][u][v]);
				}
				cplex.addGe(num_expr, 1);
			}
		}

		// 3
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				num_expr = cplex.linearNumExpr();
				num_expr.addTerm(m, q[u][v]);
				for (int r = 0; r < m; r++) {
					num_expr.addTerm(1, t[r][u]);
					num_expr.addTerm(1, t[r][v]);
					num_expr.addTerm(-2, tt[r][u][v]);
				}
				cplex.addLe(num_expr, m);
			}
		}

		// tt[r][u][v] = t[r][u]*t[r][v]
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				for (int r = 0; r < m; r++) {
					cplex.addLe(tt[r][u][v], t[r][u]);
					cplex.addLe(tt[r][u][v], t[r][v]);
					num_expr = cplex.linearNumExpr();
					num_expr.addTerm(1, tt[r][u][v]);
					num_expr.addTerm(-1, t[r][u]);
					num_expr.addTerm(-1, t[r][v]);
					cplex.addGe(num_expr, -1);
				}
			}
		}

		// 4
		for (int u = 0; u < n; u++) {
			num_expr = cplex.linearNumExpr();
			for (int v = 0; v < n; v++) {
				num_expr.addTerm(1, qp[u][v]);
			}
			cplex.addEq(num_expr, 1);
		}

		// qp[u][v] = q[u][v]*p[v]
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				cplex.addLe(qp[u][v], q[u][v]);
				cplex.addLe(qp[u][v], p[v]);
				num_expr = cplex.linearNumExpr();
				num_expr.addTerm(1, qp[u][v]);
				num_expr.addTerm(-1, q[u][v]);
				num_expr.addTerm(-1, p[v]);
				cplex.addGe(num_expr, -1);
			}
		}

		// 5 & 6 & 7 maybe we should split this,
		// we will see how it affects performance
		IloLinearNumExpr expr = cplex.linearNumExpr();
		for (int u = 0; u < n; u++) {
			expr = cplex.linearNumExpr();
			for (int index_v = 0; index_v < A.outEdges.elementAt(u).size(); index_v++) {

				// 5
				num_expr = cplex.linearNumExpr();
				num_expr.addTerm(2, x[u][index_v]);
				num_expr.addTerm(-1, p[u]);
				num_expr.addTerm(-1,
						p[A.outEdges.elementAt(u).elementAt(index_v)]);
				cplex.addLe(num_expr, 0);

				// 6
				for (int r = 0; r < m; r++) {
					if (matrix[r][u] == 2
							|| matrix[r][A.outEdges.elementAt(u).elementAt(
									index_v)] == 2) {
						num_expr = cplex.linearNumExpr();
						num_expr.addTerm(1, x[u][index_v]);
						num_expr.addTerm(1, t[r][u]);
						num_expr.addTerm(-1, t[r][A.outEdges.elementAt(u)
								.elementAt(index_v)]);
						cplex.addLe(num_expr, 1);
					}
				}
				//*6

				// 7
				expr.addTerm(1, x[u][index_v]);
			}
			cplex.addLe(expr, 1);
		}

		// Linearization for 8
		// pt[r][v] = p[v] * t[r][v]
		for (int r = 0; r < m; r++) {
			for (int v = 0; v < n; v++) {
				cplex.addLe(pt[r][v], t[r][v]);
				cplex.addLe(pt[r][v], p[v]);
				num_expr = cplex.linearNumExpr();
				num_expr.addTerm(1, pt[r][v]);
				num_expr.addTerm(-1, t[r][v]);
				num_expr.addTerm(-1, p[v]);
				cplex.addGe(num_expr, -1);
			}
		}

		// tx[r][u][v] = t[r][u] * x[u][v]
		for (int r = 0; r < m; r++) {
			for (int u = 0; u < n; u++) {
				for (int index_v = 0; index_v < A.outEdges.elementAt(u).size(); index_v++) {
					cplex.addLe(tx[r][u][index_v], x[u][index_v]);
					cplex.addLe(tx[r][u][index_v], t[r][u]);
					num_expr = cplex.linearNumExpr();
					num_expr.addTerm(1, tx[r][u][index_v]);
					num_expr.addTerm(-1, x[u][index_v]);
					num_expr.addTerm(-1, t[r][u]);
					cplex.addGe(num_expr, -1);
				}
			}
		}

		// cplex.setOut(null);
		System.out.println("Sloving ILP (Cplex output): ");
		if (cplex.solve()) {
			// objective value
			//System.out.println("Objective value: " + cplex.getBestObjValue());
			objValue = (int) cplex.getBestObjValue();

			double[] pvalues = new double[n];
			pvalues = cplex.getValues(p);

			// Set of equal columns after realization of *.
			sameColumns = new Vector<Vector<Integer>>();
			boolean[] isAdded = new boolean[n];
			for (int u = 0; u < n; u++) {
				if (!isAdded[u]) {
					Vector<Integer> copies = new Vector<Integer>();
					double[] tmp = cplex.getValues(q[u]);
					for (int v = u; v < n; v++) {
						if (tmp[v] > 0.5) { // q[u,v] = 1
							copies.add(v);
							isAdded[v] = true;
						}
					}
					sameColumns.add(copies);
				}
			}

			// M' --- try to simplify, using hash[]
			int nn = 0;
			for (int i = 0; i < n; i++) {
				if (pvalues[i] > 0.5)
					nn++;
			}
			mat = new boolean[m][nn];
			for (int r = 0; r < m; r++) {
				int k = 0;
				double[] tVal = cplex.getValues(t[r]);
				for (int u = 0; u < sameColumns.size(); u++) {
					for (int v : sameColumns.elementAt(u)) {
						if (pvalues[v] > 0.5) {
							if (tVal[v] > 0.5) {
								mat[r][k] = true;
							} else {
								mat[r][k] = false;
							}
							k++;
						}
					}
				}
			}

			int[] hash = new int[n];
			for (int u = 0; u < sameColumns.size(); u++) {
				for (int v : sameColumns.elementAt(u)) {
					hash[v] = u;
				}
			}

			// Recovering branching from ILP solution.
			branching = new Digraph(nn, "second constructor");
			for (int u = 0; u < n; u++) {
				double[] xu = cplex.getValues(x[u]);
				for (int v = 0; v < A.outEdges.elementAt(u).size(); v++) {
					if (xu[v] > 0.5) {
						branching.addEdge(hash[u], hash[A.outEdges.elementAt(u)
								.elementAt(v)]);
						break;
					}
				}
			}
			// ///// DEBUG ////////
			/*
			 * System.out.println(); System.out.print("p: "); for (int i = 0; i
			 * < n; i++) { System.out.print(" " + cplex.getValue(p[i])); }
			 * 
			 * System.out.println(); System.out.println(); for (int i = 0; i <
			 * n; i++) { for (int j = 0; j < n; j++) { if
			 * (cplex.getValue(q[i][j]) > 0) {
			 * System.out.println("colums equal " + i + " - " + j); } } }
			 * 
			 * System.out.println(); System.out.println("ipsilioni"); for (int r
			 * = 0; r < m; r++) { for (int u = 0; u < n; u++) {
			 * System.out.print(" " + cplex.getValue(y[r][u])); }
			 * System.out.println(); }
			 * 
			 * System.out.println();
			 * System.out.println("matrica sa realiziranim *"); for (int i = 0;
			 * i < m; i++) { for (int j = 0; j < nn; j++) { System.out.print(" "
			 * + (mat[i][j] ? 1 : 0)); } System.out.println(); }
			 * 
			 * System.out.println(); branching.printAdjList();
			 */
		}

		/*
		 * } catch (Exception exc) { System.err.println("Concert exception '" +
		 * exc + "' caught in ExtensionILP"); return; }
		 */
	}
	
	public Digraph findA(int[][] matrix) {
		Digraph d = new Digraph(n, "Second constructor");
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				boolean isUsubsetV = true; 		// u is a subset of v
				int i = 0;
				while (isUsubsetV && i < m) {	// while true u is subset of v
					if (matrix[i][u] == 1 && matrix[i][v] == 0)// 2>m[i][v]>=m[i][u]
						isUsubsetV = false;
					i++;
				}
				if (isUsubsetV) 	d.addEdge(u, v);
			}
		}
		return d;
	}

	public boolean[][] getMatrix() {
		return mat;
	}

	public Vector<Vector<Integer>> getCC() {
		return columnsCopies;
	}

}
