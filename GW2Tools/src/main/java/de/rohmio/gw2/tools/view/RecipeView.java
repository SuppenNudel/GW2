package de.rohmio.gw2.tools.view;

import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public abstract class RecipeView extends VBox {
	
	private Recipe recipe;
	
	public RecipeView(Recipe recipe) {
		this.recipe = recipe;
	}
	
	public Recipe getRecipe() {
		return recipe;
	}

}
