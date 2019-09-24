package de.rohmio.gw2.tools.model.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.rohmio.gw2.tools.main.Util;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.identifiable.IdentifiableInt;

public class RequestProgress<T extends IdentifiableInt> {

	// double value for how much percentage this data type is loaded
	private DoubleProperty progress = new SimpleDoubleProperty();

	private ObservableMap<Integer, T> values = FXCollections.observableHashMap();

	// list of all ids available for this data type
	private List<Integer> allIds = new ArrayList<>();

	private ObservableList<Integer> toRequest = FXCollections.observableArrayList();
	private List<Integer> alreadyGettingRequested = new ArrayList<>();

	private RequestType type;
	private RequestStrategy<T> requestStrategy;

	@SuppressWarnings("unchecked")
	public RequestProgress(RequestType type) {
		this.type = type;

		progress.bind(Bindings.createDoubleBinding(() -> {
			int valuesSize = values.size();
			int allSize = allIds.size();
			System.out.println("Values: " + valuesSize);
			System.out.println("All: " + allSize);
			return 1.0 * valuesSize / allSize;
		}, values));

		switch (type) {
		case RECIPE:
			requestStrategy = (RequestStrategy<T>) new RecipeRequestStrategy();
			break;
		case ITEM:
			requestStrategy = (RequestStrategy<T>) new ItemRequestStrategy();
			break;
		default:
			break;
		}

		try {
			allIds = requestStrategy.getIds();
		} catch (GuildWars2Exception e) {
			e.printStackTrace();
		}

		toRequest.addListener((ListChangeListener<Integer>) c -> {
			while (c.next()) {
				if (c.wasAdded()) {
					List<Integer> added = new ArrayList<>(c.getAddedSubList());
					toRequest.removeAll(added);
					requestItems(added);
				}
			}
		});
	}

	private void handleResponse(List<T> toAdds) {
		Map<Integer, T> mapped = new HashMap<>();
		for (T toAdd : toAdds) {
			mapped.put(toAdd.getId(), toAdd);
		}
		values.putAll(mapped);
	}

	private void requestItems(List<Integer> itemIds) {
		int chunkSize = 200;
		List<int[]> chunkedIds = Util.chunkUp(chunkSize, itemIds);
		List<Thread> threads = new ArrayList<>();
		for (int[] ids : chunkedIds) {
			Thread thread = new Thread(() -> {
				try {
					List<T> items = requestStrategy.getItems(ids);
					handleResponse(items);
				} catch (GuildWars2Exception e) {
					handleResponse(onError(e, ids));
				}
			}, "Request " + type + " " + itemIds);
			thread.start();
			threads.add(thread);
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private List<T> onError(GuildWars2Exception e, int[] ids) {
		if (e.getMessage().equals("Exceeded 600 requests per minute limit")) {
			System.err.println("Repeating request " + type);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
				return requestStrategy.getItems(ids);
			} catch (GuildWars2Exception e1) {
				System.err.println(e1);
				e1.printStackTrace();
				onError(e1, ids);
			}
		} else {
			e.printStackTrace();
			return null;
		}
		return null;
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
