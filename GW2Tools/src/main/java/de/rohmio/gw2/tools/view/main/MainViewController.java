package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.App;
import de.rohmio.gw2.tools.model.Data;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.account.Account;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterCraftingLevel;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterCraftingLevel.Discipline;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class MainViewController implements Initializable {

	// Begin view elements
	
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

	@FXML
	private TextField txt_maxLevel;

	@FXML // selection for disciplines
	private VBox vbox_disciplineCheck;

	@FXML // all recipes displayed
	private VBox vbox_recipes;

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

	@FXML
	private Label lbl_recipes_progress;
	
	// end view elements

	private Map<CraftingDisciplines, CheckBox> disciplineChecks = new HashMap<>();
	
//	private ObservableList<RecipeView> recipeViews = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// internationalization
		initResourceBundle();
		
		pb_getRecipes.progressProperty().bind(Data.getInstance().getRecipes().getProgress());
//		StringConverter<Number> converter = new PercentageStringConverter();
//		Bindings.bindBidirectional(lbl_recipes_progress.textProperty(), Data.getInstance().getRecipes().getProgress(), converter);
		
		Data.getInstance().getRecipes().getProgress().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		        NumberFormat format = NumberFormat.getPercentInstance();
				Platform.runLater(() -> lbl_recipes_progress.setText(format.format(newValue)));
			}
		});
		
		// discipline selection
		for (CraftingDisciplines discipline : CraftingDisciplines.values()) {
			CheckBox checkbox = new CheckBox(discipline.name());
			checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					System.out.println("filter");
				}
			});
			disciplineChecks.put(discipline, checkbox);
			vbox_disciplineCheck.getChildren().add(checkbox);
			checkbox.setUserData(discipline);
		}

		txt_minLevel.textProperty().addListener(new IntOnlyTextFieldChangeListener(txt_minLevel));
		txt_maxLevel.textProperty().addListener(new IntOnlyTextFieldChangeListener(txt_maxLevel));

		StringProperty apiKeyProperty = Data.getInstance().getSettingsWrapper().accessTokenProperty();
		checkApiKey(apiKeyProperty.get());
		apiKeyProperty.addListener((observable, oldValue, newValue) -> checkApiKey(newValue));

		choice_charName.setOnAction((event) -> onSelectCharacter());
	}

	private void initResourceBundle() {
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
		List<String> allCharacterName = gw2.getSynchronous().getAllCharacterName(Data.getInstance().getSettingsWrapper().accessTokenProperty().get());
		choice_charName.getItems().addAll(allCharacterName);
	}

	// TODO do loading on separate thread, so that the program doesn't freeze when
	// it takes longer
	/**
	 * <ul>
	 * <li>reset filters</li>
	 * <li>start displaying recipes unlockable by the character</li>
	 * </ul>
	 * 
	 * @throws GuildWars2Exception
	 */
	private void onSelectCharacter() {
		String characterName = choice_charName.getSelectionModel().getSelectedItem();
		if (characterName == null) {
			return;
		}

		try {
			resetFilters();

			/**
			 * An array containing an entry for each crafting discipline the character has
			 * unlocked
			 */
			System.out.println(String.format("Get character crafting for '%s'", characterName));
			CharacterCraftingLevel characterCrafting = GuildWars2.getInstance().getSynchronous()
					.getCharacterCrafting(Data.getInstance().getSettingsWrapper().accessTokenProperty().get(), characterName);

			/**
			 * information about recipes that are unlocked for an account <br>
			 * an array, each value being the ID of a recipe that can be resolved against
			 * /v2/recipes <br>
			 * mostly learned from item
			 */
//			List<Integer> unlockedRecipes = GuildWars2.getInstance().getSynchronous()
//					.getUnlockedRecipes(Data.getInstance().getAccessToken());

			/**
			 * An array containing an entry for each crafting discipline the character has
			 * unlocked
			 */
//			CharacterRecipes characterRecipes = GuildWars2.getInstance().getSynchronous()
//					.getCharacterUnlockedRecipes(Data.getInstance().getAccessToken(), characterName);

			for (Discipline discipline : characterCrafting.getCrafting()) {
				CheckBox checkBox = disciplineChecks.get(discipline.getDiscipline());
				checkBox.setText(String.format("%s - %d", checkBox.getUserData(), discipline.getRating()));
				checkBox.setSelected(true);
			}
		} catch (GuildWars2Exception e) {
			e.printStackTrace();
		}
	}

	private void resetFilters() {
		System.out.println("Reset Filter");
		for (CheckBox diciplineCheck : disciplineChecks.values()) {
			diciplineCheck.setSelected(false);
			Object userData = diciplineCheck.getUserData();
			if (userData instanceof CraftingDisciplines) {
				CraftingDisciplines craftingDiscipline = (CraftingDisciplines) userData; // discipline of the current
																							// button

				String buttonText = craftingDiscipline.name();
				diciplineCheck.setText(buttonText);
			} else {
				throw new ClassCastException("user data is not a CraftingDiscipline");
			}
		}

		// TODO here some of the init functions could be used for resetting the filters
	}

//	private RecipeView createRecipeView(Recipe recipe) {
//		RecipeView recipeView = new RecipeView(recipe, false);
//		recipeViews.add(recipeView);
//		Platform.runLater(() -> vbox_recipes.getChildren().add(recipeView));
//		return recipeView;
//	}

}
