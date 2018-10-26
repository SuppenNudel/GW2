package de.rohmio.gw2.tools.view.recipeTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.rohmio.gw2.tools.model.Util;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import me.xhsun.guildwars2wrapper.GuildWars2Utility;
import me.xhsun.guildwars2wrapper.model.v2.commerce.Prices.Price;

public class PriceView extends GridPane {
	
	// https://render.guildwars2.com/file/{signature}/{file_id}.{format}

	private static String[] icons = {
			"https://render.guildwars2.com/file/090A980A96D39FD36FBB004903644C6DBEFB1FFB/156904.png",
			"https://render.guildwars2.com/file/E5A2197D78ECE4AE0349C8B3710D033D22DB0DA6/156907.png",
			"https://render.guildwars2.com/file/6CF8F96A3299CFC75D5CC90617C3C70331A1EF0E/156902.png"
	};

	public PriceView(String label, Price price) throws IOException {
		addRow(0, new Label(label));
		addRow(1, new Label("Quantity: "), new Label(String.valueOf(price.getQuantity())));
		
		long[] parseCoins = GuildWars2Utility.parseCoins(price.getUnitPrice());
		HBox hBox = new HBox();
		for(int i=0; i<3; ++i) {
			long count = parseCoins[i];
			if(count > 0) {
				File imageFile = Util.getImage(icons[i]);
				Image image = new Image(new FileInputStream(imageFile), 16, 16, false, false);
				ImageView imageView = new ImageView(image);
				Label coinsCount = new Label(String.valueOf(count));
				hBox.getChildren().add(coinsCount);
				hBox.getChildren().add(imageView);
			}
		}
		
		addRow(2, new Label("Price: "), hBox);
		
		setPrefWidth(Region.USE_COMPUTED_SIZE);
		setPrefHeight(Region.USE_COMPUTED_SIZE);
		setAlignment(Pos.TOP_CENTER);
	}

}
