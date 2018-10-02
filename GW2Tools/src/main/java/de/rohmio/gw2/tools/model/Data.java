package de.rohmio.gw2.tools.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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

	private Map<Integer, Recipe> allRecipesMap = new HashMap<>();

	private Map<Integer, Item> allItemsMap = new HashMap<>();;

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
	
	private <T> T getCache(String fileName, Class<T> type) {
		Gson gson = new Gson();
		File file = new File(String.format("data/cache/%s.json", fileName));
		T object = null;
		if(file.exists()) {
			try {
				object = gson.fromJson(new FileReader(file), type);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (JsonIOException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return object;
	}
	
	private void writeCache(String fileName, Object object) {
		Gson gson = new Gson();
		File file = new File(String.format("data/cache/%s.json", fileName));
		file.getParentFile().mkdirs();
		try {
			file.createNewFile();
			String json = gson.toJson(object);
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(json);
			fileWriter.close();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Collection<Recipe> getAllRecipes() throws GuildWars2Exception {
		if (allRecipesMap.isEmpty()) {
			progress.set(0);
			iterationsDone = 0;
			GuildWars2 gw2 = GuildWars2.getInstance();

			// first get all ids
			List<Integer> allrecipeIds = gw2.getSynchronous().getAllRecipeID();
			List<Integer> toRequest = new ArrayList<>(allrecipeIds);
			
			for(Integer recipeId : allrecipeIds) {
				String fileName = "recipe/"+recipeId;
				if(allRecipesMap.containsKey(recipeId)) {
					toRequest.remove(recipeId);
				} else { // not already loaded
					Recipe recipe = getCache(fileName, Recipe.class);
					if(recipe != null) {
						toRequest.remove(recipeId);
						synchronized (allRecipesMap) {
							allRecipesMap.put(recipeId, recipe);
						}
					}
				}
			}
			
			// convert to array
			int[] allRecipeIDArray = toRequest.stream().mapToInt(i -> i).toArray();

			int chunk = 200; // chunk size to divide
			List<int[]> chunkedRecipeIds = chunkUp(chunk, allRecipeIDArray);
			int iterationsToDo = 1 + allRecipeIDArray.length / chunk;
			for(int[] recipeIds : chunkedRecipeIds) {
				try {
					// request recipes' info
					gw2.getAsynchronous().getRecipeInfo(recipeIds, new Callback<List<Recipe>>() {
						@Override
						public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
							List<Recipe> recipes = response.body();
							
							recipes.forEach(new Consumer<Recipe>() {
								@Override
								public void accept(Recipe recipe) {
									writeCache("recipe/"+recipe.getId(), recipe);
								}
							});							
							
							Map<Integer, Recipe> collect = recipes.stream().collect(Collectors.toMap(Recipe::getId, recipe -> recipe));
							synchronized (allRecipesMap) {
								allRecipesMap.putAll(collect);
							}
							++iterationsDone;
							progress.set(1.0 * iterationsDone / iterationsToDo);
							if (progress.get() == 1.0) {
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
		return allRecipesMap.values();
	}
	
	public Map<Integer, Recipe> getAllRecipesMap() {
		if(allRecipesMap == null) {
			try {
				getAllRecipes();
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		}
		return allRecipesMap;
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
		for (Integer itemId : ids) {
			if(!allItemsMap.containsKey(itemId)) { // not loaded yet
				String fileName = "item/"+itemId;
				Item item = getCache(fileName, Item.class);
				if (item == null) { // not in cache -> request
					toRequest.add(itemId);
				} else {
					result.add(item); // in cache -> load
					allItemsMap.put(itemId, item);
				}
			}
		}
		int[] toRequestArray = toRequest.stream().mapToInt(i -> i).toArray();
		List<int[]> chunkUp = chunkUp(200, toRequestArray);
		for(int[] itemIds : chunkUp) {
			try {
				List<Item> itemInfo = gw2.getSynchronous().getItemInfo(itemIds);
				Map<Integer, Item> collect = itemInfo.stream().collect(Collectors.toMap(Item::getId, item -> item));
				synchronized (allItemsMap) {
					allItemsMap.putAll(collect);
				}
				for(Item item : itemInfo) {
					writeCache("item/"+item.getId(), item);
				}
				result.addAll(itemInfo);
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}
