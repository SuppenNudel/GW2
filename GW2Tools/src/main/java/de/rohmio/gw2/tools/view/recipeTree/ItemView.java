package de.rohmio.gw2.tools.view.recipeTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.model.Util;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Item.Flag;
import me.xhsun.guildwars2wrapper.model.v2.commerce.Prices;
import me.xhsun.guildwars2wrapper.model.v2.commerce.Prices.Price;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemView extends VBox {

	private boolean detailed;

	public ItemView(int itemId, int count, boolean detailed) {
		setPrefWidth(USE_COMPUTED_SIZE);
		setPrefHeight(USE_COMPUTED_SIZE);

		this.detailed = detailed;

		Item item = Data.getInstance().getItemProgress().getById(itemId);
		try {
			init(item, count);
		} catch (NullPointerException e) {
			System.err.println("Can't load Item with ID " + itemId);
		}
	}

	// public ItemView(Item item, int count) {
	// init(item, count);
	// }

	private void init(Item item, int count) throws NullPointerException {
		setAlignment(Pos.TOP_CENTER);
		if (item == null) {
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

		if (detailed) {
			if(item.getFlags().contains(Flag.AccountBound)) {
				Label label = new Label(Flag.AccountBound.toString());
				Platform.runLater(() -> {
					getChildren().add(label);
				});
			} else {
				try {
					GuildWars2.getInstance().getAsynchronous().getPriceInfo(new int[] { item.getId() },
							new Callback<List<Prices>>() {
								@Override
								public void onFailure(Call<List<Prices>> call, Throwable t) {
								}
	
								@Override
								public void onResponse(Call<List<Prices>> call, Response<List<Prices>> response) {
									List<Prices> prices = response.body();
									if (prices == null) {
										VBox vBoxFlags = new VBox();
										vBoxFlags.setAlignment(Pos.TOP_CENTER);
										vBoxFlags.getChildren().add(new Label("No Listings:"));
										Label label = new Label(item.getFlags().toString());
										label.setWrapText(true);
										vBoxFlags.getChildren().add(label);
										// for (Flag flag : item.getFlags()) {
										// vBoxFlags.getChildren().add(new Label(flag.name()));
										// }
										Platform.runLater(() -> {
											getChildren().add(vBoxFlags);
										});
									} else {
										Prices price = prices.get(0);
										Price buys = price.getBuys();
										VBox vBoxBuys = new VBox(new Label("Buys"),
												// new Label("Listings: " + buys.getListings()),
												new Label("Quantity: " + buys.getQuantity()),
												new Label("Price: " + buys.getUnitPrice()));
	
										Price sells = price.getSells();
										VBox vBoxSells = new VBox(new Label("Sells"),
												// new Label("Listings: " + sells.getListings()),
												new Label("Quantity: " + sells.getQuantity()),
												new Label("Price: " + sells.getUnitPrice()));
										Platform.runLater(() -> {
											HBox hbox = new HBox(vBoxBuys, vBoxSells);
											hbox.setAlignment(Pos.TOP_CENTER);
											getChildren().add(hbox);
										});
									}
								}
							});
				} catch (GuildWars2Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
