package de.rohmio.gw2.tools.view;

import java.util.ArrayList;
import java.util.List;

import de.rohmio.gw2.tools.model.RecipeFilter;
import de.rohmio.gw2.tools.view.recipeTree.ItemView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class RecipeView extends AnchorPane {

	private boolean detailed;
	private RecipeFilter recipeFilter;
	
	private List<ItemView> itemViews = new ArrayList<>();
	
	public RecipeView(Recipe recipe, boolean detailed) {
		recipeFilter = new RecipeFilter(recipe);
		
		visibleProperty().bind(recipeFilter.getShow());
		managedProperty().bind(recipeFilter.getShow());
		
		recipeFilter.getShow().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue) {
					showItems(true);
				}
			}
		});

		setPrefWidth(USE_COMPUTED_SIZE);
		setPrefHeight(USE_COMPUTED_SIZE);
		
		HBox hBox = new HBox(createTree(recipeFilter.getRecipe(), -1));

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
			RecipeView recipeTreeView = new RecipeView(recipeFilter.getRecipe(), true);
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

	public RecipeFilter getRecipeFilter() {
		return recipeFilter;
	}
	
	private boolean itemsShown = false;
	
	public void showItems(boolean show) {
		if(!itemsShown) {
			itemsShown = true;
			for(ItemView itemView : itemViews) {
				itemView.show(show);
			}
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

		return root;
	}

}
