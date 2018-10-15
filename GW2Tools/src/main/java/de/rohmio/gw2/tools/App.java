package de.rohmio.gw2.tools;

import java.io.IOException;
import java.net.URL;

import de.rohmio.gw2.tools.view.startup.StartupViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
	
	private static Stage stage;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public static void setScene(Class<? extends Initializable> initializable) throws IOException {
		String fileName_view = initializable.getSimpleName().replaceAll("Controller", "");
		URL resource = initializable.getResource(String.format("/fxml/%s.fxml", fileName_view));
		Parent root = FXMLLoader.load(resource);
		Scene scene = new Scene(root);
		stage.setScene(scene);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		App.setScene(StartupViewController.class);
		primaryStage.setTitle("Guild Wars 2 Tools");
		primaryStage.show();
	}

}
