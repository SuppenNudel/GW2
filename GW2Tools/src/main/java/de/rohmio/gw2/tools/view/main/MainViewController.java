package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.rohmio.gw2.tools.App;
import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.model.RequestProgress;
import de.rohmio.gw2.tools.view.RecipeView;
import de.rohmio.gw2.tools.view.recipeTree.RecipeTreeViewController;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;
import me.xhsun.guildwars2wrapper.model.v2.account.Account;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterCraftingLevel;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterCraftingLevel.Discipline;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class MainViewController implements Initializable {

	@FXML
	private ChoiceBox<String> choice_charName;

	@FXML
	private Label lbl_accountName;

	@FXML
	private TextField txt_itemNameFilter;

	@FXML
	private TextField txt_minLevel;

	@FXML // selection for disciplines
	private HBox hbox_disciplineCheck;

	@FXML // all recipes displayed
	private FlowPane scroll_recipes;

	@FXML
	private CheckBox chbx_byableRecipe;

	@FXML
	private CheckBox chbx_showWholeRecipe;
	
	@FXML
	private CheckBox chbx_showAlreadyLearned;

	@FXML // current tasks done by application
	private VBox vbox_tasks;

	@FXML // POC for progress display
	private ProgressBar pb_getItems;

	@FXML
	private ProgressBar pb_getRecipes;

	private Map<CraftingDisciplines, RadioButton> craftingDisceplinesToCheckBox = new HashMap<>();
	private Map<Recipe, RecipeView> recipeViews = new HashMap<>();

	private StringProperty apiKeyProperty;
	
	private ToggleGroup disciplineToggle;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initResourceBundle(resources);
		
		// progress display
		pb_getItems.progressProperty().bind(Data.getInstance().getItemProgress().getProgress());
		pb_getRecipes.progressProperty().bind(Data.getInstance().getRecipeProgress().getProgress());

		// discipline selection
		disciplineToggle = new ToggleGroup();
		for (CraftingDisciplines discipline : CraftingDisciplines.values()) {
			RadioButton radioButton = new RadioButton(discipline.name());
			radioButton.setDisable(true);
			radioButton.setToggleGroup(disciplineToggle);
			hbox_disciplineCheck.getChildren().add(radioButton);
			craftingDisceplinesToCheckBox.put(discipline, radioButton);
		}
		
		txt_minLevel.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				txt_minLevel.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});
		
		apiKeyProperty = Data.getInstance().accessTokenProperty();
		checkApiKey(apiKeyProperty.get());
		apiKeyProperty.addListener((observable, oldValue, newValue) -> checkApiKey(newValue));
		
		choice_charName.setOnAction(event -> {
			try {
				onSelectCharacter();
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	private void initResourceBundle(ResourceBundle resource) {
		chbx_byableRecipe.textProperty().bind(Data.getInstance().getStringBinding("show_buyable_recipes"));
		chbx_showWholeRecipe.textProperty().bind(Data.getInstance().getStringBinding("show_whole_recipe"));
	}

	private void checkApiKey(String apiKey) {
		try {
			Account accountInfo = GuildWars2.getInstance().getSynchronous().getAccountInfo(apiKey);
			String name = accountInfo.getName();
			lbl_accountName.setText(name);
			getCharacters();
		} catch (GuildWars2Exception e) {
			lbl_accountName.setText(e.getMessage());
		}
	}

	@FXML
	private void openSettings() throws IOException {
		Scene scene = App.createScene(SettingsViewController.class);
		Stage stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(App.getStage());
		stage.setScene(scene);
		stage.showAndWait();
	}
	
	@FXML
	private void getCharacters() throws GuildWars2Exception {
		choice_charName.getItems().clear();
		GuildWars2 gw2 = GuildWars2.getInstance();
		List<String> allCharacterName = gw2.getSynchronous().getAllCharacterName(Data.getInstance().getAccessToken());
		choice_charName.getItems().addAll(allCharacterName);
	}
	
	private void onSelectCharacter() throws GuildWars2Exception {
		String characterName = choice_charName.getSelectionModel().getSelectedItem();
		
		System.out.println(String.format("Get character crafting for '%s'", characterName));
		
		CharacterCraftingLevel characterCrafting = GuildWars2.getInstance().getSynchronous()
				.getCharacterCrafting(Data.getInstance().getAccessToken(), characterName);
		
		// reset all radio buttons
		for(CraftingDisciplines discipline : CraftingDisciplines.values()) {
			RadioButton box = craftingDisceplinesToCheckBox.get(discipline);
			box.setDisable(true);
			box.setSelected(false);
			box.setText(discipline.name());
		}
		// touch radio buttons that are necessary
		for(Discipline discipline : characterCrafting.getCrafting()) {
			RadioButton radioButton = craftingDisceplinesToCheckBox.get(discipline.getDiscipline());
			radioButton.setDisable(!discipline.isActive());
			radioButton.setText(discipline.getDiscipline().name());
		}
		
		try {
			compareRecipes();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private List<Integer> charRecipes;

	private void compareRecipes() throws GuildWars2Exception, IOException, InterruptedException {
		// clear previous
		scroll_recipes.getChildren().clear();
		recipeViews.clear();

		Thread thread = new Thread(() -> {
			try {
				// get ALL recipes
				System.out.println("Getting all recipes");
				RequestProgress<Recipe> recipeProgress = Data.getInstance().getRecipeProgress().getAll();
				
				System.out.println("waiting..");
				while(recipeProgress.getProgress().get() < 1.0) {
					try {
						System.out.print(".");
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("All recipes received");
				
				List<Recipe> allRecipes = new ArrayList<>(recipeProgress.values());
			
				// get recipes selected character has already learned
				Character character = GuildWars2.getInstance().getSynchronous().getCharacter(Data.getInstance().getAccessToken(),
							choice_charName.getSelectionModel().getSelectedItem());
				charRecipes = character.getRecipes();
			
				// get filtered list
				List<Recipe> recipesToShow = allRecipes.stream()
						// remove already learned
						.filter(r -> !charRecipes.contains(r.getId()))
						// only available by discipline and rating
						.filter(r -> {
							for (Discipline discipline : character.getCrafting()) {
								if (r.getDisciplines().contains(discipline.getDiscipline())
										&& r.getMinRating() <= discipline.getRating()) {
									return true;
								}
							}
							return false;
						}).collect(Collectors.toList());
			
				System.out.println("All: " + allRecipes.size());
				System.out.println("Char: " + charRecipes.size());
				System.out.println("To Discover: " + recipesToShow.size());
			
				// fetch all Item information here, so they don't have to be called individually
				List<Integer> itemIds = new ArrayList<>();
				for (Recipe recipe : recipesToShow) {
					itemIds.add(recipe.getOutputItemId());
					List<Integer> ingredientIds = recipe.getIngredients().stream().map(Ingredient::getItemId)
							.collect(Collectors.toList());
					itemIds.addAll(ingredientIds);
				}
				Data.getInstance().getItemProgress().getByIds(itemIds);
			
				// display all discoverable recipes
				for (Recipe recipe : recipesToShow) {
					new Thread(() -> {
						RecipeView recipeView = new RecipeTreeViewController(recipe, chbx_showWholeRecipe.isSelected());
						recipeViews.put(recipe, recipeView);
						recipeView.addFilter(txt_itemNameFilter, txt_minLevel, disciplineToggle, chbx_byableRecipe);
						Platform.runLater(() -> scroll_recipes.getChildren().add(recipeView));
					}).start();
				}
				txt_itemNameFilter.setDisable(false);
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

}
