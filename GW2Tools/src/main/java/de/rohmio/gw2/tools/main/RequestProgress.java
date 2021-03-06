package de.rohmio.gw2.tools.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import me.xhsun.guildwars2wrapper.AsynchronousRequest;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.SynchronousRequest;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.identifiable.IdentifiableInt;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestProgress<T extends IdentifiableInt> extends HashMap<Integer, T> {
	
	private static Logger log = Logger.getLogger("RequestProgress");
	
	// double value for how much percentage this data type is loaded
	private DoubleProperty progress = new SimpleDoubleProperty();
	
	// list of all ids available for this data type
	private List<Integer> allIds;
	private RequestType type;

	// function to get all ids of this data type
	private Callable<List<Integer>> idCaller;
	private Function<int[], Void> infoFunction;

	public RequestProgress(RequestType type) throws NullPointerException, GuildWars2Exception {
		this.type = type;

		SynchronousRequest synchronous = GuildWars2.getInstance().getSynchronous();
		AsynchronousRequest asynchronous = GuildWars2.getInstance().getAsynchronous();
		
		switch (type) {
		case RECIPE:
			idCaller = () -> synchronous.getAllRecipeID();
			infoFunction = ids -> {
				try {
					asynchronous.getRecipeInfo(ids, new Callback<List<Recipe>>() {
						@SuppressWarnings("unchecked")
						@Override
						public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
							List<T> body = (List<T>) response.body();
							if(body != null) {
								handleResult(body);
							}
						}
						@Override
						public void onFailure(Call<List<Recipe>> call, Throwable t) {
						}
					});
				} catch (NullPointerException | GuildWars2Exception e) {
					e.printStackTrace();
				}
				return null;
			};
			break;
		case ITEM:
			idCaller = () -> synchronous.getAllItemID();
			infoFunction = ids -> {
				try {
					asynchronous.getItemInfo(ids, new Callback<List<Item>>() {
						@SuppressWarnings("unchecked")
						@Override
						public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
							List<T> body = (List<T>) response.body();
							if(body != null) {
								handleResult(body);
							}
						}
						@Override
						public void onFailure(Call<List<Item>> call, Throwable t) {
						}
					});
				} catch (NullPointerException | GuildWars2Exception e) {
					e.printStackTrace();
				}
				return null;
			};
			break;
		default:
			break;
		}
		
		try {
			allIds = idCaller.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DoubleProperty getProgress() {
		return progress;
	}

	public List<Integer> getIds() {
		return allIds;
	}
	
	private void updateProgress() {
		progress.set(1.0 * size() / allIds.size());
	}

	@Override
	public T put(Integer key, T value) {
		T put = super.put(key, value);
		updateProgress();
		return put;
	}
	
	@Override
	public void putAll(Map<? extends Integer, ? extends T> m) {
		super.putAll(m);
		updateProgress();
	}

	public RequestProgress<T> getByIds(List<Integer> itemIds) {
		List<Integer> toRequest = new ArrayList<>();
		
		for(Integer id : itemIds) {
			log.finest("Iterating getByIds: "+id);
			if(!containsKey(id)) {
				// if not already loaded
				log.finest("Iterating getByIds: "+id+" is not loaded yet");
				// try to get from cache
				T value = Util.getCache(type, id, type.getClazz());
				if(value != null) {
					// if found add to loaded
					put(id, value);
					log.finest("Iterating getByIds: "+id+" found in cache and added");
				} else {
					// if not found add to toRequest
					toRequest.add(id);
					log.finest("Iterating getByIds: "+id+" NOT found in cache -> to request");
				}
			}
		}
		
		updateProgress();
		
		// convert to array
		int[] allIdArray = toRequest.stream().mapToInt(i -> i).toArray();

		int chunk = 200; // chunk size to divide
		List<int[]> chunkedIds = Util.chunkUp(chunk, allIdArray);
		for(int[] ids : chunkedIds) {
			infoFunction.apply(ids);
		}

		return this;
	}
	
	public T getById(int id) {
		List<Integer> list = new ArrayList<>();
		list.add(id);
		T result = getByIds(list).get(id);
		return result;
	}
		
	public RequestProgress<T> getAll() {
		List<Integer> toRequest = new ArrayList<>(allIds);
		return getByIds(toRequest);
	}
	
	private RequestProgress<T> getThis() {
		return this;
	}
	
	private void handleResult(List<T> result) {
		result.forEach(r -> Util.writeCache(type, r.getId(), r));							
		Map<Integer, T> collect = result.stream().collect(Collectors.toMap(T::getId, r -> r));
		putAll(collect);
		System.out.println("Iteration: "+getThis().size());
	}

}
