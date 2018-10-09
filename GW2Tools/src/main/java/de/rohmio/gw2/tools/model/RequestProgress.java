package de.rohmio.gw2.tools.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

@SuppressWarnings("serial")
public class RequestProgress<T> extends HashMap<Integer, T> {

	private DoubleProperty progress = new SimpleDoubleProperty();
	private List<Integer> allIds;

	public RequestProgress(List<Integer> ids) {
		this.allIds = ids;
	}

	public DoubleProperty getProgress() {
		return progress;
	}

	public List<Integer> getIds() {
		return allIds;
	}
	
	private void updateProgress() {
		synchronized (progress) {
			progress.set(1.0 * size() / allIds.size());
		}
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

}
