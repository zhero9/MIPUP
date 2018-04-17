import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import ilog.cplex.*;
import ilog.concert.*;

public class MinILP {

	static boolean multipleOptima;
	static boolean[][] matrix;
	static Poset D;
	static int m, n;
	int value;
	int numOfMultipleOptimum;

	public MinILP(boolean[][] M, int numOfMultipleOptimum) {
		this.numOfMultipleOptimum = numOfMultipleOptimum;
		matrix = M;
		D = new Poset(M);
		m = M.length; 		// # of rows
		n = M[0].length;    // # of columns
	}

	public Vector<Digraph> solveOpt() {
		try {
			IloCplex cplex = new IloCplex();

			/*
			 * Variables. Observe that in the program variables y[r][v] should be considered
			 * as variables 1-y[r][v] from the paper!
			 */
			IloNumVar[][] y = new IloNumVar[m][];
			for (int i = 0; i < m; i++)
				y[i] = cplex.boolVarArray(n);

			IloNumVar[][] x = new IloNumVar[n][];
			for (int i = 0; i < n; i++)
				x[i] = cplex.boolVarArray(D.d.outEdges.elementAt(i).size());

			// objective
			IloLinearNumExpr objective = cplex.linearNumExpr();

			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					if (matrix[i][j]) {
						objective.addTerm(1, y[i][j]);
					}
				}
			}

			cplex.addMaximize(objective);

			/*
			 * The first set of constraints i.e. for all u \in V we set a constraint
			 * \sum_{(u,v) \in A } x[u][v] >= 1
			 */
			List<IloRange> constraints = new ArrayList<IloRange>();
			IloLinearNumExpr num_expr = cplex.linearNumExpr();
			for (int s = 0; s < n; s++) {
				if (D.d.outEdges.elementAt(s).size() > 0) {
					num_expr = cplex.linearNumExpr();
					for (int t = 0; t < D.d.outEdges.elementAt(s).size(); t++)
						num_expr.addTerm(1, x[s][t]);
					constraints.add(cplex.addEq(num_expr, 1));
				}
			}

			// the second set of constraints
			for (int r = 0; r < m; r++) {
				for (int s = 0; s < n; s++) {
					if (matrix[r][s]) {
						num_expr = cplex.linearNumExpr();
						num_expr.addTerm(1, y[r][s]);
						for (int t = 0; t < n; t++) {
							for (int index_s = 0; index_s < D.d.outEdges.elementAt(t).size(); index_s++) {
								if (D.d.outEdges.elementAt(t).elementAt(index_s).equals(s) && matrix[r][t]) {
									num_expr.addTerm(-1, x[t][index_s]);
								}
							}
						}
						constraints.add(cplex.addLe(num_expr, 0));
					}
				}
			}

			// check for a zero row and if there is one, then always include first in the
			// branching this is needed in order not to produce same results in multiple
			// optima
			boolean[] isNonZeroColumn = isNonZeroColumn(matrix);
			for (int s = 0; s < n; s++) {
				if (!isNonZeroColumn[s] && D.d.outEdges.elementAt(s).size() > 0) {
					num_expr = cplex.linearNumExpr();
					num_expr.addTerm(1, x[s][0]);
					constraints.add(cplex.addEq(num_expr, 1));
				}
			}

			// cplex.setOut(null);
			Vector<Digraph> solutions = new Vector<Digraph>();

			if (numOfMultipleOptimum > 1) { ////////////////// * Multiple Optima *//////////////////
				
				cplex.setParam(IloCplex.Param.MIP.Pool.AbsGap, 0.0);
				cplex.setParam(IloCplex.Param.MIP.Pool.Intensity, 4);
				cplex.setParam(IloCplex.Param.MIP.Limits.Populate, 2100000000);
				cplex.setParam(IloCplex.Param.MIP.Pool.Capacity, numOfMultipleOptimum);
				// Change parameters AbsGap and Intensity for for different setting on the
				// number of multiple optima. 0.0 and 4 are IBM recommended.

				if (cplex.populate()) {
					System.out.println("ILP: solution status = " + cplex.getStatus());
					this.value = (int) (numberOfOnes(matrix) - cplex.getObjValue());

					/* Get the number of solutions in the solution pool */
					int numsol = cplex.getSolnPoolNsolns();
					System.out.println("ILP: the total number of optimal solutions is: " + numsol + " !");

					/* Get all optimal solutions as branchings */
					for (int k = 0; k < numsol; k++) {
						Digraph branching = new Digraph(n);
						for (int i = 0; i < n; i++) {
							double[] sol = cplex.getValues(x[i], k);
							// System.out.println("Objective ILP = "+value);
							for (int j = 0; j < D.d.outEdges.elementAt(i).size(); j++) {
								if (sol[j] == 1)
									branching.addEdge(i, D.d.outEdges.elementAt(i).elementAt(j));
							}
						}
						solutions.add(branching);
					}
					return solutions;

				} else {
					System.out.println("ILP: Optimal solution not found.");
				}
			} else { /////////////////// * Single Optimum *//////////////////

				Digraph branching = new Digraph(n);
				if (cplex.solve()) {
					this.value = (int) (numberOfOnes(matrix) - cplex.getObjValue());
					// System.out.println("Objective ILP = "+value);
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < D.d.outEdges.elementAt(i).size(); j++) {
							if (cplex.getValues(x[i])[j] == 1)
								branching.addEdge(i, D.d.outEdges.elementAt(i).elementAt(j));
						}
					}
				} else {
					System.out.println("ILP: Optimal solution not found.");
				}
				solutions.add(branching);

				return solutions;
			}

		} catch (Exception exc) {
			System.err.println("ILP: Concert exception '" + exc + "' caught");
			return null;
		}
		return null;

	}

	public static int numberOfOnes(boolean[][] m) {
		int i = 0;
		for (int j = 0; j < m.length; j++) {
			for (int k = 0; k < m[0].length; k++)
				if (m[j][k])
					i++;
		}
		return i;
	}

	public static boolean[] isNonZeroColumn(boolean[][] m) {
		boolean[] num = new boolean[m[0].length];

		for (int j = 0; j < m[0].length; j++) {
			for (int k = 0; k < m.length; k++)
				if (m[k][j])
					num[j] = true;
		}

		return num;
	}

}
