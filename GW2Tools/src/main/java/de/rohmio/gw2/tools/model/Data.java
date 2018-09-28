package de.rohmio.gw2.tools.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Data {

	private static Data data;

	private ObservableMap<Integer, Recipe> recipes = FXCollections.observableHashMap();
	private IntegerProperty recipesSize = new SimpleIntegerProperty();

	public ObservableMap<Integer, Recipe> recipesProperty() {
		return recipes;
	}

	private List<Item> allItems;
	private Map<Integer, Item> allItemsMap = new HashMap<>();

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

	public File getImage(String url) throws IOException {
		String fileName = "data/img/" + new File(url).getName();
		final File file = new File(fileName);
		if (!file.exists()) {
			Request request = new Request.Builder().url(url).build();
			OkHttpClient client = ClientFactory.getClient();
			okhttp3.Response response = client.newCall(request).execute();
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(response.body().bytes());
			fileOutputStream.close();
		}
		return file;
	}

	public IntegerProperty getRecipesSize() {
		return recipesSize;
	}
	public ObservableMap<Integer, Recipe> getAllRecipes() throws GuildWars2Exception {
		GuildWars2 gw2 = GuildWars2.getInstance();

		// first get all ids
		List<Integer> result = gw2.getSynchronous().getAllRecipeID();
		// convert to array
		int[] allRecipeIDArray = result.stream().mapToInt(i -> i).toArray();
		recipesSize.set(allRecipeIDArray.length);

		int chunk = 200; // chunk size to divide
		List<int[]> chunkedRecipeIds = chunkUp(chunk, allRecipeIDArray);
		for (int[] recipeIds : chunkedRecipeIds) {
			// request recipes' info
			gw2.getAsynchronous().getRecipeInfo(recipeIds, new Callback<List<Recipe>>() {
				@Override
				public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
					List<Recipe> recipesResult = response.body();
					// add elements to member list
					Map<Integer, Recipe> collect = recipesResult.stream().collect(Collectors.toMap(Recipe::getId, recipe -> recipe));
					synchronized (recipes) {
						recipes.putAll(collect);
					}
				}

				@Override
				public void onFailure(Call<List<Recipe>> call, Throwable t) {
				}
			});
		}

		return recipes;
	}

	private List<int[]> chunkUp(int chunkSize, int[] array) {
		List<int[]> list = new ArrayList<>();
		for (int i = 0; i < array.length; i += chunkSize) {
			int[] chunkArray = Arrays.copyOfRange(array, i, Math.min(array.length, i + chunkSize));
			list.add(chunkArray);
		}
		return list;
	}

	public Item getItemById(int id) {
		Item item = allItemsMap.get(id);
		if (item == null) {
			item = getItemsById(id).get(0);
		}
		return item;
	}

	public List<Item> getItemsById(int... ids) {
		GuildWars2 gw2 = GuildWars2.getInstance();

		List<Integer> toRequest = new ArrayList<>();
		List<Item> result = new ArrayList<>();
		for (int id : ids) {
			Item item = allItemsMap.get(id);
			if (item == null) {
				toRequest.add(id);
			} else {
				result.add(item);
			}
		}
		int[] toRequestArray = toRequest.stream().mapToInt(i -> i).toArray();
		List<int[]> chunkUp = chunkUp(200, toRequestArray);
		for (int[] itemIds : chunkUp) {
			try {
				List<Item> itemInfo = gw2.getSynchronous().getItemInfo(itemIds);
				Map<Integer, Item> collect = itemInfo.stream().collect(Collectors.toMap(Item::getId, item -> item));
				allItemsMap.putAll(collect);
				result.addAll(itemInfo);
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Deprecated
	public List<Item> getAllItems() {
		if (allItems == null) {
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
