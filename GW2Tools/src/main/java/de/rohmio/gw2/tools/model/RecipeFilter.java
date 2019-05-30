package de.rohmio.gw2.tools.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableList;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeFilter {
	
	private List<RecipeWrapper> recipes = new ArrayList<>();
	
	private ObservableList<CraftingDisciplines> disciplines;
	private ObservableIntegerValue minLevel;
	private ObservableIntegerValue maxLevel;
	
	public RecipeFilter() {
		for(Recipe recipe : Data.getInstance().getRecipes().getAll().values()) {
			recipes.add(new RecipeWrapper(recipe));
		}
	};
	
	public List<RecipeWrapper> getRecipes() {
		return recipes;
	}
	
	public ObservableList<CraftingDisciplines> getDisciplines() {
		return disciplines;
	}
	
	public ObservableIntegerValue getMinLevel() {
		return minLevel;
	}
	
	public ObservableIntegerValue getMaxLevel() {
		return maxLevel;
	}
	
	public void addDisciplineFilter(ObservableList<CraftingDisciplines> disciplines) {
		System.out.println("RecipeFilter.addDisciplineFilter()");
		this.disciplines = disciplines;
		for(RecipeWrapper recipeWrapper : recipes) {
			recipeWrapper.addDisciplineFilter(disciplines);
		}
	}
	
	public void addLevelFilter(ObservableIntegerValue minLevel, ObservableIntegerValue maxLevel) {
		System.out.println("RecipeFilter.addLevelFilter()");
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		for(RecipeWrapper recipeWrapper : recipes) {
			recipeWrapper.addLevelFilter(minLevel, maxLevel);
		}
	}
	
	public void addCharacterFilter(ObservableObjectValue<Character> character) {
		System.out.println("RecipeFilter.addCharacterFilter()");
		for(RecipeWrapper recipeWrapper : recipes) {
			recipeWrapper.addCharacterFilter(character);
		}
	}
	
	public long currentlyDisplayed() {
		return recipes.stream().map(RecipeWrapper::getShow).map(BooleanProperty::get).filter(b -> b == true).count();
	}

}
