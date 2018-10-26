package de.rohmio.gw2.tools.view.recipeTree;

import java.util.List;

import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.view.RecipeView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterRecipes;

public class RecipeTreeViewController extends RecipeView { // Anchor Pane

	private boolean detailed;

	public RecipeTreeViewController(Recipe recipe, CharacterRecipes characterRecipes, List<Integer> unlockedRecipes,
			boolean detailed) {
		super(recipe, characterRecipes, unlockedRecipes);

		this.detailed = detailed;
		
		setPrefWidth(USE_COMPUTED_SIZE);
		setPrefHeight(USE_COMPUTED_SIZE);

		HBox hBox = new HBox(createTree(recipe, -1));
		
		hBox.setPrefWidth(USE_COMPUTED_SIZE);
		hBox.setPrefHeight(USE_COMPUTED_SIZE);
		hBox.setAlignment(Pos.TOP_CENTER);
		getChildren().add(hBox);
	}

	private VBox createTree(Recipe recipe, int prevCount) {
		VBox root = new VBox(10.0);
		root.setPadding(new Insets(10.0));
		
		root.setPrefWidth(USE_COMPUTED_SIZE);
		root.setPrefHeight(USE_COMPUTED_SIZE);
		
		root.setStyle("-fx-border-color: black;" + "-fx-border-width: 5;");
		root.setAlignment(Pos.TOP_CENTER);

		int outputCount;
		if(prevCount < 0) {
			outputCount = recipe.getOutputItemCount();
		} else {
			outputCount = prevCount;
		}
		int outputItemId = recipe.getOutputItemId();
		// Item outputItem = Data.getInstance().getItemProgress().getById(outputItemId);
		root.getChildren().add(new ItemView(outputItemId, outputCount, detailed));

		HBox hbox_ingredients = new HBox(10.0);
		hbox_ingredients.setAlignment(Pos.TOP_CENTER);
		hbox_ingredients.setPrefWidth(USE_COMPUTED_SIZE);
		hbox_ingredients.setPrefHeight(USE_COMPUTED_SIZE);
		for (Ingredient ingredient : recipe.getIngredients()) {
			int ingredientId = ingredient.getItemId();
			int count = ingredient.getCount();
			if (detailed) {
				List<Integer> searchRecipes = null;
				try {
					searchRecipes = GuildWars2.getInstance().getSynchronous().searchRecipes(false, ingredientId);
				} catch (GuildWars2Exception e) {
					e.printStackTrace();
				}
				if (searchRecipes != null && !searchRecipes.isEmpty()) {
					Recipe subRecipe = Data.getInstance().getRecipeProgress().get(searchRecipes.get(0));
					hbox_ingredients.getChildren().add(createTree(subRecipe, count));
				} else {
					hbox_ingredients.getChildren().add(new ItemView(ingredientId, count, detailed));
				}
			} else {
				hbox_ingredients.getChildren().add(new ItemView(ingredientId, count, detailed));
			}
		}
		root.getChildren().add(hbox_ingredients);
		return root;
	}

}
