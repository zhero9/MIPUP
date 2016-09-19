import java.util.Arrays;

//import java.util.Vector;

public class Poset  {
	public Digraph d;
	private int n;
	private int h;
	private int[] visited;

	public Poset (boolean [][] M){
		//this.M = M;
		int m = M.length; // number of rows
		n = M[0].length; // number of columns
		d = new Digraph (n);

		for (int s1 = 0; s1 < n ; s1++ ){
			for (int s2 = 0; s2 < n; s2++){
				boolean tmp = true; // s1 is a subset of s2
				int i = 0;
				while(tmp && i < m){ // while true s1 is subset of s2
					tmp = (!M[i][s1] || M[i][s2] ); 
					// !p || q  is equivalent to p \imply q is equivalent to p <=q
					i++;
				}
				if (tmp) d.addEdge(s1, s2);
			}
		}

		//maxHeight(); // calculate height
	}

	private void dfs(int v, int visina){
		h = Math.max(h, visina);
		visited[v] = 0;
		for(int i = 0; i<d.outEdges.elementAt(v).size(); i++){
			//if(visited[d.outEdges.elementAt(v).elementAt(i)] == -1)
			dfs(d.outEdges.elementAt(v).elementAt(i),visina+1);
		}
	}

	private void maxHeight(){
		this.h = 0;
		this.visited = new int[n];
		Arrays.fill(this.visited, -1);
		
		for(int i = 0; i < this.n; i++ ){
			if(this.d.inEdges.elementAt(i).size() == 0)
				dfs(i, 0);
		}
	}

	public void printHight(){
		System.out.println("Height of poset P is = "+h+"!");
	}
}