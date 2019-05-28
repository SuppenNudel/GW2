package de.rohmio.gw2.tools.model;

import org.junit.jupiter.api.Test;

import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeFilterTest {
	
	@Test
	public void test() {
		RecipeFilter recipeFilter = new RecipeFilter();
		recipeFilter.disciplines.addAll(CraftingDisciplines.Armorsmith, CraftingDisciplines.Artificer);
		recipeFilter.disciplines.remove(CraftingDisciplines.Armorsmith);
		recipeFilter.disciplines.set(0, CraftingDisciplines.Chef);
	}

}
