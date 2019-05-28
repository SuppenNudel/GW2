package de.rohmio.gw2.tools.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.rohmio.gw2.tools.main.Util;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
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

public class RequestProgress<T extends IdentifiableInt> {
	
	private static Logger log = Logger.getLogger("RequestProgress");
	
	// double value for how much percentage this data type is loaded
	private DoubleProperty progress = new SimpleDoubleProperty();
	
	private ObservableMap<Integer, T> values = FXCollections.observableHashMap();
	
	// list of all ids available for this data type
	private List<Integer> allIds;
	private RequestType type;

	// function to get all ids of this data type
	private Callable<List<Integer>> idCaller;
	private Function<int[], Void> infoFunction;

	public RequestProgress(RequestType type) {
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
		progress.set(1.0 * values.size() / allIds.size());
	}

	public T put(Integer key, T value) {
		T put = values.put(key, value);
		updateProgress();
		return put;
	}
	
	public void putAll(Map<? extends Integer, ? extends T> m) {
		values.putAll(m);
		updateProgress();
	}

	public synchronized ObservableMap<Integer, T> getByIds(List<Integer> itemIds) {
		List<Integer> toRequest = new ArrayList<>();
		
		for(Integer id : itemIds) {
			log.finest("Iterating getByIds: "+id);
			if(!values.containsKey(id)) {
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

		return values;
	}
	
	public T getById(int id) {
		// convert single id to ArrayList
		List<Integer> list = new ArrayList<>();
		list.add(id);
		T result = getByIds(list).get(id);
		return result;
	}
		
	public ObservableMap<Integer, T> getAll() {
		List<Integer> toRequest = new ArrayList<>(allIds);
		return getByIds(toRequest);
	}
	
	private void handleResult(List<T> result) {
		result.forEach(r -> Util.writeCache(type, r.getId(), r));							
		Map<Integer, T> collect = result.stream().collect(Collectors.toMap(T::getId, r -> r));
		putAll(collect);
		System.out.println("Iteration: "+values.size());
	}

}
