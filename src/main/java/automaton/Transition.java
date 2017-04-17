package automaton;

public class Transition {

	State from;
	String by;
	State to;
	
	public Transition(State from, String by, State to) {
		super();
		this.from = from;
		this.by = by;
		this.to = to;
	}
	
}
