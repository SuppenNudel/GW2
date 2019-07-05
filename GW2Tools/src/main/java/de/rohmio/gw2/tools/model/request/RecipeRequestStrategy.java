package de.rohmio.gw2.tools.model.request;

import java.util.List;

import de.rohmio.gw2.tools.model.Data;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class RecipeRequestStrategy implements RequestStrategy<Recipe> {

	private GuildWars2 api;

	public RecipeRequestStrategy() {
		GuildWars2 api = Data.getInstance().getApi();
		this.api = api;
	}

	@Override
	public List<Integer> getIds() throws GuildWars2Exception {
		List<Integer> allIds = api.getSynchronous().getAllRecipeID();
		return allIds;
	}

	@Override
	public List<Recipe> getItems(int[] itemIds) throws GuildWars2Exception {
		List<Recipe> info = api.getSynchronous().getRecipeInfo(itemIds);
		return info;
	}

}
