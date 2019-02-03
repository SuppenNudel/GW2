package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.main.Data;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;

public class SettingsViewController implements Initializable {
	
	@FXML
	private ComboBox<LanguageSelect> combo_lang;
	
	@FXML
	private TextField txt_apiKey;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		combo_lang.getItems().addAll(LanguageSelect.values());
		combo_lang.getSelectionModel().select(GuildWars2.getLanguage());
		
		txt_apiKey.setText(Data.getInstance().getAccessToken());
	}
	
	@FXML
	private void apply(ActionEvent event) throws IOException {
		LanguageSelect lang = combo_lang.getSelectionModel().getSelectedItem();
		Data.getInstance().setLanguage(lang);
		Data.getInstance().setAccessToken(txt_apiKey.getText());
		
		Node source = (Node) event.getSource();
	    Stage stage = (Stage) source.getScene().getWindow();
	    stage.close();
	}

}
