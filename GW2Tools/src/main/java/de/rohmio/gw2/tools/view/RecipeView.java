package de.rohmio.gw2.tools.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rohmio.gw2.tools.model.Data;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Flag;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public abstract class RecipeView extends VBox {

	private Recipe recipe;

	private TextField txt_filter;
	private TextField txt_minLevel;
	private ToggleGroup disciplineToggle;
	private CheckBox chbx_byableRecipe;

	public void addFilter(TextField txt_filter, TextField txt_minLevel, ToggleGroup disciplineToggle,
			CheckBox chbx_byableRecipe) {
		this.txt_filter = txt_filter;
		this.txt_minLevel = txt_minLevel;
		this.disciplineToggle = disciplineToggle;
		this.chbx_byableRecipe = chbx_byableRecipe;

		txt_filter.textProperty().addListener((observable, oldValue, newValue) -> filter());
		txt_minLevel.textProperty().addListener((observable, oldValue, newValue) -> filter());

		disciplineToggle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> filter());
		chbx_byableRecipe.selectedProperty().addListener((observable, oldValue, newValue) -> filter());
	}

	private void filter() {
		String itemNameFilter = txt_filter.getText();
		int minLevelFilter = txt_minLevel.getText().isEmpty() ? 0 : Integer.parseInt(txt_minLevel.getText());
		RadioButton selectedRadioButton = (RadioButton) disciplineToggle.getSelectedToggle();
		CraftingDisciplines disciplineFilter = null;
		if (selectedRadioButton != null) {
			disciplineFilter = CraftingDisciplines.valueOf(selectedRadioButton.getText());
		}
		boolean includeFromRecipeSelected = chbx_byableRecipe.isSelected();

		// min level filter
		boolean filterMinLevel = minLevelFilter <= recipe.getMinRating();
		if (!filterMinLevel) {
			filterOut(true);
			return;
		}

		// recipe from item filter
		boolean filterOutFromRecipe = !includeFromRecipeSelected && recipe.getFlags().contains(Flag.LearnedFromItem);
		if (filterOutFromRecipe) {
			filterOut(true);
			return;
		}

		// discipline filter
		boolean disciplineFiltered = !(disciplineFilter == null || recipe.getDisciplines().contains(disciplineFilter));
		if (disciplineFiltered) {
			filterOut(true);
			return;
		}

		// ignore already learned
		// boolean recipeAlreadyLearned = charRecipes.contains(recipe.getId());
		// boolean showAlreadyLearned = chbx_showAlreadyLearned.isSelected();
		// boolean alreadyLearnedAndIgnored = !showAlreadyLearned &&
		// recipeAlreadyLearned;
		// if (alreadyLearnedAndIgnored) {
		// filterOut(true);
		// continue;
		// }

		// get items for item name filter
		List<String> itemNames = new ArrayList<>();
		Item outputItem = Data.getInstance().getItemProgress().getById(recipe.getOutputItemId());
		String outputItemName = outputItem.getName() + outputItem.getId();
		itemNames.add(outputItemName);
		for (Ingredient ingredient : recipe.getIngredients()) {
			Item ingredientItem = Data.getInstance().getItemProgress().getById(ingredient.getItemId());
			String ingredientItemName = ingredientItem.getName() + ingredientItem.getId();
			itemNames.add(ingredientItemName);
		}

		// item name filter
		List<String> filterNames = Arrays.asList(itemNameFilter.toLowerCase().split(" "));
		boolean match = itemNames.stream().anyMatch(itemName -> {
			itemName = itemName.toLowerCase();
			for (String filterName : filterNames) {
				boolean reverse = false;
				if (filterName.startsWith("!")) {
					filterName = filterName.substring(1);
					reverse = true;
				}
				filterName = filterName.toLowerCase();
				boolean result = reverse ? !itemName.contains(filterName) : itemName.contains(filterName);
				if (!result) {
					return false;
				}
			}
			return true;
		});
		if (!match) {
			filterOut(true);
			return;
		}

		// if nothing filtered show
		filterOut(false);

	}

	private void filterOut(boolean filterOut) {
		setVisible(!filterOut);
		setManaged(!filterOut);
	}

	public RecipeView(Recipe recipe) {
		this.recipe = recipe;
	}

	public Recipe getRecipe() {
		return recipe;
	}

}
