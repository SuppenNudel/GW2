package de.rohmio.gw2.tools.main;

import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;

public class Settings {
	
	private LanguageSelect lang;
	private String accessToken;
	
	public Settings(String accessToken, LanguageSelect lang) {
		this.accessToken = accessToken;
		this.lang = lang;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setLang(LanguageSelect lang) {
		this.lang = lang;
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	public LanguageSelect getLang() {
		return lang;
	}
	
}