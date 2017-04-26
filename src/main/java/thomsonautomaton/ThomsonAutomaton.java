package thomsonautomaton;

import java.util.HashSet;
import java.util.Map.Entry;

import org.antlr.runtime.tree.Tree;
import pcreparser.PCRELexer;

public class ThomsonAutomaton {
	private State initialState;;
	private State finalState;
	
	
	public ThomsonAutomaton(Tree regEx, State initialState, State finalState) {
		this.initialState = initialState;
		this.finalState = finalState;
		@SuppressWarnings("unused")
		ThomsonAutomaton subAutomaton1, subAutomaton2;
		switch(regEx.getType()) {
		
		case PCRELexer.LITERAL:
			if(regEx.getText().charAt(0) == 'ε') {
				initialState.addEmptyTransition(finalState);
				break;
			}
			initialState.addTransition(regEx.getText().charAt(0), finalState);
			break;
			
		case PCRELexer.OR:	
			for (int i = 0; i < regEx.getChildCount(); i++) {
				subAutomaton1 = new ThomsonAutomaton(regEx.getChild(i));
				initialState.addEmptyTransition(subAutomaton1.initialState);
				subAutomaton1.finalState.addEmptyTransition(finalState);
			}
			break;
			
		case PCRELexer.ALTERNATIVE:
			if (regEx.getChildCount() == 2) {
				State temp = new State();
				subAutomaton1 = new ThomsonAutomaton(regEx.getChild(0), initialState, temp);
				subAutomaton2 = new ThomsonAutomaton(regEx.getChild(1), temp, finalState);
			} else {
				System.out.println("CONCAT.getChildCount() != 2");
			}
			break;
			
		case PCRELexer.ELEMENT:
			subAutomaton1 = new ThomsonAutomaton(regEx.getChild(0));
			initialState.addEmptyTransition(subAutomaton1.initialState);
			subAutomaton1.finalState.addEmptyTransition(subAutomaton1.initialState);
			subAutomaton1.finalState.addEmptyTransition(finalState);
			initialState.addEmptyTransition(finalState);
		}
	}
	
	public ThomsonAutomaton(Tree regEx) {
		this(regEx, new State(), new State());
	}
	
	
	public HashSet<State> getEmptyTransitionClosure(State state) {
		HashSet<State> result = new HashSet<State>(state.getNextStates());
		result.add(state); // kiindulópont kell bele?
		HashSet<State> temp = new HashSet<State>();
		while (true) {
			temp.clear();
			for (State s : result) {
				temp.addAll(s.getNextStates());
			}
			if (result.containsAll(temp)) {
				return result;
			} else {
				result.addAll(temp);
			}
		}
	}
	
	public boolean match(String s) {
		HashSet<State> actualStates = new HashSet<State>();
		HashSet<State> tempStateSet = new HashSet<State>();
		actualStates.add(initialState);
		actualStates.addAll(getEmptyTransitionClosure(initialState));
		for (int i = 0; i < s.length(); i++) {
			tempStateSet.clear();
			for (State state : actualStates) {
				tempStateSet.addAll(state.getNextStates(s.charAt(i)));
			}
			actualStates.clear();
			for (State state : tempStateSet) {
				actualStates.addAll(getEmptyTransitionClosure(state));
			}
		}
		for (State state : actualStates) {
			if (state == finalState) {
				return true;
			} 
		}
		return false;
	}
	
	public int getTransitionCount() {
		int res = 0;
		HashSet<State> S = new HashSet<State>(); //visited
		HashSet<State> Q = new HashSet<State>();
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
					res += p.getEmptyTransitions().size();
					temp.addAll(p.getEmptyTransitions());
					S.add(p);
				}
			}
			Q.clear();
			Q.addAll(temp);
		}
		return res;
	}
	
	public int getStateCount() {
		HashSet<State> S = new HashSet<State>(); //visited
		HashSet<State> Q = new HashSet<State>();
		HashSet<State> temp = new HashSet<State>();
		Q.add(initialState);
		while (!S.containsAll(Q)) {
			temp.clear();
			for (State p : Q) {
				if (!S.contains(p)) {
					for (Entry<Character, HashSet<State>> entry : p.getCharTransitions().entrySet()) {
						temp.addAll(entry.getValue());
					}
					temp.addAll(p.getEmptyTransitions());
					S.add(p);
				}
			}
			Q.clear();
			Q.addAll(temp);
		}
		return S.size();
	}
	
	
	
	//GETTERS, SETTERS
	public State getInitialState() {
		return initialState;
	}

	public void setInitialState(State initialState) {
		this.initialState = initialState;
	}

	public State getFinalState() {
		return finalState;
	}

	public void setFinalState(State finalState) {
		this.finalState = finalState;
	}
	
	
}
