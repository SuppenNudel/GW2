package de.rohmio.gw2.tools;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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

//	public void testRecipes() throws GuildWars2Exception, InterruptedException {
//		RequestProgress<Recipe> allRecipes = Data.getInstance().getRecipeProgress().getAll();
//		System.out.println("Before wait: "+allRecipes.size());
//		while(allRecipes.getProgress().get() < 1.0) {
//			System.out.println(allRecipes.getProgress().get());
//			Thread.sleep(1000);
//		}
//		System.out.println(allRecipes.getProgress().get());
//		System.out.println("After wait: "+allRecipes.size());
////		allRecipes.forEach((t, u) -> System.out.println(u));
//	}
}
