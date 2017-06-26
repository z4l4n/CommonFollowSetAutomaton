import org.antlr.runtime.tree.CommonTree;

import cfsautomaton.CFSAutomaton;
import pcreparser.PCRE;
import thomsonautomaton.ThomsonAutomaton;

public class Runnable {

	public static void main(String[] args) {

		/*
		 * if(args.length != 1) { System.err.println(
		 * "usage: java -jar PCRE.jar 'regex-pattern'"); System.exit(42); }
		 */
		CommonTree regexTree = null;
		CFSAutomaton cfs;
		ThomsonAutomaton thomson;
		
		int cfsTotalStates= 0;
		int thomsonTotalStates = 0;	
		int cfsTotalTransitions = 0;
		int thomsonTotalTransitions = 0;
		
		int cfsTran;
		int tTran;
		int cfsStates;
		int tStates;
		
		
		for (int regexSize = 1; regexSize < 40; regexSize++) {
			
			for (int i = 0; i < 4; i++) {
				regexTree = PCRE.buildRandomRegex(regexSize, regexSize, true, true);
				cfs = new CFSAutomaton(regexTree);
				thomson = new ThomsonAutomaton(regexTree);
				System.out.println(PCRE.reparse(regexTree));
				System.out.println("Regex size: " + regexSize);

				cfsTotalTransitions += cfsTran = cfs.getTransitionCount();
				thomsonTotalTransitions += tTran = thomson.getTransitionCount();
				cfsTotalStates += cfsStates = cfs.getStateCount();
				thomsonTotalStates += tStates = thomson.getStateCount();
				
				System.out.println("Common follow set transition count: " + cfsTran);
				System.out.println("Thomson automaton transition count: " + tTran);
				System.out.println("Common follow set state count: " + cfsStates);
				System.out.println("Thomson automaton state count: " + tStates);
				
				System.out.println();
			}
			
		}
		System.out.println("Common follow set total transition count: " + cfsTotalTransitions);
		System.out.println("Thomson transition count: " + thomsonTotalTransitions);
		System.out.println("Common follow set total state count: " + cfsTotalStates);
		System.out.println("Thomson automaton  total state count: " + thomsonTotalStates);
		
		
		
		
		
	}

}
