import java.util.List;
import java.util.ArrayList;
import ilog.cplex.*;
import ilog.concert.*;


public class MinDistinctILP {

	static boolean[][] matrix;
	static Poset D;
	static int m,n;
	int objValue;

	public MinDistinctILP(boolean [][] M){
		matrix = M;
		D = new Poset(M);
		m = M.length; // # of rows
		n = M[0].length;  // # of columns
	}

	public Digraph solveOpt(){
		try{
			IloCplex cplex = new IloCplex();
			
			//Define variables
			IloNumVar[] z = new IloNumVar[n];
			z = cplex.boolVarArray(n);
			
			IloNumVar[][] x = new IloNumVar[n][];
			for(int i = 0; i< n; i++){
				x[i] = cplex.boolVarArray(D.d.outEdges.elementAt(i).size());
				}
			
			//Define objective
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int v = 0; v< n; v++){
				objective.addTerm(1, z[v]);
			}
			
			cplex.addMinimize(objective);
			
			// The first set of constraints:
			List<IloRange> constraints = new ArrayList<IloRange>();
			IloLinearNumExpr num_expr = cplex.linearNumExpr();
			for(int u = 0; u< n; u++){ 										// for all s \in V
				if (D.d.outEdges.elementAt(u).size() > 0){ 						// N+(s) != emptySet
					num_expr = cplex.linearNumExpr(); 							// clear old num_expr
					for(int v = 0; v < D.d.outEdges.elementAt(u).size(); v++)  // sum_{t \in N+(s)}
						num_expr.addTerm(1, x[u][v]); 
					constraints.add(cplex.addEq(num_expr, 1));
				}
			}
			
			// The second set of constraints:
			for (int r = 0;  r  < m ; r++){
				for(int v = 0; v < n ; v++){
					if(matrix[r][v]){
						num_expr = cplex.linearNumExpr();
						num_expr.addTerm(1, z[v]);
						for(int u = 0; u < n; u++){
							for(int index_v = 0; index_v < D.d.outEdges.elementAt(u).size(); index_v++){
								if (D.d.outEdges.elementAt(u).elementAt(index_v).equals((new Integer(v))) && matrix[r][u]){ 
									num_expr.addTerm(1, x[u][index_v]);
								}
							}
						}
						constraints.add(cplex.addGe(num_expr, 1));
					}
				}
			}
			
			cplex.setOut(null);
			
			Digraph branching = new Digraph(n);
			if(cplex.solve()){
				this.objValue = (int) cplex.getObjValue();
				for(int i = 0; i< n; i++){
					for(int j = 0; j< D.d.outEdges.elementAt(i).size(); j++){
						if(cplex.getValues(x[i])[j] == 1) branching.addEdge(i, D.d.outEdges.elementAt(i).elementAt(j));
					}
				}	
			} else {
				System.out.println("CPLEX couldn't find the optimal solution !!!");
			}
			return branching;
			
		} catch(IloException exc) {
			System.err.println("Concert exception '" + exc + "' caught");
			return null;
		}
	}
}
