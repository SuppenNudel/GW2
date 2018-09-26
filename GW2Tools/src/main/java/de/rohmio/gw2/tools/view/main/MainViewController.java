package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.rohmio.gw2.tools.model.ClientFactory;
import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.view.recipe.RecipeViewController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Flag;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.character.CharacterCraftingLevel.Discipline;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class MainViewController implements Initializable {

	@FXML
	private TextField txt_apiKey;
	@FXML
	private TextField txt_charName;

	@FXML
	private HBox hbox_disciplineCheck;
	@FXML
	private HBox hbox_langRadio;

	@FXML
	private FlowPane scroll_recipes;

	@FXML
	private VBox vbox_tasks;

	@FXML
	private Map<CraftingDisciplines, CheckBox> cbx_craftingDisceplines;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		txt_apiKey.setText(ClientFactory.ACCESS_KEY);
		txt_charName.setText(ClientFactory.CHAR_NAME);

		cbx_craftingDisceplines = new HashMap<>();
		for (CraftingDisciplines discipline : CraftingDisciplines.values()) {
			CheckBox checkBox = new CheckBox(discipline.toString());
			cbx_craftingDisceplines.put(discipline, checkBox);
			hbox_disciplineCheck.getChildren().add(checkBox);
		}

		ToggleGroup langGroup = new ToggleGroup();
		for (LanguageSelect lang : LanguageSelect.values()) {
			RadioButton radio = new RadioButton(lang.getValue());
			radio.setToggleGroup(langGroup);
			radio.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					GuildWars2.setLanguage(lang);
				}
			});
			hbox_langRadio.getChildren().add(radio);
		}

	}

	@FXML
	private void analyse() throws GuildWars2Exception, IOException {
		scroll_recipes.getChildren().clear();

		List<Recipe> allRecipes = Data.getInstance().getAllRecipes();
		Character character = GuildWars2.getInstance().getSynchronous().getCharacter(txt_apiKey.getText(),
				txt_charName.getText());

		List<Integer> charRecipes = character.getRecipes();

		List<Recipe> collect = allRecipes.stream().filter(r -> !charRecipes.contains(r.getId())) // remove already
																									// learned
				.filter(r -> !r.getFlags().contains(Flag.LearnedFromItem)) // remove only available through item
				.filter(r -> {
					for (Discipline discipline : character.getCrafting()) { // only available by discipline and rating
						if (r.getDisciplines().contains(discipline.getDiscipline())
								&& r.getMinRating() <= discipline.getRating()) {
							return true;
						}
					}
					return false;
				}).filter(r -> {
					for (CraftingDisciplines discipline : r.getDisciplines()) {
						if (cbx_craftingDisceplines.get(discipline).isSelected()) {
							return true;
						}
					}
					return false;
				}).collect(Collectors.toList());

		System.out.println("All: " + allRecipes.size());
		System.out.println("Char: " + charRecipes.size());
		System.out.println("Collect: " + collect.size());

		for (Recipe recipe : collect) {
			RecipeViewController controller = new RecipeViewController(recipe);
			scroll_recipes.getChildren().add(controller);
		}
	}

}
