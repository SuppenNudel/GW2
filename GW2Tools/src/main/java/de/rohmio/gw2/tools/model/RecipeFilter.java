package de.rohmio.gw2.tools.model;

import java.util.Collections;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.ObservableList;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeFilter {

	private Recipe recipe;

	private BooleanProperty show = new SimpleBooleanProperty();

	private BooleanProperty discipline = new SimpleBooleanProperty();
	private BooleanProperty level = new SimpleBooleanProperty();
	private BooleanProperty learnedFromItem = new SimpleBooleanProperty();
	private BooleanProperty autoLearned = new SimpleBooleanProperty();
	private BooleanProperty character = new SimpleBooleanProperty();
	private BooleanProperty itemName = new SimpleBooleanProperty();

	public RecipeFilter(Recipe recipe) {
		this.recipe = recipe;
		show.bind(level.and(discipline).and(character)); //.and(learnedFromItem).and(autoLearned));
	}
	
	public BooleanProperty getShow() {
		return show;
	}
	
	public Recipe getRecipe() {
		return recipe;
	}

	public void addDisciplineFilter(ObservableList<CraftingDisciplines> disciplines) {
		discipline.bind(Bindings.createBooleanBinding( () -> !Collections.disjoint(disciplines, recipe.getDisciplines()), disciplines ));
	}

	public void addLevelFilter(ObservableIntegerValue minLevel, ObservableIntegerValue maxLevel) {
		int minRating = recipe.getMinRating();
		level.bind(Bindings.greaterThanOrEqual(minRating, minLevel).and(Bindings.lessThanOrEqual(minRating, maxLevel)));
	}
	
	public void addItemNameFilter(ObservableStringValue itemName) {
//		this.itemName.bind(observable);
	}
	
	public void addLearnedFromItemFilter(ObservableBooleanValue learnedFromItem) {
		this.learnedFromItem.bind(learnedFromItem);
	}
	
	public void addAutoLearnedFilter(ObservableBooleanValue autoLearned) {
		this.autoLearned.bind(autoLearned);
	}
	
	public void addCharacterFilter(ObservableObjectValue<Character> character) {
		this.character.bind(Bindings.createBooleanBinding(() -> !character.get().getRecipes().contains(recipe.getId()), character));		
	}

}
