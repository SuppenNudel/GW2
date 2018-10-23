package de.rohmio.gw2.tools.view.recipeTree;

import java.util.List;

import de.rohmio.gw2.tools.view.RecipeView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterRecipes;

public class RecipeTreeViewController extends RecipeView { // VBox

	public RecipeTreeViewController(Recipe recipe, CharacterRecipes characterRecipes, List<Integer> unlockedRecipes) {
		super(recipe, characterRecipes, unlockedRecipes);

		getChildren().add(createTree(recipe));
	}

	private VBox createTree(Recipe recipe) {
		VBox root = new VBox(10.0);
		setSpacing(10.0);
		setStyle("-fx-border-color: black;" + "-fx-border-width: 5;");
		setAlignment(Pos.TOP_CENTER);
		setPadding(new Insets(10.0));

		int outputItemId = recipe.getOutputItemId();
//		Item outputItem = Data.getInstance().getItemProgress().getById(outputItemId);
		getChildren().add(new ItemView(outputItemId, recipe.getOutputItemCount()));

		HBox hbox_ingredients = new HBox();
		hbox_ingredients.setAlignment(Pos.TOP_CENTER);
		hbox_ingredients.setSpacing(10.0);
		for (Ingredient ingredient : recipe.getIngredients()) {
			int ingredientId = ingredient.getItemId();
			int count = ingredient.getCount();
//			Item ingredientItem = Data.getInstance().getItemProgress().getById(ingredientId);
			/*
			 * if(recursive) { List<Integer> searchRecipes =
			 * GuildWars2.getInstance().getSynchronous().searchRecipes(false, ingredientId);
			 * if(searchRecipes.isEmpty()) { try { ItemView itemView = new
			 * ItemView(ingredientItem, count);
			 * hbox_ingredients.getChildren().add(itemView); } catch (NullPointerException
			 * e) { System.err.println(outputItemId + " not loaded"); } } else { Recipe
			 * subRecipe = Data.getInstance().getRecipeProgress().get(searchRecipes.get(0));
			 * hbox_ingredients.getChildren().add(createTree(subRecipe)); } } else {
			 */
			ItemView itemView = new ItemView(ingredientId, count);
			hbox_ingredients.getChildren().add(itemView);
			// }
		}
		getChildren().add(hbox_ingredients);
		return root;
	}

}
