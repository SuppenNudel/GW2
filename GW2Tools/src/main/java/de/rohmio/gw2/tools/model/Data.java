package de.rohmio.gw2.tools.model;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.main.ClientFactory;
import de.rohmio.gw2.tools.main.Util;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class Data {
	
	// singleton
	private static Data data;
	
	/**
	 * location where all app data is stored
	 */
	public static File DOCS = new File(System.getProperty("user.home")+"/AppData/Roaming/GW2 Tools");

	/**
	 * settings file location
	 */
	private static File settingsFile = new File(DOCS, "settings.json");
	private Settings settings;
	
	private StringProperty accessTokenProperty = new SimpleStringProperty();
	
	private ObjectProperty<ResourceBundle> resources = new SimpleObjectProperty<>();
	
	private RequestProgress<Recipe> recipes;
	
	private Data() throws NullPointerException, GuildWars2Exception {
		GuildWars2.setInstance(ClientFactory.getClient());
		getSettings();
		recipes = new RequestProgress<>(RequestType.RECIPE);
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
	
	public ObjectProperty<ResourceBundle> resourcesProperty() {
		return resources;
	}
	private final void setResources(ResourceBundle resources) {
		resourcesProperty().set(resources);
	}
	public final ResourceBundle getResources() {
		return resourcesProperty().get();
	}
	
	public StringProperty accessTokenProperty() {
		return accessTokenProperty;
	}
	public void setAccessToken(String accessToken) {
		settings.setAccessToken(accessToken);
		accessTokenProperty.set(accessToken);
		saveSettings();
	}
	public final String getAccessToken() {
		return settings.getAccessToken();
	}
	
	public StringBinding getStringBinding(String key) {
		return new StringBinding() {
			{ bind(resourcesProperty()); }
			@Override
			protected String computeValue() {
				return getResources().getString(key);
			}
		};
	}
	
	public void setLanguage(LanguageSelect lang) {
		GuildWars2.setLanguage(lang);
		Locale locale = new Locale(lang.getValue());
		ResourceBundle resources = ResourceBundle.getBundle("bundle.MyBundle", locale);
		setResources(resources);
		settings.setLang(lang);
		saveSettings();
	}
	
	private void saveSettings() {
		Util.writeFile(settingsFile, settings);
	}
	
	public Settings getSettings() {
		if(settings == null) {
			if(settingsFile.exists()) {
				settings = Util.readFile(settingsFile, Settings.class);
			} else {
				// settings file does not exist yet
				// create settings file
				settings = new Settings("", GuildWars2.getLanguage());
			}
			setLanguage(settings.getLang());
			setAccessToken(settings.getAccessToken());
		}
		return settings;
	}
	
	public RequestProgress<Recipe> getRecipes() {
		return recipes;
	}
	
}
