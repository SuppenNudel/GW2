package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import de.rohmio.gw2.tools.App;
import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.model.RecipeFilter;
import de.rohmio.gw2.tools.view.RecipeView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
	private CheckBox chbx_showAlreadyLearned;

	@FXML
	private Label lbl_currentlyDisplayed;

	@FXML // current tasks done by application
	private VBox vbox_tasks;

	@FXML // POC for progress display
	private ProgressBar pb_getItems;

	@FXML
	private ProgressBar pb_getRecipes;

	@FXML
	private Label lbl_recipes_progress;
	
	// end view elements

	private ObjectProperty<Character> selectedCharacter = new SimpleObjectProperty<>();
	private ObservableList<CraftingDisciplines> disciplinesFilter = FXCollections.observableArrayList();
	private SimpleIntegerProperty minLevel = new SimpleIntegerProperty();
	private SimpleIntegerProperty maxLevel = new SimpleIntegerProperty();
	
//	private ObservableList<RecipeView> recipeViews = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// internationalization
		initResourceBundle();
		
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
			checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if(newValue) {
						disciplinesFilter.add(discipline);
					} else {
						disciplinesFilter.remove(discipline);
					}
				}
			});
			vbox_disciplineCheck.getChildren().add(checkbox);
			checkbox.setUserData(discipline);
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
		
		// create recipe views		
		List<RecipeView> recipeViews = new ArrayList<>();
		new Thread(() -> {
			for(Recipe recipe : Data.getInstance().getRecipes().getAll().values()) {
				RecipeView recipeView = createRecipeView(recipe);
				recipeViews.add(recipeView);
			}
		}).start();

		lbl_currentlyDisplayed.textProperty().bind(Bindings.createStringBinding(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return String.valueOf(recipeViews.stream().map(view -> view.getRecipeFilter()).filter(filter -> filter.getShow().get()).count());
			}
		}));
	}

	private RecipeView createRecipeView(Recipe recipe) {
		RecipeView recipeView = new RecipeView(recipe, false);
		RecipeFilter recipeFilter = recipeView.getRecipeFilter();
		recipeFilter.addDisciplineFilter(disciplinesFilter);
		recipeFilter.addLevelFilter(minLevel, maxLevel);
		Platform.runLater(() -> flow_recipes.getChildren().add(recipeView));
		return recipeView;
	}

	private void initResourceBundle() {
		chbx_byableRecipe.textProperty().bind(Data.getInstance().getStringBinding("show_buyable_recipes"));
		chbx_showWholeRecipe.textProperty().bind(Data.getInstance().getStringBinding("show_whole_recipe"));
		chbx_showAlreadyLearned.textProperty().bind(Data.getInstance().getStringBinding("show_already_learned"));
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

	@FXML
	private void getCharacters() throws GuildWars2Exception {
		choice_charName.getItems().clear();
		GuildWars2 gw2 = GuildWars2.getInstance();
		List<String> allCharacterName = gw2.getSynchronous().getAllCharacterName(Data.getInstance().getSettingsWrapper().accessTokenProperty().get());
		choice_charName.getItems().addAll(allCharacterName);
	}

	// TODO do loading on separate thread, so that the program doesn't freeze when
	// it takes longer
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
			return;
		}
		
		try {
			selectedCharacter.set(GuildWars2.getInstance().getSynchronous().getCharacter(Data.getInstance().getSettingsWrapper().accessTokenProperty().get(), characterName));
//			recipeFilter.addCharacterFilter(selectedCharacter);
		} catch (GuildWars2Exception e) {
			e.printStackTrace();
		}

	}

}
