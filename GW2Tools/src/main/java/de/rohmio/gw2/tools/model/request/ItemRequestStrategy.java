package de.rohmio.gw2.tools.model.request;

import java.util.List;

import de.rohmio.gw2.tools.model.Data;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;

public class ItemRequestStrategy implements RequestStrategy<Item> {

	private GuildWars2 api;

	public ItemRequestStrategy() {
		GuildWars2 api = Data.getInstance().getApi();
		this.api = api;
	}

	@Override
	public List<Integer> getIds() throws GuildWars2Exception {
		List<Integer> allIds = api.getSynchronous().getAllItemID();
		return allIds;
	}

	@Override
	public List<Item> getItems(int[] itemIds) throws GuildWars2Exception {
		List<Item> info = api.getSynchronous().getItemInfo(itemIds);
		return info;
	}

}
