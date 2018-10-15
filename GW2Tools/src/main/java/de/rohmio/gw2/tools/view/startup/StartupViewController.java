package de.rohmio.gw2.tools.view.startup;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.App;
import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.view.main.MainViewController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;

public class StartupViewController implements Initializable {
	
	@FXML
	private ComboBox<LanguageSelect> cb_lang;
	
	@FXML
	private TextField txt_apiKey;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cb_lang.getItems().addAll(LanguageSelect.values());
	}
	
	@FXML
	private void apply() throws IOException {
		GuildWars2.setLanguage(cb_lang.getValue());
		Data.getInstance().setApiKey(txt_apiKey.getText());
		App.setScene(MainViewController.class);
		new Thread(() -> { 
			Data.getInstance().getRecipeProgress().getAll();
		}).start();
	}

}
