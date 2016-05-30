import java.util.Arrays;
import java.util.Vector;


public class RowSplit {
	
	private static boolean[][] matrix;
	public boolean[][] rowSplitM;
	public boolean[][] solution;
	private static int m;
	private int n,t;
	public  int[] rows;
	private Digraph optB;
	private Vector<Vector<Integer>> out;
	private static Vector<Vector<Integer>> columnsCopies;

	public RowSplit(boolean[][] mat,String alg){
		matrix = filterDoubleColumns(mat);
		m = matrix.length; // number of rows
		n = matrix[0].length; // number of columns
		if(alg.equals("ip")){
			ILP lp = new ILP(matrix);
			optB = lp.solveOpt();
			t = lp.value;
		}else if(alg.equals("greedy")){
			SequentialGreedy sg = new SequentialGreedy(matrix);
			optB = sg.solve();
			t = sg.value();
		} else if(alg.equals("ls")){
			LocalSearch ls = new LocalSearch(matrix);
			optB = ls.branching;
			t = evaluateBranching(optB);
		}else if (alg.equals("ipd")){
			MinDistinctILP distinct = new MinDistinctILP(matrix);
			optB = distinct.solveOpt();
			t = evaluateBranching(optB); 
		}
		
		rows = new int [t];
		recoverRowSplit();
		solution = new boolean[rowSplitM.length][mat[0].length];
		solve();
		
	}
	
	private void solve(){
		for(int i = 0; i< rowSplitM.length; i++){
			for(int j = 0; j< columnsCopies.size(); j++){
				for(int k = 0; k< columnsCopies.elementAt(j).size(); k++){
					solution[i][columnsCopies.elementAt(j).elementAt(k)] = rowSplitM[i][j];
				}
			}
		}
	}

	private void recoverRowSplit(){
		out = new Vector<Vector<Integer>> (); // calcualte B^+(v) for all v\in V
		for(int i = 0; i< optB.outEdges.size(); i++){
			out.addElement(optB.reachable(i));
		}
		rowSplitM = new boolean[t][matrix[0].length];
		int i = 0;
		for(int r = 0; r<m; r++){ 
			for(int v = 0; v < n; v++){
				if(!r_covered_v(r,v) && matrix[r][v]){
					//System.out.println("r:"+r+" v:"+v);
					for(int j = 0; j< n; j++){
						if(out.elementAt(v).contains(j) || v==j){
							//System.out.println("     i:"+i+" j:"+j);
							rowSplitM[i][j] =  true;
						} 
					}
					rows[i]=r;
					i++;
				}
			}
		}
	}

	private boolean r_covered_v (int r, int v){
		for(int i = 0; i< optB.inEdges.elementAt(v).size(); i++){
			if(matrix[r][optB.inEdges.elementAt(v).elementAt(i)]) return true;
		}
		return false;
	}

	private static boolean[][] filterDoubleColumns(boolean[][] m){
		// check for two equal columns
		boolean[] tmp= new boolean[m[0].length];
		
		columnsCopies = new Vector<Vector<Integer>>();
		for(int s1 = 0 ; s1 < m[0].length; s1++){
			if(!tmp[s1]){ // s1 is not added
				Vector<Integer> copies = new Vector<Integer>();
				copies.add(s1);
				tmp[s1] = true;
				for(int s2 = s1+1 ; s2  < m[0].length; s2++){
					boolean tmp1 = true;
					int i = 0;
					while (tmp1 && i<m.length && !tmp[s2]){ // is s1 = s2
						if(m[i][s1] !=  m[i][s2]) tmp1 = false;
						i++;
					}
					if(tmp1 && !tmp[s2]){
						copies.add(s2);
						tmp[s2] = true;
					}
				}
				columnsCopies.add(copies);
			}
		}

		boolean[][] filteredM = new boolean[m.length][columnsCopies.size()] ;
			for (int i = 0; i < filteredM[0].length; i++){
			for (int k = 0; k< m.length; k++){
				filteredM[k][i] = m[k][columnsCopies.elementAt(i).elementAt(0)];
			}
		}
		return filteredM;
	}
	
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
	}
}
