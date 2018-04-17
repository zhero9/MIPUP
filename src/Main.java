public class Main {

	public static void main(String[] args) {

		String pathToMatrix = null;
		String formatOfInput = "csv";
		double minVAFPresent = 0;
		double maxVAFnotPresent = 0;
		String alg = "ip";
		int numOfSolutions = 1;

		
		if (args.length == 2) {
			pathToMatrix = args[0];
			alg = args[1];
		} else if (args.length == 4) {
			pathToMatrix = args[0];
			alg = args[1];
			formatOfInput = "VAF1";
			minVAFPresent = Double.parseDouble(args[3]);
		} else if (args.length == 5 && args[2].equals("VAF1")) {
			pathToMatrix = args[0];
			alg = args[1];
			formatOfInput = "VAF1";
			minVAFPresent = Double.parseDouble(args[3]);
			if(args[4].equals("m")){
				numOfSolutions = 100; /// Cplex Maximum is 2.100.000.000
			} else {
				numOfSolutions = Integer.parseInt(args[4]);
			}
		} else if (args.length == 5) {
			pathToMatrix = args[0];
			alg = args[1];
			formatOfInput = "VAF2";
			 minVAFPresent = Double.parseDouble(args[3]);
			maxVAFnotPresent = Double.parseDouble(args[4]);
		} else {
			System.out.println("Wrong number/format of arguments!");
			return;
		}
		/**/
		
		/*
		pathToMatrix = "/Users/HUSIC/Desktop/MIPUP/MIPUP/Example MultipleOpt/test.txt";
		formatOfInput = "VAF1";
		maxVAFnotPresent = 0.05;
		minVAFPresent = 0.105;
		alg = "ip"; */

		if (numOfSolutions > 1) {
			if(!alg.equals("ip") || !formatOfInput.equals("VAF1")) {
				System.out.println("MultipleOptima is currently available only for alg=ip and VAF1.");
			}
			formatOfInput = "VAF1";
			SolverMultiple sm = new SolverMultiple(pathToMatrix, minVAFPresent, alg, numOfSolutions);
			sm.solveAndWrite();
		} else if (formatOfInput.equals("VAF2")) {
			Solver s = new Solver(pathToMatrix, maxVAFnotPresent, minVAFPresent, alg);
		} else if (formatOfInput.equals("VAF1")) {
			Solver s = new Solver(pathToMatrix, minVAFPresent, alg);
			s.solveAndWrite();
		} else if (formatOfInput.equals("csv")){
			Solver s = new Solver(pathToMatrix, alg);
			s.solveAndWrite();
		} else {
			System.out.println("Unknown type of input format.");
		}
		
		System.out.println("Program finished successfully.");
		/**/
	}
}
