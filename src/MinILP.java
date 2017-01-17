import java.util.List;
import java.util.ArrayList;
import ilog.cplex.*;
import ilog.concert.*;


public class MinILP {

	static boolean[][] matrix;
	static Poset D;
	static int m,n;
	int value;

	public MinILP(boolean [][] M){
		matrix = M;
		D = new Poset(M);
		m = M.length; 	// # of rows
		n = M[0].length;  // # of columns
	}

	public Digraph solveOpt(){
		try{
			IloCplex cplex = new IloCplex();

			/* Variables. Observe that in the program variables y[r][v] 
			 * should be considered as variables 1-y[r][v] from the paper!*/
			IloNumVar[][] y = new IloNumVar[m][];
			for(int i = 0; i < m; i++)
				y[i] = cplex.boolVarArray(n);

			IloNumVar[][] x = new IloNumVar[n][];
			for(int i = 0; i< n; i++)
				x[i] = cplex.boolVarArray(D.d.outEdges.elementAt(i).size());

			// objective
			IloLinearNumExpr objective = cplex.linearNumExpr();

			for (int i = 0; i< m; i++){
				for(int j = 0; j< n; j++){
					if (matrix[i][j]){
						objective.addTerm(1, y[i][j]);
					}
				}
			}

			cplex.addMaximize(objective);

			/* The first set of constraints 
			 * i.e. for all u \in V we set a constraint \sum_{(u,v) \in A } x[u][v] >= 1	*/
			List<IloRange> constraints = new ArrayList<IloRange>();
			IloLinearNumExpr num_expr = cplex.linearNumExpr();
			for(int s = 0; s< n; s++){ 										
				if (D.d.outEdges.elementAt(s).size() > 0){ 						
					num_expr = cplex.linearNumExpr(); 							
					for(int t = 0; t< D.d.outEdges.elementAt(s).size(); t++)  
						num_expr.addTerm(1, x[s][t]); 
					constraints.add(cplex.addEq(num_expr, 1));
				}
			}

			// the second set of constraints
			for (int r = 0;  r  < m ; r++){
				for(int s = 0; s < n ; s++){
					if(matrix[r][s]){
						num_expr = cplex.linearNumExpr();
						num_expr.addTerm(1, y[r][s]);
						for(int t = 0; t < n; t++){
							for(int index_s = 0; index_s < D.d.outEdges.elementAt(t).size(); index_s++){
								if (D.d.outEdges.elementAt(t).elementAt(index_s).equals((new Integer(s))) && matrix[r][t]){ 
									num_expr.addTerm(-1, x[t][index_s]);
								}
							}
						}
						constraints.add(cplex.addLe(num_expr, 0));
					}
				}
			}

			//cplex.setOut(null);

			Digraph branching = new Digraph(n);
			if(cplex.solve()){	 
				this.value = (int) (numberOfOnes(matrix)-cplex.getObjValue());
				//System.out.println("Objective ILP = "+value);
				for(int i = 0; i< n; i++){
					for(int j = 0; j< D.d.outEdges.elementAt(i).size(); j++){
						if(cplex.getValues(x[i])[j] == 1) branching.addEdge(i, D.d.outEdges.elementAt(i).elementAt(j));
					}
				}
				
			} else {
				System.out.println("Optimal solution not found");
			}
			return branching;

		}
		catch (Exception exc) {
			System.err.println("Concert exception '" + exc + "' caught");
			return null;
		}
	}

	public static int numberOfOnes(boolean[][] m){
		int i = 0;
		for(int j = 0; j< m.length; j++){
			for(int k = 0; k < m[0].length; k++)
				if(m[j][k]) i++;
		}
		return i;
	}

}
