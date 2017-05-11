import org.antlr.runtime.tree.CommonTree;

import pcreparser.PCRE;

public class Runnable {

	public static void main(String[] args) {

		/*
		 * if(args.length != 1) { System.err.println(
		 * "usage: java -jar PCRE.jar 'regex-pattern'"); System.exit(42); }
		 */
		
		CommonTree t = PCRE.buildRandomRegex(1, 2, true, true);
		System.out.println(t.toStringTree());
		System.out.println(PCRE.reparse(t));
	}

}
