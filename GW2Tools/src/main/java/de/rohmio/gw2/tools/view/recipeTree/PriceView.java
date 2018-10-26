package de.rohmio.gw2.tools.view.recipeTree;

import java.io.IOException;

import de.rohmio.gw2.tools.view.CoinsPanel;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import me.xhsun.guildwars2wrapper.model.v2.commerce.Prices.Price;

public class PriceView extends GridPane {

	public PriceView(String label, Price price, int count) throws IOException {
		addRow(0, new Label(label));
		addRow(1, new Label("Quantity: "), new Label(String.valueOf(price.getQuantity())));
		addRow(2, new Label("Price à: "), new CoinsPanel(price.getUnitPrice()));
		if (count != 1) {
			addRow(3, new Label("Price: "), new CoinsPanel(price.getUnitPrice() * count));
		}

		setPrefWidth(Region.USE_COMPUTED_SIZE);
		setPrefHeight(Region.USE_COMPUTED_SIZE);
		setAlignment(Pos.TOP_CENTER);
	}

}
