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
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;

public class RecipeTreeViewController extends RecipeView {
	
	private boolean recursive;
	
	public RecipeTreeViewController(Recipe recipe, boolean recursive) {
		super(recipe);
		
		this.recursive = recursive;
		getChildren().add(createTree(recipe));
	}
	
	private VBox createTree(Recipe recipe) {
		VBox root = new VBox(10.0);
		root.setStyle("-fx-border-color: black;"
				+ "-fx-border-width: 5;");
		root.setAlignment(Pos.TOP_CENTER);
		root.setPadding(new Insets(10.0));
		
		int outputItemId = recipe.getOutputItemId();
		Item outputItem = Data.getInstance().getItemProgress().getById(outputItemId);
		root.getChildren().add(new ItemView(outputItem, recipe.getOutputItemCount()));
		
		HBox hbox_ingredients = new HBox();
		hbox_ingredients.setAlignment(Pos.TOP_CENTER);
		hbox_ingredients.setSpacing(10.0);
		for(Ingredient ingredient : recipe.getIngredients()) {
			try {
				int ingredientId = ingredient.getItemId();
				int count = ingredient.getCount();
				Item ingredientItem = Data.getInstance().getItemProgress().getById(ingredientId);
				if(recursive) {
					List<Integer> searchRecipes = GuildWars2.getInstance().getSynchronous().searchRecipes(false, ingredientId);
					if(searchRecipes.isEmpty()) {
						ItemView itemView = new ItemView(ingredientItem, count);
						hbox_ingredients.getChildren().add(itemView);
					} else {
						Recipe subRecipe = Data.getInstance().getRecipeProgress().get(searchRecipes.get(0));
						hbox_ingredients.getChildren().add(createTree(subRecipe));	
					}
				} else {
					ItemView itemView = new ItemView(ingredientItem, count);
					hbox_ingredients.getChildren().add(itemView);
				}
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		}
		root.getChildren().add(hbox_ingredients);
		return root;
	}

}
