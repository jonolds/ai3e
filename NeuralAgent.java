class NeuralAgent extends IAgent {
	NeuralNet nn;
	double[] weights, in = new double[20];
	int[][] acts = new int[][]{new int[3], new int[3], new int[3]};
	int agentNum = 0;

	NeuralAgent(double[] chromes) {
		this.weights = chromes;
		setWeights(this.weights);
	}

	@Override
	public void update(Model m) {
		// Compute some features
		in[0] = m.getX(0) / 600.0 - 0.5;
		in[1] = m.getY(0) / 600.0 - 0.5;
		in[2] = m.getX(1) / 600.0 - 0.5;
		in[3] = m.getY(1) / 600.0 - 0.5;
		in[4] = m.getX(2) / 600.0 - 0.5;
		in[5] = m.getY(2) / 600.0 - 0.5;
		in[6] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[7] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[8] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[9] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[10] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[11] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[12] = m.getEnergySelf(0);
		in[13] = m.getEnergySelf(1);
		in[14] = m.getEnergySelf(2);
		in[15] = m.getEnergyOpponent(0);
		in[16] = m.getEnergyOpponent(1);
		in[17] = m.getEnergyOpponent(2);
		in[18] = m.getScoreSelf();
		in[19] = m.getScoreOppo();	
		
		// Determine what each agent should do
		double[] out = nn.forwardProp(in);
		// Do it
		for(int i = 0; i < 3; i++) {
			if(out[i] < -0.333) {
				beDefender(m, i);
			}
			else if(out[i] > 0.333) {
				beAggressor(m, i);
			}
			else {
				beFlagAttacker(m, i);
			}	
		}
	}
	
	void setWeights(double[] weights) {	/// Sets the parames of this agent with the specified weights
		nn = new NeuralNet();
		nn.layers.add(new LayerLinear(in.length, 8));
		nn.layers.add(new LayerTanh(8));
		nn.layers.add(new LayerLinear(8, 10));
		nn.layers.add(new LayerTanh(10));
		nn.layers.add(new LayerLinear(10, 3));
		nn.layers.add(new LayerTanh(3));
		
		if(weights.length != countWeights())
			throw new IllegalArgumentException("Wrong number of weights. Got " + Integer.toString(weights.length) + ", expected " + Integer.toString(countWeights()));
		
		int start = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			start += nn.layers.get(i).setWeights(weights, start);
	}
	
	int countWeights() {	/// get # of weights necessary to fully-parameterize this agent
		int n = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			n += nn.layers.get(i).countWeights();
		return n;
	}
	
	static void printActPercents(int[][] acts) {
		for(int i = 0; i < 3; i++) {
			int tot = acts[i][0] + acts[i][1] + acts[i][2];
			System.out.println(i + ":  " + percent(acts[i][0], tot) + "%   " + percent(acts[i][1], tot) + "%   " + percent(acts[i][2], tot) + "%" );
		}
		System.out.println();
	}
	
	static double percent(int sample, int total) {
		double d = 100*(double)sample/total;
		return (double)((int)(10*d))/10;
	}
}