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
import de.rohmio.gw2.tools.model.Settings;
import de.rohmio.gw2.tools.view.RecipeView;
import de.rohmio.gw2.tools.view.recipeTree.RecipeTreeViewController;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Flag;
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
	private Button btn_analyse;

	@FXML
	private TextField txt_filter;

	@FXML
	private TextField txt_minLevel;

	@FXML // selection for disciplines
	private HBox hbox_disciplineCheck;

	@FXML // all recipes displayed
	private FlowPane scroll_recipes;

	@FXML
	private CheckBox chbx_fromRecipe;

	@FXML
	private CheckBox chbx_recipesRecursively;

	@FXML // current tasks done by application
	private VBox vbox_tasks;

	@FXML // POC for progress display
	private ProgressBar pb_getItems;

	@FXML
	private ProgressBar pb_getRecipes;

	private Map<CraftingDisciplines, RadioButton> craftingDisceplinesToCheckBox = new HashMap<>();
	private Map<Recipe, RecipeView> recipeViews = new HashMap<>();

	private StringProperty apiKeyProperty;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Data.getInstance().getRecipeProgress().getProgress().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> {
			if (newValue.doubleValue() < 1.0) {
				btn_analyse.setDisable(true);
			} else {
				btn_analyse.setDisable(false);
			}
		});

		// progress display
		pb_getItems.progressProperty().bind(Data.getInstance().getItemProgress().getProgress());
		pb_getRecipes.progressProperty().bind(Data.getInstance().getRecipeProgress().getProgress());

		ToggleGroup toggleGroup = new ToggleGroup();
		// discipline selection
		for (CraftingDisciplines discipline : CraftingDisciplines.values()) {
			RadioButton radioButton = new RadioButton(discipline.name());
			radioButton.setToggleGroup(toggleGroup);
			craftingDisceplinesToCheckBox.put(discipline, radioButton);
			hbox_disciplineCheck.getChildren().add(radioButton);
		}

		// filters
		txt_filter.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> filter());
		txt_minLevel.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> filter());

		apiKeyProperty = Settings.getInstance().getApiKeyProperty();
		checkApiKey(apiKeyProperty.get());
		apiKeyProperty.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
			checkApiKey(newValue);
		});
		
		choice_charName.setOnAction(event -> {
			try {
				selectActiveDisciplines();
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void checkApiKey(String apiKey) {
		try {
			Account accountInfo = GuildWars2.getInstance().getSynchronous().getAccountInfo(apiKey);
			String name = accountInfo.getName();
			lbl_accountName.setText(name);
			btn_analyse.setDisable(false);
			getCharacters();
		} catch (GuildWars2Exception e) {
			lbl_accountName.setText(e.getMessage());
			btn_analyse.setDisable(false);
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
	
	private Map<String, CharacterCraftingLevel> characterCraftingMap = new HashMap<>();
	
	@FXML
	private void getCharacters() throws GuildWars2Exception {
		choice_charName.getItems().clear();
		characterCraftingMap.clear();

		GuildWars2 gw2 = GuildWars2.getInstance();
		List<String> allCharacterName = gw2.getSynchronous().getAllCharacterName(Settings.getInstance().getApiKey());
		choice_charName.getItems().addAll(allCharacterName);
	}
	
	private void selectActiveDisciplines() throws GuildWars2Exception {
		String name = choice_charName.getSelectionModel().getSelectedItem();
		CharacterCraftingLevel characterCrafting = characterCraftingMap.get(name);
		if(characterCrafting == null) {
			characterCrafting = GuildWars2.getInstance().getSynchronous()
					.getCharacterCrafting(Settings.getInstance().getApiKey(), name);
			characterCraftingMap.put(name, characterCrafting);
		}
		for(CraftingDisciplines discipline : CraftingDisciplines.values()) {
			RadioButton box = craftingDisceplinesToCheckBox.get(discipline);
			box.setDisable(true);
			box.setSelected(false);
			box.setText(discipline.name());
		}
		for(Discipline discipline : characterCrafting.getCrafting()) {
			RadioButton radioButton = craftingDisceplinesToCheckBox.get(discipline.getDiscipline());
			radioButton.setDisable(!discipline.isActive());
			radioButton.setText(discipline.getDiscipline().name() + " - "+discipline.getRating());
		}
	}

	@FXML
	private void analyse() throws GuildWars2Exception, IOException, InterruptedException {
		btn_analyse.setDisable(true);

		// clear previous analysation
		scroll_recipes.getChildren().clear();
		recipeViews.clear();

		Thread thread = new Thread(() -> {
			try {
				// get ALL recipes
				RequestProgress<Recipe> recipeProgress = Data.getInstance().getRecipeProgress().getAll();
				List<Recipe> allRecipes = new ArrayList<>(recipeProgress.values());
			
				// get recipes selected character has already learned
				Character character;
					character = GuildWars2.getInstance().getSynchronous().getCharacter(Settings.getInstance().getApiKey(),
							choice_charName.getSelectionModel().getSelectedItem());
				List<Integer> charRecipes = character.getRecipes();
			
				// get filtered list
				List<Recipe> recipesToShow = allRecipes.stream()
						// remove already learned
						.filter(r -> !charRecipes.contains(r.getId()))
						// remove only available through item
						.filter(r -> chbx_fromRecipe.isSelected() || !r.getFlags().contains(Flag.LearnedFromItem))
						// only disciplines that are checked
						.filter(r -> {
							for (CraftingDisciplines discipline : r.getDisciplines()) {
								if (craftingDisceplinesToCheckBox.get(discipline).isSelected()) {
									return true;
								}
							}
							return false;
						})
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
					RecipeView recipeView = new RecipeTreeViewController(recipe, chbx_recipesRecursively.isSelected());
					recipeViews.put(recipe, recipeView);
					Platform.runLater(() -> scroll_recipes.getChildren().add(recipeView));
				}
				btn_analyse.setDisable(false);
				txt_filter.setDisable(false);
			
				filter();

			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void filter() {
		String filterText = txt_filter.getText();
		for (Recipe recipe : recipeViews.keySet()) {
			Item outputItem = Data.getInstance().getItemProgress().getById(recipe.getOutputItemId());
			String outputItemName = outputItem.getName();

			String compoundNames = outputItemName;
			for (Ingredient ingredient : recipe.getIngredients()) {
				compoundNames = compoundNames + " "
						+ Data.getInstance().getItemProgress().getById(ingredient.getItemId()).getName();
			}

			boolean txtFilter = true;
			String[] split = filterText.toLowerCase().split(" ");
			for (String part : split) {
				boolean contains;
				if (part.startsWith("!")) {
					String substring = part.substring(1);
					if (substring.isEmpty()) {
						contains = true;
					} else {
						contains = !compoundNames.toLowerCase().contains(substring);
					}
				} else {
					contains = compoundNames.toLowerCase().contains(part);
				}
				txtFilter = txtFilter && contains;
			}

			int minLevel = 0;
			try {
				minLevel = Integer.parseInt(txt_minLevel.getText());
			} catch (NumberFormatException e) {
			}
			boolean filterMinLevel = minLevel <= recipe.getMinRating();

			boolean show = txtFilter && filterMinLevel;

			RecipeView view = recipeViews.get(recipe);
			view.setVisible(show);
			view.setManaged(show);
		}
	}

}
