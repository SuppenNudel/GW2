package de.rohmio.gw2.tools.main;

import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public enum RequestType {
	ITEM(Item.class, "item"), RECIPE(Recipe.class, "recipe");
	
	private Class<?> clazz;
	private String path;
	
	private RequestType(Class<?> clazz, String path) {
		this.clazz = clazz;
		this.path = path;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
	public String getPath() {
		return path;
	}
}
