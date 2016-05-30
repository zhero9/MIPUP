import java.util.Vector;


public class Digraph {

	public Vector<Vector<Integer>> inEdges = new Vector<Vector<Integer>>();
	public Vector<Vector<Integer>> outEdges = new Vector<Vector<Integer>>();
	public short[][] adjMatrix;

	public Digraph(int n){
		adjMatrix = new short[n][n];
		for (int i = 0; i< n; i++){
			inEdges.add(new Vector<Integer>());
			outEdges.add(new Vector<Integer>());
			for(int j = 0; j < n; j++){
				adjMatrix[i][j] = -1;
			}
		}
	}

	public void addEdge (int u, int v){
		if (u == v) return;
		inEdges.elementAt(v).add(u);
		outEdges.elementAt(u).add(v);
	}

	public void setWeight (int u, int v, short w){
		adjMatrix[u][v] = w;
	}

	public void printAdjList (){
		for (int i = 0 ; i < outEdges.size(); i++){
			System.out.print("N+("+i+") : ");
			for (int j = 0; j < outEdges.elementAt(i).size(); j++){
				System.out.print(" "+outEdges.elementAt(i).elementAt(j));
			}
			System.out.println();
		}
	}
	
	public void printInEdges (){
		for (int i = 0 ; i < inEdges.size(); i++){
			System.out.print("N-("+i+") : ");
			for (int j = 0; j < inEdges.elementAt(i).size(); j++){
				System.out.print(" "+inEdges.elementAt(i).elementAt(j));
			}
			System.out.println();
		}
	}
	
	public void deleteOutNeighbours(int u){
		this.outEdges.elementAt(u).clear();
		for(int i = 0; i<inEdges.size(); i++){
			this.inEdges.elementAt(i).remove(new Integer(u));
		}
		// Should delete also same edges from inEdges !!!
	}
	
	public void resetWeights(){
		for(int i = 0; i< adjMatrix.length; i++){
			for(int j = 0; j< adjMatrix[0].length;j++){
				adjMatrix[i][j] = -1;
			}
		}
	}
	
	private Vector<Integer> a = new Vector<Integer>();
	private void reachable1 (int l){
		if(outEdges.elementAt(l).size() > 0){
			a.add(outEdges.elementAt(l).elementAt(0));
			reachable1(outEdges.elementAt(l).elementAt(0));
		}
	}
	
	public Vector<Integer> reachable(int l){
		a = new Vector<Integer>();
		reachable1(l);
		return a;
	}
	
	
}
