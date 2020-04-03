package de.rohmio.gw2.tools.view.main;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.main.Data;
import de.rohmio.gw2.tools.main.ProxySettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.GuildWars2.LanguageSelect;

public class SettingsViewController implements Initializable {
	
	@FXML
	private ComboBox<LanguageSelect> combo_lang;
	
	@FXML
	private TextField txt_apiKey;
	
	@FXML
	private CheckBox cbx_proxy;
	
	@FXML
	private Label lbl_host;
	@FXML
	private TextField txt_host;

	@FXML
	private Label lbl_port;
	@FXML
	private TextField txt_port;

	@FXML
	private Label lbl_user;
	@FXML
	private TextField txt_user;

	@FXML
	private Label lbl_password;
	@FXML
	private TextField txt_password;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		combo_lang.getItems().addAll(LanguageSelect.values());
		combo_lang.getSelectionModel().select(GuildWars2.getLanguage());
		
		txt_apiKey.setText(Data.getInstance().getAccessToken());

		Control[] proxyControls = {
				lbl_host, txt_host,
				lbl_port, txt_port,
				lbl_user, txt_user,
				lbl_password, txt_password
		};
		for(Control control : proxyControls) {
			control.disableProperty().bind(cbx_proxy.selectedProperty());
		}
	}
	
	@FXML
	private void apply(ActionEvent event) throws IOException {
		LanguageSelect lang = combo_lang.getSelectionModel().getSelectedItem();
		Data.getInstance().setLanguage(lang);
		Data.getInstance().setAccessToken(txt_apiKey.getText());
		
		ProxySettings proxySettings = new ProxySettings(
				txt_host.getText(),
				Integer.parseInt(txt_port.getText()),
				txt_user.getText(),
				txt_password.getText());
		Data.getInstance().setProxySettings(proxySettings);
		
		Node source = (Node) event.getSource();
	    Stage stage = (Stage) source.getScene().getWindow();
	    stage.close();
	}

}
