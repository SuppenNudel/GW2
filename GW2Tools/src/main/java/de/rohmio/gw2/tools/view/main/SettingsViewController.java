package de.rohmio.gw2.tools.view.main;

import java.net.URL;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.model.Data;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;

public class SettingsViewController implements Initializable {

	@FXML
	private ChoiceBox<LanguageSelect> choice_lang;

	@FXML
	private TextField txt_apiKey;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		choice_lang.getItems().addAll(LanguageSelect.values());

		txt_apiKey.textProperty().bindBidirectional(Data.getInstance().getSettingsWrapper().accessTokenProperty());
		choice_lang.valueProperty().bindBidirectional(Data.getInstance().getSettingsWrapper().languageProperty());
	}

}
