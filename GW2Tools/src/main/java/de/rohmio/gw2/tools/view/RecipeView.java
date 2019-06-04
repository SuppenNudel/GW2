package de.rohmio.gw2.tools.view;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.rohmio.gw2.tools.App;
import de.rohmio.gw2.tools.model.RecipeFilter;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class RecipeView extends AnchorPane {
	
	// TODO either on click or context menu show details and copy link to clipboard

//	private boolean detailed;
	private RecipeFilter recipeFilter;
	
//	private List<ItemView> itemViews = new ArrayList<>();
	private ObservableList<RecipeView> recipeViews;
	
	public RecipeView(Recipe recipe, boolean detailed, ObservableList<RecipeView> recipeViews) {
		this.recipeViews = recipeViews;
		recipeFilter = new RecipeFilter(recipe);
		
		visibleProperty().bind(recipeFilter.getShow()); //.and(Bindings.createBooleanBinding(() -> count() <= 200, recipeViews)));
		managedProperty().bind(recipeFilter.getShow()); //.and(Bindings.createBooleanBinding(() -> count() <= 200, recipeViews)));
		
		/*
		recipeFilter.getShow().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue) {
					showItems(true);
				}
			}
		});
		*/

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
	
//	private long count() {
//		return recipeViews.stream().map(view -> view.getRecipeFilter()).filter(filter -> filter.getShow().get()).count();
//	}

	private ContextMenu createContextMenu() {
		MenuItem showDetailed = new MenuItem("Show Detailed");
		showDetailed.setOnAction(event -> {
			RecipeView recipeTreeView = new RecipeView(recipeFilter.getRecipe(), true, recipeViews);
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
		MenuItem linkToClipboard = new MenuItem("Copy Link to Clipboard");
		linkToClipboard.setOnAction(event -> {
			String chatLink = recipeFilter.getRecipe().getChatLink();
			Clipboard clipboard = Clipboard.getSystemClipboard();
			ClipboardContent content = new ClipboardContent();
			content.putString(chatLink);
			content.putHtml("<b>Some</b> text");
			clipboard.setContent(content);
		});
		
		MenuItem goToWiki = new MenuItem("Go to Wiki");
		goToWiki.setOnAction(event -> {
//			"https://wiki.guildwars2.com/wiki/?search=%5B%26 Cb0RAAA %3D%5D"[&Cb4RAAA=]
			String chatLink = recipeFilter.getRecipe().getChatLink();
			try {
				String url = String.format("https://wiki.guildwars2.com/wiki/?search=%s", URLEncoder.encode(chatLink, "UTF-8"));
				App.app().getHostServices().showDocument(url);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		});
		
		return new ContextMenu(showDetailed, linkToClipboard, goToWiki);
	}

	public RecipeFilter getRecipeFilter() {
		return recipeFilter;
	}
	
//	private boolean itemsShown = false;
	
	/*
	public void showItems(boolean show) {
		if(!itemsShown) {
			itemsShown = true;
			for(ItemView itemView : itemViews) {
				itemView.show(show);
			}
		}
	}
	*/

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
//		ItemView itemView = new ItemView(outputItemId, outputCount, detailed);
//		itemViews.add(itemView);
//		root.getChildren().add(itemView);

		return root;
	}

}
