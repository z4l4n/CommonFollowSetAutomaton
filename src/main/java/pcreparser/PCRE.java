package pcreparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.DOTTreeGenerator;
import org.antlr.runtime.tree.Tree;
import org.antlr.stringtemplate.StringTemplate;

import cfsautomaton.CFS;

public class PCRE {

	private final PCREParser parser;
	public static int counter;

	public PCRE(String regex) {
		try {
			PCRELexer lexer = new PCRELexer(new ANTLRStringStream(regex));
			parser = new PCREParser(new CommonTokenStream(lexer));

			ParserRuleReturnScope capture0 = parser.parse();
			parser.captureReturns.put(0, capture0);
		} catch (RecognitionException e) {
			throw new RuntimeException(e);
		}
	}

	public String toStringASCII() {
		return toStringASCII(0);
	}

	public String toStringASCII(String name) {

		StringBuilder builder = new StringBuilder();
		walk(getCommonTree(name), builder);

		return builder.toString();
	}

	public String toStringASCII(int group) {

		StringBuilder builder = new StringBuilder();
		walk(getCommonTree(group), builder);

		return builder.toString();
	}

	public CommonTree getCommonTree() {
		return getCommonTree(0);
	}

	public CommonTree getCommonTree(String name) {

		ParserRuleReturnScope retval = parser.namedReturns.get(name);

		if (retval == null) {
			throw new RuntimeException("no such named group: " + name);
		}

		return (CommonTree) retval.getTree();
	}

	public CommonTree getCommonTree(int group) {

		ParserRuleReturnScope retval = parser.captureReturns.get(group);

		if (retval == null) {
			throw new RuntimeException("no such capture group: " + group);
		}

		return (CommonTree) retval.getTree();
	}

	public String toStringDOT() {
		return toStringDOT(0);
	}

	public String toStringDOT(int group) {
		DOTTreeGenerator gen = new DOTTreeGenerator();
		StringTemplate st = gen.toDOT(getCommonTree(group));
		return st.toString();
	}

	public String toStringDOT(String name) {
		DOTTreeGenerator gen = new DOTTreeGenerator();
		StringTemplate st = gen.toDOT(getCommonTree(name));
		return st.toString();
	}

	public int getGroupCount() {
		return parser.captureReturns.size() - 1;
	}

	public String toStringLisp() {
		return toStringLisp(0);
	}

	public String toStringLisp(String name) {
		return getCommonTree(name).toStringTree();
	}

	public String toStringLisp(int group) {
		return getCommonTree(group).toStringTree();
	}

	public int getNamedGroupCount() {
		return parser.namedReturns.size();
	}

	@SuppressWarnings("unchecked")
	private void walk(CommonTree tree, StringBuilder builder) {

		List<CommonTree> firstStack = new ArrayList<CommonTree>();
		firstStack.add(tree);

		List<List<CommonTree>> childListStack = new ArrayList<List<CommonTree>>();
		childListStack.add(firstStack);

		while (!childListStack.isEmpty()) {

			List<CommonTree> childStack = childListStack.get(childListStack.size() - 1);

			if (childStack.isEmpty()) {
				childListStack.remove(childListStack.size() - 1);
			} else {
				tree = childStack.remove(0);

				String indent = "";

				for (int i = 0; i < childListStack.size() - 1; i++) {
					indent += (childListStack.get(i).size() > 0) ? "|  " : "   ";
				}

				String tokenName = PCREParser.tokenNames[tree.getType()];
				String tokenText = tree.getText();

				builder.append(indent).append(childStack.isEmpty() ? "'- " : "|- ").append(tokenName)
				.append(!tokenName.equals(tokenText) ? "='" + tree.getText() + "'" : "").append("\n");

				if (tree.getChildCount() > 0) {
					childListStack.add(new ArrayList<CommonTree>((List<CommonTree>) tree.getChildren()));
				}
			}
		}
	}


	private static boolean doesItContainEpsilon(Tree t) {
		switch (t.getType()) {
		case PCRELexer.LITERAL:
			if (t.getText().equals("ε")) {
				return true;
			}
			break;

		case PCRELexer.OR:
			for (int i = 0; i < t.getChildCount(); i++) {
				if (doesItContainEpsilon(t.getChild(0))) {
					return true;
				}
			}

		case PCRELexer.ALTERNATIVE:
			for (int i = 0; i < t.getChildCount(); i++) {
				if (!doesItContainEpsilon(t.getChild(0))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private static CommonTree getLiteralNode() {
		Random r = new Random();
		char c = (char)(r.nextInt(26) + 'a');
		CommonTree result = new CommonTree(new CommonToken(PCRELexer.LITERAL, "" + c));	
		return result;
	}



	//mayBeEpsilon: ε, a | ε, 
	public static CommonTree buildRandomRegex(int minSize, int maxSize, boolean mayBeKleene, boolean mayBeEpsilon) {
		CommonTree result = null;
		ArrayList<String> list = new ArrayList<String>();
		if (minSize == 0 && maxSize == 0) {
			return new CommonTree(new CommonToken(PCRELexer.LITERAL, "ε"));
		} else if (minSize == 0 && maxSize == 1) {

			list.add("LITERAL");
			if (mayBeEpsilon) {
				list.add("EPSILON");
				list.add("OR");
			}
			if (mayBeKleene) {
				list.add("KLEENE");
			}
			Collections.shuffle(list);

			switch(list.get(0)) {
			case "OR":
				result = new CommonTree(new CommonToken(PCRELexer.OR, "OR"));
				CommonTree leftChild = buildRandomRegex(0, 1, false, true);
				int leftChildSize = CFS.getPositionMap(leftChild).size();
				CommonTree rightChild;
				while (CFS.doesEmptyStringMatch(leftChild) && leftChildSize == 1) { // a* , a|eps nem lehet egyik oldalon sem 
					leftChild = buildRandomRegex(0, 1, false, true);
					leftChildSize = CFS.getPositionMap(leftChild).size();
				}
				if (leftChildSize == 0) {
					rightChild = buildRandomRegex(1, 1, false, false);
				} else {
					rightChild = buildRandomRegex(0, 0, false, true);
				}
				result.addChild(leftChild);
				result.addChild(rightChild);
				break;

			case "LITERAL":
				result = getLiteralNode();
				break;

			case "EPSILON":
				result = new CommonTree(new CommonToken(PCRELexer.LITERAL, "ε"));
				break;

			case "KLEENE":
				result = new CommonTree(new CommonToken(PCRELexer.ELEMENT, "ELEMENT"));
				CommonTree quantif = new CommonTree(new CommonToken(PCRELexer.QUANTIFIER, "QUANTIFIER"));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, "0")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, Integer.MAX_VALUE + "")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.GREEDY, "GREEDY")));
				CommonTree child = buildRandomRegex(1, 1, false, false);
				result.addChild(child);
				result.addChild(quantif);
			}

		} else if (minSize == 0 && maxSize > 1) {
			list.add("LITERAL");
			list.add("OR");
			list.add("AND");
			if (mayBeEpsilon) {
				list.add("EPSILON");
			}
			if (mayBeKleene) {
				list.add("KLEENE");
			}
			Collections.shuffle(list);

			switch(list.get(0)) {
			case "OR": {
				result = new CommonTree(new CommonToken(PCRELexer.OR, "OR"));
				CommonTree leftChild;
				CommonTree rightChild;
				if (mayBeEpsilon) {
					leftChild = buildRandomRegex(0, maxSize, true, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
					while (CFS.doesEmptyStringMatch(leftChild) && leftChildSize == maxSize) { // expr* , expr|eps nem lehet egyik oldalon sem, ha |expr| == maxSize 
						leftChild = buildRandomRegex(0, maxSize, true, true);
						leftChildSize = CFS.getPositionMap(leftChild).size();
					}
					if (CFS.doesEmptyStringMatch(leftChild)) {
						if (doesItContainEpsilon(leftChild)) {
							rightChild = buildRandomRegex(0, maxSize - leftChildSize, false, false);
						} else {
							rightChild = buildRandomRegex(0, maxSize - leftChildSize, true, false);
						}

					} else {
						rightChild = buildRandomRegex(0, maxSize - leftChildSize, true, true);
					}

				} else {
					if (mayBeKleene) {
						leftChild = buildRandomRegex(1, maxSize-1, true, false);
						int leftChildSize = CFS.getPositionMap(leftChild).size();
						rightChild = buildRandomRegex(1, maxSize - leftChildSize, true, false);
					} else {
						leftChild = buildRandomRegex(1, maxSize-1, false, false);
						int leftChildSize = CFS.getPositionMap(leftChild).size();
						rightChild = buildRandomRegex(1, maxSize - leftChildSize, false, false);
					}
				}
				result.addChild(leftChild);
				result.addChild(rightChild);
				break;
			}

			case "AND": { 
				CommonTree leftChild;
				CommonTree rightChild;
				result = new CommonTree(new CommonToken(PCRELexer.ALTERNATIVE, "ALTERNATIVE"));
				if (mayBeEpsilon || mayBeKleene) {
					leftChild = buildRandomRegex(1, maxSize-1, true, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
					rightChild = buildRandomRegex(1, maxSize-leftChildSize, true, true);
				} else {
					leftChild = buildRandomRegex(1, maxSize-1, true, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
					if (CFS.doesEmptyStringMatch(leftChild)) {
						rightChild = buildRandomRegex(1, maxSize-leftChildSize, false, false);
					} else {
						rightChild = buildRandomRegex(1, maxSize-leftChildSize, true, true);
					}
				}
				result.addChild(leftChild);
				result.addChild(rightChild);
				break;
			} 

			case "LITERAL": 
				result = getLiteralNode();
				break;

			case "EPSILON":
				result = new CommonTree(new CommonToken(PCRELexer.LITERAL, "ε"));
				break;

			case "KLEENE":
				result = new CommonTree(new CommonToken(PCRELexer.ELEMENT, "ELEMENT"));
				CommonTree quantif = new CommonTree(new CommonToken(PCRELexer.QUANTIFIER, "QUANTIFIER"));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, "0")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, Integer.MAX_VALUE + "")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.GREEDY, "GREEDY")));
				CommonTree child = buildRandomRegex(1, maxSize, false, false); //kleene false, mert ((a|ε)(b|ε))* == (a|b)*, és AND-nél a kleene falsenál nem lehet az előbbi
				result.addChild(child);
				result.addChild(quantif);
			}
		} else if (minSize == 1 && maxSize == 1) {
			list.add("LITERAL");
			if (mayBeKleene) {
				list.add("KLEENE");
			}
			if (mayBeEpsilon) {
				list.add("OR");
			}
			Collections.shuffle(list);

			switch(list.get(0)) {
			case "OR":
				result = new CommonTree(new CommonToken(PCRELexer.OR, "OR"));
				CommonTree leftChild = buildRandomRegex(0, 1, false, true);
				int leftChildSize = CFS.getPositionMap(leftChild).size();
				CommonTree rightChild;
				while (CFS.doesEmptyStringMatch(leftChild) && leftChildSize == 1) { // a* , a|ε nem lehet egyik oldalon sem 
					leftChild = buildRandomRegex(0, 1, false, true);
					leftChildSize = CFS.getPositionMap(leftChild).size();
				}
				if (leftChildSize == 0) {
					rightChild = buildRandomRegex(1, 1, false, false);
				} else {
					rightChild = buildRandomRegex(0, 0, false, true);
				}
				result.addChild(leftChild);
				result.addChild(rightChild);
				break;

			case "KLEENE":
				result = new CommonTree(new CommonToken(PCRELexer.ELEMENT, "ELEMENT"));
				CommonTree quantif = new CommonTree(new CommonToken(PCRELexer.QUANTIFIER, "QUANTIFIER"));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, "0")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, Integer.MAX_VALUE + "")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.GREEDY, "GREEDY")));
				CommonTree child = buildRandomRegex(1, 1, false, false);
				result.addChild(child);
				result.addChild(quantif);
				break;

			case "LITERAL":
				result = getLiteralNode();
				break;
			}
		} else if (minSize == 1 && maxSize > 1) {
			list.add("LITERAL");
			list.add("OR");
			list.add("AND");
			if (mayBeKleene) {
				list.add("KLEENE");
			}
			Collections.shuffle(list);
			switch(list.get(0)) {
			case "LITERAL":
				result = getLiteralNode();
				break;

			case "OR": {
				result = new CommonTree(new CommonToken(PCRELexer.OR, "OR"));
				CommonTree leftChild;
				CommonTree rightChild;
				if (mayBeEpsilon) {
					leftChild = buildRandomRegex(0, maxSize, true, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
					while (CFS.doesEmptyStringMatch(leftChild) && leftChildSize == maxSize) { // expr* , expr|eps nem lehet egyik oldalon sem, ha |expr| == maxSize 
						leftChild = buildRandomRegex(0, maxSize, true, true);
						leftChildSize = CFS.getPositionMap(leftChild).size();
					}
					if (CFS.doesEmptyStringMatch(leftChild)) {
						if (doesItContainEpsilon(leftChild)) {
							rightChild = buildRandomRegex(1, maxSize - leftChildSize, false, false);
						} else {
							rightChild = buildRandomRegex(1, maxSize - leftChildSize, true, false);
						}

					} else {
						rightChild = buildRandomRegex(0, maxSize - leftChildSize, true, true);
					}

				} else {
					if (mayBeKleene) {
						leftChild = buildRandomRegex(1, maxSize-1, true, false);
						int leftChildSize = CFS.getPositionMap(leftChild).size();
						rightChild = buildRandomRegex(1, maxSize - leftChildSize, true, false);
					} else {
						leftChild = buildRandomRegex(1, maxSize-1, false, false);
						int leftChildSize = CFS.getPositionMap(leftChild).size();
						rightChild = buildRandomRegex(1, maxSize - leftChildSize, false, false);
					}
				}
				result.addChild(leftChild);
				result.addChild(rightChild);
				break;
			}

			case "AND": { 
				CommonTree leftChild;
				CommonTree rightChild;
				result = new CommonTree(new CommonToken(PCRELexer.ALTERNATIVE, "ALTERNATIVE"));
				if (mayBeEpsilon || mayBeKleene) {
					leftChild = buildRandomRegex(1, maxSize-1, true, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
					rightChild = buildRandomRegex(1, maxSize-leftChildSize, true, true);
				} else {
					leftChild = buildRandomRegex(1, maxSize-1, true, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
					if (CFS.doesEmptyStringMatch(leftChild)) {
						rightChild = buildRandomRegex(1, maxSize-leftChildSize, false, false);
					} else {
						rightChild = buildRandomRegex(1, maxSize-leftChildSize, true, true);
					}
				}
				result.addChild(leftChild);
				result.addChild(rightChild);
				break;
			} 

			case "KLEENE":
				result = new CommonTree(new CommonToken(PCRELexer.ELEMENT, "ELEMENT"));
				CommonTree quantif = new CommonTree(new CommonToken(PCRELexer.QUANTIFIER, "QUANTIFIER"));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, "0")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, Integer.MAX_VALUE + "")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.GREEDY, "GREEDY")));
				CommonTree child = buildRandomRegex(1, maxSize, false, false);
				result.addChild(child);
				result.addChild(quantif);
				break;
			}
		} else if (minSize > 1 && maxSize > 1) {
			list.add("OR");
			list.add("AND");
			if (mayBeKleene) {
				list.add("KLEENE");
			}
			Collections.shuffle(list);
			switch(list.get(0)) {
			case "KLEENE":
				result = new CommonTree(new CommonToken(PCRELexer.ELEMENT, "ELEMENT"));
				CommonTree quantif = new CommonTree(new CommonToken(PCRELexer.QUANTIFIER, "QUANTIFIER"));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, "0")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, Integer.MAX_VALUE + "")));
				quantif.addChild(new CommonTree(new CommonToken(PCRELexer.GREEDY, "GREEDY")));
				CommonTree child = buildRandomRegex(minSize, maxSize, false, false);
				result.addChild(child);
				result.addChild(quantif);
				break;

			case "OR": {
				result = new CommonTree(new CommonToken(PCRELexer.OR, "OR"));
				CommonTree leftChild;
				CommonTree rightChild;
				if (mayBeEpsilon) {
					leftChild = buildRandomRegex(0, maxSize, true, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
					while (CFS.doesEmptyStringMatch(leftChild) && leftChildSize == maxSize) { // expr* , expr|eps nem lehet egyik oldalon sem, ha |expr| == maxSize 
						leftChild = buildRandomRegex(0, maxSize, true, true);
						leftChildSize = CFS.getPositionMap(leftChild).size();
					}
					if (CFS.doesEmptyStringMatch(leftChild)) {
						if (doesItContainEpsilon(leftChild)) {
							rightChild = buildRandomRegex((leftChildSize < minSize ? minSize - leftChildSize : 0), maxSize - leftChildSize, false, false);
						} else {
							rightChild = buildRandomRegex((leftChildSize < minSize ? minSize - leftChildSize : 0), maxSize - leftChildSize, true, false);
						}

					} else {
						rightChild = buildRandomRegex((leftChildSize < minSize ? minSize - leftChildSize : 0), maxSize - leftChildSize, true, true);
					}

				} else {
					if (mayBeKleene) {
						leftChild = buildRandomRegex(1, maxSize-1, true, false);
						int leftChildSize = CFS.getPositionMap(leftChild).size();
						rightChild = buildRandomRegex((leftChildSize < minSize ? minSize - leftChildSize : 0), maxSize - leftChildSize, true, false);
					} else {
						leftChild = buildRandomRegex(1, maxSize-1, false, false);
						int leftChildSize = CFS.getPositionMap(leftChild).size();
						rightChild = buildRandomRegex((leftChildSize < minSize ? minSize - leftChildSize : 0), maxSize - leftChildSize, false, false);
					}
				}
				result.addChild(leftChild);
				result.addChild(rightChild);
				break;
			}

			case "AND": { 
				CommonTree leftChild;
				CommonTree rightChild;
				result = new CommonTree(new CommonToken(PCRELexer.ALTERNATIVE, "ALTERNATIVE"));
				if (mayBeEpsilon || mayBeKleene) {
					leftChild = buildRandomRegex(1, maxSize-1, true, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
					rightChild = buildRandomRegex((leftChildSize < minSize ? minSize - leftChildSize : 0), maxSize-leftChildSize, true, true);
				} else {
					leftChild = buildRandomRegex(1, maxSize-1, true, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
					if (CFS.doesEmptyStringMatch(leftChild)) {
						rightChild = buildRandomRegex((leftChildSize < minSize ? minSize - leftChildSize : 0), maxSize-leftChildSize, false, false);
					} else {
						rightChild = buildRandomRegex((leftChildSize < minSize ? minSize - leftChildSize : 0), maxSize-leftChildSize, true, true);
					}
				}
				result.addChild(leftChild);
				result.addChild(rightChild);
				break;
			} 
			}
		}
		return result;
	}
	
	/*private CommonTree buildRandomRegex(int fatherType, int minSize, int maxSize, boolean mayEmptyStringMatch) {
		CommonTree result;
		if (fatherType == 0) {// no father, ekkor minSize == maxSize
			if (maxSize == 0) {
				return new CommonTree(new CommonToken(PCRELexer.LITERAL, "ε"));
			}
			if (maxSize == 1) {
					double i = Math.random();

					if (i < 0.33333) { // ALTERNATION
						result = new CommonTree(new CommonToken(PCRELexer.OR, "OR"));
						CommonTree leftChild = buildRandomRegex(PCRELexer.OR, 0, 1, true);
						int leftChildSize = CFS.getPositionMap(leftChild).size();
						CommonTree rightChild;
						while (CFS.doesEmptyStringMatch(leftChild) && leftChildSize == 1) { // a* nem lehet egyik oldalon sem 
							leftChild = buildRandomRegex(PCRELexer.OR, 0, 1, true);
							leftChildSize = CFS.getPositionMap(leftChild).size();
						}
						if (leftChildSize == 0) {
							rightChild = buildRandomRegex(PCRELexer.OR, 1, 1, false);
						} else {
							rightChild = buildRandomRegex(PCRELexer.OR, 0, 0, true);
						}
						result.addChild(leftChild);
						result.addChild(rightChild);

					} else if (i < 0.66666) { // KLEENE
						result = new CommonTree(new CommonToken(PCRELexer.ELEMENT, "ELEMENT"));
						CommonTree quantif = new CommonTree(new CommonToken(PCRELexer.QUANTIFIER, "QUANTIFIER"));
						quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, "0")));
						quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, Integer.MAX_VALUE + "")));
						quantif.addChild(new CommonTree(new CommonToken(PCRELexer.GREEDY, "GREEDY")));
						CommonTree child = buildRandomRegex(PCRELexer.ELEMENT, 1, maxSize, false);
						result.addChild(child);
						result.addChild(quantif);

					} else { // LITERAL
						result = getLiteralNode(true);	
					}
				}
				if (maxSize > 1) {
					double i = Math.random();

					if (i < 0.33333) { // ALTERNATION
						result = new CommonTree(new CommonToken(PCRELexer.OR, "OR"));
						CommonTree leftChild = buildRandomRegex(PCRELexer.OR, 0, maxSize, true);
						int leftChildSize = CFS.getPositionMap(leftChild).size();
						CommonTree rightChild;
						while (CFS.doesEmptyStringMatch(leftChild) && leftChildSize == maxSize) { // expr*, expr|ε, ahol |expr| == maxSize nem lehet egyik oldalon sem 
							leftChild = buildRandomRegex(PCRELexer.OR, 0, maxSize, true);
							leftChildSize = CFS.getPositionMap(leftChild).size();
						}
						if (!CFS.doesEmptyStringMatch(leftChild)) {
							rightChild = buildRandomRegex(PCRELexer.OR, maxSize - leftChildSize, maxSize - leftChildSize, true);
						} else {
							rightChild = buildRandomRegex(PCRELexer.OR, maxSize - leftChildSize, maxSize - leftChildSize, false);
						}
						result.addChild(leftChild);
						result.addChild(rightChild);

					} else if (i < 0.66666) { // CONCATENATION
						result = new CommonTree(new CommonToken(PCRELexer.ALTERNATIVE, "ALTERNATIVE"));
						CommonTree leftChild = buildRandomRegex(PCRELexer.ALTERNATIVE, 1, maxSize, true);
						int leftChildSize = CFS.getPositionMap(leftChild).size();
						CommonTree rightChild = buildRandomRegex(PCRELexer.ALTERNATIVE, maxSize-leftChildSize, maxSize-leftChildSize, true);
					} else { // KLEENE
						result = new CommonTree(new CommonToken(PCRELexer.ELEMENT, "ELEMENT"));
						CommonTree quantif = new CommonTree(new CommonToken(PCRELexer.QUANTIFIER, "QUANTIFIER"));
						quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, "0")));
						quantif.addChild(new CommonTree(new CommonToken(PCRELexer.NUMBER, Integer.MAX_VALUE + "")));
						quantif.addChild(new CommonTree(new CommonToken(PCRELexer.GREEDY, "GREEDY")));
						CommonTree child = buildRandomRegex(PCRELexer.ELEMENT, maxSize, maxSize, false);
						result.addChild(child);
						result.addChild(quantif);
					}
				}


			}
		 else { // father != 0
			if (minSize == 0 && maxSize == 0) {
				return new CommonTree(new CommonToken(PCRELexer.LITERAL, "ε"));
			} else if (minSize == 0 && maxSize == 1) {
				double i = Math.random();

				if (i < 0.25) { // ALTERNATION
					result = new CommonTree(new CommonToken(PCRELexer.OR, "OR"));
					CommonTree leftChild = buildRandomRegex(PCRELexer.OR, 0, maxSize, true);
					int leftChildSize = CFS.getPositionMap(leftChild).size();
				}

			}



		}



	}
	 */
	/*public CommonTree getRandomRegexTree(int size) {
		if (size == 0) {
			return new CommonTree(new CommonToken(PCRELexer.LITERAL, "ε"));
		}
		Tree t = new CommonTree(new CommonToken(PCRELexer.LITERAL, "ε"));
		((CommonToken) t).setText("asd");
		return buildRandomRegex(size, size);
	}*/

	private void deleteUnnecessaryElements(Tree t) {
		if (t.getType() == PCRELexer.ELEMENT && t.getChildCount() == 1) {
			t.getParent().setChild(t.getChildIndex(), t.getChild(0));
			return; // Jó ez így? Kell-e az egy gyerekes element kölykeit
			// vizsgálni?
		}
		for (int i = 0; i < t.getChildCount(); i++) {
			deleteUnnecessaryElements(t.getChild(i));
		}
	}

	private Tree getUnnecessaryElementFreeTree(Tree t) {
		if (t.getType() == PCRELexer.ELEMENT && t.getChildCount() == 1) {
			t = t.getChild(0);
		}
		deleteUnnecessaryElements(t);
		return t;
	}

	// BINARY FORM elott kell, mert (esetleg unáris) ALT nagyapára számít a
	// függv.
	private void removeParentheses(Tree t) {
		if (t.getType() == PCRELexer.CAPTURING_GROUP) {
			// ELEMENT apának van QUANTIFIER fia (csak ekkor lehet 1 < fia)
			// pl:(ELEMENT (CAPTURING_GROUP (OR (ALTERNATIVE (ELEMENT b))
			// (ALTERNATIVE (ELEMENT c)))) (QUANTIFIER 1 2147483647 GREEDY))
			if (t.getParent().getChildCount() > 1) {
				t.getParent().setChild(0, t.getChild(0));
			} else {
				// ALT <- (elemA, (ELEM <- CPT_GROUP <- OR <- (elemC,
				// elemD,...)), elemB)'-ból
				// ALT <- (elemA, OR(elemC, elemD), elemB)' lesz pl.
				Tree grandFather = t.getParent().getParent();
				Tree father = t.getParent();
				int fatherIndex = father.getChildIndex();
				grandFather.setChild(fatherIndex, t.getChild(0));
				//
				t = t.getChild(0);
			}
		}
		for (int i = 0; i < t.getChildCount(); i++) {
			removeParentheses(t.getChild(i));
		}
	}

	// Törli az unáris konkatenációkat. Zárójeltörlés után kell meghívni.
	// (OR (ALTERNATIVE (ELEMENT a)) (ALTERNATIVE (ELEMENT c) (ELEMENT d))) ->
	// (OR (ELEMENT a)(ALTERNATIVE (ELEMENT c) (ELEMENT d)))
	private Tree getUnaryConcatFreeTree(Tree t) {
		// (ALTERNATIVE (ELEMENT (CAPTURING_GROUP (ALTERNATIVE (ELEMENT a)))))
		// alakok ('(a)') zárójeltelenítésekor
		// ALTERNATIVE-nak lehet egy ALTERNATIVE gyereke
		while (t.getType() == PCRELexer.ALTERNATIVE && t.getChildCount() == 1 && t.getParent() == null) {
			t = t.getChild(0);
			t.setParent(null);
		}
		removeUnaryConcats(t);
		return t;
	}

	private  void removeUnaryConcats(Tree t) {
		switch (t.getType()) {
		case PCRELexer.ALTERNATIVE:
			if (t.getChildCount() == 1 && t.getParent() != null) {
				t.getParent().setChild(t.getChildIndex(), t.getChild(0));
				t = t.getChild(0);
				removeUnaryConcats(t);
				return;
			}
		}
		for (int i = 0; i < t.getChildCount(); i++) {
			removeUnaryConcats(t.getChild(i));
		}
	}

	// binárissá konvertálja az or és concat részfákat
	private void convertToBinaryTree(Tree t) {
		switch (t.getType()) {

		case PCRELexer.ALTERNATIVE:
			if (t.getChildCount() > 2) {
				List<Tree> children = new ArrayList<Tree>();
				Tree act;
				CommonTree child;
				boolean left;
				while (t.getChildCount() > 1) {
					children.add(t.getChild(1));
					t.deleteChild(1);
				}
				act = t;
				left = false;
				while (!children.isEmpty()) {
					if (!left) {
						if (children.size() > 1) {
							child = new CommonTree(new CommonToken(PCRELexer.ALTERNATIVE, "ALTERNATIVE"));
							act.addChild(child);
							act = child;
						} else {
							act.addChild(children.remove(0));
						}
						left = true;
						continue;
					}
					if (left) {
						act.addChild(children.remove(0));
						left = false;
					}
				}
			}
			break;

		case PCRELexer.OR:
			if (t.getChildCount() > 2) {
				List<Tree> children = new ArrayList<Tree>();
				Tree act;
				CommonTree child;
				boolean left;

				while (t.getChildCount() > 1) {
					children.add(t.getChild(1));
					t.deleteChild(1);
				}

				act = t;
				left = false;
				while (!children.isEmpty()) {
					if (!left) {
						if (children.size() > 1) {
							child = new CommonTree(new CommonToken(PCRELexer.OR, "OR"));
							act.addChild(child);
							act = child;
						} else {
							act.addChild(children.remove(0));
						}
						left = true;
					} else {
						act.addChild(children.remove(0));
						left = false;
					}
				}
			}
			break;
		}
		for (int i = 0; i < t.getChildCount(); i++) {
			convertToBinaryTree(t.getChild(i));
		}
	}

	// Zárójel, start, unaryConcatFree, binary a jó sorrend
	public  Tree getAppropriateTree() {
		
		Tree t = getCommonTree();
		removeParentheses(t);
		t = getUnaryConcatFreeTree(t);
		convertToBinaryTree(t);
		t = getUnnecessaryElementFreeTree(t);
		return t;
	}
	
	public static String reparse(Tree regexTree) {
		return "Hi!";
	}

}