package de.rohmio.gw2.tools.view.recipeTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import de.rohmio.gw2.tools.main.Util;
import de.rohmio.gw2.tools.model.Data;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemView extends VBox {

	private boolean detailed;
	private int itemId;
	private Item item;
	private int count;

	public ItemView(int itemId, int count, boolean detailed) {
		setPrefWidth(USE_COMPUTED_SIZE);
		setPrefHeight(USE_COMPUTED_SIZE);
		
		this.itemId = itemId;
		
		this.detailed = detailed;

		this.count = count;
		try {
			init();
		} catch (NullPointerException e) {
			System.err.println("Can't load Item with ID " + itemId);
		}
	}
	
	public void show(boolean show) {
		if(show) {
			item = Data.getInstance().getItems().getById(itemId);
			init();
		}
	}

	// public ItemView(Item item, int count) {
	// init(item, count);
	// }
	
	// TODO put detailed recipe contextMenu here

	private void init() throws NullPointerException {
//		ContextMenu contextMenu = createContextMenu();
//		Node owner = this;
//		setOnContextMenuRequested(event -> {
//			contextMenu.show(owner, event.getScreenX(), event.getScreenY());
//			event.consume();
//		});
		
		setAlignment(Pos.TOP_CENTER);
		if (item == null) {
			return;
		}
		Platform.runLater(() -> {
			getChildren().add(new Label(String.format("%dx %s", count, item.getName())));
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
										vBoxFlags.setPrefWidth(USE_COMPUTED_SIZE);
										vBoxFlags.setPrefHeight(USE_COMPUTED_SIZE);
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
										try {
											PriceView buysView = new PriceView("Buys", price.getBuys(), count);
											PriceView sellsView = new PriceView("Sells", price.getSells(), count);
											Platform.runLater(() -> {
												HBox hbox = new HBox(buysView, sellsView);
												hbox.setAlignment(Pos.TOP_CENTER);
												getChildren().add(hbox);
											});
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
							});
				} catch (GuildWars2Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

//	private ContextMenu createContextMenu() {
//		MenuItem openWiki = new MenuItem("Open Wiki");
//		openWiki.setOnAction(event -> {
//			if (java.awt.Desktop.isDesktopSupported()) {
//				try {
//					String path = item.getName().replace(" ", "_");
//					java.awt.Desktop.getDesktop().browse(new URI("https://wiki.guildwars2.com/wiki/"+path));
//				} catch (IOException e) {
//					e.printStackTrace();
//				} catch (URISyntaxException e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		return new ContextMenu(openWiki);
//	}

}
