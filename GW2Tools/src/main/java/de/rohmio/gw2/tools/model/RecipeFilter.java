package de.rohmio.gw2.tools.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableMap;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Flag;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeFilter {

	private Recipe recipe;

	private BooleanProperty show = new SimpleBooleanProperty();

	private BooleanProperty discipline = new SimpleBooleanProperty(true);
	private BooleanProperty level = new SimpleBooleanProperty(true);
	private BooleanProperty learnedFromItem = new SimpleBooleanProperty(true);
	private BooleanProperty autoLearned = new SimpleBooleanProperty(true);
	private BooleanProperty discoverable = new SimpleBooleanProperty(true);
	private BooleanProperty character = new SimpleBooleanProperty(true);
//	private BooleanProperty itemName = new SimpleBooleanProperty(true);

	public RecipeFilter(Recipe recipe) {
		this.recipe = recipe;
		show.bind(level.and(discipline).and(character).and(learnedFromItem).and(autoLearned).and(discoverable));
	}
	
	public BooleanProperty getShow() {
		return show;
	}
	
	public Recipe getRecipe() {
		return recipe;
	}

	public void addDisciplineFilter(ObservableMap<CraftingDisciplines, Boolean> disciplines) {
		discipline.bind(Bindings.createBooleanBinding(() -> {
			for(CraftingDisciplines discipline : recipe.getDisciplines()) {
				Boolean contains = disciplines.get(discipline);
				if(contains != null && contains == true) {
					return true;
				}
			}
			return false;
		}, disciplines));
	}

	public void addLevelFilter(ObservableIntegerValue minLevel, ObservableIntegerValue maxLevel) {
		int minRating = recipe.getMinRating();
		level.bind(Bindings.greaterThanOrEqual(minRating, minLevel)
				.and(Bindings.lessThanOrEqual(minRating, maxLevel)));
	}
	
	/* TODO implement
	public void addItemNameFilter(ObservableStringValue itemName) {
		Item item = Data.getInstance().getItems().getById(recipe.getOutputItemId());
		this.itemName.bind(Bindings.createBooleanBinding(() -> 
			item.getName().contains(itemName.get()), itemName));
	}
	*/
	
	// TODO figure out how "containsFlag" constant can be set, so that it doesn't have to be checked every time
	public void addLearnedFromItemFilter(ObservableBooleanValue learnedFromItem) {
		boolean containsFlag = recipe.getFlags().contains(Flag.LearnedFromItem);
		this.learnedFromItem.bind(Bindings.createBooleanBinding(() ->
			!containsFlag || containsFlag && learnedFromItem.get(),
			learnedFromItem));
	}
	
	public void addAutoLearnedFilter(ObservableBooleanValue autoLearned) {
		boolean containsFlag = recipe.getFlags().contains(Flag.AutoLearned);
		this.autoLearned.bind(Bindings.createBooleanBinding(() ->
			!containsFlag || containsFlag && autoLearned.get(),
			autoLearned));
	}
	
	public void addDiscoverableFilter(ObservableBooleanValue discoverable) {
		boolean empty = recipe.getFlags().isEmpty();
		this.discoverable.bind(Bindings.createBooleanBinding(() -> !empty || empty && discoverable.get(), discoverable));
	}
	
	public void addCharacterFilter(ObservableObjectValue<Character> character) {
		this.character.bind(Bindings.createBooleanBinding(() -> {
			if(character.get() == null) {
				return true;
			}
			return !character.get().getRecipes().contains(recipe.getId());	
		}, character));		
	}

}
