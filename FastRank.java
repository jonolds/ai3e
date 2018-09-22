//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.Arrays;
//
//class FastRank implements Runnable {
//	private String threadName;
//	private Thread t;
//	private Thread[] threads;
//	Matrix population;
//	ArrayList<IAgent> candidates;
//	ArrayList<ArrayList<IAgent>> groups;
//	long startTime;
//	int t_num = 0;
//	int groupSize = 10;
//	int finished = 0;
//	
//	FastRank(String name, Matrix pop, long time) {
//		groups = new ArrayList<>();
//		threads = new Thread[pop.rows()/groupSize];
//		
//		for(int i = 0; i < threads.length; i++) {
//			ArrayList<IAgent> newGroup = new ArrayList<>();
//			for(int k = 0; k < groupSize; k++) {
//				newGroup.add(new NeuralAgent(pop.row(i*groupSize+k)));
//			}
//			groups.add(newGroup);
//			threads[i] = new Thread(this, "Thread" + Integer.toString(i));
//			threads[i].start();
//		}
//		
//		
//		threadName = name;
//		this.population = pop;
//		candidates = new ArrayList<>();
//		population.m_data.forEach(x->candidates.add(new NeuralAgent(x)));
//		startTime = time;
//		
//		t = new Thread(this, threadName);
//		t.start();
//	}
//	
//	boolean isFinished() throws InterruptedException {
//		return (finished == threads.length) ? true : false;
//	}
//	
//	public void run() {
//		try{ Controller.doTournament(candidates, "a", 0); } catch(Exception e) {e.printStackTrace();}
//		Long endTime = (long) ((Instant.now().toEpochMilli()-startTime));
//		System.out.println(threadName + " ending at: " + endTime + " ms");
//	}
//}