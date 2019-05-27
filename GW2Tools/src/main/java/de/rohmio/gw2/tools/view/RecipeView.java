package de.rohmio.gw2.tools.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.rohmio.gw2.tools.view.recipeTree.ItemView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeView extends AnchorPane {

	private Recipe recipe;
	private boolean detailed;
	
	private List<ItemView> itemViews = new ArrayList<>();
	
	public RecipeView(Recipe recipe, boolean detailed) {
		this.recipe = recipe;
		
		show(false);

		setPrefWidth(USE_COMPUTED_SIZE);
		setPrefHeight(USE_COMPUTED_SIZE);
		
		HBox hBox = new HBox(createTree(recipe, -1));

		hBox.setPrefWidth(USE_COMPUTED_SIZE);
		hBox.setPrefHeight(USE_COMPUTED_SIZE);
		hBox.setAlignment(Pos.TOP_CENTER);
		getChildren().add(hBox);
		
		ContextMenu contextMenu = createContextMenu();
		Node owner = this;
		setOnContextMenuRequested(event -> {
			contextMenu.show(owner, event.getScreenX(), event.getScreenY());
		});
	}

	private ContextMenu createContextMenu() {
		MenuItem showDetailed = new MenuItem("Show Detailed");
		showDetailed.setOnAction(event -> {
			RecipeView recipeTreeView = new RecipeView(recipe, true);
			ScrollPane scrollPane = new ScrollPane(recipeTreeView);
			scrollPane.setPrefHeight(USE_COMPUTED_SIZE);
			scrollPane.setPrefWidth(USE_COMPUTED_SIZE);
			Scene scene = new Scene(scrollPane);
			Stage stage = new Stage();
			stage.setWidth(600);
			stage.setHeight(400);
			stage.setScene(scene);
			stage.show();
		});
		return new ContextMenu(showDetailed);
	}

	public Recipe getRecipe() {
		return recipe;
	}
	
	private void show(boolean show) {
		setVisible(show);
		setManaged(show);
		if(show) {
			showItems(show);
		}
	}
	
	public void showItems(boolean show) {
		for(ItemView itemView : itemViews) {
			itemView.show(show);
		}
	}

	public void addDisciplineFilter(Map<CraftingDisciplines, CheckBox> disciplineChecks) {
		// only check for those disciplines that are relevant for this Recipe
		for(CraftingDisciplines discipline : recipe.getDisciplines()) {
			CheckBox checkBox = disciplineChecks.get(discipline);
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if(newValue) {
						show(true);
					} else {
						for(CraftingDisciplines discipline : recipe.getDisciplines()) {
							CheckBox checkBox = disciplineChecks.get(discipline);
							if(checkBox.isSelected()) {
								show(true);
								return;
							}
						}
						show(false);
					}
				}
			});
		}
	}

	private VBox createTree(Recipe recipe, int prevCount) {
		VBox root = new VBox(10.0);
		root.setPadding(new Insets(10.0));

		root.setPrefWidth(USE_COMPUTED_SIZE);
		root.setPrefHeight(USE_COMPUTED_SIZE);

		root.setStyle("-fx-border-color: black;" + "-fx-border-width: 5;");
		root.setAlignment(Pos.TOP_CENTER);

		int outputCount;
		if (prevCount < 0) {
			outputCount = recipe.getOutputItemCount();
		} else {
			outputCount = prevCount;
		}
		int outputItemId = recipe.getOutputItemId();
		root.getChildren().add(new Label(String.format("%dx %d", outputCount, outputItemId)));
		root.getChildren().add(new Label(recipe.getDisciplines().toString()));
		ItemView itemView = new ItemView(outputItemId, outputCount, detailed);
		itemViews.add(itemView);
		root.getChildren().add(itemView);

		/*
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
					Recipe subRecipe = Data.getInstance().getRecipes().getById(searchRecipes.get(0));
					hbox_ingredients.getChildren().add(createTree(subRecipe, count));
				} else {
					hbox_ingredients.getChildren().add(new ItemView(ingredientId, count, detailed));
				}
			} else {
				hbox_ingredients.getChildren().add(new ItemView(ingredientId, count, detailed));
			}
		}
		root.getChildren().add(hbox_ingredients);
		 */
		return root;
	}

}
