package de.rohmio.gw2.tools.model.request;

import java.util.List;

import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;

public interface RequestStrategy<T> {

	public List<Integer> getIds() throws GuildWars2Exception;
	public List<T> getItems(int[] itemIds) throws GuildWars2Exception;

}
