import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class Writter {

	public Writter(boolean[][] matrix, int[] rows, String colNames,
			String[] rowNames, String path, String alg) {
		this.matrix = matrix;
		this.rows = rows;
		this.colNames = colNames;
		this.rowNames = rowNames;
		this.path = makePath(path).concat("_"+alg);
		this.columns = colNames.split(";");
		//this.alg = alg;
	}
	
	public Writter(boolean[][] matrix,boolean[][] filteredMatrix ,int[] rows, String colNames,
			String[] rowNames, String path, String alg) {
		this.matrix = matrix;
		this.matrixF = filteredMatrix;
		this.rows = rows;
		this.colNames = colNames;
		this.rowNames = rowNames;
		this.path = makePath(path).concat("_"+alg);
		this.columns = colNames.split(";");
		//this.alg = alg;
	}
	

	private boolean[][] matrix;
	private boolean[][] matrixF; // no duplicated columns matrix
	private int[] rows;
	private String colNames;
	private String[] rowNames;
	private String path;
	private String[] columns;
	//private String alg;
	
	String[] nodeColors = {"#73B2F9", "#EC7877", "#8ACB69", "#F5DB5D", "#BD80E5", "#F2A253", "#A37242", "#3D73A3", "#43A373", "#724AA4"};
	public Vector<String> rowN;

	public String makePath(String p){
		return p.substring(0, p.length()-4);
	}
	
	public void writeFile() {

		BufferedWriter writer = null;
		rowN = new Vector<String>();
		try {

			File file = new File(path+"_RS.csv");

			if (!file.exists()) {
				file.createNewFile();
			}

			writer = new BufferedWriter(new FileWriter(file));

			writer.write(colNames);
			writer.newLine();
			int p = 1;
			for(int i = 0; i< matrix.length; i++){
				if (p == 1  && i< rows.length-1 && rows[i+1] > rows[i] ){
					writer.write(rowNames[rows[i]]);
					rowN.addElement(rowNames[rows[i]]);
				}else if( i == rows.length-1 && rows[i-1] < rows[i]){
					writer.write(rowNames[rows[i]]);
					rowN.addElement(rowNames[rows[i]]);
				}else{
					writer.write(rowNames[rows[i]]+"_"+p);
					rowN.addElement(rowNames[rows[i]]+"_"+p);
				}
				p++;
				if( i< rows.length-1 && rows[i+1] > rows[i]) p = 1;
				for(int j = 0; j< matrix[0].length; j++){
					writer.write(";"+ (matrix[i][j] ? 1 : 0));
				}
				writer.newLine();
				
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception ex) {
				System.out.println("Error in closing the BufferedWriter" + ex);
			}
		}

	}
	
	public void writePhylogenyTreeFile(){
		Vector<Vector<Integer>> rowCopies = new Vector<Vector<Integer>>();
		Vector<String> legend = new Vector<String>();
		Vector<boolean[]> setL = new Vector<boolean[]>();
		Vector<String> labels = new Vector<String>();
		boolean[] tmp= new boolean[rowN.size()];
		
		for(int i = 0; i< tmp.length; i++){
			if(!tmp[i]){
				Vector<Integer> copies = new Vector<Integer>();
				copies.add(i);
				setL.add(matrixF[i]);
				labels.add(rowN.elementAt(i));
				tmp[i] = true;
				for(int j = i+1; j< tmp.length; j++){
					boolean tmp1 = true;
					int k = 0;
					while (tmp1 && k<matrixF[0].length && !tmp[j]){ // is s1 = s2
						if(matrixF[i][k] !=  matrixF[j][k]) tmp1 = false;
						k++;
					}
					if(tmp1 && !tmp[j]){
						copies.add(j);
						tmp[j] = true;
					}
				}
				if(copies.size() > 1){
					String tmp2=rowN.elementAt(i);
					for(int k = 1; k<copies.size();k++){
						tmp2 = tmp2.concat("="+rowN.elementAt(copies.elementAt(k)));
					}
					//System.out.println(tmp2);
					legend.add(tmp2);
				}
				rowCopies.add(copies);
			}
		}
		
		BufferedWriter writer = null;
		try{
			File file = new File(path+"_tree.dot");

			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(file));
			
			writer.write("digraph {");
			writer.newLine();
			
			/// Writing leafs on the tree:
			for(int i = 0; i<rowCopies.size();i++){
				int t = rowCopies.elementAt(i).elementAt(0);
				if(i < nodeColors.length){
					writer.write(rowN.elementAt(t)+"[label=\""+rowN.elementAt(t)+"\",shape=box,style=filled,fontsize=28,fillcolor=\""+nodeColors[i]+"\"];");
				}else{
					writer.write(rowN.elementAt(t)+"[label=\""+rowN.elementAt(t)+"\",shape=box,style=filled,fontsize=28,fillcolor=\""+nodeColors[i%nodeColors.length]+"\"];");
				}
				writer.newLine();
			}
			
			
			/// Writing legend:
			if(legend.size() > 0){
			writer.write("legend[label=\"Equalities among split rows:");
			writer.newLine();
			for(int k=0; k< legend.size(); k++){
				writer.write(legend.elementAt(k));
				writer.newLine();
			}
			writer.write("\",shape=box,fontsize=18];");
			writer.newLine();
			}else{
				writer.write("legend[label=\" Matrix without conflicts.\",shape = box,fontsize=18];");
				writer.newLine();
			}
			
			/// adding inner points and edges to the tree:
			int numOfInV = 0;
			while(setL.size() > 1){
				boolean[] maxIntersection = new boolean[matrixF[0].length];
				int max = 0;
				int a=0, b=0;
				for(int i = 0; i<setL.size(); i++){
					for(int j = i+1; j< setL.size(); j++){
						if(sizeOfSet(intersection(setL.elementAt(i),setL.elementAt(j))) >=max){
							max = sizeOfSet(intersection(setL.elementAt(i),setL.elementAt(j)));
							maxIntersection = intersection(setL.elementAt(i),setL.elementAt(j));
							a = i; b =j;
						}
					}
				}
				
				boolean[] dif_a = setL.elementAt(a).clone();
				boolean[] dif_b = setL.elementAt(b).clone();
				for(int i = 0; i< setL.size(); i++){ //// label of edges, maxIntersection changes meaning
					if(i != a){
						for(int k = 0; k<matrixF[0].length; k++){
							dif_a[k] =  dif_a[k] && !setL.elementAt(i)[k];
						}
					}
					if(i != b){
						for(int k = 0; k<matrixF[0].length; k++){
							dif_b[k] =  dif_b[k] && !setL.elementAt(i)[k];
						}
					}
				}
				
				numOfInV ++;
				labels.add("Int"+numOfInV);
				setL.add(maxIntersection);
				writer.write("Int"+numOfInV+"[shape=point,style=filled,fillcolor=black,label=\"\"];");
				writer.newLine();
				

				
				String edgeLabel_a="";
				String edgeLabel_b="";
				for(int k =0; k< matrixF[0].length; k++){
					if(dif_a[k]) edgeLabel_a = edgeLabel_a.concat(columns[k+1]);
					if(dif_b[k]) edgeLabel_b = edgeLabel_b.concat(columns[k+1]);
				}
				
				

				
				writer.write("Int"+numOfInV+" -> "+labels.elementAt(a)+"[arrowhead=none, label=\""+edgeLabel_a+"\"];");
				writer.newLine();
				writer.write("Int"+numOfInV+" -> "+labels.elementAt(b)+"[arrowhead=none, label=\""+edgeLabel_b+"\"];");
				writer.newLine();
				if(a < b){
					labels.remove(b); setL.remove(b);
					labels.remove(a); setL.remove(a);
				}else{ // else could be remove, but just in case.
					labels.remove(a); setL.remove(a);
					labels.remove(b); setL.remove(b);
				}
			}
			
			writer.write("}");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception ex) {
				System.out.println("Error in closing the BufferedWriter" + ex);
			}
		}
	}
	
	public int sizeOfIntersection (int i, int j){
		int tmp = 0;
		for(int k = 0; k< matrixF[0].length;k++){
			if(matrixF[i][k] == matrixF[j][k]) tmp++;
		}
		return tmp;
	}
	
	public boolean[] intersection(boolean[]  a,boolean[] b){
		boolean[] intersection = new boolean[a.length];
		for(int k = 0; k< intersection.length; k++){
			intersection[k] = a[k] && b[k]; 
		}
		return intersection;
	}
	
	public int sizeOfSet(boolean[] a){
		int size = 0;
		for(int i = 0; i< a.length; i++){
			if(a[i]) size++;
		}
		return size;
	}
	
	
	
}
