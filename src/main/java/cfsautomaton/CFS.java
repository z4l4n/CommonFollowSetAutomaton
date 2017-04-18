package cfsautomaton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.antlr.runtime.tree.Tree;
import pcreparser.PCRE;
// Common Follow Set Automaton
public class CFS {
	
	//private HashMap<>
	private HashSet<State> states;
	private State initialState;
	private HashMap<Integer, HashSet<HashMap<Tree, Integer>>> decomposition;
	private HashMap<Tree, Integer> last;
	private HashMap<Tree, Integer> first;
	
	
	
	public CFS(Tree t) {
		this.last = PCRE.getLastMap(t);
		this.decomposition = PCRE.algorithm1(PCRE.getPositionMap(t), t);
		this.first = PCRE.getFirstMap(t);
		this.initialState = new State(first, PCRE.canBeEmptyString(t));
		states = new HashSet<State>();
		this.states.add(initialState);
		
		LinkedList<State> S = new LinkedList<State>();
		S.add(initialState);
		State from, to;
		HashSet<HashMap<Tree, Integer>> tempSet;
		while (!S.isEmpty()) {
			from = S.removeFirst();
			for (Entry<Tree, Integer> e : from.getPositions().entrySet()) {
				tempSet = decomposition.get(e.getValue());
				for (HashMap<Tree, Integer> i : tempSet) {
					if (i.isEmpty() && !last.containsKey(e.getKey())) {
						continue;
					}
					to = new State(i, last.containsValue(e.getValue()));
					if (states.contains(to)) {
						for (State j : states) {
							if (j.equals(to)) {
								to = j;
								break;
							}
						}
					} else {
						S.add(to);
						states.add(to);
					}
					from.addEdge(e.getKey().toString().charAt(0), to);
				}
			}
		}
	}
	
	
	
	public boolean matches(String s) {
		HashSet<State> actualStates = new HashSet<State>();
		HashSet<State> tempStateSet = new HashSet<State>();
		HashSet<State> t;
		actualStates.add(initialState);
		
		for (int i = 0; i < s.length(); i++) {
			tempStateSet.clear();
			t = null;
			for (State state : actualStates) {
				t = state.getNextStates(s.charAt(i));
				if (t != null) {
					tempStateSet.addAll(t);
				}
				
			}
			actualStates.clear();
			actualStates.addAll(tempStateSet);
		}
		for (State state : actualStates) {
			if (state.isFinal()) {
				return true;
			} 
		}
		return false;
	}



	public HashSet<State> getStates() {
		return states;
	}



	public void setStates(HashSet<State> states) {
		this.states = states;
	}



	public State getInitialState() {
		return initialState;
	}



	public HashMap<Integer, HashSet<HashMap<Tree, Integer>>> getDecomposition() {
		return decomposition;
	}
	
	
}
