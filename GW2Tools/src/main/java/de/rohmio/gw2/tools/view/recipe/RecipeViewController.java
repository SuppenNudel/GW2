package de.rohmio.gw2.tools.view.recipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.rohmio.gw2.tools.model.Data;
import de.rohmio.gw2.tools.view.RecipeView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeViewController extends RecipeView implements Initializable {

	@FXML
	private Label lbl_outputName;
	@FXML
	private ImageView img_outputItem;
	@FXML
	private Label lbl_source;
	@FXML
	private Label lbl_recipeType;
	@FXML
	private Label lbl_itemType;
	@FXML
	private Label lbl_outputQty;
	@FXML
	private VBox vbox_discipline;
	@FXML
	private Label lbl_reqRating;
	@FXML
	private VBox vbox_ingredients;

	public RecipeViewController(Recipe recipe) {
		super(recipe);
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RecipeView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		List<Integer> list = getRecipe().getIngredients().stream().map(Ingredient::getItemId).collect(Collectors.toList());
		list.add(getRecipe().getOutputItemId());
		int[] itemIds = list.stream().mapToInt(Integer::intValue).toArray();
		List<Item> itemsById = Data.getInstance().getItemsById(itemIds);
		Map<Integer, Item> collect = itemsById.stream()
				.collect(Collectors.toMap(Item::getId, c -> c));
		getRecipe().getIngredients().forEach(i -> {
			Platform.runLater(() -> {
				Item item = collect.get(i.getItemId());
				Label lbl_count = new Label(String.valueOf(i.getCount()));

				ImageView img_itemIcon = new ImageView();
				Platform.runLater(() -> {
					try {
						File imageFile = Data.getInstance().getImage(item.getIcon());
						Image img = new Image(new FileInputStream(imageFile), 20, 20, false, true);
						img_itemIcon.setImage(img);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				Label lbl_name = new Label(item.getName());
				vbox_ingredients.getChildren().add(new HBox(lbl_count, img_itemIcon, lbl_name));
			});
		});
		Platform.runLater(() -> {
			Item outputItem = collect.get(getRecipe().getOutputItemId());
			lbl_outputName.setText(outputItem.getName());

			String icon = outputItem.getIcon();
			Platform.runLater(() -> {
				try {
					File imageFile = Data.getInstance().getImage(icon);
					Image img = new Image(new FileInputStream(imageFile), 64, 64, false, false);
					img_outputItem.setImage(img);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			lbl_itemType.setText(outputItem.getType().name());
		});
		lbl_source.setText(String.valueOf(getRecipe().getFlags()));
		lbl_recipeType.setText(String.valueOf(getRecipe().getType()));
		lbl_outputQty.setText(String.valueOf(getRecipe().getOutputItemCount()));
		for (CraftingDisciplines discipline : getRecipe().getDisciplines()) {
			vbox_discipline.getChildren().add(new Label(String.valueOf(discipline.name())));
		}
		lbl_reqRating.setText(String.valueOf(getRecipe().getMinRating()));
	}

}
