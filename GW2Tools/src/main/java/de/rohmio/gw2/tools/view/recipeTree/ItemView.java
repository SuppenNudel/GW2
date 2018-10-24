package de.rohmio.gw2.tools.view.recipeTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.model.Util;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.model.v2.Item;

public class ItemView extends VBox {

	public ItemView(int itemId, int count) {
		setPrefWidth(USE_COMPUTED_SIZE);
		setPrefHeight(USE_COMPUTED_SIZE);
		
		Item item = Data.getInstance().getItemProgress().getById(itemId);
		try {
			init(item, count);
		} catch (NullPointerException e) {
			System.err.println("Can't load Item with ID " + itemId);
		}
	}

//	public ItemView(Item item, int count) {
//		init(item, count);
//	}

	private void init(Item item, int count) throws NullPointerException {
		setAlignment(Pos.TOP_CENTER);
		if(item == null) {
			return;
		}
		getChildren().add(new Label(String.format("%dx %s", count, item.getName())));
		Platform.runLater(() -> {
			try {
				File imgFile = Util.getImage(item.getIcon());
				Image image = new Image(new FileInputStream(imgFile), 64, 64, false, false);
				getChildren().add(new ImageView(image));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}
