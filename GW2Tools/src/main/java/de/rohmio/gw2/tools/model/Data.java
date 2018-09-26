package de.rohmio.gw2.tools.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class Data {

	private static Data data;

	private List<Recipe> allRecipes;
	
	private List<Item> allItems;
	private Map<Integer, Item> allItemsMap;

	private Data() {
		try {
			GuildWars2.setInstance(ClientFactory.getClient());
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
				System.out.println("finished loading recipes");
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		}
		return allRecipes;
	}
	
	public Item getItemById(int id) {
		if(allItemsMap == null) {
			allItemsMap = getAllItems().stream().collect(Collectors.toMap(Item::getId, c -> c));
		}
		return allItemsMap.get(id);
	}
	
	public List<Item> getAllItems() {
		if(allItems == null) {
			GuildWars2 gw2 = GuildWars2.getInstance();
			List<Integer> allItemID;
			try {
				allItemID = gw2.getSynchronous().getAllItemID();
				int[] allItemIDArray = allItemID.stream().mapToInt(i -> i).toArray();
				
				allItems = new ArrayList<>();
	
				int chunk = 200; // chunk size to divide
				for (int i = 0; i < allItemIDArray.length; i += chunk) {
					int[] chunkArray = Arrays.copyOfRange(allItemIDArray, i,
							Math.min(allItemIDArray.length, i + chunk));
					List<Item> itemInfo = gw2.getSynchronous().getItemInfo(chunkArray);
					System.out.println(itemInfo);
					allItems.addAll(itemInfo);
				}
				System.out.println("finished loading items");
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		}
		return allItems;
	}

}
