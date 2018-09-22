import java.time.Instant;
import java.util.ArrayList;

class FastRank implements Runnable {
	private String threadName;
	private Thread t;
	private Thread[] ts;
	Matrix population;
	ArrayList<IAgent> candidates;
	ArrayList<ArrayList<IAgent>> canSet;
	long startTime;
	int t_num = 0;
	int groupSize = 10;
	
	FastRank(String name, Matrix pop, long time) {
		//add a column for AgentNumber;
		pop.addColFilled("agentNum", 0.0);
		System.out.println(pop.row(27)[pop.cols()-1]);
		
//		
//		canSet = new ArrayList<>();
//		ts = new Thread[pop.rows()/groupSize];
//		for(int i = 0; i < ts.length; i++) {
//			ArrayList<IAgent> newRow = new ArrayList<>();
//			for(int k = 0; k < groupSize; k++) {
//				newRow.add(new NeuralAgent(pop.row(i*groupSize+k)));
//			}
//			canSet.add(newRow);
//		}
//		
//		
//		threadName = name;
//		this.population = pop;
//		pop.saveARFF(threadName);
//		candidates = new ArrayList<>();
//		population.m_data.forEach(x->candidates.add(new NeuralAgent(x)));
//		startTime = time;
//		
//		t = new Thread(this, threadName);
//		t.start();
	}
	
	public void run() {
		try{ Controller.doTournament(candidates, "a", 0); } catch(Exception e) {e.printStackTrace();}
		Long endTime = (long) ((Instant.now().toEpochMilli()-startTime));
		System.out.println(threadName + " ending at: " + endTime + " ms");
	}
}