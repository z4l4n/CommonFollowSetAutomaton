package cfsautomaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.antlr.runtime.tree.Tree;

public class State {
	//
	
	private HashMap<Tree, Integer> positions;
	private boolean isFinal;
	private HashMap<Character, HashSet<State>> charTransitions;
	
	public State(HashMap<Tree, Integer> positions, boolean isFinal) {
		super();
		this.positions = positions;
		this.isFinal = isFinal;
		charTransitions = new HashMap<Character, HashSet<State>>();
	}
	
	
	public void addEdge(Character character, State state) {
		if (!charTransitions.containsKey(character)) {
			HashSet<State> set = new HashSet<State>();
			set.add(state);
			charTransitions.put(character, set);
		} else {
			charTransitions.get(character).add(state);
		}
	}
	
	public HashSet<State> getNextStates(Character c) {
		HashSet<State> s = charTransitions.get(c);
		return (s == null ? new HashSet<State>() : s);
	}
	
	public HashMap<Tree, Integer> getPositions() {
		return positions;
	}

	public void setPositions(HashMap<Tree, Integer> positions) {
		this.positions = positions;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public HashMap<Character, HashSet<State>> getCharTransitions() {
		return charTransitions;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isFinal ? 1231 : 1237);
		
		if (positions == null) {
			result = prime * result;
		} else {
			int t = 3;
			for (Entry<Tree, Integer> e : positions.entrySet()) {
				t += e.getValue();
			}
			result = prime * result + t;
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		State other = (State) obj;
		if (isFinal != other.isFinal) {
			return false;
		}
		if (positions == null) {
			if (other.positions != null) {
				return false;
			}
		} else if (!CFSAutomaton.areEqual(positions, other.positions)) {
			return false;
		}
		return true;
	}
	@Override
	public String toString() {
		return "(" + positions + ", " + (isFinal ? "1" : "0") + ")";
	}
	
}
