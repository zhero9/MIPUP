
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String pathToMatrix = null;
		String formatOfInput = null;
		double minVAFPresent = -1;
		String alg = "ipd";


		/**/if (args.length == 2) { 
			pathToMatrix = args[0]; alg = args[1];
		} else if (args.length == 4) { 
			pathToMatrix = args[0]; alg = args[1];
			formatOfInput = "VAF"; 
			minVAFPresent = Double.parseDouble(args[3]);
		} else {
			System.out.println("Wrong number of arguments"); 
			return; }
	

		/*pathToMatrix = "/home/edin/ConflictFreeExamples/ex.csv";
		formatOfInput = "VAF+not";
		minVAFPresent = 0.07;
		alg = "ip"; */

		try{
			if(formatOfInput.equals("VAF")){
				Solver s = new Solver(pathToMatrix, minVAFPresent, alg);
				s.solveAndWrite();
			}else{
				Solver s = new Solver(pathToMatrix, alg);
				s.solveAndWrite();
			}
		}catch(Exception e){
			System.out.println("Program failed.");
			return;
		}
		System.out.println("Program finished successfully.");
	}
}
