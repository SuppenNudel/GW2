package de.rohmio.gw2.tools;

import de.rohmio.gw2.tools.model.Data;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}

	public void testRecipes() throws GuildWars2Exception {
		Data.getInstance().getAllRecipes();
	}
}
