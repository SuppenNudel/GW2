package de.rohmio.gw2.tools.main;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;
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
	
	private RequestProgress<Item> itemProgress;
	private RequestProgress<Recipe> recipeProgress;
	
	private StringProperty accessTokenProperty = new SimpleStringProperty();
	
	private ObjectProperty<ProxySettings> proxySettingsProperty = new SimpleObjectProperty<>();
	
	private ObjectProperty<ResourceBundle> resources = new SimpleObjectProperty<>();

	private Data() throws NullPointerException, GuildWars2Exception {
		loadSettings();
		ProxySettings proxySettings = settings.getProxySettings();
		GuildWars2.setInstance(ClientFactory.getClient(proxySettings));
		
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
	
	public ObjectProperty<ResourceBundle> resourcesProperty() {
		return resources;
	}
	public final ResourceBundle getResources() {
		return resourcesProperty().get();
	}
	private final void setResources(ResourceBundle resources) {
		resourcesProperty().set(resources);
	}
	
	public void setLanguage(LanguageSelect lang) {
		GuildWars2.setLanguage(lang);
		Locale locale = new Locale(lang.getValue());
		ResourceBundle resources = ResourceBundle.getBundle("bundle.MyBundle", locale);
		setResources(resources);
		settings.setLang(lang);
		saveSettings();
	}
	
	public void setProxySettings(ProxySettings proxySettings) {
		settings.setProxySettings(proxySettings);
		proxySettingsProperty.set(proxySettings);
		saveSettings();
	}
	
	public void setAccessToken(String accessToken) {
		settings.setAccessToken(accessToken);
		accessTokenProperty.set(accessToken);
		saveSettings();
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
	
	public final ProxySettings getProxySettings() {
		return settings.getProxySettings();
	}
	
	public final String getAccessToken() {
		return settings.getAccessToken();
	}
	
	private void loadSettings() {
		if(settingsFile.exists()) {
			settings = Util.readFile(settingsFile, Settings.class);
		} else {
			settings = new Settings("", GuildWars2.getLanguage());
			setLanguage(settings.getLang());
		}
		setLanguage(settings.getLang());
		setAccessToken(settings.getAccessToken());
		setProxySettings(settings.getProxySettings());
	}
	
	private void saveSettings() {
		Util.writeFile(settingsFile, settings);
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	public StringProperty accessTokenProperty() {
		return accessTokenProperty;
	}
	
	public ObjectProperty<ProxySettings> getProxySettingsProperty() {
		return proxySettingsProperty;
	}
	
}
