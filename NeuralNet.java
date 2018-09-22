import java.util.ArrayList;
import java.util.Random;


abstract class Json {
	abstract void write(StringBuilder sb);

	public static Json newObject() {
		return new JObject();
	}

	public static Json newList() {
		return new JList();
	}

	public static Json parseNode(StringParser p) {
		p.skipWhitespace();
		if(p.remaining() == 0)
			throw new RuntimeException("Unexpected end of JSON file");
		char c = p.peek();
		if(c == '"')
			return new JString(JString.parseString(p));
		else if(c == '{')
			return JObject.parseObject(p);
		else if(c == '[')
			return JList.parseList(p);
		else if(c == 't') {
			p.expect("true");
			return new JBool(true);
		}
		else if(c == 'f') {
			p.expect("false");
			return new JBool(false);
		}
		else if(c == 'n') {
			p.expect("null");
			return new JNull();
		}
		else if((c >= '0' && c <= '9') || c == '-')
			return JDouble.parseNumber(p);
		else
			throw new RuntimeException("Unexpected token at " + p.str.substring(p.pos, Math.min(p.remaining(), 50)));
	}

	public int size() {
		return this.asList().size();
	}

	public Json get(String name) {
		return this.asObject().field(name);
	}

	public Json get(int index) {
		return this.asList().get(index);
	}

	public long getLong(String name) {
		return get(name).asLong();
	}

	public double getDouble(int index) {
		return get(index).asDouble();
	}

	public void add(String name, Json val) {
		this.asObject().add(name, val);
	}

	public void add(String name, long val) {
		this.asObject().add(name, new Json.JLong(val));
	}

	public void add(Json item) {
		this.asList().add(item);
	}

	public void add(double val) {
		this.asList().add(new Json.JDouble(val));
	}

	public boolean asBool() {
		return ((JBool)this).value;
	}

	public long asLong() {
		return ((JLong)this).value;
	}

	public double asDouble() {
		return ((JDouble)this).value;
	}

	public String asString() {
		return ((JString)this).value;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		write(sb);
		return sb.toString();
	}

	private JObject asObject() { return (JObject)this; }

	private JList asList() { return (JList)this; }

	public static Json parse(String s) {
		StringParser p = new StringParser(s);
		return Json.parseNode(p);
	}

	public static class StringParser {
		String str;
		int pos;

		StringParser(String s) {
			str = s;
			pos = 0;
		}

		int remaining() {
			return str.length() - pos;
		}

		char peek() {
			return str.charAt(pos);
		}

		void advance(int n) {
			pos += n;
		}

		void skipWhitespace() {
			while(pos < str.length() && str.charAt(pos) <= ' ')
				pos++;
		}

		void expect(String s) {
			if(!str.substring(pos, Math.min(str.length(), pos + s.length())).equals(s))
				throw new RuntimeException("Expected \"" + s + "\", Got \"" + str.substring(pos, Math.min(str.length(), pos + s.length())) + "\"");
			pos += s.length();
		}

		String until(char c) {
			int i = pos;
			while(i < str.length() && str.charAt(i) != c)
				i++;
			String s = str.substring(pos, i);
			pos = i;
			return s;
		}

		String until(char a, char b) {
			int i = pos;
			while(i < str.length() && str.charAt(i) != a && str.charAt(i) != b)
				i++;
			String s = str.substring(pos, i);
			pos = i;
			return s;
		}

		String untilWhitespace() {
			int i = pos;
			while(i < str.length() && str.charAt(i) > ' ')
				i++;
			String s = str.substring(pos, i);
			pos = i;
			return s;
		}

		String untilQuoteSensitive(char a, char b) {
			if(peek() == '"') {
				advance(1);
				String s = "\"" + until('"') + "\"";
				advance(1);
				until(a, b);
				return s;
			}
			else
				return until(a, b);
		}

		String whileReal() {
			int i = pos;
			while(i < str.length()) {
				char c = str.charAt(i);
				if((c >= '0' && c <= '9') ||
					c == '-' ||
					c == '+' ||
					c == '.' ||
					c == 'e' ||
					c == 'E')
					i++;
				else
					break;
			}
			String s = str.substring(pos, i);
			pos = i;
			return s;
		}
	}

	private static class NameVal {
		String name;
		Json value;

		NameVal(String nam, Json val) {
			if(nam == null)
				throw new IllegalArgumentException("The name cannot be null");
			if(val == null)
				val = new JNull();
			name = nam;
			value = val;
		}
	}

	private static class JObject extends Json {
		ArrayList<NameVal> fields;

		JObject() {
			fields = new ArrayList<NameVal>();
		}

		@Override
		public void add(String name, Json val) {
			fields.add(new NameVal(name, val));
		}

		Json fieldIfExists(String name) {
			for(NameVal nv : fields) {
				if(nv.name.equals(name))
					return nv.value;
			}
			return null;
		}

		Json field(String name) {
			Json n = fieldIfExists(name);
			if(n == null)
				throw new RuntimeException("No field named \"" + name + "\" found.");
			return n;
		}

		void write(StringBuilder sb) {
			sb.append("{");
			for(int i = 0; i < fields.size(); i++) {
				if(i > 0)
					sb.append(",");
				NameVal nv = fields.get(i);
				JString.write(sb, nv.name);
				sb.append(":");
				nv.value.write(sb);
			}
			sb.append("}");
		}

		static JObject parseObject(StringParser p) {
			p.expect("{");
			JObject newOb = new JObject();
			boolean readyForField = true;
			while(p.remaining() > 0) {
				char c = p.peek();
				if(c <= ' ') {
					p.advance(1);
				}
				else if(c == '}') {
					p.advance(1);
					return newOb;
				}
				else if(c == ',') {
					if(readyForField)
						throw new RuntimeException("Unexpected ','");
					p.advance(1);
					readyForField = true;
				}
				else if(c == '\"') {
					if(!readyForField)
						throw new RuntimeException("Expected a ',' before the next field in JSON file");
					p.skipWhitespace();
					String name = JString.parseString(p);
					p.skipWhitespace();
					p.expect(":");
					Json value = Json.parseNode(p);
					newOb.add(name, value);
					readyForField = false;
				}
				else
					throw new RuntimeException("Expected a '}' or a '\"'. Got " + p.str.substring(p.pos, p.pos + 10));
			}
			throw new RuntimeException("Expected a matching '}' in JSON file");
		}
	}

	private static class JList extends Json {
		ArrayList<Json> list;

		JList() {
			list = new ArrayList<Json>();
		}

		@Override
		public void add(Json item) {
			if(item == null)
				item = new JNull();
			list.add(item);
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public Json get(int index) {
			return list.get(index);
		}

		void write(StringBuilder sb) {
			sb.append("[");
			for(int i = 0; i < list.size(); i++) {
				if(i > 0)
					sb.append(",");
				list.get(i).write(sb);
			}
			sb.append("]");
		}

		static JList parseList(StringParser p) {
			p.expect("[");
			JList newList = new JList();
			boolean readyForValue = true;
			while(p.remaining() > 0) {
				p.skipWhitespace();
				char c = p.peek();
				if(c == ']') {
					p.advance(1);
					return newList;
				}
				else if(c == ',') {
					if(readyForValue)
						throw new RuntimeException("Unexpected ',' in JSON file");
					p.advance(1);
					readyForValue = true;
				}
				else {
					if(!readyForValue)
						throw new RuntimeException("Expected a ',' or ']' in JSON file");
					newList.list.add(Json.parseNode(p));
					readyForValue = false;
				}
			}
			throw new RuntimeException("Expected a matching ']' in JSON file");
		}
	}

	private static class JBool extends Json {
		boolean value;

		JBool(boolean val) {
			value = val;
		}

		void write(StringBuilder sb) {
			sb.append(value ? "true" : "false");
		}
	}

	private static class JLong extends Json {
		long value;

		JLong(long val) {
			value = val;
		}

		void write(StringBuilder sb) {
			sb.append(value);
		}
	}

	private static class JDouble extends Json {
		double value;

		JDouble(double val) {
			value = val;
		}

		void write(StringBuilder sb) {
			sb.append(value);
		}

		static Json parseNumber(StringParser p) {
			String s = p.whileReal();
			if(s.indexOf('.') >= 0)
				return new JDouble(Double.parseDouble(s));
			else
				return new JLong(Long.parseLong(s));
		}
	}

	private static class JString extends Json {
		String value;

		JString(String val) {
			value = val;
		}

		static void write(StringBuilder sb, String value) {
			sb.append('"');
			for(int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if(c < ' ') {
					switch(c) {
						case '\b': sb.append("\\b"); break;
						case '\f': sb.append("\\f"); break;
						case '\n': sb.append("\\n"); break;
						case '\r': sb.append("\\r"); break;
						case '\t': sb.append("\\t"); break;
						default:
							sb.append(c);
					}
				}
				else if(c == '\\')
					sb.append("\\\\");
				else if(c == '"')
					sb.append("\\\"");
				else
					sb.append(c);
			}
			sb.append('"');
		}

		void write(StringBuilder sb) {
			write(sb, value);
		}

		static String parseString(StringParser p) {
			StringBuilder sb = new StringBuilder();
			p.expect("\"");
			while(p.remaining() > 0) {
				char c = p.peek();
				if(c == '\"') {
					p.advance(1);
					return sb.toString();
				}
				else if(c == '\\') {
					p.advance(1);
					c = p.peek();
					p.advance(1);
					switch(c) {
						case '"': sb.append('"'); break;
						case '\\': sb.append('\\'); break;
						case '/': sb.append('/'); break;
						case 'b': sb.append('\b'); break;
						case 'f': sb.append('\f'); break;
						case 'n': sb.append('\n'); break;
						case 'r': sb.append('\r'); break;
						case 't': sb.append('\t'); break;
						case 'u': throw new RuntimeException("Sorry, unicode characters are not yet supported");
						default: throw new RuntimeException("Unrecognized escape sequence");
					}
				}
				else {
					sb.append(c);
					p.advance(1);
				}
			}
			throw new RuntimeException("No closing \"");
		}
	}

	private static class JNull extends Json {
		JNull() {
		}

		void write(StringBuilder sb) {
			sb.append("null");
		}
	}
}
abstract class Layer {
	double[] activation, error;

	static final int t_linear = 0;
	static final int t_tanh = 1;

	Layer(int outputs) {									/// General-purpose Layer constructor
		activation = new double[outputs];
		error = new double[outputs];
	}
	Layer(Layer that) {										/// Copy constructor
		activation = Vec.copy(that.activation);
		error = Vec.copy(that.error);
	}
	Layer(Json n) {											/// Unmarshal from a JSON DOM
		int units = (int)n.getLong("units");
		activation = new double[units];
		error = new double[units];
	}

	void computeError(double[] target) {
		if(target.length != activation.length)
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(target.length) + " != " + Integer.toString(activation.length));
		for(int i = 0; i < activation.length; i++)
			error[i] = target[i] - activation[i];
	}

	int outputCount() {
		return activation.length;
	}

	static Layer unmarshal(Json n) {
		int t = (int)n.getLong("type");
		switch(t) {
			case t_linear: return new LayerLinear(n);
			case t_tanh: return new LayerTanh(n);
			default: throw new RuntimeException("Unrecognized type");
		}
	}

	protected abstract Layer clone();
	abstract Json marshal();
	abstract int type();
	abstract int inputCount();
	abstract void initWeights(Random r);
	abstract double[] forwardProp(double[] in);
	abstract void backProp(Layer upStream);
	abstract void scaleGradient(double momentum);
	abstract void updateGradient(double[] in);
	abstract void step(double stepSize);
	abstract int countWeights();
	abstract int setWeights(double[] w, int start);
	abstract void regularizeWeights(double lambda);
}

class LayerLinear extends Layer {
	Matrix weights; // rows are inputs, cols are outputs
	Matrix weightsGrad;
	double[] bias;
	double[] biasGrad;

	LayerLinear(int inputs, int outputs) {					/// General-purpose LayerLinear constructor
		super(outputs);
		weights = new Matrix();
		weights.setSize(inputs, outputs);
		weightsGrad = new Matrix();
		weightsGrad.setSize(inputs, outputs);
		bias = new double[outputs];
		biasGrad = new double[outputs];
	}
	LayerLinear(LayerLinear that) {							/// Copy constructor LayerLinear
		super(that);
		weights = new Matrix(that.weights);
		weightsGrad = new Matrix(that.weightsGrad);
		bias = Vec.copy(that.bias);
		biasGrad = Vec.copy(that.biasGrad);
		weightsGrad = new Matrix();
		weightsGrad.setSize(weights.rows(), weights.cols());
		weightsGrad.setAll(0.0);
		biasGrad = new double[weights.cols()];
		Vec.setAll(biasGrad, 0.0);
	}

	LayerLinear(Json n) {									/// Unmarshal from a JSON DOM
		super(n);
		weights = new Matrix(n.get("weights"));
		bias = Vec.unmarshal(n.get("bias"));
	}

	@Override
	protected LayerLinear clone() {
		return new LayerLinear(this);
	}

	@Override
	Json marshal() {										/// Marshal into a JSON DOM
		Json ob = Json.newObject();
		ob.add("units", outputCount()); // required in all layers
		ob.add("weights", weights.marshal());
		ob.add("bias", Vec.marshal(bias));
		return ob;
	}

	void copy(LayerLinear src) {
		if(src.weights.rows() != weights.rows() || src.weights.cols() != weights.cols())
			throw new IllegalArgumentException("mismatching sizes");
		weights.copyBlock(0, 0, src.weights, 0, 0, src.weights.rows(), src.weights.cols());
		for(int i = 0; i < bias.length; i++)
			bias[i] = src.bias[i];
	}

	@Override
	int type() { return t_linear; }
	@Override
	int inputCount() { return weights.rows(); }

	void initWeights(Random r) {
		double dev = Math.max(0.3, 1.0 / weights.rows());
		for(int i = 0; i < weights.rows(); i++) {
			double[] row = weights.row(i);
			for(int j = 0; j < weights.cols(); j++)
				row[j] = dev * r.nextGaussian();
		}
		for(int j = 0; j < weights.cols(); j++)
			bias[j] = dev * r.nextGaussian();
		weightsGrad.setAll(0.0);
		Vec.setAll(biasGrad, 0.0);
	}

	@Override
	int countWeights() {
		return weights.rows() * weights.cols() + bias.length;
	}

	@Override
	int setWeights(double[] w, int start) {
		int oldStart = start;
		for(int i = 0; i < bias.length; i++)
			bias[i] = w[start++];
		for(int i = 0; i < weights.rows(); i++) {
			double[] row = weights.row(i);
			for(int j = 0; j < weights.cols(); j++)
				row[j] = w[start++];
		}
		return start - oldStart;
	}

	@Override
	double[] forwardProp(double[] in) {
		if(in.length != weights.rows())
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(in.length) + " != " + Integer.toString(weights.rows()));
		for(int i = 0; i < activation.length; i++)
			activation[i] = bias[i];
		for(int j = 0; j < weights.rows(); j++) {
			double v = in[j];
			double[] w = weights.row(j);
			for(int i = 0; i < weights.cols(); i++)
				activation[i] += v * w[i];
		}
		return activation;
	}

	double[] forwardProp2(double[] in1, double[] in2) {
		if(in1.length + in2.length != weights.rows())
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(in1.length) + " + " + Integer.toString(in2.length) + " != " + Integer.toString(weights.rows()));
		for(int i = 0; i < activation.length; i++)
			activation[i] = bias[i];
		for(int j = 0; j < in1.length; j++) {
			double v = in1[j];
			double[] w = weights.row(j);
			for(int i = 0; i < weights.cols(); i++)
				activation[i] += v * w[i];
		}
		for(int j = 0; j < in2.length; j++) {
			double v = in2[j];
			double[] w = weights.row(in1.length + j);
			for(int i = 0; i < weights.cols(); i++)
				activation[i] += v * w[i];
		}
		return activation;
	}

	@Override
	void backProp(Layer upStream) {
		if(upStream.outputCount() != weights.rows())
			throw new IllegalArgumentException("size mismatch");
		for(int j = 0; j < weights.rows(); j++) {
			double[] w = weights.row(j);
			double d = 0.0;
			for(int i = 0; i < weights.cols(); i++)
				d += error[i] * w[i];
			upStream.error[j] = d;
		}
	}

	void refineInputs(double[] inputs, double learningRate) {
		if(inputs.length != weights.rows())
			throw new IllegalArgumentException("size mismatch");
		for(int j = 0; j < weights.rows(); j++) {
			double[] w = weights.row(j);
			double d = 0.0;
			for(int i = 0; i < weights.cols(); i++)
				d += error[i] * w[i];
			inputs[j] += learningRate * d;
		}
	}

	@Override
	void scaleGradient(double momentum) {
		weightsGrad.scale(momentum);
		Vec.scale(biasGrad, momentum);
	}

	@Override
	void updateGradient(double[] in) {
		for(int i = 0; i < bias.length; i++)
			biasGrad[i] += error[i];
		for(int j = 0; j < weights.rows(); j++) {
			double[] w = weightsGrad.row(j);
			double x = in[j];
			for(int i = 0; i < weights.cols(); i++)
				w[i] += x * error[i];
		}
	}

	@Override
	void step(double stepSize) {
		weights.addScaled(weightsGrad, stepSize);
		Vec.addScaled(bias, biasGrad, stepSize);
	}

	// Applies both L2 and L1 regularization to the weights and bias values
	@Override
	void regularizeWeights(double lambda) {
		for(int i = 0; i < weights.rows(); i++) {
			double[] row = weights.row(i);
			for(int j = 0; j < row.length; j++) {
				row[j] *= (1.0 - lambda);
				if(row[j] < 0.0)
					row[j] += lambda;
				else
					row[j] -= lambda;
			}
		}
		for(int j = 0; j < bias.length; j++) {
			bias[j] *= (1.0 - lambda);
			if(bias[j] < 0.0)
				bias[j] += lambda;
			else
				bias[j] -= lambda;
		}
	}
}

class LayerTanh extends Layer {
	LayerTanh(int nodes) { super(nodes); }					/// General-purpose LayerTanh constructor
	LayerTanh(LayerTanh that) {	super(that); }				/// Copy constructor
	LayerTanh(Json n) {	super(n); }							/// Unmarshal from a JSON DOM
	@Override
	protected LayerTanh clone() {							/// Unmarshal from a JSON DOM
		return new LayerTanh(this);
	}

	@Override
	Json marshal() {										/// Marshal into a JSON DOM
		Json ob = Json.newObject();
		ob.add("units", outputCount()); //required in all layers
		return ob;
	}

	void copy(LayerTanh src) {
	}

	@Override
	int type() { return t_tanh; }
	@Override
	int inputCount() { return activation.length; }

	void initWeights(Random r) {
	}

	@Override
	int countWeights() {
		return 0;
	}

	@Override
	int setWeights(double[] w, int start) {
		return 0;
	}

	@Override
	double[] forwardProp(double[] in) {
		if(in.length != outputCount())
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(in.length) + " != " + Integer.toString(outputCount()));
		for(int i = 0; i < activation.length; i++)
			activation[i] = Math.tanh(in[i]);
		return activation;
	}

	@Override
	void backProp(Layer upStream) {
		if(upStream.outputCount() != outputCount())
			throw new IllegalArgumentException("size mismatch");
		for(int i = 0; i < activation.length; i++)
			upStream.error[i] = error[i] * (1.0 - activation[i] * activation[i]);
	}

	@Override
	void scaleGradient(double momentum) {
	}

	@Override
	void updateGradient(double[] in) {
	}

	@Override
	void step(double stepSize) {
	}

	// Applies both L2 and L1 regularization to the weights and bias values
	@Override
	void regularizeWeights(double lambda) {
	}
}

public class NeuralNet {
	public ArrayList<Layer> layers;

	NeuralNet() { layers = new ArrayList<Layer>(); }		/// NN constructor with 0 layers
	NeuralNet(NeuralNet that) {								/// NN Copy constructor
		layers = new ArrayList<Layer>();
		for(int i = 0; i < that.layers.size(); i++)
			layers.add(that.layers.get(i).clone());
	}

	NeuralNet(Json n) {										/// Unmarshals from a JSON DOM.
		layers = new ArrayList<Layer>();
		Json l = n.get("layers");
		for(int i = 0; i < l.size(); i++)
			layers.add(Layer.unmarshal(l.get(i)));
	}

	Json marshal() {										/// Marshal this nn into a JSON DOM.
		Json ob = Json.newObject();
		Json l = Json.newList();
		ob.add("layers", l);
		for(int i = 0; i < layers.size(); i++)
			l.add(layers.get(i).marshal());
		return ob;
	}

	void init(Random r) {									/// Init weights/biases w/ small random values
		for(int i = 0; i < layers.size(); i++)
			layers.get(i).initWeights(r);
	}

	/// Feeds "in" into this neural network and propagates it forward to compute predicted outputs.
	double[] forwardProp(double[] in) {
		for(int i = 0; i < layers.size(); i++)
			in = layers.get(i).forwardProp(in);
		return in;
	}

	/// Feeds the concatenation of "in1" and "in2" into this neural network and propagates it forward to compute predicted outputs.
	double[] forwardProp2(double[] in1, double[] in2) {
		double[] in = ((LayerLinear)layers.get(0)).forwardProp2(in1, in2);
		for(int i = 1; i < layers.size(); i++)
			in = layers.get(i).forwardProp(in);
		return in;
	}
	
	void backProp(double[] target) {						/// Backpropagates the error to the upstream layer.
		int i = layers.size() - 1;
		Layer l = layers.get(i);
		l.computeError(target);
		for(i--; i >= 0; i--) {
			Layer upstream = layers.get(i);
			l.backProp(upstream);
			l = upstream;
		}
	}

	/// Backpropagates the error from another neural network. (This is used when training autoencoders.)
	void backPropFromDecoder(NeuralNet decoder) {
		int i = layers.size() - 1;
		Layer l = decoder.layers.get(0);
		Layer upstream = layers.get(i);
		l.backProp(upstream);
		l = upstream;
		for(i--; i >= 0; i--) {
			upstream = layers.get(i);
			l.backProp(upstream);
			l = upstream;
		}
	}

	void descendGradient(double[] in, double learnRate) {	/// Updates the weights and biases
		for(int i = 0; i < layers.size(); i++) {
			Layer l = layers.get(i);
			l.scaleGradient(0.0);
			l.updateGradient(in);
			l.step(learnRate);
			in = l.activation;
		}
	}

	void regularize(double amount) {						/// Keeps weights/biases from getting too big
		for(int i = 0; i < layers.size(); i++) {
			Layer lay = layers.get(i);
			lay.regularizeWeights(amount);
		}
	}

	/// Refines the weights and biases with on iteration of stochastic gradient descent.
	void trainIncremental(double[] in, double[] target, double learningRate) {
		forwardProp(in);
		backProp(target);
		//backPropAndBendHinge(target, learningRate);
		descendGradient(in, learningRate);
	}

	/// Refines "in" with one iteration of stochastic gradient descent.
	void refineInputs(double[] in, double[] target, double learningRate) {
		forwardProp(in);
		backProp(target);
		((LayerLinear)layers.get(0)).refineInputs(in, learningRate);
	}

	static void testMath() {
		NeuralNet nn = new NeuralNet();
		LayerLinear l1 = new LayerLinear(2, 3);
		l1.weights.row(0)[0] = 0.1;
		l1.weights.row(0)[1] = 0.0;
		l1.weights.row(0)[2] = 0.1;
		l1.weights.row(1)[0] = 0.1;
		l1.weights.row(1)[1] = 0.0;
		l1.weights.row(1)[2] = -0.1;
		l1.bias[0] = 0.1;
		l1.bias[1] = 0.1;
		l1.bias[2] = 0.0;
		nn.layers.add(l1);
		nn.layers.add(new LayerTanh(3));

		LayerLinear l2 = new LayerLinear(3, 2);
		l2.weights.row(0)[0] = 0.1;
		l2.weights.row(0)[1] = 0.1;
		l2.weights.row(1)[0] = 0.1;
		l2.weights.row(1)[1] = 0.3;
		l2.weights.row(2)[0] = 0.1;
		l2.weights.row(2)[1] = -0.1;
		l2.bias[0] = 0.1;
		l2.bias[1] = -0.2;
		nn.layers.add(l2);
		nn.layers.add(new LayerTanh(2));

		System.out.println("l1 weights:" + l1.weights.toString());
		System.out.println("l1 bias:" + Vec.toString(l1.bias));
		System.out.println("l2 weights:" + l2.weights.toString());
		System.out.println("l2 bias:" + Vec.toString(l2.bias));

		System.out.println("----Forward prop");
		double in[] = new double[2];
		in[0] = 0.3;
		in[1] = -0.2;
		double[] out = nn.forwardProp(in);
		System.out.println("activation:" + Vec.toString(out));

		System.out.println("----Back prop");
		double targ[] = new double[2];
		targ[0] = 0.1;
		targ[1] = 0.0;
		nn.backProp(targ);
		System.out.println("error 2:" + Vec.toString(l2.error));
		System.out.println("error 1:" + Vec.toString(l1.error));
		
		nn.descendGradient(in, 0.1);
		System.out.println("----Descending gradient");
		System.out.println("l1 weights:" + l1.weights.toString());
		System.out.println("l1 bias:" + Vec.toString(l1.bias));
		System.out.println("l2 weights:" + l2.weights.toString());
		System.out.println("l2 bias:" + Vec.toString(l2.bias));

		if(Math.abs(l1.weights.row(0)[0] - 0.10039573704287) > 0.0000000001)
			throw new IllegalArgumentException("failed");
		if(Math.abs(l1.weights.row(0)[1] - 0.0013373814241446) > 0.0000000001)
			throw new IllegalArgumentException("failed");
		if(Math.abs(l1.bias[1] - 0.10445793808048) > 0.0000000001)
			throw new IllegalArgumentException("failed");
		System.out.println("passed");
	}
}