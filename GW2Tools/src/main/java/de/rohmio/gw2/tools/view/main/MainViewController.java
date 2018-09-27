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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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

	@FXML // selection for disciplines
	private HBox hbox_disciplineCheck;

	@FXML // selection for language
	private HBox hbox_langRadio;
	
	@FXML // list of all disciplines the character has
	private VBox vbox_charDisciplines;

	@FXML // all recipes displayed
	private FlowPane scroll_recipes;
	
	@FXML
	private CheckBox chbx_fromRecipe;

	@FXML // current tasks done by application
	private VBox vbox_tasks;

	@FXML // POC for progress display
	private ProgressBar pb_getItems;

	private Map<CraftingDisciplines, CheckBox> craftingDisceplinesToCheckBox;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		pb_getItems.progressProperty().bind(Data.getInstance().progress);
		new Thread(() -> {
			try {
				Data.getInstance().getAllRecipes();
			} catch (GuildWars2Exception e) {
				e.printStackTrace();
			}
		}).start();

		txt_apiKey.setText(ClientFactory.ACCESS_KEY);
		txt_charName.setText(ClientFactory.CHAR_NAME);

		// create check boxes for discipline selection
		craftingDisceplinesToCheckBox = new HashMap<>();
		for (CraftingDisciplines discipline : CraftingDisciplines.values()) {
			CheckBox checkBox = new CheckBox(discipline.toString());
			craftingDisceplinesToCheckBox.put(discipline, checkBox);
			hbox_disciplineCheck.getChildren().add(checkBox);
		}

		// create radio buttons for language
		ToggleGroup langGroup = new ToggleGroup();
		for (LanguageSelect lang : LanguageSelect.values()) {
			RadioButton radio = new RadioButton(lang.getValue());
			radio.setToggleGroup(langGroup);
			radio.setOnAction(event -> GuildWars2.setLanguage(lang));
			hbox_langRadio.getChildren().add(radio);
		}

	}

	@FXML
	private void analyse() throws GuildWars2Exception, IOException {
		// clear previous analysation
		scroll_recipes.getChildren().clear();

		// get ALL recipes
		List<Recipe> allRecipes = Data.getInstance().getAllRecipes();

		// get recipes selected character has already learned
		Character character = GuildWars2.getInstance().getSynchronous().getCharacter(txt_apiKey.getText(),
				txt_charName.getText());
		
		for(Discipline discipline : character.getCrafting()) {
			vbox_charDisciplines.getChildren().add(new Label(String.format("%s: %d - active: %s", discipline.getDiscipline().name(), discipline.getRating(), discipline.isActive())));
		}

		List<Integer> charRecipes = character.getRecipes();

		List<Recipe> collect = allRecipes.stream()
				 // remove already learned
				.filter(r -> !charRecipes.contains(r.getId()))
				 // remove only available through item
				.filter(r -> chbx_fromRecipe.isSelected() || !r.getFlags().contains(Flag.LearnedFromItem))
				// only disciplines that are checked
				.filter(r -> {
					for (CraftingDisciplines discipline : r.getDisciplines()) {
						if (craftingDisceplinesToCheckBox.get(discipline).isSelected()) {
							return true;
						}
					}
					return false;
				})
				 // only available by discipline and rating
				.filter(r -> {
					for (Discipline discipline : character.getCrafting()) {
						if (r.getDisciplines().contains(discipline.getDiscipline())
								&& r.getMinRating() <= discipline.getRating()) {
							return true;
						}
					}
					return false;
				})
				.collect(Collectors.toList());

		System.out.println("All: " + allRecipes.size());
		System.out.println("Char: " + charRecipes.size());
		System.out.println("To Discover: " + collect.size());

		// display all discoverable recipes
		for (Recipe recipe : collect) {
			RecipeViewController controller = new RecipeViewController(recipe);
			scroll_recipes.getChildren().add(controller);
		}
	}

}
