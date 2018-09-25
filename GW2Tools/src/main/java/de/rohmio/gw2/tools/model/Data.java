package de.rohmio.gw2.tools.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import okhttp3.Cache;

public class Data {

	private static Data data;

	private List<Recipe> allRecipes;

	private Data() {
		try {
			GuildWars2.setInstance(new Cache(new File("cache"), 4096));
		} catch (GuildWars2Exception e) {
			e.printStackTrace();
		}
	}

	public static Data getInstance() {
		if (data == null) {
			data = new Data();
		}
		return data;
	}

	public List<Recipe> getAllRecipes() {
		// File file = new File("recipes.json");
		// Type type = new TypeToken<List<Recipe>>() {
		// }.getType();
		// if (allRecipes == null) {
		// try {
		// if (file.exists()) {
		// List<Recipe> fromJson = new Gson().fromJson(new FileReader(file), type);
		// allRecipes = fromJson;
		// }
		// } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
		// e.printStackTrace();
		// }
		// }
		if (allRecipes == null) {
			GuildWars2 gw2 = GuildWars2.getInstance();
			try {
				List<Integer> allRecipeID = gw2.getSynchronous().getAllRecipeID();

				int[] allRecipeIDArray = allRecipeID.stream().mapToInt(i -> i).toArray();

				allRecipes = new ArrayList<>();

				int chunk = 200; // chunk size to divide
				for (int i = 0; i < allRecipeIDArray.length; i += chunk) {
					int[] chunkArray = Arrays.copyOfRange(allRecipeIDArray, i,
							Math.min(allRecipeIDArray.length, i + chunk));
					List<Recipe> recipeInfo = gw2.getSynchronous().getRecipeInfo(chunkArray);
					System.out.println(recipeInfo);
					allRecipes.addAll(recipeInfo);
				}
				// file.createNewFile();
				// new Gson().toJson(allRecipes, type, new FileWriter(file));
				System.out.println("finished loading recipes");
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		}
		return allRecipes;
	}

}
