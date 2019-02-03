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
	
	private ObjectProperty<ResourceBundle> resources = new SimpleObjectProperty<>();
	private StringProperty accessToken = new SimpleStringProperty();

	private Data() throws NullPointerException, GuildWars2Exception {
		GuildWars2.setInstance(ClientFactory.getClient());
		loadSettings();
		
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
		saveSettings();
	}
	
	public void setAccessToken(String accessToken) {
		accessTokenProperty().set(accessToken);
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
	
	public StringProperty accessTokenProperty() {
		return accessToken;
	}
	public final String getAccessToken() {
		return accessTokenProperty().get();
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
	}
	
	private void saveSettings() {
		Util.writeFile(settingsFile, settings);
	}
	
	public class Settings {
		
		private LanguageSelect lang;
		private String accessToken;
		
		private Settings(String accessToken, LanguageSelect lang) {
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

	
}
