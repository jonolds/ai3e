import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

class Controller implements MouseListener {
	public static final long MAX_ITERS = 18000; //Max game length. At 20 frame/sec, 18000 frame = 15 minute
	
	private Model model; // holds all the game data
	private View view; // the GUI
	private Object secret_symbol; // Prevents agents from accessing methods that they could use to cheat
	private IAgent blueAgent, redAgent;
	LinkedList<MouseEvent> mouseEvents; // a queue of mouse events (used by the human agent)
	int selectedSprite; // the blue agent to draw a box around (used by the human agent)
	private long agent_frame_time = 0;
	private long blue_time_balance, red_time_balance;
	private long iter;
	private boolean amIblue;
	
	//DO A TOURNAMENT - calls rankAgents method
	static void doTournament(ArrayList<IAgent> agents, String letter, int num) throws Exception { // NO_UCD (unused code)
		int[] wins = new int[agents.size()+1]; //Extra space for ties
		// Make every agent battle against every other agent
		for(int i = 0; i < agents.size(); i++) {
			for(int j = 0; j < agents.size(); j++) {
				if(j != i) { //Skip iteration if agent is fighting itself
					int outcome = Controller.doBattleNoGui(agents.get(i), agents.get(j));
					if(outcome > 0)
						wins[i]++;
					else if(outcome < 0)
						wins[j]++;
					else 
						wins[agents.size()]++;
				}
			}
		}
		printWins(wins, letter + num);
	}

	static void printWins(int[] wins, String code) throws IOException {
		System.out.println("\nRankings:");
		PrintWriter pw = new PrintWriter(new FileWriter(code + ".txt"));
		for(int i = 0; i < wins.length - 1; i++) {
			pw.println(i + " " + wins[i]);
			System.out.println(wins[i] + " wins  #" + i);
		}
		pw.println("Ties: " + wins[wins.length-1]);
		System.out.println("Ties: " + wins[wins.length -1]);
		pw.close();
	}
	
	//NO GUI BATTLE
	static int doBattleNoGui(IAgent blue, IAgent red) throws Exception {
		Object ss = new Object();
		Controller c = new Controller(ss, blue, red);
		c.initializeGame();
		while(c.update()) { }
		
		//Determines who won after doBattleNoGui() finishes simulation
		return getNoGuiWinner(c);
	}
	static int getNoGuiWinner(Controller c) {
		c.model.setPerspectiveBlue(c.secret_symbol);
		if(c.model.getScoreSelf() < 0.0f && c.model.getScoreOppo() >= 0.0f)
			return -1;
		else if(c.model.getScoreOppo() < 0.0f && c.model.getScoreSelf() >= 0.0f)
			return 1;
		else
			return 0;
	}
	
	//REGULAR BATTLE
	static void doBattle(IAgent blue, IAgent red) throws Exception { // NO_UCD (unused code)
		Object ss = new Object();
		Controller c = new Controller(ss, blue, red);
		c.initializeGame();
		c.view = new View(c, c.model, ss);
		new Timer(20, c.view).start();
	}
	

	Controller(Object secret_symbol, IAgent blueAgent, IAgent redAgent) {
		this.secret_symbol = secret_symbol;
		this.selectedSprite = -1;
		this.mouseEvents = new LinkedList<MouseEvent>();
		this.blueAgent = blueAgent;
		this.redAgent = redAgent;
	}
	void initializeGame() throws Exception {
		this.model = new Model(this, secret_symbol);
		this.model.initGame();
		this.iter = 0;
		blueAgent.reset();
		redAgent.reset();
		calibrateTimer();
	}
	void calibrateTimer() {
		if(agent_frame_time == 0) {
			long timeA = System.nanoTime();
			for(int i = 0; i < 420; i++)
				for(int y = 0; y < 60; y++)
					for(int x = 0; x < 120; x++)
						model.getTravelSpeed(10 * x, 10 * y);
			long timeB = System.nanoTime();
			agent_frame_time = timeB - timeA;
			//System.out.println("Cycles=" + Long.toString(agent_frame_time));
		}
		blue_time_balance = 20 * agent_frame_time;
		red_time_balance = blue_time_balance;
	}
	boolean update() {		//Updates/Keeps threads in sync
		long timeA = System.nanoTime();
		try {
			model.setPerspectiveBlue(secret_symbol); // Blue on left, Red on right
			if(blue_time_balance >= 0)
				blueAgent.update(model);
		} catch(Exception e) {
			model.setFlagEnergyBlue(secret_symbol, -100.0f);
			e.printStackTrace();
			return false;
		}
		long timeB = System.nanoTime();
		try {
			model.setPerspectiveRed(secret_symbol); // Red on left, Blue on right
			if(red_time_balance >= 0)
				redAgent.update(model);
		} catch(Exception e) {
			model.setFlagEnergyRed(secret_symbol, -100.0f);
			e.printStackTrace();
			return false;
		}
		long timeC = System.nanoTime();
		blue_time_balance = Math.min(blue_time_balance + agent_frame_time + timeA - timeB, 20 * agent_frame_time);
		red_time_balance = Math.min(red_time_balance + agent_frame_time + timeB - timeC, 20 * agent_frame_time);
		if(iter++ >= MAX_ITERS)
			return false; // out of time
		model.update();
		if(amIblue)
			model.setPerspectiveBlue(secret_symbol); // Blue on left, Red on right
		else
			model.setPerspectiveRed(secret_symbol); // Red on left, Blue on right
		return model.getScoreSelf() >= 0.0f && model.getScoreOppo() >= 0.0f;
	}
	
	
	//GETTERS
	int getSelectedSprite() { return selectedSprite; }
	long getTimeBalance(Object secret_symbol, boolean blue) { return blue ? blue_time_balance : red_time_balance; }

	Controller fork(IAgent myShadowAgent, IAgent opponentShadowAgent) {
		amIblue = model.amIblue(secret_symbol);
		Controller c = new Controller(secret_symbol, amIblue ? myShadowAgent : opponentShadowAgent, amIblue ? opponentShadowAgent : myShadowAgent);
		c.agent_frame_time = agent_frame_time;
		c.blue_time_balance = blue_time_balance;
		c.red_time_balance = red_time_balance;
		c.iter = iter;
		c.amIblue = amIblue;
		c.model = model.clone(c, secret_symbol);
		return c;
	}
	
	//Mouse Stuff
	MouseEvent nextMouseEvent() { // NO_UCD (unused code)
		if(mouseEvents.size() == 0)
			return null;
		return mouseEvents.remove();
	}
	public void mousePressed(MouseEvent e) {
		if(e.getY() < 600) {
			mouseEvents.add(e);
			if(mouseEvents.size() > 20) // discard events if the queue gets big
				mouseEvents.remove();
		}
	}
	public void mouseReleased(MouseEvent e) {    }
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }
	public void mouseClicked(MouseEvent e) {    }
}