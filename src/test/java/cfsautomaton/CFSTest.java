package cfsautomaton;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;

import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import pcreparser.PCRE;
import thomsonautomaton.ThomsonAutomaton;

import org.junit.runner.RunWith;


public class CFSTest {
	private PCRE pcre;
	private CFS cfs;
	private HashMap<Tree, Integer> positions;
	private HashMap<Character, Integer> expectedPositions;
	
	public static boolean areEqual(HashMap<Tree, Integer> map1, HashMap<Character, Integer>map2) {
		if (map1.size() != map2.size()) {
			return false;
		}
		for (Entry<Tree, Integer> e : map1.entrySet()) {
			if (map2.containsKey(e.getKey().getText().charAt(0)) && map2.get(e.getKey().getText().charAt(0)).equals(e.getValue())) {
				;
			} else {
				return false;
			}
		}
		return true;
	}
	
	
	@Test
	public void testGetFirstMap() {
		
		/*pcre = new PCRE("ε");
		expectedPositions = new HashMap<Character, Integer>();
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		*/
		pcre = new PCRE("a");
		expectedPositions = new HashMap<Character, Integer>();
		expectedPositions.put('a', 0);
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a|b");
		expectedPositions = new HashMap<Character, Integer>();
		expectedPositions.put('a', 0);
		expectedPositions.put('b', 1);
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a|b|c");
		expectedPositions = new HashMap<Character, Integer>();
		expectedPositions.put('a', 0);
		expectedPositions.put('b', 1);
		expectedPositions.put('c', 2);
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("ab");
		expectedPositions = new HashMap<Character, Integer>();
		expectedPositions.put('a', 0);
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a*b");
		expectedPositions = new HashMap<Character, Integer>();
		expectedPositions.put('a', 0);
		expectedPositions.put('b', 1);
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		/*pcre = new PCRE("εb");
		expectedPositions = new HashMap<Character, Integer>();
		expectedPositions.put('b', 1);
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("(a|ε)b");
		expectedPositions = new HashMap<Character, Integer>();
		expectedPositions.put('a', 0);
		expectedPositions.put('b', 1);
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		*/
		pcre = new PCRE("a*");
		expectedPositions = new HashMap<Character, Integer>();
		expectedPositions.put('a', 0);
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("(af)*");
		expectedPositions = new HashMap<Character, Integer>();
		expectedPositions.put('a', 0);
		assertTrue(areEqual(CFS.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
	}
	
	@Test
	public void testCFS() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStateCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTransitionCount() {
		fail("Not yet implemented");
	}
	
	
	@Test
	public void testMatch() {
		pcre = new PCRE("a");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertFalse(cfs.match(""));
		assertFalse(cfs.match("b"));
		
		pcre = new PCRE("ab");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("ab"));
		assertFalse(cfs.match("a"));
		assertFalse(cfs.match(""));
		
		pcre = new PCRE("a|b");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match("b"));
		assertFalse(cfs.match(""));
		assertFalse(cfs.match("c"));
		
		pcre = new PCRE("a*");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("aaa"));
		assertFalse(cfs.match("ba"));
		
		pcre = new PCRE("ab*");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertFalse(cfs.match("aa"));
		assertTrue(cfs.match("ab"));
		assertTrue(cfs.match("abb"));
		
		pcre = new PCRE("ab*a");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("aa"));
		assertFalse(cfs.match("a"));
		assertTrue(cfs.match("aba"));
		assertTrue(cfs.match("abba"));
		assertFalse(cfs.match("abbab"));
		
		pcre = new PCRE("a*|b");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match("aaa"));
		assertFalse(cfs.match("bb"));
		
		pcre = new PCRE("(ab)*");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("ab"));
		assertTrue(cfs.match("ababab"));
		assertFalse(cfs.match("aba"));
		
		pcre = new PCRE("(abc)*");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("abcabc"));
		assertFalse(cfs.match("abca"));
		
		pcre = new PCRE("(a|b)*");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match("ab"));
		assertTrue(cfs.match("ba"));
		assertTrue(cfs.match("aabaa"));
		assertTrue(cfs.match("bbb"));
		assertFalse(cfs.match("ababc"));
		
		pcre = new PCRE("(a|b|c)*");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("aabbc"));
		assertFalse(cfs.match("afa"));
		
		pcre = new PCRE("(ab)*d(e|f)*");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("d"));
		assertTrue(cfs.match("abdee"));
		assertTrue(cfs.match("ababdf"));
		assertTrue(cfs.match("deeff"));
		assertTrue(cfs.match("abd"));
		assertFalse(cfs.match("ade"));
		assertFalse(cfs.match("abadef"));
		
		pcre = new PCRE("(a|b)((cd)*|d)*");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match("ad"));
		assertTrue(cfs.match("add"));
		assertTrue(cfs.match("bcd"));
		assertTrue(cfs.match("acddcd"));
		assertFalse(cfs.match("bcddc"));
		assertFalse(cfs.match(""));
		
		pcre = new PCRE("b(c|d)*");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("bdc"));
		
		pcre = new PCRE("ε");
		cfs = new CFS(pcre.getAppropriateTree());
		assertFalse(cfs.match("b"));
		assertTrue(cfs.match(""));
		
		pcre = new PCRE("b|ε");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match(""));
		
		pcre = new PCRE("c(b|ε)");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("c"));
		assertTrue(cfs.match("cb"));
		
		pcre = new PCRE("(b|ε)c");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match("c"));
		assertTrue(cfs.match("bc"));
		
		pcre = new PCRE("(a|ε)(b|ε)");
		cfs = new CFS(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("ab"));
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match("a"));
		assertFalse(cfs.match("ba"));
		
	}

	@Test
	public void testAreEqual() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStates() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetStates() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInitialState() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDecomposition() {
		fail("Not yet implemented");
	}

}
