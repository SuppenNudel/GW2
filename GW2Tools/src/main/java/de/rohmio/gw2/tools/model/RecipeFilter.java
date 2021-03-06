package de.rohmio.gw2.tools.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rohmio.gw2.tools.main.Data;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Flag;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterRecipes;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeFilter {

	private Recipe recipe;
	private CharacterRecipes characterRecipes;
	private List<Integer> unlockedRecipes;

	private StringProperty minRecipeLevelFilter;
	private StringProperty itemNameFilter;
	private ObjectExpression<Toggle> disciplineFilter;
	private BooleanProperty byableRecipeFilter;
	private BooleanProperty alreadyLearnedFilter;
	
	private BooleanProperty display = new SimpleBooleanProperty();
	
	public RecipeFilter(Recipe recipe, CharacterRecipes characterRecipes, List<Integer> unlockedRecipes) {
		this.recipe = recipe;
		this.characterRecipes = characterRecipes;
		this.unlockedRecipes = unlockedRecipes;
	}

	public void handleFilter(boolean show) {
		if (show) {
			// nicht gleich zeigen sondern andere noch überprüfen
			show(disciplineFilter() && itemNameFilter() && minRecipeLevelFilter() && byableRecipeFilter()
					&& alreadyLearnedFilter());
		} else {
			// falls eh nicht angezeigt dann gleich raus filtern
			show(false);
		}
	}

	public void addRecipeLevelFilter(StringProperty textProperty) {
		minRecipeLevelFilter = textProperty;
		synchronized (minRecipeLevelFilter) {
			minRecipeLevelFilter.addListener((observable, oldValue, newValue) -> handleFilter(minRecipeLevelFilter()));
		}
	}

	public void addItemNameFilter(StringProperty textProperty) {
		itemNameFilter = textProperty;
		synchronized (itemNameFilter) {
			itemNameFilter.addListener((observable, oldValue, newValue) -> handleFilter(itemNameFilter()));
		}
	}

	public void addDisciplineFilter(ObjectExpression<Toggle> toggleProperty) {
		disciplineFilter = toggleProperty;
		synchronized (disciplineFilter) {
			disciplineFilter.addListener((observable, oldValue, newValue) -> handleFilter(disciplineFilter()));
		}
	}

	public void addByableRecipeFilter(BooleanProperty selectedProperty) {
		byableRecipeFilter = selectedProperty;
		synchronized (byableRecipeFilter) {
			byableRecipeFilter.addListener((observable, oldValue, newValue) -> handleFilter(byableRecipeFilter()));
		}
	}

	public void addAlreadyLearnedFilter(BooleanProperty selectedProperty) {
		alreadyLearnedFilter = selectedProperty;
		synchronized (alreadyLearnedFilter) {
			alreadyLearnedFilter.addListener((observable, oldValue, newValue) -> handleFilter(alreadyLearnedFilter()));
		}
	}

	private boolean alreadyLearnedFilter() {
		boolean characterUnlocked = characterRecipes.getRecipes().contains(recipe.getId());
		boolean accountUnlocked = unlockedRecipes.contains(recipe.getId());
		boolean showAlreadyLearned = alreadyLearnedFilter.get();
		boolean unlockedAndIgnored = showAlreadyLearned || (!characterUnlocked && !accountUnlocked);
		return unlockedAndIgnored;
	}

	private boolean minRecipeLevelFilter() {
		if (minRecipeLevelFilter.get().isEmpty()) {
			return true;
		}
		int minLevel = Integer.parseInt(minRecipeLevelFilter.get());
		return recipe.getMinRating() >= minLevel;
	}

	private boolean byableRecipeFilter() {
		boolean show = true;
		if (!byableRecipeFilter.get() && recipe.getFlags().contains(Flag.LearnedFromItem)) {
			show = false;
		}
		return show;
	}

	private boolean disciplineFilter() {
		Toggle toggle = disciplineFilter.get();
		if (toggle == null) {
			return true;
		}
		boolean result = true;
		if (toggle instanceof RadioButton) {
			RadioButton radio = (RadioButton) toggle;
			CraftingDisciplines discipline = (CraftingDisciplines) radio.getUserData();
			result = recipe.getDisciplines().contains(discipline);
		} else {
			System.err.println(toggle + " is not a radio button");
		}
		return result;
	}

	private boolean itemNameFilter() {
		String filterText = itemNameFilter.get();

		// get items for item name filter
		List<String> itemNames = new ArrayList<>();
		Item outputItem = Data.getInstance().getItemProgress().getById(recipe.getOutputItemId());
		String outputItemName = outputItem.getName(); // + outputItem.getId() for filter by id
		itemNames.add(outputItemName);
		for (Ingredient ingredient : recipe.getIngredients()) {
			Item ingredientItem = Data.getInstance().getItemProgress().getById(ingredient.getItemId());
			String ingredientItemName = ingredientItem.getName(); // + ingredientItem.getId() for filter by id
			itemNames.add(ingredientItemName);
		}

		// item name filter
		final List<String> filterNames = Arrays.asList(filterText.toLowerCase().split(" "));
		boolean match = filterNames.stream().allMatch(filterName -> {
			filterName = filterName.toLowerCase();
			boolean not = false;
			if (filterName.startsWith("!")) {
				filterName = filterName.substring(1);
				not = true;
			}
			boolean result = not; // grundsätzlich alles anzeigen außer not filter ist aktiv
			for (String itemName : itemNames) {
				itemName = itemName.toLowerCase();
				boolean contains = itemName.contains(filterName);
				if (not) {
					contains = !contains;
					result &= contains; // wenn not filter aktiv filterung umdrehen
				} else {
					result |= contains;
				}
			}
			return result;
		});
		return match;
	}

	private void show(boolean show) {
		display.set(show);
//		if(show) {
//			collection.add(recipe);
//		} else {
//			collection.remove(recipe);
//		}
	}
	
	public BooleanProperty displayProperty() {
		return display;
	}

}
