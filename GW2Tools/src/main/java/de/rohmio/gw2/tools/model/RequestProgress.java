package de.rohmio.gw2.tools.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import de.rohmio.gw2.tools.main.Util;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import me.xhsun.guildwars2wrapper.AsynchronousRequest;
import me.xhsun.guildwars2wrapper.SynchronousRequest;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.identifiable.IdentifiableInt;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestProgress<T extends IdentifiableInt> {

	// double value for how much percentage this data type is loaded
	private DoubleProperty progress = new SimpleDoubleProperty();

	private ObservableMap<Integer, T> values = FXCollections.observableHashMap();

	// list of all ids available for this data type
	private List<Integer> allIds = new ArrayList<>();

	// function to get all ids of this data type
	private Callable<List<Integer>> idCaller;

	// function to get all that get requested
	private Function<int[], Void> infoFunction;

	private ObservableList<Integer> toRequest = FXCollections.observableArrayList();
	private List<Integer> alreadyGettingRequested = new ArrayList<>();

	private RequestType type;

	@SuppressWarnings("unchecked")
	public RequestProgress(RequestType type) {
		this.type = type;

		progress.bind(Bindings.createDoubleBinding(() -> {
			int valuesSize = values.size();
			int allSize = allIds.size();
			System.out.println("Values: " + valuesSize);
			System.out.println("All: " + allSize);
			return 100.0 * valuesSize / allSize;
		}, values));

		SynchronousRequest synchronous = Data.getInstance().getApi().getSynchronous();
		AsynchronousRequest asynchronous = Data.getInstance().getApi().getAsynchronous();

		switch (type) {
		case RECIPE:
			idCaller = () -> synchronous.getAllRecipeID();
			infoFunction = ids -> {
				try {
					asynchronous.getRecipeInfo(ids, new Callback<List<Recipe>>() {
						@Override
						public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
							List<T> body = (List<T>) response.body();
							handleResponse(body);
						}

						@Override
						public void onFailure(Call<List<Recipe>> call, Throwable t) {
							t.printStackTrace();
						}
					});
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (GuildWars2Exception e) {
					onError(e, ids);
				}
				return null;
			};
			break;
		case ITEM:
			idCaller = () -> synchronous.getAllItemID();
			infoFunction = ids -> {
				try {
					asynchronous.getItemInfo(ids, new Callback<List<Item>>() {
						@Override
						public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
							handleResponse((List<T>) response.body());
						}

						@Override
						public void onFailure(Call<List<Item>> call, Throwable t) {
							t.printStackTrace();
						}
					});
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (GuildWars2Exception e) {
					onError(e, ids);
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

		toRequest.addListener((ListChangeListener<Integer>) c -> {
			while (c.next()) {
				if (c.wasAdded()) {
					List<? extends Integer> added = new ArrayList<>(c.getAddedSubList());
					toRequest.removeAll(added);
					request(added);
				}
			}
		});
	}

	private void request(List<? extends Integer> itemIds) {
		int chunk = 200; // chunk size to divide
		List<int[]> chunkedIds = Util.chunkUp(chunk, itemIds);
		for (int[] ids : chunkedIds) {
			infoFunction.apply(ids);
		}
		System.out.println("Request finished");
	}

	private void handleResponse(List<T> toAdds) {
		Map<Integer, T> mapped = new HashMap<>();
		for(T toAdd : toAdds) {
			mapped.put(toAdd.getId(), toAdd);
		}
		synchronized (values) {
			values.putAll(mapped);
		}
	}

	private void onError(GuildWars2Exception e, int[] ids) {
		if (e.getMessage().equals("Exceeded 600 requests per minute limit")) {
			System.err.println("Repeating request " + type);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			infoFunction.apply(ids);
		}
		e.printStackTrace();
	}

	public ObservableMap<Integer, T> getValues() {
		return values;
	}

	public DoubleProperty getProgress() {
		return progress;
	}

	public List<Integer> getIds() {
		return allIds;
	}

	public synchronized ObservableMap<Integer, T> getByIds(List<Integer> itemIds) {
		List<Integer> requested = new ArrayList<>();
		// add items that are not yet in values
		for (Integer id : itemIds) {
			if (!values.containsKey(id) && !alreadyGettingRequested.contains(id)) {
				requested.add(id);
				alreadyGettingRequested.add(id);
			}
		}
		this.toRequest.addAll(requested);
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

}
