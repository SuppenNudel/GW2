package de.rohmio.gw2.tools.view.recipeTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.rohmio.gw2.tools.model.Data;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.model.v2.Item;

public class ItemView extends VBox {
	
	public ItemView(Item item) {
		setAlignment(Pos.CENTER);
		
		getChildren().add(new Label(item.getName()));
		Platform.runLater(() -> {
			try {
				File imgFile = Data.getInstance().getImage(item.getIcon());
				Image image = new Image(new FileInputStream(imgFile), 64, 64, false, false);
				getChildren().add(new ImageView(image));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}
