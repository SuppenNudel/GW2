package de.rohmio.gw2.tools.model;

import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public enum RequestType {
	ITEM(Item.class, Item[].class, "item"), RECIPE(Recipe.class, Recipe[].class, "recipe");

	private Class<?> clazz;
	private Class<?> arrClazz;
	private String path;

	private RequestType(Class<?> clazz, Class<?> arrClazz, String path) {
		this.clazz = clazz;
		this.arrClazz = arrClazz;
		this.path = path;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Class<?> getArrClazz() {
		return arrClazz;
	}

	public String getPath() {
		return path;
	}
}
