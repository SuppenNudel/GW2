package de.rohmio.gw2.tools;

import java.net.URL;

import de.rohmio.gw2.tools.view.main.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		URL resource = MainViewController.class.getResource("MainView.fxml");
		Parent root = FXMLLoader.load(resource);
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Guild Wars 2 Tools");
		primaryStage.show();
	}

}
