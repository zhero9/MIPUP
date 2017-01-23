import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class WritterPhylogenyTree {

	public WritterPhylogenyTree(boolean[][] orgMatrix, boolean[][] matrix,
			boolean[][] filteredMatrix, int[] rows, String colNames,
			String[] rowNames, String path,
			Vector<Vector<Integer>> columnsCopies, Vector<String> rowN) {
		
		this.originalMatrix = orgMatrix;
		this.matrix = matrix; // only used to determine isSubset
		this.matrixF = filteredMatrix;
		this.rows = rows;
		this.rowNames = rowNames;
		this.path = path;
		this.columnsCopies = columnsCopies;
		this.rowN = rowN;
	}

	private boolean[][] originalMatrix;
	private boolean[][] matrix;
	private boolean[][] matrixF; // no duplicated columns matrix
	private int[] rows;
	private String[] rowNames;
	private String path;
	public Vector<Vector<Integer>> columnsCopies;
	public Vector<String> rowN;

	Vector<Vector<Integer>> rowCopies = new Vector<Vector<Integer>>();
	private int[] toWhichRowCopy;
	Vector<String> legend = new Vector<String>();
	Vector<boolean[]> setL = new Vector<boolean[]>();
	Vector<String> labels = new Vector<String>();
	String[] nodeColors = { "#a4c639", "#cd9575", "#7fffd4", "#e9d66b",
			"#ff9966", "#ffe135", "#5d8aa8", "#efdecd", "#98777b", "#fe6f5e",
			"#ace5ee", "#a2a2d0", "#bf94e4", "#ffc1cc",
			"#f0dc82",
			"#a67b5b",
			"#ffff99",
			"#ff7f50",
			"#bdb76b",
			"#6c541e", // old ones:
			"#AEEBD7", "#8595e1", "#EBAEC3", "#f79cd4", "#b5bbe3", "#e6afb9",
			"#d33f6a", "#8e063b", "#11c638", "#8ACB69", "#023fa5", "#43A373",
			"#ead3c6", "#f0b98d", "#ef9708", "#8dd593", "#0fcfc0", "#A37242",
			"#D99DF5", "#9cded6", "#d5eae7", "#d6bcc0", "#f6c4e1", "#73B2F9",
			"#EC7877", "#BD80E5", "#e07b91", "#bec1d4", "#f3e1eb", "#bb7784",
			"#7d87b9" };

	private void calculateDataForPhylogenyTree() {
		boolean[] tmp = new boolean[rowN.size()];
		toWhichRowCopy = new int[rowN.size()];

//		System.out.println("Row names:");
//		for (int i = 0; i < rowN.size(); i++)
//		{
//			System.out.println(rowN.elementAt(i));
//		}
		
		for (int i = 0; i < tmp.length; i++) {
			if (!tmp[i]) {
				toWhichRowCopy[i] = i;
				Vector<Integer> copies = new Vector<Integer>();
				copies.add(i);
				setL.add(matrixF[i]);
				labels.add(rowN.elementAt(i));
				tmp[i] = true;
				for (int j = i + 1; j < tmp.length; j++) {
					boolean tmp1 = true;
					int k = 0;
					while (tmp1 && k < matrixF[0].length && !tmp[j]) { // is s1
																		// = s2
						if (matrixF[i][k] != matrixF[j][k])
							tmp1 = false;
						k++;
					}
					if (tmp1 && !tmp[j]) {
						copies.add(j);
						toWhichRowCopy[j] = i;
						tmp[j] = true;
					}
				}
				if (copies.size() > 1) {
					String tmp2 = rowN.elementAt(i);
					for (int k = 1; k < copies.size(); k++) {
						tmp2 = tmp2.concat("="
								+ rowN.elementAt(copies.elementAt(k)));
					}
					// System.out.println(tmp2);
					legend.add(tmp2);
				}
				rowCopies.add(copies);
			}
		}
	}

	public void writePhylogenyTreeFile(String pathToMatrix, int tmp) {
		// VAF files writer of phylogeny trees

		BufferedWriter writer = null;
		try {
			calculateDataForPhylogenyTree();

			File file = new File(path + "_tree.dot");

			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(file));

			writer.write("digraph {");
			writer.newLine();

			// / adding additional leafs=rows of original matrix
			for (int i = 0; i < rowNames.length; i++) {
				writer.write("row" + rowNames[i] + "[label=\"" + rowNames[i]
						+ "\",shape=box,style=filled,fontsize=40];");
				writer.newLine();
			}

//			System.out.println("rowCopies:");
//			for (int i = 0; i < rowCopies.size(); i++) {
//				for (int ii = 0; ii < rowCopies.elementAt(i).size(); ii++)
//				{
//					System.out.print(Integer.toString(rowCopies.elementAt(i).elementAt(ii) + 2) + ";");
//				}
//				System.out.println();
//			}

			
			// / Writing leafs on the tree:
			for (int i = 0; i < rowCopies.size(); i++) {
				int t = rowCopies.elementAt(i).elementAt(0);
				if (rows[t] < nodeColors.length) {
					writer.write(rowN.elementAt(t)
							+ "[label=\""
							+ rowN.elementAt(t)
							+ "\",shape=oval,style=filled,fontsize=28,fillcolor=\""
							+ nodeColors[rows[t]] + "\"];");
				} else {
					writer.write(rowN.elementAt(t)
							+ "[label=\""
							+ rowN.elementAt(t)
							+ "\",shape=oval,style=filled,fontsize=28,fillcolor=\""
							+ nodeColors[t % nodeColors.length] + "\"];");
				}
				writer.newLine();
//				for (int p = 0; p < rowNames.length; p++) {
//					if (isSubset(t, p)) {
//						writer.write(rowN.elementAt(t) + " -> " + "row"
//								+ rowNames[p] + "[arrowhead=\"normal\"];");
//						writer.newLine();
//					}
//				}				
			}
			
			for (int i = 0; i < rowN.size(); i++)
			{
				for (int p = 0; p < rowNames.length; p++) {
					if (rowN.elementAt(i).contains(rowNames[p])) {
						writer.write(rowN.elementAt(toWhichRowCopy[i]) + " -> " + "row"
								+ rowNames[p] + "[arrowhead=\"normal\"];");
						writer.newLine();
					}
				}
			}

			
			// telling DOT to put all leaves on the same level
			writer.write("{rank = same;");
			for (int i = 0; i < rowCopies.size(); i++) {
				int t = rowCopies.elementAt(i).elementAt(0);
				writer.write(rowN.elementAt(t) + ";");
			}
			writer.write("}");
			writer.newLine();

			// / Writing legend:
			if (legend.size() > 0) {
				writer.write("legend[label=\"Equalities among split rows:");
				writer.newLine();
				for (int k = 0; k < legend.size(); k++) {
					writer.write(legend.elementAt(k));
					writer.newLine();
				}
				writer.write("\",shape=box,fontsize=18];");
				writer.newLine();
			} else {
				writer.write("legend[label=\" Matrix without equal rows.\",shape = box,fontsize=18];");
				writer.newLine();
			}

			// / adding inner points and edges to the tree:
			String[] stats = ColumnStatistics.calculate(pathToMatrix,
					originalMatrix, tmp, columnsCopies);

			int numOfInV = 0;
			while (setL.size() > 1) {
				boolean[] maxIntersection = new boolean[matrixF[0].length];
				int max = 0;
				int a = 0, b = 0;
				for (int i = 0; i < setL.size(); i++) {
					for (int j = i + 1; j < setL.size(); j++) {
						if (sizeOfSet(intersection(setL.elementAt(i),
								setL.elementAt(j))) >= max) {
							max = sizeOfSet(intersection(setL.elementAt(i),
									setL.elementAt(j)));
							maxIntersection = intersection(setL.elementAt(i),
									setL.elementAt(j));
							a = i;
							b = j;
							// System.out.println("a,b="+i+","+j);
						}
					}
				}

				boolean[] dif_a = setL.elementAt(a).clone();
				boolean[] dif_b = setL.elementAt(b).clone();
				for (int i = 0; i < setL.size(); i++) { // // label of edges
					if (i != a) {
						for (int k = 0; k < matrixF[0].length; k++) {
							dif_a[k] = dif_a[k] && !setL.elementAt(i)[k];
						}
					}
					if (i != b) {
						for (int k = 0; k < matrixF[0].length; k++) {
							dif_b[k] = dif_b[k] && !setL.elementAt(i)[k];
						}
					}
				}

				numOfInV++;
				labels.add("Int" + numOfInV);
				setL.add(maxIntersection);
				writer.write("Int"
						+ numOfInV
						+ "[shape=point,style=filled,fillcolor=black,label=\"\"];");
				writer.newLine();

				String edgeLabel_a = "";
				String edgeLabel_b = "";
				int num;
				for (int k = 0; k < matrixF[0].length; k++) {
					// l = columnsCopies.elementAt(k).elementAt(0);
					num = columnsCopies.elementAt(k).size();
					if (dif_a[k])
						edgeLabel_a = edgeLabel_a.concat(str(k) + " | " + num
								+ " | " + stats[k]);
					if (dif_b[k])
						edgeLabel_b = edgeLabel_b.concat(str(k) + " | " + num
								+ " | " + stats[k]);
				}

				writer.write("Int" + numOfInV + " -> " + labels.elementAt(a)
						+ "[arrowhead=none, label=\"" + edgeLabel_a + "\"];");
				writer.newLine();
				writer.write("Int" + numOfInV + " -> " + labels.elementAt(b)
						+ "[arrowhead=none, label=\"" + edgeLabel_b + "\"];");
				writer.newLine();
				if (a < b) {
					labels.remove(b);
					setL.remove(b);
					labels.remove(a);
					setL.remove(a);
				}
			}

			// / add a new root node GL and corresponding edge.
			writer.write("GL"
					+ "[label=\"GL\",shape=box,style=filled,fillcolor=white];");
			writer.newLine();
			boolean[] dif = setL.elementAt(0).clone();
			String edgeLabelGL = "";
			for (int k = 0; k < matrixF[0].length; k++) {
				if (dif[k])
					edgeLabelGL = edgeLabelGL.concat(str(k) + " | "
							+ columnsCopies.elementAt(k).size() + " | "
							+ stats[k]);
			}
			writer.write("GL -> " + labels.elementAt(0)
					+ "[arrowhead=none, label=\"" + edgeLabelGL + "\"];");
			writer.newLine();

			writer.write("}");

		} catch (IOException e) {
			System.out
					.println("Error in writing .dot  file for phylogeny tree.");
			e.printStackTrace();
			return;
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception ex) {
				System.out.println("Error in closing the BufferedWriter" + ex);
				return;
			}
		}
	}

	public void writePhylogenyTreeFile() { // / for .csv files

		BufferedWriter writer = null;
		try {
			calculateDataForPhylogenyTree();

			File file = new File(path + "_tree.dot");

			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(file));

			writer.write("digraph {");
			writer.newLine();
			
			// / adding additional leafs=rows of original matrix
			for (int i = 0; i < rowNames.length; i++) {
				writer.write("row" + rowNames[i] + "[label=\"" + rowNames[i]
						+ "\",shape=box,style=filled,fontsize=40];");
			}
			

			// / Writing leafs on the tree:
			for (int i = 0; i < rowCopies.size(); i++) {
				int t = rowCopies.elementAt(i).elementAt(0);
				if (rows[t] < nodeColors.length) {
					writer.write(rowN.elementAt(t)
							+ "[label=\""
							+ rowN.elementAt(t)
							+ "\",shape=oval,style=filled,fontsize=28,fillcolor=\""
							+ nodeColors[rows[t]] + "\"];");
				} else {
					writer.write(rowN.elementAt(t)
							+ "[label=\""
							+ rowN.elementAt(t)
							+ "\",shape=oval,style=filled,fontsize=28,fillcolor=\""
							+ nodeColors[t % nodeColors.length] + "\"];");
				}
				writer.newLine();
//				for (int p = 0; p < rowNames.length; p++) {
//					if (isSubset(t, p)) {
//						writer.write(rowN.elementAt(t) + " -> " + "row"
//								+ rowNames[p] + "[arrowhead=\"normal\"];");
//						writer.newLine();
//					}
//				}
			}
			for (int i = 0; i < rowN.size(); i++)
			{
				for (int p = 0; p < rowNames.length; p++) {
					if (rowN.elementAt(i).contains(rowNames[p])) {
						writer.write(rowN.elementAt(toWhichRowCopy[i]) + " -> " + "row"
								+ rowNames[p] + "[arrowhead=\"normal\"];");
						writer.newLine();
					}
				}
			}

			
			// telling DOT to put all leaves on the same level
			writer.write("{rank = same;");
			for (int i = 0; i < rowCopies.size(); i++) {
				int t = rowCopies.elementAt(i).elementAt(0);
				writer.write(rowN.elementAt(t) + ";");
			}
			writer.write("}");
			writer.newLine();


			// / Writing legend:
			if (legend.size() > 0) {
				writer.write("legend[label=\"Equalities among split rows:");
				writer.newLine();
				for (int k = 0; k < legend.size(); k++) {
					writer.write(legend.elementAt(k));
					writer.newLine();
				}
				writer.write("\",shape=box,fontsize=18];");
				writer.newLine();
			} else {
				writer.write("legend[label=\" Matrix without equal rows.\",shape = box,fontsize=18];");
				writer.newLine();
			}

			// / adding inner points and edges to the tree:
			int numOfInV = 0;
			while (setL.size() > 1) {
				boolean[] maxIntersection = new boolean[matrixF[0].length];
				int max = 0;
				int a = 0, b = 0;
				for (int i = 0; i < setL.size(); i++) {
					for (int j = i + 1; j < setL.size(); j++) {
						if (sizeOfSet(intersection(setL.elementAt(i),
								setL.elementAt(j))) >= max) {
							max = sizeOfSet(intersection(setL.elementAt(i),
									setL.elementAt(j)));
							maxIntersection = intersection(setL.elementAt(i),
									setL.elementAt(j));
							a = i;
							b = j;
							// System.out.println("a,b="+i+","+j);
						}
					}
				}

				boolean[] dif_a = setL.elementAt(a).clone();
				boolean[] dif_b = setL.elementAt(b).clone();
				for (int i = 0; i < setL.size(); i++) { // // label of edges
					if (i != a) {
						for (int k = 0; k < matrixF[0].length; k++) {
							dif_a[k] = dif_a[k] && !setL.elementAt(i)[k];
						}
					}
					if (i != b) {
						for (int k = 0; k < matrixF[0].length; k++) {
							dif_b[k] = dif_b[k] && !setL.elementAt(i)[k];
						}
					}
				}

				numOfInV++;
				labels.add("Int" + numOfInV);
				setL.add(maxIntersection);
				writer.write("Int"
						+ numOfInV
						+ "[shape=point,style=filled,fillcolor=black,label=\"\"];");
				writer.newLine();

				String edgeLabel_a = "";
				String edgeLabel_b = "";
				int num;
				for (int k = 0; k < matrixF[0].length; k++) {
					num = columnsCopies.elementAt(k).size();
					if (dif_a[k])
						edgeLabel_a = edgeLabel_a.concat(str(k) + "|" + num);
					if (dif_b[k])
						edgeLabel_b = edgeLabel_b.concat(str(k) + "|" + num);
				}

				writer.write("Int" + numOfInV + " -> " + labels.elementAt(a)
						+ "[arrowhead=none, label=\"" + edgeLabel_a + "\"];");
				writer.newLine();
				writer.write("Int" + numOfInV + " -> " + labels.elementAt(b)
						+ "[arrowhead=none, label=\"" + edgeLabel_b + "\"];");
				writer.newLine();
				if (a < b) {
					labels.remove(b);
					setL.remove(b);
					labels.remove(a);
					setL.remove(a);
				}
			}

			// / add a new root node GL and corresponding edge.
			writer.write("GL"
					+ "[label=\"GL\",shape=box,style=filled,fillcolor=white];");
			writer.newLine();
			boolean[] dif = setL.elementAt(0).clone();
			String edgeLabelGL = "";
			for (int k = 0; k < matrixF[0].length; k++) {
				if (dif[k])
					edgeLabelGL = edgeLabelGL.concat(str(k) + "|"
							+ columnsCopies.elementAt(k).size());
			}
			writer.write("GL -> " + labels.elementAt(0)
					+ "[arrowhead=none, label=\"" + edgeLabelGL + "\"];");
			writer.newLine();

			writer.write("}");

		} catch (IOException e) {
			System.out
					.println("Error in writing .dot  file for phylogeny tree.");
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

	private boolean isSubset(int k, int t) {
		boolean tmp = true;
		for (int i = 0; i < matrix[0].length; i++) {
			if (!originalMatrix[t][i] && matrix[k][i])
				tmp = false;
		}
		return tmp;
	}

	private boolean[] intersection(boolean[] a, boolean[] b) {
		boolean[] intersection = new boolean[a.length];
		for (int k = 0; k < intersection.length; k++)
			intersection[k] = a[k] && b[k];
		return intersection;
	}

	private int sizeOfSet(boolean[] a) {
		int size = 0;
		for (int i = 0; i < a.length; i++)
			if (a[i])
				size++;
		return size;
	}

	private String str(int k) {
		if (k / 60 <= 1) {
			return Character.toString((char) (65 + k % 60));
		} else {
			return Character.toString((char) (65 + k % 60)).concat(
					"" + ((int) k / 60));
		}
	}
}
