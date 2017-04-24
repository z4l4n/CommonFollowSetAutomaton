import org.antlr.runtime.tree.Tree;

import cfsautomaton.CFS;
import pcreparser.PCRE;
import thomsonautomaton.ThomsonAutomaton;

public class Runnable {

	public static void main(String[] args) {

		/*
		 * if(args.length != 1) { System.err.println(
		 * "usage: java -jar PCRE.jar 'regex-pattern'"); System.exit(42); }
		 */

		
		String regex = "abc*";
		PCRE pcre2 = new PCRE(regex);
		Tree t = pcre2.getAppropriateTree();
		
		ThomsonAutomaton c = new ThomsonAutomaton(t);
		
		
		CFS c2 = new CFS(t);
		// ε empty string
	
		System.out.println("Thomson state count: " + c.getStateCount());
		System.out.println("Thomson transition count: " + c.getTransitionCount());
		System.out.println("CFS state count: " + c2.getStateCount());
		System.out.println("CFS transition count: " + c2.getTransitionCount());
		
		String inputString = "ab";
		
		System.out.println(c.matches(inputString) ? "Thomson matches!" : "Thomson doesnt match!");
		System.out.println(c2.matches(inputString) ? "CFS matches!" : "CFS doesnt match!");
		System.out.println("joooo");
	}

}
