package de.rohmio.gw2.tools.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Flag;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeFilterTest {

	private String apiKey = "8DC74D37-14E9-8041-B393-5A0B644E53F122F3448B-A74C-422A-B386-2ED26469D2BD";
	private String charName = "Mori Shizen";

	private List<RecipeFilter> recipeFilters;

	private ObservableMap<CraftingDisciplines, Boolean> disciplines = FXCollections.observableHashMap();
	private IntegerProperty minLevel = new SimpleIntegerProperty();
	private IntegerProperty maxLevel = new SimpleIntegerProperty();
	private ObjectProperty<Character> character = new SimpleObjectProperty<>();
	private BooleanProperty learnedFromItem = new SimpleBooleanProperty();
	private BooleanProperty autoLearned = new SimpleBooleanProperty();

	@BeforeClass
	public static void loadRecipes() {
		System.out.println("RecipeFilterTest.loadRecipes()");
		Date start = new Date();
		ObservableMap<Integer, Recipe> all = Data.getInstance().getRecipes().getAll();
		Date loadedRecipes = new Date();
		System.out.println("Time to Load: " + String.valueOf(loadedRecipes.getTime() - start.getTime()));
		System.out.println(all.size());
	}

	@Before
	public void createRecipeFilter() {
		System.out.println("RecipeFilterTest.createRecipeFilter()");
		recipeFilters = new ArrayList<>();
		for (Recipe recipe : Data.getInstance().getRecipes().getValues().values()) {
			recipeFilters.add(new RecipeFilter(recipe));
		}
		printAmountOfShownRecipes("Before everything");
	}

	private void printAmountOfShownRecipes(String message) {
		long count = recipeFilters.stream().filter(filter -> filter.getShow().get()).count();
		System.out.println(message + ": " + count);
	}

	@Test
	public void craftingDiscipines() {
		System.out.println("RecipeFilterTest.craftingDiscipines()");

		recipeFilters.forEach(filter -> filter.addDisciplineFilter(disciplines));

		disciplines.put(CraftingDisciplines.Huntsman, true);
		testForDisciplines();

		disciplines.put(CraftingDisciplines.Armorsmith, true);
		disciplines.put(CraftingDisciplines.Artificer, true);
		testForDisciplines();

		disciplines.put(CraftingDisciplines.Huntsman, false);
		testForDisciplines();

		disciplines.put(CraftingDisciplines.Chef, true);
		testForDisciplines();
	}

	private void testForDisciplines() {
		for (RecipeFilter filters : recipeFilters) {
			List<CraftingDisciplines> recipeDisciplines = filters.getRecipe().getDisciplines();
			boolean show = filters.getShow().get();
			boolean containsAny = false;
			for (CraftingDisciplines discipline : recipeDisciplines) {
				Boolean selected = disciplines.get(discipline);
				if (selected != null && selected == true) {
					containsAny = true;
					break;
				}
			}
			// if is shown then it should contain the discipline
			Assert.assertTrue(show == containsAny);
		}
		printAmountOfShownRecipes(disciplines.toString());
	}

	@Test
	public void level() {
		System.out.println("RecipeFilterTest.level()");
		recipeFilters.forEach(filter -> filter.addLevelFilter(minLevel, maxLevel));

		minLevel.set(0);
		maxLevel.set(500);

		printAmountOfShownRecipes("Before level filter");

		minLevel.set(150);
		testForLevel();
		printAmountOfShownRecipes("After first");

		maxLevel.set(350);
		testForLevel();
		printAmountOfShownRecipes("After second");
	}

	private void testForLevel() {
		int minLevel = this.minLevel.get();
		int maxLevel = this.maxLevel.get();

		for (RecipeFilter filter : recipeFilters) {
			int minRating = filter.getRecipe().getMinRating();
			boolean show = filter.getShow().get();

			boolean lessThanMax = maxLevel >= minRating;
			boolean moreThanMin = minLevel <= minRating;
			boolean inRange = moreThanMin && lessThanMax;
			Assert.assertTrue(show == inRange);
		}
	}

	@Test
	public void character() throws GuildWars2Exception {
		System.out.println("RecipeFilterTest.character()");

		character.set(Data.getInstance().getApi().getSynchronous().getCharacter(apiKey, charName));

		printAmountOfShownRecipes("Before character");
		recipeFilters.forEach(filter -> filter.addCharacterFilter(character));
		printAmountOfShownRecipes("After character");

		testForCharacter();
	}

	private void testForCharacter() {
		for (RecipeFilter filter : recipeFilters) {
			boolean contains = character.get().getRecipes().contains(filter.getRecipe().getId());
			Assert.assertTrue(filter.getShow().get() != contains);
		}
	}

	@Test
	public void learnedFromItem() {
		System.out.println("RecipeFilterTest.learnedFromItem()");

		learnedFromItem.set(true);

		recipeFilters.forEach(filter -> filter.addLearnedFromItemFilter(learnedFromItem));

		testForLearnedFromItem();
	}

	private void testForLearnedFromItem() {
		for (RecipeFilter filter : recipeFilters) {
			boolean containsFlag = filter.getRecipe().getFlags().contains(Flag.LearnedFromItem);
			boolean learnedFromItem = filter.getRecipe().getFlags().contains(Flag.LearnedFromItem);
			Assert.assertTrue(filter.getShow().get() == !containsFlag || containsFlag && learnedFromItem);
		}
	}

	@Test
	public void autoLearned() {
		System.out.println("RecipeFilterTest.learnedFromItem()");

		autoLearned.set(true);

		recipeFilters.forEach(filter -> filter.addAutoLearnedFilter(autoLearned));

		testForAutoLearned();
	}

	private void testForAutoLearned() {
		for (RecipeFilter filter : recipeFilters) {
			boolean containsFlag = filter.getRecipe().getFlags().contains(Flag.AutoLearned);
			boolean autoLearned = containsFlag;
			Assert.assertTrue(filter.getShow().get() == !containsFlag || containsFlag && autoLearned);
		}
	}

	@Test
	public void kudzuExperimentTest() {
		recipeFilters.forEach(filter -> {
			filter.addDisciplineFilter(disciplines);
			filter.addLevelFilter(minLevel, maxLevel);
			filter.addLearnedFromItemFilter(learnedFromItem);
			filter.addAutoLearnedFilter(autoLearned);
		});

		disciplines.put(CraftingDisciplines.Huntsman, true);
		minLevel.set(450);
		maxLevel.set(450);
		learnedFromItem.set(true);
		autoLearned.set(false);

		boolean found = false;
		for (RecipeFilter filter : recipeFilters) {
			if (filter.getRecipe().getId() == 9957) {
				Assert.assertTrue(filter.getShow().get());
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
	}

}
