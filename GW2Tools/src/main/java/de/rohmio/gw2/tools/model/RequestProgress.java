package de.rohmio.gw2.tools.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.rohmio.gw2.tools.main.Util;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.SynchronousRequest;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.identifiable.IdentifiableInt;

public class RequestProgress<T extends IdentifiableInt> {
	
	// double value for how much percentage this data type is loaded
	private DoubleProperty progress = new SimpleDoubleProperty();
	
	private ObservableMap<Integer, T> values = FXCollections.observableHashMap();
	
	// list of all ids available for this data type
	private List<Integer> allIds;

	// function to get all ids of this data type
	private Callable<List<Integer>> idCaller;
	private Function<int[], List<T>> infoFunction;
	
	private RequestType type;

	@SuppressWarnings("unchecked")
	public RequestProgress(RequestType type) {
		this.type = type;
		
		values.addListener(new MapChangeListener<Integer, T>() {
			@Override
			public void onChanged(Change<? extends Integer, ? extends T> change) {
				progress.set(change.getMap().size() / allIds.size());
			}
		});

		SynchronousRequest synchronous = GuildWars2.getInstance().getSynchronous();
		
		switch (type) {
		case RECIPE:
			idCaller = () -> synchronous.getAllRecipeID();
			infoFunction = ids -> {
				try {
					return (List<T>) synchronous.getRecipeInfo(ids);
				} catch (GuildWars2Exception e) {
					return onError(e, ids);
				}
			};
			break;
		case ITEM:
			idCaller = () -> synchronous.getAllItemID();
			infoFunction = ids -> {
				try {
					return (List<T>) synchronous.getItemInfo(ids);
				} catch (GuildWars2Exception e) {
					return onError(e, ids);
				}
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
	
	private List<T> onError(GuildWars2Exception e, int[] ids) {
		if(e.getMessage().equals("Exceeded 600 requests per minute limit")) {
			System.err.println("Repeating request "+type);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return infoFunction.apply(ids);
		}
		e.printStackTrace();
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
		List<Integer> toRequest = new ArrayList<>();
		// add items that are not yet in values
		for(Integer id : itemIds) {
			if(!values.containsKey(id)) {
				toRequest.add(id);
			}
		}
		
		int chunk = 200; // chunk size to divide
		List<int[]> chunkedIds = Util.chunkUp(chunk, toRequest);
		List<Thread> threads = new ArrayList<>();
		for(int[] ids : chunkedIds) {
			Thread thread = new Thread(() -> {
				values.putAll(infoFunction.apply(ids).stream().collect(Collectors.toMap(T::getId, r -> r)));
			}, "Thread "+ids);
			threads.add(thread);
			thread.start();
		}
		threads.forEach(t -> {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println("Request finished");
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
