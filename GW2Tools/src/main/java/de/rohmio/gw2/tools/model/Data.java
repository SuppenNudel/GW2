package de.rohmio.gw2.tools.model;

import de.rohmio.gw2.tools.model.RequestProgress.RequestType;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class Data {

	private static Data data;
	private String apiKey;
	
	private RequestProgress<Item> itemProgress;
	private RequestProgress<Recipe> recipeProgress;

	private Data() throws NullPointerException, GuildWars2Exception {
		try {
			GuildWars2.setInstance(ClientFactory.getClient());
		} catch (GuildWars2Exception e) {
			e.printStackTrace();
		}
		
		itemProgress = new RequestProgress<>(RequestType.ITEM);
		recipeProgress = new RequestProgress<>(RequestType.RECIPE);
	}

	public static Data getInstance() {
		if (data == null) {
			try {
				data = new Data();
			} catch (NullPointerException | GuildWars2Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public RequestProgress<Item> getItemProgress() {
		return itemProgress;
	}
	public RequestProgress<Recipe> getRecipeProgress() {
		return recipeProgress;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getApiKey() {
		return apiKey;
	}
	
}
