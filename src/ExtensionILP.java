import java.util.Vector;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class ExtensionILP {

	private int m, n;

	private boolean[][] mat; // starting matrix i.e. M', * realized, but with no
								// column copies
	boolean[][] originalMatrix;
	Digraph branching; // opt branching of D_M'
	Vector<Vector<Integer>> colCopies;
	int objValue = 0;

	public ExtensionILP(int[][] matrix, String alg) throws IloException {
		// print(matrix);
		m = matrix.length; // number of rows
		n = matrix[0].length; // number of columns
		solve(matrix, alg);
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
		// try {
		Digraph A = findA(matrix);
		// A.printAdjList();

		IloCplex cplex = new IloCplex();

		/* Variables.! */
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

		// Additional variables used to linearize program
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

		// expresion for constraints
		IloLinearNumExpr num_expr = cplex.linearNumExpr();

		// /////////////////////////////////////////
		// block of thigs that differ two models //
		// /////////////////////////////////////////
		if (alg == "ext") {
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

			// tmp, try to delete them

		} else if (alg == "extd") {
			IloNumVar[] z = cplex.boolVarArray(n);

			// Objective function
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int v = 0; v < n; v++) {
				objective.addTerm(1, z[v]);
			}
			cplex.addMinimize(objective);

			// / 8
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
		// ////////////////////////////end

		// / all constraints except 8
		// / 1 //////////////// checked
		for (int r = 0; r < m; r++) {
			for (int v = 0; v < n; v++) {
				if (matrix[r][v] < 2) {
					cplex.addEq(t[r][v], matrix[r][v]);
				}
			}
		}

		// // 2 ///// c
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

		// // 3
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

		// / tt[r][u][v] = t[r][u]*t[r][v]
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

		// // 4 /////////////
		for (int u = 0; u < n; u++) {
			num_expr = cplex.linearNumExpr();
			for (int v = 0; v < n; v++) {
				num_expr.addTerm(1, qp[u][v]);
			}
			cplex.addEq(num_expr, 1);
		}

		// / qp[u][v] = q[u][v]*p[v]
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

		// 5 &&& 6 &&& 7 maybe we should split this, we will see how it
		// affects performance
		IloLinearNumExpr expr = cplex.linearNumExpr();
		for (int u = 0; u < n; u++) {
			expr = cplex.linearNumExpr();
			for (int index_v = 0; index_v < A.outEdges.elementAt(u).size(); index_v++) {

				// / 5 //// c
				num_expr = cplex.linearNumExpr();
				num_expr.addTerm(2, x[u][index_v]);
				num_expr.addTerm(-1, p[u]);
				num_expr.addTerm(-1,
						p[A.outEdges.elementAt(u).elementAt(index_v)]);
				cplex.addLe(num_expr, 0);

				// / 6 //// c
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
				// //*6

				// / 7 ////c
				expr.addTerm(1, x[u][index_v]);
			}
			cplex.addLe(expr, 1);
		}

		// / linearization for 8
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

		if (cplex.solve()) {
			// objective value
			System.out.println("Ob. value: " + cplex.getBestObjValue());
			objValue = (int) cplex.getBestObjValue();

			double[] pvalues = new double[n];
			pvalues = cplex.getValues(p);

			// col copies
			colCopies = new Vector<Vector<Integer>>();
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
					colCopies.add(copies);
				}
			}

			// M' --- try to simplify, using hash[]
			int nn = 0;
			for (int i = 0; i < n; i++) {
				if (pvalues[i] > 0.5)
					nn++;
			}
			mat = new boolean[m][nn];
			originalMatrix = new boolean[m][n];
			for (int r = 0; r < m; r++) {
				int k = 0;
				double[] tVal = cplex.getValues(t[r]);
				for (int u = 0; u < colCopies.size(); u++) {
					for (int v : colCopies.elementAt(u)) {
						if (tVal[v] > 0.5) {
							originalMatrix[r][v] = true;
						} else {
							originalMatrix[r][v] = false;
						}
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
			for (int u = 0; u < colCopies.size(); u++) {
				for (int v : colCopies.elementAt(u)) {
					hash[v] = u;
				}
			}

			// branching
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
				boolean isUsubsetV = true; // u is a subset of v
				int i = 0;
				while (isUsubsetV && i < m) { // while true u is subset of v
					if (matrix[i][u] == 1 && matrix[i][v] == 0)
						isUsubsetV = false;
					// 2>m[i][v]>=m[i][u]
					i++;
				}
				if (isUsubsetV)
					d.addEdge(u, v);
			}
		}
		return d;
	}

	public boolean[][] getMatrix() {
		return mat;
	}

	public Vector<Vector<Integer>> getCC() {
		return colCopies;
	}

}
