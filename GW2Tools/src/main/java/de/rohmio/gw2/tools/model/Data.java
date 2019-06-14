package de.rohmio.gw2.tools.model;

import java.util.Locale;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.main.ClientFactory;
import de.rohmio.gw2.tools.model.settings.SettingsWrapper;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class Data {

	// singleton
	private static Data data;

	// internationalization
	private ObjectProperty<ResourceBundle> resources = new SimpleObjectProperty<>();

	// loaded data
	private RequestProgress<Recipe> recipes;
	// private RequestProgress<Item> items;

	private SettingsWrapper settingsWrapper;

	private Data() throws NullPointerException, GuildWars2Exception {
		settingsWrapper = new SettingsWrapper();
		resources.set(ResourceBundle.getBundle("bundle.MyBundle",
				new Locale(settingsWrapper.languageProperty().get().toString())));
		GuildWars2.setInstance(ClientFactory.getClient());
		// items = new RequestProgress<>(RequestType.ITEM);
	}

	public GuildWars2 getApi() {
		return GuildWars2.getInstance();
	}

	public synchronized static Data getInstance() {
		if (data == null) {
			try {
				data = new Data();
			} catch (NullPointerException | GuildWars2Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public StringBinding getStringBinding(String key) {
		return new StringBinding() {
			{
				bind(resources);
			}

			@Override
			protected String computeValue() {
				return resources.get().getString(key);
			}
		};
	}

	public ObjectProperty<ResourceBundle> getResources() {
		return resources;
	}

	public RequestProgress<Recipe> getRecipes() {
		if (recipes == null) {
			recipes = new RequestProgress<>(RequestType.RECIPE);
		}
		return recipes;
	}

	// TODO first only with recipes
	// public RequestProgress<Item> getItems() {
	// return items;
	// }

	public SettingsWrapper getSettingsWrapper() {
		return settingsWrapper;
	}

}
