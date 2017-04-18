package thomsonautomaton;

import org.antlr.runtime.tree.Tree;

import pcreparser.PCRELexer;

public class ThomsonAutomaton {
	private State initialState = new State();
	private State finalState = new State();
	
	public ThomsonAutomaton(Tree regEx) {
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
				subAutomaton1 = new ThomsonAutomaton(regEx.getChild(0));
				subAutomaton2 = new ThomsonAutomaton(regEx.getChild(1));
				//
			} else {
				System.out.println("CONCAT.getChildCount() != 2");
			}
			break;
		}
		
		
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
