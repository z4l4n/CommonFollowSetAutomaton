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
		last = PCRE.getLastMap(t);
		decomposition = PCRE.algorithm1(PCRE.getPositionMap(t), t);
		first = PCRE.getFirstMap(t);
		initialState = new State(first, PCRE.canBeEmptyString(t));
		states = new HashSet<State>();
		states.add(initialState);
		
		LinkedList<State> S = new LinkedList<State>();
		S.add(initialState);
		State from, to;
		HashSet<HashMap<Tree, Integer>> tempSet;
		while (!S.isEmpty()) {
			from = S.removeFirst();
			for (Entry<Tree, Integer> e : from.getPositions().entrySet()) {
				tempSet = decomposition.get(e.getValue());
				for (HashMap<Tree, Integer> i : tempSet) {
					if (i.isEmpty() && !last.containsKey(e.getKey())) { // pl.: dec(x) = {{}, {1, 2}, {3}} üres halmazát nem tesszük be
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
	
	public int getStateCount() {
		return states.size();
	}
	
	public int getTransitionCount() {
		int res = 0;
		HashSet<State> S = new HashSet<State>(); //visited
		HashSet<State> Q = new HashSet<State>(); //unvisited list
		HashSet<State> temp = new HashSet<State>();
		Q.add(initialState);
		
		while (!S.containsAll(Q)) {
			temp.clear();
			for (State p : Q) {
				if (!S.contains(p)) {
					for (Entry<Character, HashSet<State>> entry : p.getCharTransitions().entrySet()) {
						res += entry.getValue().size();
						temp.addAll(entry.getValue());
					}
					S.add(p);
				}
			}
			Q.clear();
			Q.addAll(temp);
		}
		return res;
	}
	
	public boolean matches(String s) {
		HashSet<State> actualStates = new HashSet<State>();
		HashSet<State> tempStateSet = new HashSet<State>();
		actualStates.add(initialState);
		
		for (int i = 0; i < s.length(); i++) {
			tempStateSet.clear();
			for (State state : actualStates) {
				tempStateSet.addAll(state.getNextStates(s.charAt(i)));
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
