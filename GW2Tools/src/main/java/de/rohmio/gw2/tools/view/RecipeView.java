package de.rohmio.gw2.tools.view;

import java.util.Map;

import de.rohmio.gw2.tools.view.recipeTree.RecipeTreeViewController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public abstract class RecipeView extends AnchorPane {

	private Recipe recipe;
	
	private BooleanProperty show = new SimpleBooleanProperty(false);

	public RecipeView(Recipe recipe) {
		this.recipe = recipe;
		
		visibleProperty().bind(show);
		managedProperty().bind(show);

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

	public void addDisciplineFilter(Map<CraftingDisciplines, CheckBox> disciplineChecks) {
		BooleanProperty selected = new SimpleBooleanProperty(false);
		for(CraftingDisciplines discipline : recipe.getDisciplines()) {
			CheckBox checkBox = disciplineChecks.get(discipline);
			selected.or(checkBox.selectedProperty());
		}
		show.bind(selected);
	}

}
