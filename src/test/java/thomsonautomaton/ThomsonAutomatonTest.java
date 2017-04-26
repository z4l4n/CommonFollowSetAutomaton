package thomsonautomaton;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import pcreparser.PCRE;

public class ThomsonAutomatonTest {
	private PCRE pcre;
	private ThomsonAutomaton thomson;
	
	 @Ignore
	@Test
	public void testThomsonAutomatonTreeStateState() {
		fail("Not yet implemented");
	}

	 @Ignore
	@Test
	public void testThomsonAutomatonTree() {
		fail("Not yet implemented");
	}

	 @Ignore
	@Test
	public void testGetEmptyTransitionClosure() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatch() {
		pcre = new PCRE("a");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("a"));
		assertFalse(thomson.match(""));
		assertFalse(thomson.match("b"));
		
		pcre = new PCRE("ab");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("ab"));
		assertFalse(thomson.match("a"));
		assertFalse(thomson.match(""));
		
		pcre = new PCRE("a|b");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("a"));
		assertTrue(thomson.match("b"));
		assertFalse(thomson.match(""));
		assertFalse(thomson.match("c"));
		
		pcre = new PCRE("a*");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("a"));
		assertTrue(thomson.match(""));
		assertTrue(thomson.match("aaa"));
		assertFalse(thomson.match("ba"));
		
		pcre = new PCRE("ab*");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("a"));
		assertFalse(thomson.match("aa"));
		assertTrue(thomson.match("ab"));
		assertTrue(thomson.match("abb"));
		
		pcre = new PCRE("ab*a");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("aa"));
		assertFalse(thomson.match("a"));
		assertTrue(thomson.match("aba"));
		assertTrue(thomson.match("abba"));
		assertFalse(thomson.match("abbab"));
		
		pcre = new PCRE("a*|b");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("a"));
		assertTrue(thomson.match("b"));
		assertTrue(thomson.match("aaa"));
		assertFalse(thomson.match("bb"));
		
		pcre = new PCRE("(ab)*");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match(""));
		assertTrue(thomson.match("ab"));
		assertTrue(thomson.match("ababab"));
		assertFalse(thomson.match("aba"));
		
		pcre = new PCRE("(abc)*");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match(""));
		assertTrue(thomson.match("abcabc"));
		assertFalse(thomson.match("abca"));
		
		pcre = new PCRE("(a|b)*");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match(""));
		assertTrue(thomson.match("a"));
		assertTrue(thomson.match("b"));
		assertTrue(thomson.match("ab"));
		assertTrue(thomson.match("ba"));
		assertTrue(thomson.match("aabaa"));
		assertTrue(thomson.match("bbb"));
		assertFalse(thomson.match("ababc"));
		
		pcre = new PCRE("(a|b|c)*");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match(""));
		assertTrue(thomson.match("aabbc"));
		assertFalse(thomson.match("afa"));
		
		pcre = new PCRE("(ab)*d(e|f)*");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("d"));
		assertTrue(thomson.match("abdee"));
		assertTrue(thomson.match("ababdf"));
		assertTrue(thomson.match("deeff"));
		assertTrue(thomson.match("abd"));
		assertFalse(thomson.match("ade"));
		assertFalse(thomson.match("abadef"));
		
		pcre = new PCRE("(a|b)((cd)*|d)*");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("a"));
		assertTrue(thomson.match("b"));
		assertTrue(thomson.match("ad"));
		assertTrue(thomson.match("add"));
		assertTrue(thomson.match("bcd"));
		assertTrue(thomson.match("acddcd"));
		assertFalse(thomson.match("bcddc"));
		assertFalse(thomson.match(""));
		
		pcre = new PCRE("b(c|d)*");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("bdc"));
		
		pcre = new PCRE("ε");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertFalse(thomson.match("b"));
		assertTrue(thomson.match(""));
		
		pcre = new PCRE("b|ε");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("b"));
		assertTrue(thomson.match(""));
		
		pcre = new PCRE("c(b|ε)");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("c"));
		assertTrue(thomson.match("cb"));
		
		pcre = new PCRE("(b|ε)c");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match("c"));
		assertTrue(thomson.match("bc"));
		
		pcre = new PCRE("(a|ε)(b|ε)");
		thomson = new ThomsonAutomaton(pcre.getAppropriateTree());
		assertTrue(thomson.match(""));
		assertTrue(thomson.match("ab"));
		assertTrue(thomson.match("b"));
		assertTrue(thomson.match("a"));
		assertFalse(thomson.match("ba"));
	}

	 @Ignore
	@Test
	public void testGetTransitionCount() {
		fail("Not yet implemented");
	}

	 @Ignore
	@Test
	public void testGetStateCount() {
		fail("Not yet implemented");
	}

	 @Ignore
	@Test
	public void testGetInitialState() {
		fail("Not yet implemented");
	}

	 @Ignore
	@Test
	public void testSetInitialState() {
		fail("Not yet implemented");
	}

	 @Ignore
	@Test
	public void testGetFinalState() {
		fail("Not yet implemented");
	}

	 @Ignore
	@Test
	public void testSetFinalState() {
		fail("Not yet implemented");
	}

}
