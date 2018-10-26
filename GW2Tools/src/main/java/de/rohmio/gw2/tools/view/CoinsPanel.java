package de.rohmio.gw2.tools.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.rohmio.gw2.tools.model.Util;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import me.xhsun.guildwars2wrapper.GuildWars2Utility;

public class CoinsPanel extends HBox {
	
	// https://render.guildwars2.com/file/{signature}/{file_id}.{format}

	private static String[] icons = {
			"https://render.guildwars2.com/file/090A980A96D39FD36FBB004903644C6DBEFB1FFB/156904.png",
			"https://render.guildwars2.com/file/E5A2197D78ECE4AE0349C8B3710D033D22DB0DA6/156907.png",
			"https://render.guildwars2.com/file/6CF8F96A3299CFC75D5CC90617C3C70331A1EF0E/156902.png"
	};
	
	public CoinsPanel(long coins) {
		long[] parsedCoins = GuildWars2Utility.parseCoins(coins);
		for(int i=0; i<3; ++i) {
			long count = parsedCoins[i];
			if(count > 0) {
				Label coinsCount = new Label(String.valueOf(count));
				getChildren().add(coinsCount);
				try {
					File imageFile = Util.getImage(icons[i]);
					Image image = new Image(new FileInputStream(imageFile), 16, 16, false, false);
					ImageView imageView = new ImageView(image);
					getChildren().add(imageView);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
