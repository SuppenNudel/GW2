package de.rohmio.gw2.tools.view;

import de.rohmio.gw2.tools.view.recipeTree.RecipeTreeViewController;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public abstract class RecipeView extends AnchorPane {

	private Recipe recipe;

	public RecipeView(Recipe recipe) {
		this.recipe = recipe;
		
		ContextMenu contextMenu = createContextMenu();
		Node owner = this;
		setOnContextMenuRequested(event -> {
			contextMenu.show(owner, event.getScreenX(), event.getScreenY());
		});
	}
	
	private ContextMenu createContextMenu() {
		MenuItem showDetailed = new MenuItem("Show Detailed");
		showDetailed.setOnAction(event -> {
			RecipeTreeViewController recipeTreeView = new RecipeTreeViewController(recipe, true);
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

	public void show(boolean show) {
		setVisible(show);
		setManaged(show);
	}

}
