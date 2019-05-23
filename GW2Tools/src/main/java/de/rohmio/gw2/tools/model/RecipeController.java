package de.rohmio.gw2.tools.model;

import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class RecipeController {
	
	private Recipe recipe;
	private boolean show;
	
	public RecipeController(Recipe recipe) {
		this.recipe = recipe;
	}
	
	public void onShow() {
		// add recipe to view
	}
	
	public void onHide() {
		// remove recipe from view
	}

}
