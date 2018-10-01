package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.rohmio.gw2.tools.model.ClientFactory;
import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.view.RecipeView;
import de.rohmio.gw2.tools.view.recipeTree.RecipeTreeViewController;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Flag;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterCraftingLevel;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterCraftingLevel.Discipline;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class MainViewController implements Initializable {

	@FXML
	private TextField txt_apiKey;
	@FXML
	private ChoiceBox<String> choice_charName;
	@FXML
	private Button btn_analyse;

	@FXML
	private TextField txt_filter;

	@FXML
	private TextField txt_minLevel;

	@FXML // selection for disciplines
	private HBox hbox_disciplineCheck;

	@FXML // selection for language
	private HBox hbox_langRadio;

	@FXML // list of all disciplines the character has
	private VBox vbox_charDisciplines;

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

	private Map<CraftingDisciplines, CheckBox> craftingDisceplinesToCheckBox = new HashMap<>();
	private Map<Recipe, RecipeView> recipeViews = new HashMap<>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		txt_filter.setDisable(true);

		pb_getItems.progressProperty().bind(Data.getInstance().progress);
		new Thread(() -> {
			try {
				Data.getInstance().getAllRecipes();
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		}).start();

		txt_apiKey.setText(ClientFactory.ACCESS_KEY);

		// create check boxes for discipline selection
		for (CraftingDisciplines discipline : CraftingDisciplines.values()) {
			CheckBox checkBox = new CheckBox(discipline.toString());
			craftingDisceplinesToCheckBox.put(discipline, checkBox);
			hbox_disciplineCheck.getChildren().add(checkBox);
		}

		// create radio buttons for language
		ToggleGroup langGroup = new ToggleGroup();
		for (LanguageSelect lang : LanguageSelect.values()) {
			RadioButton radio = new RadioButton(lang.getValue());
			radio.setToggleGroup(langGroup);
			radio.setOnAction(event -> GuildWars2.setLanguage(lang));
			hbox_langRadio.getChildren().add(radio);
		}

		txt_filter.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> filter());
		txt_minLevel.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> filter());
	}

	@FXML
	private void analyse() throws GuildWars2Exception, IOException {
		btn_analyse.setDisable(true);

		// clear previous analysation
		scroll_recipes.getChildren().clear();
		vbox_charDisciplines.getChildren().clear();
		recipeViews.clear();

		// get ALL recipes
		List<Recipe> allRecipes = Data.getInstance().getAllRecipes();

		// get recipes selected character has already learned
		Character character = GuildWars2.getInstance().getSynchronous().getCharacter(txt_apiKey.getText(),
				choice_charName.getSelectionModel().getSelectedItem());
		for (Discipline discipline : character.getCrafting()) {
			vbox_charDisciplines.getChildren().add(new Label(String.format("%s: %d - active: %s",
					discipline.getDiscipline().name(), discipline.getRating(), discipline.isActive())));
		}
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
		int[] itemIdsArray = itemIds.stream().mapToInt(Integer::intValue).toArray();
		Data.getInstance().getItemsById(itemIdsArray); // now they are saved locally in a map

		// display all discoverable recipes
		for (Recipe recipe : recipesToShow) {
			// RecipeViewController recipeView = new RecipeViewController(recipe);
			RecipeView recipeView = new RecipeTreeViewController(recipe, chbx_recipesRecursively.isSelected());
			recipeViews.put(recipe, recipeView);
			scroll_recipes.getChildren().add(recipeView);
		}
		btn_analyse.setDisable(false);
		txt_filter.setDisable(false);
	}

	private void filter() {
		String filterText = txt_filter.getText();
		for (Recipe recipe : recipeViews.keySet()) {
			Item outputItem = Data.getInstance().getItemById(recipe.getOutputItemId());
			String outputItemName = outputItem.getName();

			String compoundNames = outputItemName;
			for (Ingredient ingredient : recipe.getIngredients()) {
				compoundNames = compoundNames + " " + Data.getInstance().getItemById(ingredient.getItemId()).getName();
			}

			boolean txtContains = Arrays.stream(filterText.toLowerCase().split(" "))
					.allMatch(compoundNames.toLowerCase()::contains);
			int minLevel = 0;
			try {
				minLevel = Integer.parseInt(txt_minLevel.getText());
			} catch (NumberFormatException e) {
			}
			boolean filterMinLevel = minLevel <= recipe.getMinRating();

			boolean show = txtContains && filterMinLevel;

			RecipeView view = recipeViews.get(recipe);
			view.setVisible(show);
			view.setManaged(show);
		}
	}

	@FXML
	private void getCharacters() throws GuildWars2Exception {
		choice_charName.getItems().clear();

		GuildWars2 gw2 = GuildWars2.getInstance();
		List<String> allCharacterName = gw2.getSynchronous().getAllCharacterName(txt_apiKey.getText());
		choice_charName.getItems().addAll(allCharacterName);
		choice_charName.getSelectionModel().select(0);
		String name = choice_charName.getSelectionModel().getSelectedItem();
		CharacterCraftingLevel characterCrafting = gw2.getSynchronous().getCharacterCrafting(txt_apiKey.getText(),
				name);
		for (Discipline discipline : characterCrafting.getCrafting()) {
			CheckBox checkBox = craftingDisceplinesToCheckBox.get(discipline.getDiscipline());
			checkBox.setSelected(discipline.isActive());
		}
	}

}
