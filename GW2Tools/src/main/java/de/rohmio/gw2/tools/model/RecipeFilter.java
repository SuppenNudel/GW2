package de.rohmio.gw2.tools.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeFilter {

	private List<Recipe> recipesShown = new CopyOnWriteArrayList<>();
	private List<Recipe> recipesNotShown = new CopyOnWriteArrayList<>();

	public ObservableList<CraftingDisciplines> disciplines = FXCollections.observableArrayList();

	public RecipeFilter() {
		long date = new Date().getTime();
		System.out.println("Getting all recipes");
		recipesNotShown.addAll(Data.getInstance().getRecipes().getAll().values());
		System.out.println("after recipes: "+(new Date().getTime()-date));
		
		disciplines.addListener(new ListChangeListener<CraftingDisciplines>() {
			@Override
			public void onChanged(Change<? extends CraftingDisciplines> c) {
				System.out.println("trigger filter changed");
				while (c.next()) {
					if (c.wasAdded() || c.wasRemoved()) {
						for (CraftingDisciplines remitem : c.getRemoved()) {
							System.out.println("remove: "+remitem);
							for(Recipe recipe : recipesShown) {
								boolean noMatch = Collections.disjoint(recipe.getDisciplines(), disciplines);
								if(noMatch) {
									recipesShown.remove(recipe);
									recipesNotShown.add(recipe);
								}
							}
						}
						for (CraftingDisciplines additem : c.getAddedSubList()) {
							System.out.println("add: "+additem);
							for(Recipe recipe : recipesNotShown) {
								boolean match = !Collections.disjoint(recipe.getDisciplines(), disciplines);
								if(match) {
									recipesNotShown.remove(recipe);
									recipesShown.add(recipe);
								}
							}
						}
					} 
				}
				System.out.println("after filter: "+(new Date().getTime()-date));
				System.out.println(recipesShown);
			}

		});
	}

}
