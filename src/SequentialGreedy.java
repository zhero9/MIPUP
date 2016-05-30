
public class SequentialGreedy {
	
	static boolean[][] matrix;
	static boolean[][] covered;
	static Poset D;
	static int m,n;

	public SequentialGreedy(boolean [][] M){
		matrix = M;
		D = new Poset(M);
		m = M.length; // # of rows
		n = M[0].length;  // # of columns
		covered = new boolean[m][n];
		//solve();  
	}
	
	public Digraph solve(){
		Digraph branching = new Digraph(n);
		//D.d.printAdjList();
		while(hasEdges()){
			for(int i = 0; i < n; i++){
				for(int j = 0; j < D.d.outEdges.elementAt(i).size(); j++){
					ComputeCoverageOfEdge(i,D.d.outEdges.elementAt(i).elementAt(j));
				}
			}
			int[] arc = maxCoverageIndex();
			branching.addEdge(arc[0], arc[1]);
			cover_v(arc[0],arc[1]);
			D.d.deleteOutNeighbours(arc[0]);
			//D.d.printAdjList();
			D.d.resetWeights();
		}
		//System.out.println("While finished");
		//branching.printAdjList();
		return branching;
	}
	
	private boolean hasEdges (){
		for(int i = 0; i< n; i++){
			if(D.d.outEdges.elementAt(i).size() > 0)
				return true;
		}
		return false;
	}
	
	private void ComputeCoverageOfEdge(int u, int v){
		short w = 0;
		for(int k = 0; k<m; k++){
			if(matrix[k][u] && matrix[k][v] && !covered[k][v]){
				w ++;
			}
		}
		D.d.adjMatrix[u][v] = w;
	}
	
	private int[] maxCoverageIndex(){
		int a = -1, b = -1, max = -1;
		for(int i = 0; i<n; i++){
			for(int j = 0; j<n; j++){
				if(D.d.adjMatrix[i][j] > max){
					max = D.d.adjMatrix[i][j];
					a = i;
					b = j;
				}
			}
		}
		
		return new int[] {a,b};
	}
	
	private void cover_v (int u, int v){
		for(int k = 0; k<m; k++){
			covered[k][v] = covered[k][v] || matrix[k][u];
		}
	}
	
	public int value(){
		int val = 0;
		for(int i = 0; i < m; i++){
			for(int j = 0; j<n; j++){
				if(matrix[i][j]  && !covered[i][j]) val++;
			}
		}
		return val;
	}
}
