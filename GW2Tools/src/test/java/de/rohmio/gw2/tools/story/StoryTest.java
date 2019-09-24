package de.rohmio.gw2.tools.story;

import java.util.List;

import org.junit.Test;

import de.rohmio.gw2.tools.Credentials;
import de.rohmio.gw2.tools.model.Data;
import me.xhsun.guildwars2wrapper.SynchronousRequest;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.account.Account;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.quests.Quest;

public class StoryTest {
	
	@Test
	public void getBackstory() throws GuildWars2Exception {
		SynchronousRequest synchronous = Data.getInstance().getApi().getSynchronous();
		String apiKey = Credentials.API_KEY;
		
		Account accountInfo = synchronous.getAccountInfo(apiKey);
		System.out.println(accountInfo);
		
		List<String> allCharacterName = synchronous.getAllCharacterName(apiKey);
		System.out.println(allCharacterName);
		
		String characterName = allCharacterName.get(1);
		Character character = synchronous.getCharacter(Credentials.API_KEY, characterName);
		System.out.println(character);
		
		List<Integer> characterQuests = synchronous.getCharacterQuests(apiKey, characterName);
		System.out.println(characterQuests);
		
		List<Integer> allQuestInfo = synchronous.getAllQuestIds();
		System.out.println(allQuestInfo);

		for(int questId : characterQuests) {
			Quest quest = synchronous.getQuestInfo(questId);
			System.out.println(quest);
		}
		System.out.println();
	}

}
