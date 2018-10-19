package de.rohmio.gw2.tools.model;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.model.RequestProgress.RequestType;
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
	
	private static Data data;

	private static File settingsFile = new File("data/settings.json");
	
	private RequestProgress<Item> itemProgress;
	private RequestProgress<Recipe> recipeProgress;
	
	private ObjectProperty<ResourceBundle> resources = new SimpleObjectProperty<>();
	private StringProperty accessToken = new SimpleStringProperty();

	private Data() throws NullPointerException, GuildWars2Exception {
		try {
			GuildWars2.setInstance(ClientFactory.getClient());
		} catch (GuildWars2Exception e) {
			e.printStackTrace();
		}
		
		itemProgress = new RequestProgress<>(RequestType.ITEM);
		recipeProgress = new RequestProgress<>(RequestType.RECIPE);
		
		Settings settings = Util.readFile(settingsFile, Settings.class);
		if(settings == null) {
			setLanguage(GuildWars2.getLanguage());
		} else {
			setLanguage(settings.getLang());
			setAccessToken(settings.getAccessToken());
		}
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
		Locale locale = new Locale(lang.getValue());
		GuildWars2.setLanguage(lang);
		ResourceBundle resources = ResourceBundle.getBundle("bundle.MyBundle", locale);
		setResources(resources);
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
	public final void setAccessToken(String accessToken) {
		accessTokenProperty().set(accessToken);
		saveSettings();
	}
	
	private void saveSettings() {
		Util.writeFile(settingsFile, new Settings(getAccessToken(), GuildWars2.getLanguage()));
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
