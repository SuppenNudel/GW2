package de.rohmio.gw2.tools.view;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.rohmio.gw2.tools.App;
import de.rohmio.gw2.tools.model.RecipeFilter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;

public class RecipeView extends AnchorPane {
	
//	private boolean detailed;
	private RecipeFilter recipeFilter;
	
//	private List<ItemView> itemViews = new ArrayList<>();
	
	public RecipeView(RecipeFilter recipeFilter, boolean detailed) {
		
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
		
		HBox hBox = new HBox(createTree(recipeFilter.getRecipe()));

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
			RecipeView recipeTreeView = new RecipeView(recipeFilter, true);
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
		
		// copy chat link for this recipe to clipboard
		MenuItem linkToClipboard = new MenuItem("Copy Link to Clipboard");
		linkToClipboard.setOnAction(event -> {
			String chatLink = recipeFilter.getRecipe().getChatLink();
			Clipboard clipboard = Clipboard.getSystemClipboard();
			ClipboardContent content = new ClipboardContent();
			content.putString(chatLink);
			content.putHtml("<b>Some</b> text");
			clipboard.setContent(content);
		});
		
		// open browser with the wiki article of this recipe
		MenuItem goToWiki = new MenuItem("Go to Wiki");
		goToWiki.setOnAction(event -> {
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

	private VBox createTree(Recipe recipe) {
		VBox root = new VBox();
		root.setPadding(new Insets(10.0));

		root.setPrefWidth(USE_COMPUTED_SIZE);
		root.setPrefHeight(USE_COMPUTED_SIZE);

		root.setStyle("-fx-border-color: black;" + "-fx-border-width: 5;");
		root.setAlignment(Pos.TOP_CENTER);

		root.getChildren().add(new Label(String.valueOf( recipe.getOutputItemId())));
		GridPane grid_data = new GridPane();
		grid_data.add(new Label("Source"), 0, 0);
		grid_data.add(new Label(recipe.getFlags().toString()), 1, 0);
		
		grid_data.add(new Label("Type"), 0, 1);
		grid_data.add(new Label(String.valueOf(recipe.getType())), 1, 1);
		
		grid_data.add(new Label("Output qty."), 0, 2);
		grid_data.add(new Label(String.valueOf(recipe.getOutputItemCount())), 1, 2);
		
		grid_data.add(new Label("Discipline"), 0, 3);
		grid_data.add(new Label(recipe.getDisciplines().toString()), 1, 3);
		
		grid_data.add(new Label("Req. rating"), 0, 4);
		grid_data.add(new Label(String.valueOf(recipe.getMinRating())), 1, 4);
		
		grid_data.add(new Label("Chat link"), 0, 5);
		grid_data.add(new TextField(recipe.getChatLink()), 1, 5);
		
//		ItemView itemView = new ItemView(outputItemId, outputCount, detailed);
//		itemViews.add(itemView);
//		root.getChildren().add(itemView);
		
		root.getChildren().add(grid_data);
		
		root.getChildren().add(new Label("Ingredients"));
		for(Ingredient ingredient : recipe.getIngredients()) {
			root.getChildren().add(new Label(String.format("%dx %d", ingredient.getCount(), ingredient.getItemId())));
		}

		return root;
	}

}
