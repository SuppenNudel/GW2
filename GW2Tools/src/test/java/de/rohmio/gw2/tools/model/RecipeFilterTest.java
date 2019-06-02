package de.rohmio.gw2.tools.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeFilterTest {
	
	private String apiKey = "8DC74D37-14E9-8041-B393-5A0B644E53F122F3448B-A74C-422A-B386-2ED26469D2BD";
	private String charName = "Mori Shizen";

	private List<RecipeFilter> recipeFilters;

	private ObservableList<CraftingDisciplines> disciplinesFilter = FXCollections.observableArrayList();
	private IntegerProperty minLevel = new SimpleIntegerProperty();
	private IntegerProperty maxLevel = new SimpleIntegerProperty();
	
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
		for(Recipe recipe : Data.getInstance().getRecipes().getValues().values()) {
			recipeFilters.add(new RecipeFilter(recipe));
		}
		printAmountOfShownRecipes("Before everything");
	}
	
	private void printAmountOfShownRecipes(String message) {
		long count = recipeFilters.stream().filter(filter -> filter.getShow().get()).count();
		System.out.println(message+": "+count);
	}
	
	private void testForDisciplines() {
		for(RecipeFilter filters : recipeFilters) {
			List<CraftingDisciplines> recipeDisciplines = filters.getRecipe().getDisciplines();
			boolean show = filters.getShow().get();
			boolean containsAny = !Collections.disjoint(disciplinesFilter, recipeDisciplines);
			// if is shown then it should contain the discipline 
			Assert.assertTrue(show == containsAny);
		}
		printAmountOfShownRecipes(disciplinesFilter.toString());
	}
	
	private void testForLevel() {
		int minLevel = this.minLevel.get();
		int maxLevel = this.maxLevel.get();
		
		for(RecipeFilter filter : recipeFilters) {
			int minRating = filter.getRecipe().getMinRating();
			boolean show = filter.getShow().get();
			
			boolean lessThanMax = maxLevel >= minRating;
			boolean moreThanMin = minLevel <= minRating;
			boolean inRange = moreThanMin && lessThanMax;
			Assert.assertTrue(show == inRange);
		}
	}
	
	@Test
	public void craftingDiscipines() {
		System.out.println("RecipeFilterTest.craftingDiscipines()");
		ObservableList<CraftingDisciplines> craftingDisciplines = FXCollections.observableArrayList();
		
		recipeFilters.forEach(filter -> filter.addDisciplineFilter(craftingDisciplines));
		
		craftingDisciplines.add(CraftingDisciplines.Huntsman);
		testForDisciplines();
		
		craftingDisciplines.addAll(CraftingDisciplines.Armorsmith, CraftingDisciplines.Artificer);
		testForDisciplines();
		
		craftingDisciplines.remove(CraftingDisciplines.Huntsman);
		testForDisciplines();
		
		craftingDisciplines.add(CraftingDisciplines.Chef);
		testForDisciplines();
	}
	
	@Test
	public void level() {
		System.out.println("RecipeFilterTest.level()");
		recipeFilters.forEach(filter -> filter.addLevelFilter(minLevel, maxLevel));
		recipeFilters.forEach(filter -> filter.addDisciplineFilter(disciplinesFilter));
		disciplinesFilter.addAll(CraftingDisciplines.values());
		
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
	
	@Test
	public void character() throws GuildWars2Exception {
		System.out.println("RecipeFilterTest.character()");
		
		ObjectProperty<Character> character = new SimpleObjectProperty<>();
		character.set(GuildWars2.getInstance().getSynchronous().getCharacter(apiKey, charName));
		
		printAmountOfShownRecipes("Before character");
		recipeFilters.forEach(filter -> filter.addCharacterFilter(character));
		printAmountOfShownRecipes("After character");

		for(RecipeFilter filter : recipeFilters) {
			int recipeId = filter.getRecipe().getId();
			
			boolean show = filter.getShow().get();
			List<Integer> recipes = character.get().getRecipes();
			
			Assert.assertTrue(show == (!recipes.contains(recipeId)));
		}
	}

}
