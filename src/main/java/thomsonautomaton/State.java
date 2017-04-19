package thomsonautomaton;

import java.util.HashMap;
import java.util.HashSet;



public class State {
	
	private HashMap<Character, HashSet<State>> charTransitions = new HashMap<Character, HashSet<State>>();
	private HashSet<State> emptyTransitions = new HashSet<State>();
	
	
	public HashSet<State> getNextStates(Character c) {
		HashSet<State> s = charTransitions.get(c);
		return s == null ? new HashSet<State>() : s;
	}
	
	//EMPTY TRANSITION
	public HashSet<State> getNextStates() {
		return emptyTransitions;
	}
	
	public void addTransition(Character character, State state) {
		if (!charTransitions.containsKey(character)) {
			HashSet<State> set = new HashSet<State>();
			set.add(state);
			charTransitions.put(character, set);
		} else {
			charTransitions.get(character).add(state);
		}
	}
	
	public void addEmptyTransition(State state) {
		emptyTransitions.add(state);
	}
	
}
