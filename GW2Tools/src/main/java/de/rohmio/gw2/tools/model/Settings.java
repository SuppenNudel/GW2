package de.rohmio.gw2.tools.model;

import java.io.File;

import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;

public class Settings {
	
	private static File file = new File("data/settings.json");
	private static Settings settings;
	
	private LanguageSelect lang;
	private String apiKey;
	
	public static Settings getInstance() {
		if (settings == null) {
			settings = Util.readFile(file, Settings.class);
			GuildWars2.setLanguage(settings.lang);
		}
		return settings;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		save();
	}
	public void setLang(LanguageSelect lang) {
		this.lang = lang;
		GuildWars2.setLanguage(lang);
		save();
	}
	
	public String getApiKey() {
		return apiKey;
	}
	public LanguageSelect getLang() {
		return lang;
	}
	
	private void save() {
		Util.writeFile(file, this);
	}
	
}
