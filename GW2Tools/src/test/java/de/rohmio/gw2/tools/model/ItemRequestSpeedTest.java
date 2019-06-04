package de.rohmio.gw2.tools.model;

import java.util.Date;

import org.junit.Test;

import javafx.collections.ObservableMap;
import me.xhsun.guildwars2wrapper.model.v2.Item;

public class ItemRequestSpeedTest {
	
	@Test
	public void getAllItems() {
		
		Data instance = Data.getInstance();
		long start = new Date().getTime();
		ObservableMap<Integer, Item> all = instance.getItems().getAll();
		System.out.println(String.format("Request Time: %d ms", new Date().getTime()-start));
		System.out.println("Size: "+all.size());
	}

}
