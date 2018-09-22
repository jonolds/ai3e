import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	static final String[] extraVars = {"Win%", "Loss%", "Tie%", "Busy"};
	
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
		Controller.doTournament(pop);

		return new double[] {0.0};

	}
	
	static Matrix initPopulationSize(int size) {
		Matrix pop = new Matrix(size, 291 + extraVars.length);
		System.out.println(pop.cols());

		pop.setAll(0.0);
		Random r = new Random();
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < 291; j++)  // chromosome.length = 291
				pop.row(i)[j] = .3 * r.nextGaussian();
			for(int j = 291; j < 291 + extraVars.length; j++)
				pop.m_attr_name.set(j-291, extraVars[j-291]);
		}
		return pop;
	}
	
	static double[] extractWeights(double[] in) {
		return Arrays.copyOfRange(in, 0, 291);
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

	static void printWeights(double[] weights) {
		Arrays.stream(weights).forEach(x->System.out.print(x + " "));
	}	
	static double round(double d) {
		return ((int)(1000*d))/100.0;
	}
}