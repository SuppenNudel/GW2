package de.rohmio.gw2.tools;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.rohmio.gw2.tools.main.Data;
import de.rohmio.gw2.tools.view.main.MainViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
	
	private static Stage stage;
	private static Class<? extends Initializable> startingView = MainViewController.class;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public static Scene createScene(Class<? extends Initializable> controllerClass) throws IOException {
		String fileName_view = controllerClass.getSimpleName().replaceAll("Controller", "");
		URL location = controllerClass.getResource(String.format("/fxml/%s.fxml", fileName_view));
		
		ResourceBundle resources = Data.getInstance().getResources();
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
		primaryStage.setOnCloseRequest(event -> { 
			Platform.exit();
			System.exit(0);
		});
		primaryStage.show();
	}
	
	public static Stage getStage() {
		return stage;
	}

}
