package de.rohmio.gw2.tools;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.model.Settings;
import de.rohmio.gw2.tools.view.main.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.xhsun.guildwars2wrapper.GuildWars2;

public class App extends Application {
	
	private static Stage stage;
	private static Class<? extends Initializable> startingView = MainViewController.class;
	
	public static void main(String[] args) {
		Settings.getInstance();
		launch(args);
	}
	
	public static Scene createScene(Class<? extends Initializable> controllerClass) throws IOException {
		String fileName_view = controllerClass.getSimpleName().replaceAll("Controller", "");
		URL location = controllerClass.getResource(String.format("/fxml/%s.fxml", fileName_view));
		ResourceBundle resources = ResourceBundle.getBundle("bundle.MyBundle", new Locale(GuildWars2.getLanguage().getValue()));
		Parent root = FXMLLoader.load(location, resources);
		Scene scene = new Scene(root);
		return scene;
	}

	public static void setScene(Class<? extends Initializable> controllerClass) throws IOException {
		Scene scene = App.createScene(controllerClass);
		stage.setScene(scene);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		setScene(startingView);
		primaryStage.setTitle("Guild Wars 2 Tools");
		primaryStage.show();
	}
	
	public static Stage getStage() {
		return stage;
	}

}
