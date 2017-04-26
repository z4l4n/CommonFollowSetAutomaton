package cfsautomaton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.runtime.tree.BaseTree;
import org.antlr.runtime.tree.Tree;
import pcreparser.PCRELexer;

// Common Follow Set Automaton
public class CFS {

	//private HashMap<>
	private HashSet<State> states;
	private State initialState;
	private HashMap<Integer, HashSet<HashMap<Tree, Integer>>> decomposition;
	private HashMap<Tree, Integer> last;
	private HashMap<Tree, Integer> first;
	private static int counter = 0;


	public CFS(Tree t) {
		last = getLastMap(t);
		decomposition = algorithm1(getPositionMap(t), t);
		first = getFirstMap(t);
		initialState = new State(first, doesEmptyStringMatch(t));
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

	public boolean match(String s) {
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

	// parenthesisRemove után lehet csak, mert nem kezeli a zárójeleket
	public static boolean doesEmptyStringMatch(Tree t) {
		switch (t.getType()) {
		case PCRELexer.OR:
			for (int i = 0; i < t.getChildCount(); i++) {
				if (doesEmptyStringMatch(t.getChild(i))) {
					return true;
				}
			}
			return false;

		case PCRELexer.ALTERNATIVE:
			for (int i = 0; i < t.getChildCount(); i++) {
				if (!doesEmptyStringMatch(t.getChild(i))) {
					return false;
				}
			}
			return true;

		case PCRELexer.ELEMENT:
			if (t.getChildCount() > 1) {
				if (t.getChild(1).getChild(0).toString().equals("0")) {
					return true;
				} else {
					return doesEmptyStringMatch(t.getChild(0));
				}
			} 
		
		case PCRELexer.LITERAL:
			if (t.getText().charAt(0) == 'ε') {
				return true;
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
				if (t.getChildCount() > 1 && doesEmptyStringMatch(t.getChild(1))) { //legalabb 2 hosszu alternative, es az utolso lehet ures - > utolso elotti is jatszik
					calculateLast(t.getChild(0), positionMap, lastMap);
					calculateLast(t.getChild(1), positionMap, lastMap);
					t.addChild(tempTree);
				} else {
					calculateLast(t.getChild(1), positionMap, lastMap);
				}
			}
			break;

			/*case PCRELexer.LITERAL:	
				
				}*/
		case PCRELexer.ELEMENT:
			calculateLast(t.getChild(0), positionMap, lastMap);
			break;
		case PCRELexer.WordBoundary:
		case PCRELexer.CHARACTER_CLASS:
		case PCRELexer.NEGATED_CHARACTER_CLASS:
		case PCRELexer.WhiteSpace:
		case PCRELexer.LITERAL:
			if (t.getText().charAt(0) == 'ε') {
				break;
			}
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

			if (t.getChildCount() > 0) {
				if (t.getChildCount() > 1 && doesEmptyStringMatch(t.getChild(0))) {

					calculateFirst(t.getChild(0), positionMap, firstMap);
					calculateFirst(t.getChild(1), positionMap, firstMap); 

				} else {
					calculateFirst(t.getChild(0), positionMap, firstMap);
				}
			}
			break;

		case PCRELexer.ELEMENT:
			calculateFirst(t.getChild(0), positionMap, firstMap);
			break;

		case PCRELexer.LITERAL:	
				if (t.getText().charAt(0) == 'ε') {
					break;
				}
		
		case PCRELexer.WordBoundary:
		case PCRELexer.NonWordBoundary:
		case PCRELexer.CHARACTER_CLASS:
		case PCRELexer.NEGATED_CHARACTER_CLASS:
		case PCRELexer.WhiteSpace:
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
			if (t.getText().charAt(0) == 'ε') {
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

	private void calculateFollow(Tree regex, HashMap<Tree, Integer> rootRegexPositions, HashMap<Tree, Integer> followMap, Integer position, Tree letter) {
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

	private HashMap<Tree, Integer> getFollowMap(Tree regexTree, Tree letter) {
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

	@SuppressWarnings("unused")
	private HashMap<Tree, Integer> getFollowMap(Tree regexTree, Integer position) {
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

	private Tree next(Tree t) {
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
	@SuppressWarnings("unused")
	private HashMap<Tree, Integer> followTree(Tree t, Integer position) {
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

	/** 
	 * @param map1 the containing map 
	 * @param map2 the contained map
	 *  */
	private boolean containsAll(HashMap<Tree, Integer> map1, HashMap<Tree, Integer>map2) {
		for (Entry<Tree, Integer> e : map2.entrySet()) {
			if (map1.get(e.getKey()) != e.getValue()) {
				return false;
			}
		}
		return true;
	}

	public static boolean areEqual(HashMap<Tree, Integer> map1, HashMap<Tree, Integer>map2) {
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

	private HashMap<Tree, Integer> getPositionIntersectionMap(HashMap<Tree,Integer> m1, HashMap<Tree, Integer>m2) {
		HashMap<Tree, Integer> result = new HashMap<Tree, Integer>();
		for (Entry<Tree, Integer> e : m1.entrySet()) {
			if (m2.containsKey(e.getKey()) && m2.containsValue(e.getValue())) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}

	// 1 méretű fára decomposition
	private HashSet<HashMap<Tree, Integer>> dec1(HashMap<Tree, Integer> rootPositions, Integer position, Tree subExpr) {
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
	// leszármazott == bc, pozíciók: b-0, c-1
	// így last(bc) == c-2 lesz c-1 helyett
	private HashMap<Tree, Integer> getSubTreePositionMap(HashMap<Tree, Integer> rootPositions, HashMap<Tree, Integer> subExprPositions) {
		HashMap<Tree, Integer> resultMap = new HashMap<Tree, Integer>();

		for (Tree i : subExprPositions.keySet()) {
			if (rootPositions.containsKey(i))
				resultMap.put(i, rootPositions.get(i));
		}
		return resultMap;
	}


	private HashMap<Integer, HashSet<HashMap<Tree, Integer>>> algorithm1(HashMap<Tree, Integer> rootPositions, Tree subExpr) {
		HashMap<Integer, HashSet<HashMap<Tree, Integer>>> resultMap = new HashMap<Integer, HashSet<HashMap<Tree, Integer>>>();
		HashMap<Tree, Integer> subExprPositions = getSubTreePositionMap(rootPositions, getPositionMap(subExpr));
		int subExprSize = subExprPositions.size();
		if (subExprSize == 1) {
			int position = -1;
			for (Entry<Tree, Integer> e : subExprPositions.entrySet()) {
				position = e.getValue();
			}
			resultMap.put(position, dec1(rootPositions, position, subExpr));
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
					int t1Index = t1.getChildIndex();
					Tree t1Parent = t1.getParent();
					t1Parent.deleteChild(t1Index);
					HashMap<Integer, HashSet<HashMap<Tree, Integer>>> t1Dec = algorithm1(rootPositions, t1);
					HashMap<Integer, HashSet<HashMap<Tree, Integer>>> t2Dec = algorithm1(rootPositions, subExpr);
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
							if (!t1Last.containsValue(position)) {
								resultMap.put(position, t1Dec.get(position));
							} else {
								HashMap<Tree, Integer> C1 = new HashMap<Tree, Integer>();
								Tree g = t1;
								while (g != subExpr) {
									HashMap<Tree, Integer> gLastMap = getSubTreePositionMap(rootPositions, getLastMap(g));
									HashMap<Tree, Integer> interSect = getPositionIntersectionMap(gLastMap, t1Positions);
									if (areEqual(interSect, t1Last)) {
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
							HashSet<HashMap<Tree, Integer>> temp = t2Dec.get(position);
							if (containsAll(getSubTreePositionMap(rootPositions, getFollowMap(subExpr, letter)), t1First)) {
								HashMap<Tree, Integer> C2 = getPositionIntersectionMap(subExprPositions, t1First);
								//System.out.println(C2);
								temp.add(C2);
							}
							resultMap.put(position, temp);
						}
					}
					return resultMap;
				}
				for (int i = 0; i < t1.getChildCount(); i++) {
					s.add(t1.getChild(i));
				}
			}
		}
		System.out.println("err");
		return null;
	}




	// GETTERS, SETTERS
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
