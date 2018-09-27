package de.rohmio.gw2.tools.view.recipeTree;

import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.view.RecipeView;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;

public class RecipeTreeViewController extends RecipeView {
	
	public RecipeTreeViewController(Recipe recipe) {
		setStyle("-fx-border-color: black;"
				+ "-fx-border-width: 5;");
		setPadding(new Insets(10.0));
		setSpacing(10.0);
		
		int outputItemId = recipe.getOutputItemId();
		Item outputItem = Data.getInstance().getItemById(outputItemId);
		getChildren().add(new ItemView(outputItem));
		
		HBox hbox_ingredients = new HBox();
		hbox_ingredients.setSpacing(10.0);
		for(Ingredient ingredient : recipe.getIngredients()) {
			int ingredientId = ingredient.getItemId();
			Item ingredientItem = Data.getInstance().getItemById(ingredientId);
			hbox_ingredients.getChildren().add(new ItemView(ingredientItem));
		}
		getChildren().add(hbox_ingredients);
	}

}
