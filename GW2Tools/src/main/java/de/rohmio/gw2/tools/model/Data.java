package de.rohmio.gw2.tools.model;

import java.util.Locale;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.model.RequestProgress.RequestType;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class Data {
	
	// TODO keep settings values here
	// TODO use Settings class only to save settings in json file and load it from there

	private static Data data;
	
	private RequestProgress<Item> itemProgress;
	private RequestProgress<Recipe> recipeProgress;
	
	private ObjectProperty<ResourceBundle> resources = new SimpleObjectProperty<>();

	private Data() throws NullPointerException, GuildWars2Exception {
		try {
			GuildWars2.setInstance(ClientFactory.getClient());
		} catch (GuildWars2Exception e) {
			e.printStackTrace();
		}
		
		itemProgress = new RequestProgress<>(RequestType.ITEM);
		recipeProgress = new RequestProgress<>(RequestType.RECIPE);
	}

	public static Data getInstance() {
		if (data == null) {
			try {
				data = new Data();
				ResourceBundle resources = ResourceBundle.getBundle("bundle.MyBundle", new Locale(GuildWars2.getLanguage().getValue()));
				data.setResources(resources);
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
	public final void setResources(ResourceBundle resources) {
		resourcesProperty().set(resources);
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
	
}
