package de.rohmio.gw2.tools.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rohmio.gw2.tools.main.Util;
import junit.framework.Assert;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class RequestSpeedTest {
	
	private static List<Integer> allRecipeIds;
	private static List<int[]> chunkedIds;
	
	private static List<Recipe> recipes = new ArrayList<>();
	
	@BeforeClass
	public static void directFromApi() throws Exception {
		allRecipeIds = GuildWars2.getInstance().getSynchronous().getAllRecipeID();

		int chunk = 200; // chunk size to divide
		chunkedIds = Util.chunkUp(chunk, allRecipeIds);
		
		Date start = new Date();
		for(int[] ids : chunkedIds) {
			recipes.addAll(GuildWars2.getInstance().getSynchronous().getRecipeInfo(ids));
		}
		Date end = new Date();
		System.out.println("Direct time: "+(end.getTime()-start.getTime())+"ms");
		
		for(Recipe recipe : recipes) {
			File filePath = Util.getFilePath(RequestType.RECIPE, recipe.getId());
			if(!filePath.exists()) {
				Util.writeCache(RequestType.RECIPE, recipe);
			}
		}
		File filePath = Util.getFilePath(RequestType.RECIPE);
		if(!filePath.exists()) {
			Util.writeCache(RequestType.RECIPE, recipes);
		}
	}
	
	@Before
	public void clear() {
		recipes = new ArrayList<>();
	}
	
	@After
	public void testSize() {
		Assert.assertEquals(allRecipeIds.size(), recipes.size());
	}
	
	@Test
	public void directThreaded() {
		List<Thread> threads = new ArrayList<>();
		
		Date start = new Date();
		for(int[] ids : chunkedIds) {
			Thread thread = new Thread(() -> {
				try {
					recipes.addAll(GuildWars2.getInstance().getSynchronous().getRecipeInfo(ids));
				} catch (GuildWars2Exception e) {
					e.printStackTrace();
				}
			});
			threads.add(thread);
			thread.start();
		}
		threads.forEach(thread -> {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		Date end = new Date();
		System.out.println("Direct threaded time: "+(end.getTime()-start.getTime())+"ms");
	}
	
	@Test
	public void singleRecipeFile() {
		Date start = new Date();
		recipes = Util.getCache(RequestType.RECIPE);
		Date end = new Date();
		System.out.println("Single File time: "+(end.getTime()-start.getTime())+"ms");
	}

	@Test
	public void multipleRecipeFiles() {
		Date start = new Date();
		for(int id : allRecipeIds) {
			recipes.add(Util.getCache(RequestType.RECIPE, id));
		}
		Date end = new Date();
		System.out.println("File time: "+(end.getTime()-start.getTime())+"ms");
	}
	
	
}
