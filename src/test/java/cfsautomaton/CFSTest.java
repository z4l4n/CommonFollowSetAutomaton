package cfsautomaton;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.antlr.runtime.tree.Tree;
import org.junit.Ignore;
import org.junit.Test;

import pcreparser.PCRE;


public class CFSTest {
	private PCRE pcre;
	private CFSAutomaton cfs;
	
	private HashSet<String> expectedPositions;
	
	public static boolean areEqual(HashMap<Tree, Integer> map1, HashSet<String>map2) {
		if (map1.size() != map2.size()) {
			return false;
		}
		for (Entry<Tree, Integer> e : map1.entrySet()) {
			if (map2.contains(e.getKey().getText().charAt(0) + "=" + e.getValue())) {
				;
			} else {
				return false;
			}
		}
		return true;
	}
	
	
	@Test
	public void testGetPositionMap() {

		pcre = new PCRE("ε");
		expectedPositions = new HashSet<String>();
		assertTrue(areEqual(CFSAutomaton.getPositionMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("εab");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		assertTrue(areEqual(CFSAutomaton.getPositionMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("abεc");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		expectedPositions.add("c=2");
		assertTrue(areEqual(CFSAutomaton.getPositionMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("abεcεd");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		expectedPositions.add("c=2");
		expectedPositions.add("d=3");
		assertTrue(areEqual(CFSAutomaton.getPositionMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("(a|ε)*bcε*d");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		expectedPositions.add("c=2");
		expectedPositions.add("d=3");
		assertTrue(areEqual(CFSAutomaton.getPositionMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("aab(c|d)ea");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("a=1");
		expectedPositions.add("b=2");
		expectedPositions.add("c=3");
		expectedPositions.add("d=4");
		expectedPositions.add("e=5");
		expectedPositions.add("a=6");
		assertTrue(areEqual(CFSAutomaton.getPositionMap(pcre.getAppropriateTree()), expectedPositions));
	}
	
	@Test
	public void testGetLastMap() {
		pcre = new PCRE("ε");
		expectedPositions = new HashSet<String>();
		assertTrue(areEqual(CFSAutomaton.getLastMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("g");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("g=0");
		assertTrue(areEqual(CFSAutomaton.getLastMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("g|h");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("g=0");
		expectedPositions.add("h=1");
		assertTrue(areEqual(CFSAutomaton.getLastMap(pcre.getAppropriateTree()), expectedPositions));
		

		pcre = new PCRE("fg");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("g=1");
		assertTrue(areEqual(CFSAutomaton.getLastMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("gf|hkj*");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("f=1");
		expectedPositions.add("k=3");
		expectedPositions.add("j=4");
		assertTrue(areEqual(CFSAutomaton.getLastMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("gfy*|c(ε|hkj)");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("f=1");
		expectedPositions.add("y=2");
		expectedPositions.add("c=3");
		expectedPositions.add("j=6");
		assertTrue(areEqual(CFSAutomaton.getLastMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("d(eab*c*)*(f|ε)");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("b=3");
		expectedPositions.add("a=2");
		expectedPositions.add("c=4");
		expectedPositions.add("d=0");
		expectedPositions.add("f=5");
		assertTrue(areEqual(CFSAutomaton.getLastMap(pcre.getAppropriateTree()), expectedPositions));
	}
	
	
	@Test
	public void testGetFirstMap() {
		
		pcre = new PCRE("ε");
		expectedPositions = new HashSet<String>();
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a|b");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a|b|c");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		expectedPositions.add("c=2");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("ab");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a*b");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("εb");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("b=0");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("(a|ε)b");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a*");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("(af)*");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("abc");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a|b|c|d");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		expectedPositions.add("c=2");
		expectedPositions.add("d=3");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("(a|b)(c|d)");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("(a|b|c)(c|d)");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		expectedPositions.add("c=2");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
	
		pcre = new PCRE("ab|cd");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("c=2");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a*c*");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("c=1");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a*|c*");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("c=1");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("(a*b|c*d)*ef");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("b=1");
		expectedPositions.add("c=2");
		expectedPositions.add("d=3");
		expectedPositions.add("e=4");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("(f|c|ε|a)gh");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("f=0");
		expectedPositions.add("c=1");
		expectedPositions.add("a=2");
		expectedPositions.add("g=3");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
		
		pcre = new PCRE("a*εfh");
		expectedPositions = new HashSet<String>();
		expectedPositions.add("a=0");
		expectedPositions.add("f=1");
		assertTrue(areEqual(CFSAutomaton.getFirstMap(pcre.getAppropriateTree()), expectedPositions));
	}
	
	@Test
	public void testDoesEmptyStringMatch() {
		
		pcre = new PCRE("ε");
		assertTrue(CFSAutomaton.doesEmptyStringMatch(pcre.getAppropriateTree()));
		
		pcre = new PCRE("a|ε");
		assertTrue(CFSAutomaton.doesEmptyStringMatch(pcre.getAppropriateTree()));
		
		pcre = new PCRE("a*b*");
		assertTrue(CFSAutomaton.doesEmptyStringMatch(pcre.getAppropriateTree()));
		
		pcre = new PCRE("a*(b|c|ε|d)e*");
		assertTrue(CFSAutomaton.doesEmptyStringMatch(pcre.getAppropriateTree()));
		
		pcre = new PCRE("a*(b|c*|d)e*");
		assertTrue(CFSAutomaton.doesEmptyStringMatch(pcre.getAppropriateTree()));
		
		pcre = new PCRE("(a|b)*(f*g)*");
		assertTrue(CFSAutomaton.doesEmptyStringMatch(pcre.getAppropriateTree()));
		
		pcre = new PCRE("εf*ε(a|ε)");
		assertTrue(CFSAutomaton.doesEmptyStringMatch(pcre.getAppropriateTree()));
		
		pcre = new PCRE("εf*ε(a|c)d*");
		assertFalse(CFSAutomaton.doesEmptyStringMatch(pcre.getAppropriateTree()));
	}
	
	 @Ignore
	@Test
	public void testCFS() {
		fail("Not yet implemented");
	}
	 @Ignore
	@Test
	public void testGetStateCount() {
		fail("Not yet implemented");
	}
	 @Ignore
	@Test
	public void testGetTransitionCount() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testMatch() {
		pcre = new PCRE("a");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertFalse(cfs.match(""));
		assertFalse(cfs.match("b"));
		
		System.out.println("ab regex automaton");
		pcre = new PCRE("ab");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("ab"));
		assertFalse(cfs.match("a"));
		assertFalse(cfs.match(""));
		
		pcre = new PCRE("a|b");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match("b"));
		assertFalse(cfs.match(""));
		assertFalse(cfs.match("c"));
		
		pcre = new PCRE("a*");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("aaa"));
		assertFalse(cfs.match("ba"));
		
		pcre = new PCRE("ab*");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertFalse(cfs.match("aa"));
		assertTrue(cfs.match("ab"));
		assertTrue(cfs.match("abb"));
		
		pcre = new PCRE("ab*a");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
	//	assertTrue(cfs.match("aa"));
		assertFalse(cfs.match("a"));
	//	assertTrue(cfs.match("aba"));
		assertTrue(cfs.match("abba"));
		assertFalse(cfs.match("abbab"));
		
		pcre = new PCRE("a*|b");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match("aaa"));
		assertFalse(cfs.match("bb"));
		
		pcre = new PCRE("(ab)*");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("ab"));
		assertTrue(cfs.match("ababab"));
		assertFalse(cfs.match("aba"));
		
		pcre = new PCRE("(abc)*");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("abcabc"));
		assertFalse(cfs.match("abca"));
		
		pcre = new PCRE("(a|b)*");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match("ab"));
		assertTrue(cfs.match("ba"));
		assertTrue(cfs.match("aabaa"));
		assertTrue(cfs.match("bbb"));
		assertFalse(cfs.match("ababc"));
		
		pcre = new PCRE("(a|b|c)*");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("aabbc"));
		assertFalse(cfs.match("afa"));
		
		pcre = new PCRE("(ab)*d(e|f)*");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("d"));
		assertTrue(cfs.match("abdee"));
		assertTrue(cfs.match("ababdf"));
		assertTrue(cfs.match("deeff"));
		assertTrue(cfs.match("abd"));
		assertFalse(cfs.match("ade"));
		assertFalse(cfs.match("abadef"));
		
		pcre = new PCRE("(a|b)((cd)*|d)*");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("a"));
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match("ad"));
		assertTrue(cfs.match("add"));
		assertTrue(cfs.match("bcd"));
		assertTrue(cfs.match("acddcd"));
		assertFalse(cfs.match("bcddc"));
		assertFalse(cfs.match(""));
		
		pcre = new PCRE("b(c|d)*");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("bdc"));
		
		pcre = new PCRE("ε");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertFalse(cfs.match("b"));
		assertTrue(cfs.match(""));
		
		pcre = new PCRE("b|ε");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match(""));
		
		pcre = new PCRE("c(b|ε)");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("c"));
		assertTrue(cfs.match("cb"));
		
		pcre = new PCRE("(b|ε)c");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match("c"));
		assertTrue(cfs.match("bc"));
		
		pcre = new PCRE("(a|ε)(b|ε)");
		cfs = new CFSAutomaton(pcre.getAppropriateTree());
		assertTrue(cfs.match(""));
		assertTrue(cfs.match("ab"));
		assertTrue(cfs.match("b"));
		assertTrue(cfs.match("a"));
		assertFalse(cfs.match("ba"));
		
	}
	@Ignore
	@Test
	public void testAreEqual() {
		fail("Not yet implemented");
	}
	 @Ignore
	@Test
	public void testGetStates() {
		fail("Not yet implemented");
	}
	 @Ignore
	@Test
	public void testSetStates() {
		fail("Not yet implemented");
	}
	 @Ignore
	@Test
	public void testGetInitialState() {
		fail("Not yet implemented");
	}
	 @Ignore
	@Test
	public void testGetDecomposition() {
		fail("Not yet implemented");
	}

}
