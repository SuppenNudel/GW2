package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.rohmio.gw2.tools.App;
import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.model.RecipeFilter;
import de.rohmio.gw2.tools.view.RecipeView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.LongBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.account.Account;
import me.xhsun.guildwars2wrapper.model.v2.character.Character;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class MainViewController implements Initializable {

	// Begin view elements
	
	@FXML
	private ChoiceBox<String> choice_charName;

	@FXML
	private CheckBox chbx_pauseFilter;

	@FXML
	private Label lbl_accountName;

	@FXML
	private TextField txt_itemNameFilter;

	@FXML
	private TextField txt_minLevel;

	@FXML
	private TextField txt_maxLevel;

	@FXML // selection for disciplines
	private VBox vbox_disciplineCheck;

	@FXML // all recipes displayed
	private FlowPane flow_recipes;

	@FXML
	private CheckBox chbx_byableRecipe;

	@FXML
	private CheckBox chbx_showWholeRecipe;

	@FXML
	private CheckBox chbx_showAutoLearned;
	
	@FXML
	private CheckBox chbx_showDiscoverable;

	@FXML
	private Label lbl_currentlyDisplayed;

	@FXML // POC for progress display
	private ProgressBar pb_getItems;

	@FXML
	private ProgressBar pb_getRecipes;

	@FXML
	private Label lbl_recipes_progress;
	
	// filter properties
	
	private final ObservableList<RecipeFilter> recipeFilters = FXCollections.observableArrayList();
	
	private ObjectProperty<Character> selectedCharacter = new SimpleObjectProperty<>();
	// selected Disciplines
	private ObservableMap<CraftingDisciplines, Boolean> disciplinesFilter = FXCollections.observableHashMap();
	private SimpleIntegerProperty minLevel = new SimpleIntegerProperty();
	private SimpleIntegerProperty maxLevel = new SimpleIntegerProperty();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// internationalization
		initResourceBundle();
		
		chbx_showDiscoverable.setSelected(true);
		
		// bind progress 
		DoubleProperty progress = Data.getInstance().getRecipes().getProgress();
		progress.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {
					pb_getRecipes.setProgress(newValue.doubleValue());
					NumberFormat format = NumberFormat.getPercentInstance();
					lbl_recipes_progress.setText(format.format(newValue));
				});
			}
		});
		Platform.runLater(() -> {
			pb_getRecipes.setProgress(progress.doubleValue());
			NumberFormat format = NumberFormat.getPercentInstance();
			lbl_recipes_progress.setText(format.format(progress.doubleValue()));
		});
		
		// discipline selection
		for (CraftingDisciplines discipline : CraftingDisciplines.values()) {
			CheckBox checkbox = new CheckBox(discipline.name());
			disciplinesFilter.put(discipline, checkbox.isSelected());
			checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					disciplinesFilter.put(discipline, newValue);
				}
			});
			vbox_disciplineCheck.getChildren().add(checkbox);
		}

		// level filter integer only
		txt_minLevel.textProperty().addListener(new IntOnlyTextFieldChangeListener(txt_minLevel));
		txt_maxLevel.textProperty().addListener(new IntOnlyTextFieldChangeListener(txt_maxLevel));

		StringProperty apiKeyProperty = Data.getInstance().getSettingsWrapper().accessTokenProperty();
		checkApiKey(apiKeyProperty.get());
		apiKeyProperty.addListener((observable, oldValue, newValue) -> checkApiKey(newValue));

		choice_charName.setOnAction((event) -> onSelectCharacter());
		
		Bindings.bindBidirectional(txt_minLevel.textProperty(), minLevel, new NumberStringConverter());
		Bindings.bindBidirectional(txt_maxLevel.textProperty(), maxLevel, new NumberStringConverter());
		
		new Thread(() -> {
			for(Recipe recipe : Data.getInstance().getRecipes().getAll().values()) {
				createRecipeView(recipe);
			}
		}).start();

		List<BooleanProperty> collect = recipeFilters.stream().map(filter -> filter.getShow()).collect(Collectors.toList());
		BooleanProperty[] collectArr = collect.toArray(new BooleanProperty[collect.size()]);
		LongBinding count = Bindings.createLongBinding(() -> recipeFilters.stream().filter(f -> f.getShow().get()).count(), collectArr);
		count.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue);
			}
		});
	}
	
	private void createRecipeView(Recipe recipe) {
		RecipeFilter recipeFilter = new RecipeFilter(recipe);
		recipeFilters.add(recipeFilter);
		recipeFilter.addDisciplineFilter(disciplinesFilter);
		recipeFilter.addLevelFilter(minLevel, maxLevel);
		recipeFilter.addCharacterFilter(selectedCharacter);
		recipeFilter.addLearnedFromItemFilter(chbx_byableRecipe.selectedProperty());
		recipeFilter.addAutoLearnedFilter(chbx_showAutoLearned.selectedProperty());
		recipeFilter.addDiscoverableFilter(chbx_showDiscoverable.selectedProperty());
		RecipeView recipeView = new RecipeView(recipeFilter, false);
		recipeFilter.getShow().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue && !flow_recipes.getChildren().contains(recipeView)) {
					Platform.runLater(() -> flow_recipes.getChildren().add(recipeView));
				}
				int iterator = 0;
				for(RecipeFilter filter : recipeFilters) {
					synchronized (filter) {
						if(filter.getShow().get()) {
							++iterator;
						}
					}
				}
				final int count = iterator;
				Platform.runLater(() -> lbl_currentlyDisplayed.setText(String.valueOf(count)));
			}
		});
	}

	private void initResourceBundle() {
		chbx_byableRecipe.textProperty().bind(Data.getInstance().getStringBinding("show_buyable_recipes"));
		chbx_showAutoLearned.textProperty().bind(Data.getInstance().getStringBinding("show_auto_learned"));
		chbx_showWholeRecipe.textProperty().bind(Data.getInstance().getStringBinding("show_whole_recipe"));
	}

	private void checkApiKey(String apiKey) {
		try {
			Account accountInfo = GuildWars2.getInstance().getSynchronous().getAccountInfo(apiKey);
			String name = accountInfo.getName();
			lbl_accountName.setText(name);
			getCharacters();
		} catch (GuildWars2Exception e) {
			lbl_accountName.setText(e.getMessage());
		}
	}

	@FXML
	private void openSettings() throws IOException {
		Scene scene = App.createScene(SettingsViewController.class);
		Stage stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(App.getStage());
		stage.setScene(scene);
		stage.showAndWait();
	}

	private void getCharacters() throws GuildWars2Exception {
		choice_charName.getItems().clear();
		GuildWars2 gw2 = GuildWars2.getInstance();
		List<String> allCharacterName = gw2.getSynchronous().getAllCharacterName(Data.getInstance().getSettingsWrapper().accessTokenProperty().get());
		choice_charName.getItems().add(null);
		choice_charName.getItems().addAll(allCharacterName);
	}

	/**
	 * <ul>
	 * <li>reset filters</li>
	 * <li>start displaying recipes unlockable by the character</li>
	 * </ul>
	 * 
	 * @throws GuildWars2Exception
	 */
	private void onSelectCharacter() {
		String characterName = choice_charName.getSelectionModel().getSelectedItem();
		if (characterName == null) {
			selectedCharacter.set(null);
			return;
		}
		
		try {
			selectedCharacter.set(GuildWars2.getInstance().getSynchronous().getCharacter(Data.getInstance().getSettingsWrapper().accessTokenProperty().get(), characterName));
		} catch (GuildWars2Exception e) {
			e.printStackTrace();
		}

	}
	
}
