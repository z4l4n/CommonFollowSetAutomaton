package thomsonautomaton;

import java.util.HashSet;
import org.antlr.runtime.tree.Tree;
import pcreparser.PCRELexer;

public class ThomsonAutomaton {
	private State initialState;;
	private State finalState;
	
	
	public ThomsonAutomaton(Tree regEx, State initialState, State finalState) {
		this.initialState = initialState;
		this.finalState = finalState;
		ThomsonAutomaton subAutomaton1, subAutomaton2;
		switch(regEx.getType()) {
		
		case PCRELexer.LITERAL:
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
	//TODO empty transition bug
	public boolean matches(String s) {
		HashSet<State> actualStates = new HashSet<State>();
		HashSet<State> tempStateSet = new HashSet<State>();
		actualStates.add(initialState);
		actualStates.addAll(getEmptyTransitionClosure(initialState));
		for (int i = 0; i < s.length(); i++) {
			tempStateSet.clear();
			for (State state : actualStates) {
				tempStateSet.addAll(state.getNextStates(s.charAt(i)));
				tempStateSet.addAll(getEmptyTransitionClosure(state));
				
			}
			actualStates.clear();
			actualStates.addAll(tempStateSet);
		}
		for (State state : actualStates) {
			if (state == finalState) {
				return true;
			} 
		}
		return false;
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
