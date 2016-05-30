//import java.util.Arrays;
import java.util.Random;


public class LocalSearch {

	static boolean[][] matrix;
	static Poset D;
	static int m,n;
	Digraph branching;

	public LocalSearch(boolean [][] M, Digraph b){
		matrix = M;
		D = new Poset(M);
		m = M.length; // # of rows
		n = M[0].length;  // # of columns
		this.branching = b;
		improve();
	}

	public LocalSearch(boolean [][] M){
		matrix = M;
		D = new Poset(M);
		m = M.length; // # of rows
		n = M[0].length;  // # of columns
		this.branching = new Digraph(n);
		randomMaxBranching();
		improve();
	}

	public void improve(){
		//branching.printAdjList();
		for(int i = 0; i<n; i++){
			if( branching.outEdges.elementAt(i).size() > 0){
				for(int jj = 0;jj < D.d.outEdges.elementAt(i).size(); jj++){
					int s1 = branching.outEdges.elementAt(i).elementAt(0);
					int s2 = D.d.outEdges.elementAt(i).elementAt(jj);
					if(coverageInB(i, s1) < coverageInB(i, s2)	&& (s1 != s2)){
						branching.deleteOutNeighbours(i);
						branching.addEdge(i, s2);
						//System.out.println("Changed "+i+","+s1+" with "+i+","+s2);
						i = 0;
					}
				}
			} 
		}
	}

	public short coverageInB(int s, int t){
		boolean[] tmp = new boolean[m];
		for(int j = 0; j< m; j++ ){
			tmp[j] = matrix[j][t];
		}
		for(int j = 0; j < branching.inEdges.elementAt(t).size(); j++){
			if (!branching.inEdges.elementAt(t).elementAt(j).equals(new Integer(s))){
				for(int k = 0; k < m; k++){
					tmp[k] = tmp[k] && !matrix[k][branching.inEdges.elementAt(t).elementAt(j)]; //
				}
			}
		}

		short count = 0;
		for(int j = 0; j<m ; j++){
			if(tmp[j]) count++;
		}
		return count;
	}

	public void randomMaxBranching(){
		Random gen = new Random();
		for(int i = 0; i<n ; i++){
			if(D.d.outEdges.elementAt(i).size()> 0){
				int j = gen.nextInt(D.d.outEdges.elementAt(i).size());
				branching.addEdge(i, D.d.outEdges.elementAt(i).elementAt(j));
			}
		}
	}
	/*
	public int evaluateBranching(Digraph b){
		int[] uncovered = new int[n];
		for(int i = 0; i< n; i++){
			if(b.inEdges.elementAt(i).size() == 0){
				uncovered[i] = sizeOfSet(i);
			}else{
				uncovered[i] = sizeOfSet(i) - sizeOfSet(disjunctionOfInEdges(i,b));
				//System.out.println(" "+sizeOfSet(disjunctionOfInEdges(i,b)));
			}

		}
		return Arrays.stream(uncovered).sum();
	}

	public static boolean[] disjunctionOfInEdges (int s, Digraph b){
		boolean[] tmp = new boolean[m];
		for(int j = 0; j < b.inEdges.elementAt(s).size(); j++){
			for(int k = 0; k < m; k++){
				tmp[k] = tmp[k] || matrix[k][b.inEdges.elementAt(s).elementAt(j)];
			}
		}
		return tmp;
	}

	public static int sizeOfSet(int s){
		int size = 0;
		for(int i =0; i< m; i++){
			if(matrix[i][s]) size++;
		}
		return size;
	}

	public static int sizeOfSet(boolean [] s){
		int size = 0;
		for(int i = 0; i< s.length; i++){
			if(s[i]) size++;
		}
		return size;
	}*/
}
