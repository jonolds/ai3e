import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

class Game { // NO_UCD (unused code)
	static char codeLet = 'a';
	static int codeNum = 0;
	static final int startNum = 1;
	static final int numOfSims = 1;
	static final int popSize = 100;
	static String timestamp;
	boolean[] finished;
	
	public static void main(String[] args) throws Exception {
		Game g = new Game();
		g.initGame();
	}
	
	void initGame() throws Exception {
		timestamp = (new SimpleDateFormat("M-d_H.mm.s")).format(new Date());	
		//Calls genetic algorithym to find a winning genome
		double[] weights = evolveWeights();
		//Puts best NeuralAgent in real battle with GUI
//		Controller.doBattle(new ReflexAgent(), new NeuralAgent(weights));
//		NeuralAgent agent = new NeuralAgent(weights);
//		Controller.doBattleNoGui(new ReflexAgent(), agent);
//		Arrays.stream(weights).forEach(x->System.out.print(x + " "));
	}
	
	double[] evolveWeights() throws Exception {
		// Create a random initial population
		Matrix pop = initPopulationSize(popSize);
		


		
		
		
		return new double[] {0.0};
		
//		
//		Long startTime = Instant.now().toEpochMilli();
//		System.out.println("Start time: " + startTime);
//		
//		
//		FastTourney evol1 = new FastTourney("t1", pop, startTime);
		
		
//		Controller.doTournament(candidates, "a", 0);
		
//		for(int i = 0; i < popSize; i++)
		
//		System.out.println("Ending prog");
//		System.out.println((Instant.now().toEpochMilli()-startTime)*.001 + " seconds");
		
		// Evolve them. For tournament selection, call Controller.doBattleNoGui(agent1, agent2).
		
		// Return an arbitrary member from the population
//		double[] weights = population.row(0);
//		printWeights(weights);
//		return weights;
//		Thread.sleep(2000);
//		System.out.println(pop.cols() + "x" +pop.rows());
//		return pop.row(0);
	}
	
	static Matrix initPopulationSize(int size) {
		Random r = new Random();
		Matrix population = new Matrix(size, 291);
		for(int i = 0; i < size; i++) {
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)  // chromosome.length = 291
				chromosome[j] = .3 * r.nextGaussian();
		
		}
		return population;
	}
	
	static void printWeights(double[] weights) {
//		for(int i = 0; i < weights.length; i++)
//			System.out.print(weights[i] + " ");
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

	
	static double round(double d) {
		return ((int)(1000*d))/100.0;
	}
}