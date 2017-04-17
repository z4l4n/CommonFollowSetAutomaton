package pcreparser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.BaseTree;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.DOTTreeGenerator;
import org.antlr.runtime.tree.Tree;
import org.antlr.stringtemplate.StringTemplate;

import antlr.Token;
import automaton.CFS;
import automaton.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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




	// parenthesisRemove után lehet csak, mert nem kezeli a zárójeleket
	public static boolean canBeEmptyString(Tree t) {
		switch (t.getType()) {
		case PCRELexer.OR:
			for (int i = 0; i < t.getChildCount(); i++) {
				if (canBeEmptyString(t.getChild(i))) {
					return true;
				}
			}
			return false;

		case PCRELexer.ALTERNATIVE:
			for (int i = 0; i < t.getChildCount(); i++) {
				if (!canBeEmptyString(t.getChild(i))) {
					return false;
				}
			}
			return true;


			/*case PCRELexer.CAPTURING_GROUP:
			return canBeEmptyString(t.getChild(0));*/

		case PCRELexer.ELEMENT: // JÓ EZ ÍGY?
			if (t.getChildCount() > 1) {
				if (t.getChild(1).getChild(0).toString().equals("0")) {
					return true;
				} else {
					return canBeEmptyString(t.getChild(0));
				}
			} 
		}
		return false;
	}

	public static void calculateLast(Tree t, HashMap<Tree, Integer> positionMap, HashMap<Tree, Integer> lastMap) {
		if (t == null) {
			return;
		}
		Tree tempTree = null; 
		switch (t.getType()) {
		case PCRELexer.OR:
			for (int i = 0; i < t.getChildCount(); i++) {
				calculateLast(t.getChild(i), positionMap, lastMap);
			}
			break;

		case PCRELexer.ALTERNATIVE:
			if (t.getChildCount() > 0) {
				if (t.getChildCount() > 1 && canBeEmptyString(t.getChild(1))) { //legalabb 2 hosszu alternative, es az utolso lehet ures - > utolso elotti is jatszik
					//tempTree = (Tree) t.deleteChild(t.getChildCount() - 1);
					calculateLast(t.getChild(0), positionMap, lastMap);
					calculateLast(t.getChild(1), positionMap, lastMap);
					t.addChild(tempTree);
				}/* else if (t.getChildCount() > 1 && t.getChild(t.getChildCount() - 1).getChild(0).getType() == PCRELexer.EndOfSubjectOrLine) {
					tempTree = (Tree) t.deleteChild(t.getChildCount() - 1);
//					calculateLast(t, positionMap, lastMap);
					t.addChild(tempTree);

				}*/else {
					calculateLast(t.getChild(1), positionMap, lastMap);
				}
			}
			break;
		case PCRELexer.ELEMENT:
		case PCRELexer.CAPTURING_GROUP:
			calculateLast(t.getChild(0), positionMap, lastMap);
			break;
		case PCRELexer.WordBoundary:
		case PCRELexer.CHARACTER_CLASS:
		case PCRELexer.NEGATED_CHARACTER_CLASS:
		case PCRELexer.WhiteSpace:
		case PCRELexer.LITERAL:
		case PCRELexer.ANY:
		case PCRELexer.NotWhiteSpace:
		case PCRELexer.DecimalDigit:
		case PCRELexer.NotDecimalDigit:
		case PCRELexer.WordChar:
		case PCRELexer.NotWordChar:
			lastMap.put(t, positionMap.get(t));
			break;
		}
	}

	public static HashMap<Tree, Integer> getLastMap(Tree t) {
		HashMap<Tree, Integer> lastMap = new HashMap<Tree, Integer>();
		HashMap<Tree, Integer> positionMap = getPositionMap(t);
		calculateLast(t, positionMap, lastMap);
		return lastMap;
	}

	public static void calculateFirst(Tree t, HashMap<Tree, Integer> positionMap, HashMap<Tree, Integer> firstMap) {
		if (t == null) {
			return;
		}
		switch (t.getType()) {
		case PCRELexer.OR:
			for (int i = 0; i < t.getChildCount(); i++) {
				calculateFirst(t.getChild(i), positionMap, firstMap);
			}
			break;

		case PCRELexer.ALTERNATIVE:
		//	Tree tempTree = null; 
			if (t.getChildCount() > 0) {
				if (t.getChildCount() > 1 && canBeEmptyString(t.getChild(0))) {
					//tempTree = (Tree) t.deleteChild(0);
					calculateFirst(t.getChild(0), positionMap, firstMap);
					calculateFirst(t.getChild(1), positionMap, firstMap); 
					//((BaseTree) t).insertChild(0, tempTree); 
				} /*else  if (t.getChildCount() > 1 && t.getChild(0).getChild(0).getType() == PCRELexer.START_OF_SUBJECT){
					tempTree = (Tree) t.deleteChild(0);
					calculateFirst(t, positionMap, firstMap);
					((BaseTree) t).insertChild(0, tempTree);
				}*/else {
					calculateFirst(t.getChild(0), positionMap, firstMap);
				}
			}
			break;

		case PCRELexer.ELEMENT:
		case PCRELexer.CAPTURING_GROUP:
			calculateFirst(t.getChild(0), positionMap, firstMap);
			break;
		case PCRELexer.WordBoundary:
		case PCRELexer.NonWordBoundary:
		case PCRELexer.CHARACTER_CLASS:
		case PCRELexer.NEGATED_CHARACTER_CLASS:
		case PCRELexer.WhiteSpace:
		case PCRELexer.LITERAL:
		case PCRELexer.ANY:
		case PCRELexer.NotWhiteSpace:
		case PCRELexer.DecimalDigit:
		case PCRELexer.NotDecimalDigit:
		case PCRELexer.WordChar:
		case PCRELexer.NotWordChar:
			firstMap.put(t, positionMap.get(t));
			break;
		}
	}

	public static HashMap<Tree, Integer> getFirstMap(Tree t) {
		HashMap<Tree, Integer> firstMap = new HashMap<Tree, Integer>();
		if (t == null) {
			return firstMap;
		}
		HashMap<Tree, Integer> positionMap = getPositionMap(t);
		calculateFirst(t, positionMap, firstMap);
		return firstMap;
	}
	public static HashMap<Tree, Integer> getPositionMap(Tree t) {
		if (t == null) {
			System.out.println("null input tree");
			return null;
		}
		HashMap<Tree, Integer> positionMap = new HashMap<Tree, Integer>();
		counter = 0;
		calculatePositions(t, positionMap);
		return positionMap;
	}
	public static void calculatePositions(Tree t, HashMap<Tree, Integer> positionMap) {
		switch (t.getType()) {
		case PCRELexer.LITERAL:
			// character class-en belüli literal miatt
			if (t.getParent().getType() == PCRELexer.CHARACTER_CLASS || t.getParent().getType() == PCRELexer.NEGATED_CHARACTER_CLASS) {
				break;
			}
		case PCRELexer.CHARACTER_CLASS:
		case PCRELexer.NEGATED_CHARACTER_CLASS:
		case PCRELexer.WhiteSpace:
		case PCRELexer.ANY:
		case PCRELexer.NotWhiteSpace:
		case PCRELexer.DecimalDigit:
		case PCRELexer.NotDecimalDigit:
		case PCRELexer.WordChar:
		case PCRELexer.NotWordChar:
		case PCRELexer.NonWordBoundary:
		case PCRELexer.WordBoundary:
			positionMap.put(t, counter++);
		}

		Tree s;
		for (int i = 0; i < t.getChildCount(); i++) {
			s = t.getChild(i);
			if (s != null) {
				calculatePositions(s, positionMap);
			}
		}
	}

	public static void calculateFollow(Tree regex, HashMap<Tree, Integer> rootRegexPositions, HashMap<Tree, Integer> followMap, Integer position, Tree letter) {
		switch(regex.getType()) {
		case PCRELexer.OR:
			for (int i = 0; i < regex.getChildCount(); i++) {
				HashMap<Tree, Integer> childPositionMap = getPositionMap(regex.getChild(i));
				if (childPositionMap.containsKey(letter)) {
					calculateFollow(regex.getChild(i), rootRegexPositions, followMap, position, letter);
				}
			}
			break;	
		case PCRELexer.ALTERNATIVE: 
			if (regex.getChildCount() > 0) { //????
				Tree firstChild = regex.getChild(0);
				Tree secondChild = regex.getChild(1);
				HashMap<Tree, Integer> firstChildPositionMap = getPositionMap(firstChild);
				HashMap<Tree, Integer> firstChildLastMap = getLastMap(firstChild);
				if (firstChildPositionMap.containsKey(letter) && !firstChildLastMap.containsKey(letter)) {
					calculateFollow(firstChild, rootRegexPositions, followMap, position, letter);
				} else if (firstChildLastMap.containsKey(letter)) {
					calculateFollow(firstChild, rootRegexPositions, followMap, position, letter);
					HashMap<Tree, Integer> secondChildFirstMap = getFirstMap(secondChild);
					
					for (Tree i : secondChildFirstMap.keySet()) {
						followMap.put(i, rootRegexPositions.get(i));
					}
				} else {	
					calculateFollow(secondChild, rootRegexPositions, followMap, position, letter);
				}
			}
			break;
		case PCRELexer.ELEMENT:
			Tree firstChild = regex.getChild(0);
			if (regex.getChildCount() > 1) { // Quantifier
					HashMap<Tree, Integer> firstChildLastMap = getLastMap(firstChild);
					if (firstChildLastMap.containsKey(letter)) {
						// Quantifier lehet 1-nél nagyobb; ha csak 1 lehetne max, akkor rosszul számolná, 
						// mert ha eleme last-nek, akkor a first-tel uniózni kéne, itt viszont ez nem jöhet szóba
						
						if (Integer.parseInt(regex.getChild(regex.getChildCount() - 1).getChild(1).getText()) > 1) {
							HashMap<Tree, Integer> firstChildFirstMap = getFirstMap(firstChild);
							for (Tree i : firstChildFirstMap.keySet()) {
								followMap.put(i, rootRegexPositions.get(i));
							}
						}
					}
					calculateFollow(firstChild, rootRegexPositions, followMap, position, letter);
				}
			//Ha nem Quantifieres- element, akkor ez nem szükséges? Follow(a) = { }
			//calculateFollow(firstChild, rootRegexPositions, followMap, position, letter);
			break;
		}
	}
	public static HashMap<Tree, Integer> getFollowMap(Tree regexTree, Tree letter) {
		Integer position = null;
		HashMap<Tree, Integer> positionMap = getPositionMap(regexTree);
		//System.out.println("position map(followon belül):" + positionMap);
		HashMap<Tree, Integer> followMap = new HashMap<Tree, Integer>();
		for (Map.Entry<Tree, Integer> e : positionMap.entrySet()) {
			if (e.getKey() == letter) {
				position = e.getValue();
			}
		}
		if (position == null) {
			System.out.println("Position does not exist!");
			return followMap;
		}
		calculateFollow(regexTree, positionMap, followMap,  position, letter);
		return followMap;
	}
	public static HashMap<Tree, Integer> getFollowMap(Tree regexTree, Integer position) {
		Tree letter = null;
		HashMap<Tree, Integer> positionMap = getPositionMap(regexTree);
		//System.out.println("position map(followon belül):" + positionMap);
		HashMap<Tree, Integer> followMap = new HashMap<Tree, Integer>();
		for (Map.Entry<Tree, Integer> e : positionMap.entrySet()) {
			if (e.getValue() == position) {
				letter = e.getKey();
			}
		}
		if (letter == null) {
			System.out.println("Position does not exist!");
			return followMap;
		}
		calculateFollow(regexTree, positionMap, followMap,  position, letter);
		return followMap;
	}
	
	// VIGYÁZAT!!
		// Visszaadhat literált önmagában, pl. ekkor: (ELEMENT a (QUANTIFIER 0 2147483647 GREEDY))
		// de (ELEMENT a)-ként is, pl. ekkor: (ALTERNATIVE (ELEMENT b) (ELEMENT a)).
		public static Tree next(Tree t) {
			if (t.getParent() != null) {
				Tree father = t.getParent();
				
				// F* apa eset
				if (father.getType() == PCRELexer.ELEMENT && father.getChildCount() > 1 && Integer.parseInt(father.getChild(father.getChildCount() - 1).getChild(1).getText()) > 1) {
					return t;
				} 
				if (father.getType() == PCRELexer.ALTERNATIVE && t.getChildIndex() == 0 && father.getChildCount() == 2) {
					return father.getChild(1);
				}
			}
			return null;
		}	
	
	// Lemma 5.1(a) szerinti follow
	// úgy tűnik, kész
	public static HashMap<Tree, Integer> followTree(Tree t, Integer position) {
		HashMap<Tree, Integer> result = new HashMap<Tree, Integer>(); 
		HashMap<Tree, Integer> positions = getPositionMap(t);
		LinkedList<Tree> s = new LinkedList<Tree>();
		HashMap<Tree, Integer> last;
		for (int i = 0; i < t.getChildCount(); i++) {
			s.add(t.getChild(i));
		}
		while (!s.isEmpty()) {
			Tree p = s.removeFirst();
			if (p.getType() == PCRELexer.QUANTIFIER) {
				continue;
			}
			for (int i = 0; i < p.getChildCount(); i++) {
				s.addLast(p.getChild(i));
			}
			last = getSubTreePositionMap(positions, getLastMap(p));
			if (last.containsValue(position)) {
				result.putAll(getPositionIntersectionMap(positions, getSubTreePositionMap(positions, getFirstMap(next(p)))));
			}
		}
		return result;
	}
	
	public static Tree treeCopier(Tree t) {
		CommonTree tElem = new CommonTree(new CommonToken(t.getType(), t.getText()));
		for (int i = 0; i < t.getChildCount(); i++) {
			tElem.addChild(treeCopier(t.getChild(i)));
		}
		return tElem;
	}
	
	/** 
	 * @param map1 the containing map 
	 * @param map2 the contained map
	 *  */
	public static boolean containsAll(HashMap<Tree, Integer> map1, HashMap<Tree, Integer>map2) {
		for (Entry<Tree, Integer> e : map2.entrySet()) {
			if (map1.get(e.getKey()) != e.getValue()) {
				return false;
			}
		}
		return true;
	}
		
	public static boolean areEquals(HashMap<Tree, Integer> map1, HashMap<Tree, Integer>map2) {
		if (map1 == map2) {
			return true;
		}
		if (map1.size() != map2.size()) {
			return false;
		}
		for (Entry<Tree, Integer> e : map1.entrySet()) {
			if (map2.containsKey(e.getKey()) && map2.get(e.getKey()) == e.getValue()) {
				;
			} else {
				return false;
			}
		}
		return true;
	}
	
	public static HashMap<Tree, Integer> getPositionIntersectionMap(HashMap<Tree,Integer> m1, HashMap<Tree, Integer>m2) {
		HashMap<Tree, Integer> result = new HashMap<Tree, Integer>();
		for (Entry<Tree, Integer> e : m1.entrySet()) {
			if (m2.containsKey(e.getKey()) && m2.containsValue(e.getValue())) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}

	// 1 méretű fára decomposition
	// ezt csinálni!
	public static HashSet<HashMap<Tree, Integer>> dec1(HashMap<Tree, Integer> rootPositions, Integer position, Tree subExpr) {
		HashMap<Tree, Integer> subPositions = getSubTreePositionMap(rootPositions, getPositionMap(subExpr));
		if (!subPositions.containsValue(position)) {
			System.out.println("There is not position" + position + "in the (sub)tree!");
			return null;
		}
			LinkedList<Tree> s = new LinkedList<Tree>();
			HashSet<HashMap<Tree, Integer>> resultSet = new HashSet<HashMap<Tree, Integer>>();
			HashMap<Tree, Integer> resultMap = new HashMap<Tree, Integer>();
			 
			 //fa bejárása (gyökértől különböző csúcsok) a* esetben bug?	
			for (int i = 0; i < subExpr.getChildCount(); i++) {
				s.add(subExpr.getChild(i));
			}
			while (!s.isEmpty()) {
				Tree p = s.removeFirst();
				HashMap<Tree, Integer> last = getSubTreePositionMap(rootPositions,getLastMap(p));
				HashMap<Tree, Integer> first = getSubTreePositionMap(rootPositions, getFirstMap(next(p)));
				//System.out.println("last: " + last);
				//System.out.println("first: " + first);
				if (last.containsValue(position) && first.containsValue(position)) {
					 for (Entry<Tree, Integer> e : last.entrySet()) {
						 if (e.getValue() == position) {
							 resultMap.put(e.getKey(), position);
						 }
					 }
				}
				for (int i = 0; i < p.getChildCount(); i++) {
					s.addLast(p.getChild(i));
				}
			}
			resultSet.add(resultMap);
			return resultSet;
	}
	
	
	// pozíciókat  a root kifejezéshez viszonyítva konvertálja 
	// pl: ős == 'abc', pozíciók: a-0, b-1, c-2
	//     leszármazott == bc, pozíciók: b-0, c-1
	// így last(bc) == c-2 lesz c-1 helyett
	//DONE
	public static HashMap<Tree, Integer> getSubTreePositionMap(HashMap<Tree, Integer> rootPositions, HashMap<Tree, Integer> subExprPositions) {
		HashMap<Tree, Integer> resultMap = new HashMap<Tree, Integer>();
		
		for (Tree i : subExprPositions.keySet()) {
			if (rootPositions.containsKey(i))
			resultMap.put(i, rootPositions.get(i));
		}
		return resultMap;
	}
	

	public static HashMap<Integer, HashSet<HashMap<Tree, Integer>>> algorithm1(HashMap<Tree, Integer> rootPositions, Tree subExpr) {
		HashMap<Integer, HashSet<HashMap<Tree, Integer>>> resultMap = new HashMap<Integer, HashSet<HashMap<Tree, Integer>>>();
		HashMap<Tree, Integer> subExprPositions = getSubTreePositionMap(rootPositions, getPositionMap(subExpr));
		int subExprSize = subExprPositions.size();
		if (subExprSize == 1) {
			//System.out.println("size:1");
			int position = -1;
			for (Entry<Tree, Integer> e : subExprPositions.entrySet()) {
				position = e.getValue();
			}
			//System.out.println("1 méretű fa: " + subExpr.toStringTree());
			resultMap.put(position, dec1(rootPositions, position, subExpr));
			//System.out.println("dec-e az 1 méretűnek: " + dec1(rootPositions, position, subExpr));
			return resultMap;
		}
		if (subExprSize > 1) {
			//System.out.println("size > 1");
			LinkedList<Tree> s = new LinkedList<Tree>();
			s.add(subExpr);
			while (!s.isEmpty()) {
				Tree t1 = s.removeFirst();
				int t1Size = getPositionMap(t1).size();
				if ((subExprSize / 3.0) <= t1Size && t1Size <= (subExprSize * 2 / 3.0)) {
					System.out.println(" ");
					System.out.println("t1: " + t1.toStringTree());
					int t1Index = t1.getChildIndex();
					Tree t1Parent = t1.getParent();
					t1Parent.deleteChild(t1Index);
					System.out.println("t2: " + subExpr.toStringTree());
					HashMap<Integer, HashSet<HashMap<Tree, Integer>>> t1Dec = algorithm1(rootPositions, t1);
					//System.out.println("t1Dec: " + t1Dec);
					HashMap<Integer, HashSet<HashMap<Tree, Integer>>> t2Dec = algorithm1(rootPositions, subExpr);
					//System.out.println("t2Dec: " + t2Dec);
					HashMap<Tree, Integer> t1Positions = getSubTreePositionMap(rootPositions, getPositionMap(t1));
					HashMap<Tree, Integer> t2Positions = getSubTreePositionMap(rootPositions, getPositionMap(subExpr));
					((BaseTree) t1Parent).insertChild(t1Index, t1);
					//System.out.println("visszarakás után: " + subExpr.toStringTree());
					int position;
					Tree letter;
					for (Entry<Tree, Integer> entry : subExprPositions.entrySet() ) {
						position = entry.getValue();
						letter = entry.getKey();
						if (t1Positions.containsValue(position)) {
							
							HashMap<Tree, Integer> t1Last = getSubTreePositionMap(rootPositions, getLastMap(t1));
							//System.out.println("t1 last:" + t1Last);
							if (!t1Last.containsValue(position)) {
								
								resultMap.put(position, t1Dec.get(position));
							} else {
								//System.out.println("C1-es matyizás..");
								HashMap<Tree, Integer> C1 = new HashMap<Tree, Integer>();
								Tree g = t1;
								
								// F == subExpr???
								while (g != subExpr) {
									HashMap<Tree, Integer> gLastMap = getSubTreePositionMap(rootPositions, getLastMap(g));
									HashMap<Tree, Integer> interSect = getPositionIntersectionMap(gLastMap, t1Positions);
								//	System.out.println("intersect" + interSect);
									if (areEquals(interSect, t1Last)) {
										/*System.out.println("equals!");
										System.out.println("G:" + g.toStringTree());
										System.out.println("g.parent " + g.getParent().toStringTree());
										System.out.println("first(next(g)) " + getFirstMap(next(g)));*/
										C1.putAll(getPositionIntersectionMap(subExprPositions, getSubTreePositionMap(rootPositions, getFirstMap(next(g)))));
									}
									g = g.getParent();
								}
								HashSet<HashMap<Tree, Integer>> temp = t1Dec.get(position);
								temp.add(C1);
								resultMap.put(position, temp);
							}
						} else {
							if (!t2Positions.containsValue(position)) {
								System.out.println("bug!");
							}
							HashMap<Tree, Integer> t1First = getSubTreePositionMap(rootPositions, getFirstMap(t1));
							System.out.println("t1first: " + t1First);
							System.out.println("follow(E," + position +"):" + getSubTreePositionMap(rootPositions, getFollowMap(subExpr, letter)));
							HashSet<HashMap<Tree, Integer>> temp = t2Dec.get(position);
							//System.out.println("t2dec: " + temp);
							if (containsAll(getSubTreePositionMap(rootPositions, getFollowMap(subExpr, letter)), t1First)) {
								
								HashMap<Tree, Integer> C2 = getPositionIntersectionMap(subExprPositions, t1First);
								//System.out.println(C2);
								temp.add(C2);
							}
							resultMap.put(position, temp);
							
							
						}
						
					}
					return resultMap;
					//break;
				}
				for (int i = 0; i < t1.getChildCount(); i++) {
					s.add(t1.getChild(i));
				}
			}
		}
		System.out.println("err");
		return null;
	}
	
	
	//					Megfelelő(bináris, zárójelmentes stb.) regex tree készítő függvények
	public static void deleteUnnecessaryElements(Tree t) {
		if (t.getType() == PCRELexer.ELEMENT && t.getChildCount() == 1 ) {
				t.getParent().setChild(t.getChildIndex(), t.getChild(0));
				return; // Jó ez így? Kell-e az egy gyerekes element kölykeit vizsgálni?
		}
		for (int i = 0; i < t.getChildCount(); i++) {
			deleteUnnecessaryElements(t.getChild(i));
		}
	}
	public static Tree getUnnecessaryElementFreeTree(Tree t) {
		if(t.getType() == PCRELexer.ELEMENT && t.getChildCount() == 1) {
			t = t.getChild(0);
		}
		deleteUnnecessaryElements(t);
		return t;
	}
	public static CommonTree generateDotStarElement() {
		CommonTree tElem = new CommonTree(new CommonToken(PCRELexer.ELEMENT, "ELEMENT"));
		CommonTree tAny = new CommonTree(new CommonToken(PCRELexer.ANY, "ANY"));
		CommonTree tQuantifier = new CommonTree(new CommonToken(PCRELexer.QUANTIFIER, "QUANTIFIER"));
		CommonTree tZero = new CommonTree(new CommonToken(PCRELexer.NUMBER, "0"));
		CommonTree tMax = new CommonTree(new CommonToken(PCRELexer.NUMBER, "2147483647"));
		CommonTree tGreedy = new CommonTree(new CommonToken(PCRELexer.GREEDY, "GREEDY"));
		tElem.addChild(tAny);
		tElem.addChild(tQuantifier);
		tQuantifier.addChild(tZero);
		tQuantifier.addChild(tMax);
		tQuantifier.addChild(tGreedy);
		return tElem;
	}

	// converToBinary törli az egy gyerekes ALTERNATIVE-okat, ezért ezt előtte kell futtatni! (mert 2 gyerekes lehet ezután, és akkor nem kell törölni az ALTERNATIVE-ot)
	public static void addStartOfSubject(Tree t) {
		switch(t.getType()) {
		case PCRELexer.OR:
			for (int i = 0; i < t.getChildCount(); i++) {
				addStartOfSubject(t.getChild(i));
			}
			break;

		case PCRELexer.ALTERNATIVE:
			if (t.getChildCount() > 0) {
				if (t.getChild(0).getChild(0).getType() != PCRELexer.START_OF_SUBJECT) {
					/*if (t.getChild(0).getChild(0).getType() == PCRELexer.CAPTURING_GROUP) {
						addStartOfSubject(t.getChild(0).getChild(0).getChild(0));
			} else*/ 
					for (int i = 0; i < t.getChildCount(); i++) { // ha az első n fiú lehet üres szó, akkor töröljük őket, és utána szúrjuk az elejére a ".*"-ot (mert pl.: ".*f*as" == ".*as")
						if (canBeEmptyString(t.getChild(i))) {
							t.deleteChild(i);
						} else {
							break;
						}
					}
					((BaseTree) t).insertChild(0, generateDotStarElement());	
				} else {
					t.deleteChild(0); // töröljük a "^" jelet
				}
			}		
			break;
		}
	}

	//DONE
	public static void addEndOfSubject(Tree t) {
		switch (t.getType()) {
		case PCRELexer.OR:
			for (int i = 0; i < t.getChildCount(); i++) {
				addEndOfSubject(t.getChild(i));
			}
			break;

		case PCRELexer.ALTERNATIVE:
			if (t.getChildCount() > 0) {
				if (t.getChild(t.getChildCount() - 1).getChild(0).getType() != PCRELexer.EndOfSubjectOrLine) {//Ha "."-al végződik, kicseréljük .*-al, ha nem, mögé szúrjuk

					for (int i = t.getChildCount() - 1; i >= 0; i--) {
						if (canBeEmptyString(t.getChild(i))) {
							t.deleteChild(i);
						} else {
							break;
						}
					}

					t.addChild(generateDotStarElement());

				} else {
					t.deleteChild(t.getChildCount() - 1);
				}
			}		
			break;
		}
	}

	//DONE
	public static void addStartAndEndOfSubject(Tree t) {
		switch (t.getType()) {
		case PCRELexer.OR:
			for (int i = 0; i < t.getChildCount(); i++) {
				addStartAndEndOfSubject(t.getChild(i));
			}
			break;

		case PCRELexer.ALTERNATIVE:
			addStartOfSubject(t);
			addEndOfSubject(t);
			break;
		}
	}
	
	// BINARY FORM elott kell, mert (esetleg unáris) ALT nagyapára számít a függv.
	public static void removeParenthesis(Tree t) {
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
			removeParenthesis(t.getChild(i));
		}
	}
	//  Törli az unáris konkatenációkat. Zárójeltörlés után kell meghívni.
	//  pl: (OR (ALTERNATIVE (ELEMENT a)) (ALTERNATIVE (ELEMENT c) (ELEMENT d))) -ből
	//  (OR (ELEMENT a)(ALTERNATIVE (ELEMENT c) (ELEMENT d))) lesz
	public static Tree getUnaryConcatFreeTree(Tree t) {
		// (ALTERNATIVE (ELEMENT (CAPTURING_GROUP (ALTERNATIVE (ELEMENT a))))) alakok ('(a)')  zárójeltelenítésekor
		//  ALTERNATIVE-nak lehet egy ALTERNATIVE gyereke
		while(t.getType() == PCRELexer.ALTERNATIVE && t.getChildCount() == 1 && t.getParent() == null) {
			t = t.getChild(0);
			t.setParent(null);
		}
		removeUnaryConcats(t);
		return t;
	}
	public static void removeUnaryConcats(Tree t) {
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

	// binárissá convertálja az or és concat részfákat
	public static void convertToBinaryTree(Tree t) { 
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
	public static Tree getAppropriateTree(CommonTree t) {
		removeParenthesis(t);
		t = (CommonTree) getUnaryConcatFreeTree(t);
		convertToBinaryTree(t);
		t = (CommonTree) getUnnecessaryElementFreeTree(t);
		return t;
	}
	
	//				Megfelelő(bináris, zárójelmentes stb.) regex tree készítő függvények VÉGE
	
	
	
	public static void main(String[] args) {

		/*
		 * if(args.length != 1) { System.err.println(
		 * "usage: java -jar PCRE.jar 'regex-pattern'"); System.exit(42); }
		 */

		PCRE pcre2 = new PCRE("b(c|d)*");
		Tree t = getAppropriateTree(pcre2.getCommonTree());
		
		System.out.println("alga 1: " + algorithm1(getPositionMap(t), t));
		CFS c = new CFS(t);
		System.out.println("hehooo");
		System.out.println(c.matches("bdc") ? "matches!" : "doesnt match!");
		System.out.println(t.toStringTree());
		/*System.out.println("Kezdő: " + c.getInitialState());
		for (State s : c.getStates()) {
			System.out.println("State: " + s + "kiélek: " + s.getEdges());
		}
		
		//CommonTree t;
		//System.out.println(pcre2.getCommonTree().getChild(1).getChild(0).getType());
		//Tree t = getAppropriateTree(pcre2.getCommonTree());
		//System.out.println(t);
		//PCRELexer.*/
	}
}
//                           (ELEMENT c (QUANTIFIER 0 2147483647 GREEDY))
// típusok:                   QUANTIFIER- QUANTIFIER,  0 - NUMBER, 2147483647 - NUMBER, GREEDY-GREEDY
//

//Tested: removeUnaryConats, getUnaryConcatFreeTree, first, last, follow, convertToBinaryTree, CanBeEmpty, positions