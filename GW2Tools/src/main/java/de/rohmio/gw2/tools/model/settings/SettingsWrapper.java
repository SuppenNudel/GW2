package de.rohmio.gw2.tools.model.settings;

import java.io.File;

import de.rohmio.gw2.tools.main.Util;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;

public class SettingsWrapper {

	/**
	 * settings file location
	 */
	private static File settingsFile = new File(Util.DOCS, "settings.json");

	private StringProperty accessTokenProperty = new SimpleStringProperty();
	private ObjectProperty<LanguageSelect> languageProperty = new SimpleObjectProperty<>(LanguageSelect.English);

	public SettingsWrapper() {
		loadSettings();
		accessTokenProperty.addListener((observable, oldValue, newValue) -> saveSettings());
		languageProperty.addListener((observable, oldValue, newValue) -> saveSettings());
	}

	public StringProperty accessTokenProperty() {
		return accessTokenProperty;
	}

	public ObjectProperty<LanguageSelect> languageProperty() {
		return languageProperty;
	}

	private void mapFromSettings(Settings settings) {
		accessTokenProperty.set(settings.getAccessToken());
		languageProperty.set(settings.getLang());
	}

	private Settings mapToSettings() {
		Settings settings = new Settings();
		settings.setLang(languageProperty.get());
		settings.setAccessToken(accessTokenProperty.get());
		return settings;
	}

	private void saveSettings() {
		System.out.println("settings changed");
		Util.writeFile(settingsFile, mapToSettings());
	}

	private void loadSettings() {
		if (settingsFile.exists()) {
			Settings settings = Util.readFile(settingsFile, Settings.class);
			if(settings == null) {
				settingsFile.delete();
				loadSettings();
			}
			mapFromSettings(settings);
		} else {
			// settings file does not exist yet
			// create settings file
			saveSettings();
		}
	}

}
