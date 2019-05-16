package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.rohmio.gw2.tools.App;
import de.rohmio.gw2.tools.main.Data;
import de.rohmio.gw2.tools.main.RequestProgress;
import de.rohmio.gw2.tools.model.RecipeFilter;
import de.rohmio.gw2.tools.view.recipeTree.RecipeTreeViewController;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;
import me.xhsun.guildwars2wrapper.model.v2.account.Account;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterCraftingLevel;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterCraftingLevel.Discipline;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterRecipes;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class MainViewController implements Initializable {

	@FXML
	private ChoiceBox<String> choice_charName;
	
	@FXML
	private CheckBox chbx_pauseFilter;

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

	@FXML
	private Label lbl_currentlyDisplayed;

	@FXML // current tasks done by application
	private VBox vbox_tasks;

	@FXML // POC for progress display
	private ProgressBar pb_getItems;

	@FXML
	private ProgressBar pb_getRecipes;

	private ToggleGroup disciplineToggle;

	/**
	 * probably recipes unlocked by a character
	 */
	private CharacterRecipes characterRecipes;
	
	/**
	 * information about recipes that are unlocked for an account
	 * <br>
	 * an array, each value being the ID of a recipe that can be resolved against /v2/recipes
	 * <br>
	 * mostly learned from item
	 */
	private List<Integer> unlockedRecipes;
	
	/**
	 * An array containing an entry for each crafting discipline the character has unlocked
	 */
	private CharacterCraftingLevel characterCrafting;

	private ObservableSet<Recipe> recipesToDisplay = FXCollections.observableSet();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initResourceBundle(resources);

		// progress display
		pb_getItems.progressProperty().bind(Data.getInstance().getItemProgress().getProgress());
		pb_getRecipes.progressProperty().bind(Data.getInstance().getRecipeProgress().getProgress());

		Thread thread = new Thread(() -> {
			Data.getInstance().getRecipeProgress().getAll();
		});
		thread.setDaemon(true);
		thread.start();

		// discipline selection
		disciplineToggle = new ToggleGroup();
		for (CraftingDisciplines discipline : CraftingDisciplines.values()) {
			RadioButton radioButton = new RadioButton(discipline.name());
			radioButton.setToggleGroup(disciplineToggle);
			hbox_disciplineCheck.getChildren().add(radioButton);
			radioButton.setUserData(discipline);
			// disable property bind to character crafting active
		}

		txt_minLevel.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				txt_minLevel.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});

		StringProperty apiKeyProperty = Data.getInstance().accessTokenProperty();
		checkApiKey(apiKeyProperty.get());
		apiKeyProperty.addListener((observable, oldValue, newValue) -> checkApiKey(newValue));

		choice_charName.setOnAction(event -> {
			try {
				onSelectCharacter();
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		});
		
		chbx_pauseFilter.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			if(!newValue) { // got deselected
				try {
					compareRecipes();
				} catch (GuildWars2Exception | IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		recipesToDisplay.addListener((SetChangeListener<Recipe>) change -> {
			if (change.wasAdded() && !chbx_pauseFilter.isSelected()) {
				Recipe recipe1 = change.getElementAdded();
				Platform.runLater(() -> {
					RecipeTreeViewController view1 = new RecipeTreeViewController(recipe1, false);
					scroll_recipes.getChildren().add(view1);
				});
			} else if (change.wasRemoved()) {
				Recipe recipe2 = change.getElementRemoved();
				Platform.runLater(() -> {
					RecipeTreeViewController toRemove = null;
					for (Node node : scroll_recipes.getChildren()) {
						if (node instanceof RecipeTreeViewController) {
							RecipeTreeViewController view2 = (RecipeTreeViewController) node;
							if (view2.getRecipe() == recipe2) {
								toRemove = view2;
								break;
							}
						}
					}
					scroll_recipes.getChildren().remove(toRemove);
				});
			} else {
				System.err.println("Change on nothing added or removed");
			}
			lbl_currentlyDisplayed.setText("Recipes: " + recipesToDisplay.size());
		});
	}

	private void initResourceBundle(ResourceBundle resource) {
		chbx_byableRecipe.textProperty().bind(Data.getInstance().getStringBinding("show_buyable_recipes"));
		chbx_showWholeRecipe.textProperty().bind(Data.getInstance().getStringBinding("show_whole_recipe"));
		chbx_showAlreadyLearned.textProperty().bind(Data.getInstance().getStringBinding("show_already_learned"));
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
	
	// TODO do loading on separate thread, so that the program doesn't freeze when it takes longer
	/**
	 * <ul>
	 * 	<li>reset filters</li>
	 * 	<li>start displaying recipes unlockable by the character</li>
	 * </ul>
	 * @throws GuildWars2Exception
	 */
	@FXML
	private void onSelectCharacter() throws GuildWars2Exception {
		String characterName = choice_charName.getSelectionModel().getSelectedItem();
		if (characterName == null) {
			return;
		}

		System.out.println(String.format("Get character crafting for '%s'", characterName));
		characterCrafting = GuildWars2.getInstance().getSynchronous()
				.getCharacterCrafting(Data.getInstance().getAccessToken(), characterName);

		resetFilters();
		
		unlockedRecipes = GuildWars2.getInstance().getSynchronous()
				.getUnlockedRecipes(Data.getInstance().getAccessToken());
		characterRecipes = GuildWars2.getInstance().getSynchronous()
				.getCharacterUnlockedRecipes(Data.getInstance().getAccessToken(), characterName);

		try {
			compareRecipes();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void resetFilters() {
		for (Toggle toggle : disciplineToggle.getToggles()) {
			if (toggle instanceof RadioButton) {
				RadioButton radio = (RadioButton) toggle;
				Object userData = radio.getUserData();
				if (userData instanceof CraftingDisciplines) {
					CraftingDisciplines craftingDiscipline = (CraftingDisciplines) userData; // discipline of the current button
					
					// default
					boolean disableButton = true;
					String buttonText = craftingDiscipline.name();
					
					// check if the selected character has information about the discipline
					for (Discipline discipline : characterCrafting.getCrafting()) {
						if (discipline.getDiscipline() == craftingDiscipline) {
							disableButton = !discipline.isActive();
							buttonText = buttonText + " - " + discipline.getRating();
							break; // don't look further
						}
					}
					radio.setDisable(disableButton);
					radio.setText(buttonText);
				} else {
					throw new ClassCastException("user data is not a CraftingDiscipline");
				}
			} else {
				throw new ClassCastException("toggle is not a radio button");
			}
		}
		
		// TODO here some of the init functions could be used for resetting the filters
	}


	/*
	 * TODO don't clear recipes; instead add or remove recipes according to filter changes
	 * so only go through recipes that are currently filtered out if the filter gets looser
	 * and only go through recipes that are currently displayed if the filter gets harsher
	 */
	/**
	 * @throws GuildWars2Exception
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void compareRecipes() throws GuildWars2Exception, IOException, InterruptedException {
		// clear previous
		scroll_recipes.getChildren().clear();

		// get ALL recipes
		System.out.println("Getting all recipes");
		RequestProgress<Recipe> recipeProgress = Data.getInstance().getRecipeProgress().getAll();

		// wait until all recipes are loaded
		while (recipeProgress.getProgress().get() < 1.0) {
			try {
				System.out.println(recipeProgress.getProgress().get());
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(recipeProgress.getProgress().get());
		System.out.println("All recipes received");

		List<Recipe> allRecipes = new ArrayList<>(recipeProgress.values());
		System.out.println("All Recipe Count: " + allRecipes.size());

		List<Recipe> filteredRecipes = allRecipes.stream().filter(recipe -> {
			for (Discipline charDiscipline : characterCrafting.getCrafting()) {
				// check if character is able to craft this
				if (recipe.getDisciplines().contains(charDiscipline.getDiscipline())
						&& recipe.getMinRating() <= charDiscipline.getRating()) {
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList());
		int size = filteredRecipes.size();
		System.out.println("Filtered by discipline and rating Recipe Count: " + size);

		// fetch all Item information here, so they don't have to be called individually
		List<Integer> itemIds = new ArrayList<>();
		for (Recipe recipe : filteredRecipes) {
			itemIds.add(recipe.getOutputItemId());
			List<Integer> ingredientIds = recipe.getIngredients().stream().map(Ingredient::getItemId)
					.collect(Collectors.toList());
			itemIds.addAll(ingredientIds);
		}

		RequestProgress<Item> itemProgress = Data.getInstance().getItemProgress().getByIds(itemIds);

		System.out.println("waiting..");
		while (!itemProgress.values().stream().map(i -> i.getId()).collect(Collectors.toList()).containsAll(itemIds)) {
			try {
				System.out.print(".");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("All needed items received");

		createRecipeViews(filteredRecipes);
	}

	private void createRecipeViews(List<Recipe> recipes) {
		recipesToDisplay.clear();
		for (Recipe recipe : recipes) {
			RecipeFilter recipeFilter = new RecipeFilter(recipe, characterRecipes, unlockedRecipes);

			recipeFilter.addItemNameFilter(txt_itemNameFilter.textProperty());
			recipeFilter.addRecipeLevelFilter(txt_minLevel.textProperty());
			recipeFilter.addDisciplineFilter(disciplineToggle.selectedToggleProperty());
			recipeFilter.addByableRecipeFilter(chbx_byableRecipe.selectedProperty());
			recipeFilter.addAlreadyLearnedFilter(chbx_showAlreadyLearned.selectedProperty());

			recipeFilter.displayProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
				if (newValue) { // if display
					recipesToDisplay.add(recipe);
				} else {
					recipesToDisplay.remove(recipe);
				}
			});

			recipeFilter.handleFilter(true);
		}
	}

}
