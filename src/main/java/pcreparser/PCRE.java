package pcreparser;

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
import thomsonautomaton.ThomsonAutomaton;

import java.util.ArrayList;
import java.util.List;

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

	private void deleteUnnecessaryElements(Tree t) {
		if (t.getType() == PCRELexer.ELEMENT && t.getChildCount() == 1 ) {
				t.getParent().setChild(t.getChildIndex(), t.getChild(0));
				return; // Jó ez így? Kell-e az egy gyerekes element kölykeit vizsgálni?
		}
		for (int i = 0; i < t.getChildCount(); i++) {
			deleteUnnecessaryElements(t.getChild(i));
		}
	}
	
	private Tree getUnnecessaryElementFreeTree(Tree t) {
		if(t.getType() == PCRELexer.ELEMENT && t.getChildCount() == 1) {
			t = t.getChild(0);
		}
		deleteUnnecessaryElements(t);
		return t;
	}
	
	// BINARY FORM elott kell, mert (esetleg unáris) ALT nagyapára számít a függv.
	private void removeParentheses(Tree t) {
		if (t.getType() == PCRELexer.CAPTURING_GROUP) {
			// ELEMENT apának van QUANTIFIER fia (csak ekkor lehet 1 < fia)
			// pl:(ELEMENT (CAPTURING_GROUP (OR (ALTERNATIVE (ELEMENT b)) (ALTERNATIVE (ELEMENT c)))) (QUANTIFIER 1 2147483647 GREEDY))
			if (t.getParent().getChildCount() > 1) { 		
				t.getParent().setChild(0, t.getChild(0));
			} else { 
				// ALT <- (elemA, (ELEM <- CPT_GROUP <- OR <- (elemC, elemD,...)), elemB)'-ból 
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
	//  Törli az unáris konkatenációkat. Zárójeltörlés után kell meghívni.
	// (OR (ALTERNATIVE (ELEMENT a)) (ALTERNATIVE (ELEMENT c) (ELEMENT d)))  ->  (OR (ELEMENT a)(ALTERNATIVE (ELEMENT c) (ELEMENT d))) 
	private Tree getUnaryConcatFreeTree(Tree t) {
		// (ALTERNATIVE (ELEMENT (CAPTURING_GROUP (ALTERNATIVE (ELEMENT a))))) alakok ('(a)')  zárójeltelenítésekor
		//  ALTERNATIVE-nak lehet egy ALTERNATIVE gyereke
		while(t.getType() == PCRELexer.ALTERNATIVE && t.getChildCount() == 1 && t.getParent() == null) {
			t = t.getChild(0);
			t.setParent(null);
		}
		removeUnaryConcats(t);
		return t;
	}
	
	private void removeUnaryConcats(Tree t) {
		switch (t.getType()) {
		case PCRELexer.ALTERNATIVE:
			if (t.getChildCount() == 1 && t.getParent() != null) {
				t.getParent().setChild(t.getChildIndex(), (Tree) t.getChild(0));
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
					}
					else {
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
	
	//Zárójel, start, unaryConcatFree, binary a jó sorrend
	public Tree getAppropriateTree() {
		Tree t = getCommonTree();
		removeParentheses(t);
		t = (CommonTree) getUnaryConcatFreeTree(t);
		convertToBinaryTree(t);
		t = (CommonTree) getUnnecessaryElementFreeTree(t);
		return t;
	}
	
	
	    
	
}