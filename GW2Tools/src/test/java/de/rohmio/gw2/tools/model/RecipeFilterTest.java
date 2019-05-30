package de.rohmio.gw2.tools.model;

import java.util.Collections;
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
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeFilterTest {
	
	private String apiKey = "8DC74D37-14E9-8041-B393-5A0B644E53F122F3448B-A74C-422A-B386-2ED26469D2BD";
	private String charName = "Mori Shizen";

	private RecipeFilter recipeFilter;
	
	@BeforeClass
	public static void loadRecipes() {
		System.out.println("RecipeFilterTest.loadRecipes()");
		Data.getInstance().getRecipes().getAll();
		System.out.println(Data.getInstance().getRecipes().getValues().size());
	}
	
	@Before
	public void createRecipeFilter() {
		System.out.println("RecipeFilterTest.createRecipeFilter()");
		recipeFilter = new RecipeFilter();
		printAmountOfShownRecipes("Before");
	}
	
	private void printAmountOfShownRecipes(String message) {
		System.out.println(message+": "+recipeFilter.currentlyDisplayed());
	}
	
	private void testForDisciplines() {
		ObservableList<CraftingDisciplines> disciplinesFilter = recipeFilter.getDisciplines();
		for(RecipeWrapper wrapper : recipeFilter.getRecipes()) {
			List<CraftingDisciplines> recipeDisciplines = wrapper.getRecipe().getDisciplines();
			boolean show = wrapper.getShow().get();
			boolean containsAny = !Collections.disjoint(disciplinesFilter, recipeDisciplines);
			// if is shown then it should contain the discipline 
			Assert.assertTrue(show == containsAny);
		}
		printAmountOfShownRecipes(disciplinesFilter.toString());
	}
	
	private void testForLevel() {
		int minLevel = recipeFilter.getMinLevel().get();
		int maxLevel = recipeFilter.getMaxLevel().get();
		
		for(RecipeWrapper wrapper : recipeFilter.getRecipes()) {
			int minRating = wrapper.getRecipe().getMinRating();
			boolean show = wrapper.getShow().get();
			
			boolean lessThanMax = maxLevel >= minRating;
			boolean moreThanMin = minLevel <= minRating;
			Assert.assertTrue(show == (moreThanMin && lessThanMax));
		}
	}
	
	@Test
	public void craftingDiscipines() {
		System.out.println("RecipeFilterTest.craftingDiscipines()");
		ObservableList<CraftingDisciplines> craftingDisciplines = FXCollections.observableArrayList();
		
		recipeFilter.addDisciplineFilter(craftingDisciplines);
		
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
		
		IntegerProperty minLevel = new SimpleIntegerProperty(0);
		IntegerProperty maxLevel = new SimpleIntegerProperty(500);
		
		recipeFilter.addLevelFilter(minLevel, maxLevel);
		System.out.println(recipeFilter.getRecipes().size());
		
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
		recipeFilter.addCharacterFilter(character);
		printAmountOfShownRecipes("After character");

		for(RecipeWrapper wrapper : recipeFilter.getRecipes()) {
			int recipeId = wrapper.getRecipe().getId();
			
			boolean show = wrapper.getShow().get();
			List<Integer> recipes = character.get().getRecipes();
			
			Assert.assertTrue(show == (!recipes.contains(recipeId)));
		}
	}

}
