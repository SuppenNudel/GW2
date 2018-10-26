package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

	private ToggleGroup disciplineToggle;

	private CharacterRecipes characterRecipes;
	private List<Integer> unlockedRecipes;
	private CharacterCraftingLevel characterCrafting;

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

		characterCrafting = GuildWars2.getInstance().getSynchronous()
				.getCharacterCrafting(Data.getInstance().getAccessToken(), characterName);

		// reset
		for (Toggle toggle : disciplineToggle.getToggles()) {
			if (toggle instanceof RadioButton) {
				RadioButton radio = (RadioButton) toggle;
				Object userData = radio.getUserData();
				if (userData instanceof CraftingDisciplines) {
					CraftingDisciplines craftingDiscipline = (CraftingDisciplines) userData;
					radio.setText(craftingDiscipline.name());
					radio.setDisable(true);
				} else {
					throw new ClassCastException("user data is not a CraftingDiscipline");
				}
			} else {
				throw new ClassCastException("toggle is not a radio button");
			}
		}

		// set
		for (Toggle toggle : disciplineToggle.getToggles()) {
			if (toggle instanceof RadioButton) {
				RadioButton radio = (RadioButton) toggle;
				Object userData = radio.getUserData();
				if (userData instanceof CraftingDisciplines) {
					CraftingDisciplines craftingDiscipline = (CraftingDisciplines) userData;
					for (Discipline discipline : characterCrafting.getCrafting()) {
						if (discipline.getDiscipline() == craftingDiscipline) {
							radio.setDisable(!discipline.isActive());
							radio.setText(discipline.getDiscipline().name() + " - " + discipline.getRating());
						}
					}
				} else {
					throw new ClassCastException("user data is not a CraftingDiscipline");
				}
			} else {
				throw new ClassCastException("toggle is not a radio button");
			}
		}

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

	private void compareRecipes() throws GuildWars2Exception, IOException, InterruptedException {
		// clear previous
		scroll_recipes.getChildren().clear();

		Thread thread = new Thread(() -> {
			// get ALL recipes
			System.out.println("Getting all recipes");
			RequestProgress<Recipe> recipeProgress = Data.getInstance().getRecipeProgress().getAll();

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
			System.out.println("All Recipe Count: "+allRecipes.size());
			
			List<Recipe> filteredRecipes = allRecipes.stream().filter(r -> {
				for (Discipline discipline :  characterCrafting.getCrafting()) {
					if (r.getDisciplines().contains(discipline.getDiscipline())
							&& r.getMinRating() <= discipline.getRating()) {
						return true;
					}
				}
				return false;
			}).collect(Collectors.toList());
			System.out.println("Filtered by discipline and rating Recipe Count: "+filteredRecipes.size());

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
            while(!itemProgress.values().stream().map(i -> i.getId()).collect(Collectors.toList()).containsAll(itemIds)) {
                try {
                    System.out.print(".");
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("All needed items received");
            
			// display all discoverable recipes
			for (Recipe recipe : filteredRecipes) {
				Thread recipeViewThread = new Thread(() -> {
					RecipeView recipeView = new RecipeTreeViewController(recipe, characterRecipes, unlockedRecipes, false);
					recipeView.addItemNameFilter(txt_itemNameFilter.textProperty());
					recipeView.addRecipeLevelFilter(txt_minLevel.textProperty());
					recipeView.addDisciplineFilter(disciplineToggle.selectedToggleProperty());
					recipeView.addByableRecipeFilter(chbx_byableRecipe.selectedProperty());
					recipeView.addAlreadyLearnedFilter(chbx_showAlreadyLearned.selectedProperty());
					// chbx_showWholeRecipe.selectedProperty());
					recipeView.handleFilter(true);
					Platform.runLater(() -> scroll_recipes.getChildren().add(recipeView));
				}, "Thread - create recipe view for " + recipe.getId());
				recipeViewThread.setDaemon(true);
				recipeViewThread.start();
			}
			txt_itemNameFilter.setDisable(false);
		}, "Thread - Load all Recipes");
		thread.setDaemon(true);
		thread.start();
	}

}
