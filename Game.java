import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

class Game {
	String codeLetter = "a";
	static int startNum = 1;
	static int numOfSims = 1;

	public static void main(String[] args) throws Exception {
//		readData("a", startNum, numOfSims);
		//Calls genetic algorithym to find a winning genome
//		evolveWeights();
		//Print Winning Genome
//		printWeights(weights);
		//Puts best NeuralAgent in real battle with GUI
		//Controller.doBattle(new ReflexAgent(), new NeuralAgent(weights));
		NeuralAgent agent = new NeuralAgent(evolveWeights());
		Controller.doBattleNoGui(new ReflexAgent(), agent);
		System.out.println("\n");
		(Arrays.stream(agent.weights)).forEach(x->System.out.print(Double.toString(x) + " "));
	}
	
	static double[] evolveWeights() throws Exception {
		// Create a random initial population
		Random r = new Random();
		Matrix population = new Matrix(100, 291);
		for(int i = 0; i < 100; i++) {
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)  // chromosome.length = 291
				chromosome[j] = .3 * r.nextGaussian();
		}
		
		
//		ArrayList<IAgent> candidates = new ArrayList<>();
//		for(int i = 0; i < population.rows(); i++)
//			candidates.add(new NeuralAgent(population.row(i)));
//
//		Long startTime = Instant.now().toEpochMilli();
//		for(int i = 1; i < 2; i++)
//			Controller.doTournament(candidates, "a", i);
//		System.out.println((Instant.now().toEpochMilli()-startTime)*.001 + " seconds");
		
		// Evolve them. For tournament selection, call Controller.doBattleNoGui(agent1, agent2).
		
		
		// Return an arbitrary member from the population
		double[] weights = population.row(0);
		printWeights(weights);
		return weights;
	}

	
	static ArrayList<Integer[]> readData(String letter, int start, int end) throws FileNotFoundException {
		ArrayList<Integer[]> data = new ArrayList<>();
		for(int i = start; i < start + end; i++) {
			Scanner sc = new Scanner(new File(letter + i + ".txt"));
			while(sc.hasNextInt())
				data.add(new Integer[] {sc.nextInt(), sc.nextInt()});
			sc.close();
			for(Integer[] ints: data)
				System.out.println(ints[0] + " " + ints[1]);
		}
		return data;
	}
	
	static void printWeights(double[] w) {
		for(double d: w)
			System.out.print(d + " ");
	}
	
	static double round(double d) {
		return ((int)(1000*d))/100.0;
	}
}