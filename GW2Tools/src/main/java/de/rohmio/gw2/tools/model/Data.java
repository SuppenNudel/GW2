package de.rohmio.gw2.tools.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

	public DoubleProperty progress = new SimpleDoubleProperty();
	private int iterationsDone;

	public List<Recipe> getAllRecipes() throws GuildWars2Exception {
		if (allRecipes == null) {
			progress.set(0);
			iterationsDone = 0;
			GuildWars2 gw2 = GuildWars2.getInstance();

			// first get all ids
			List<Integer> result = gw2.getSynchronous().getAllRecipeID();
			// convert to array
			int[] allRecipeIDArray = result.stream().mapToInt(i -> i).toArray();

			// init member list
			allRecipes = new ArrayList<>();

			int chunk = 200; // chunk size to divide
			int iterationsToDo = 1 + allRecipeIDArray.length / chunk;
			for (int i = 0; i < allRecipeIDArray.length; i += chunk) {
				int[] chunkArray = Arrays.copyOfRange(allRecipeIDArray, i,
						Math.min(allRecipeIDArray.length, i + chunk));
				try {
					// request recipes' info
					gw2.getAsynchronous().getRecipeInfo(chunkArray, new Callback<List<Recipe>>() {
						@Override
						public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
							List<Recipe> recipes = response.body();
							// add elements to member list
							allRecipes.addAll(recipes);
							++iterationsDone;
							progress.set(1.0 * iterationsDone / iterationsToDo);
							if(progress.get() == 1.0) {
								System.out.println("finished loading recipes");
							}
						}

						@Override
						public void onFailure(Call<List<Recipe>> call, Throwable t) {
						}
					});
				} catch (NullPointerException | GuildWars2Exception e) {
					e.printStackTrace();
				}
			}

		}
		return allRecipes;
	}

	public Item getItemById(int id) {
		if (allItemsMap == null) {
			allItemsMap = getAllItems().stream().collect(Collectors.toMap(Item::getId, c -> c));
		}
		return allItemsMap.get(id);
	}

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
