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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.Item;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeViewController extends VBox implements Initializable {

	@FXML
	private Label lbl_outputName;
	@FXML
	private ImageView img_outputItem;
	@FXML
	private Label lbl_source;
	@FXML
	private Label lbl_materialType;
	@FXML
	private Label lbl_type;
	@FXML
	private Label lbl_outputQty;
	@FXML
	private VBox vbox_discipline;
	@FXML
	private Label lbl_reqRating;
	@FXML
	private VBox vbox_ingredients;

	private Recipe recipe;

	public RecipeViewController(Recipe recipe) {
		this.recipe = recipe;

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
		GuildWars2 gw2 = GuildWars2.getInstance();
		List<Integer> list = recipe.getIngredients().stream().map(i -> i.getItemId()).collect(Collectors.toList());
		list.add(recipe.getOutputItemId());
		int[] itemIds = list.stream().mapToInt(i -> i.intValue()).toArray();
		try {
			gw2.getAsynchronous().getItemInfo(itemIds, new Callback<List<Item>>() {
				@Override
				public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
					Map<Integer, Item> collect = response.body().stream()
							.collect(Collectors.toMap(Item::getId, c -> c));
					recipe.getIngredients().forEach(i -> {
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
						Item outputItem = collect.get(recipe.getOutputItemId());
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

						// lbl_materialType.setText(outputItem.getDetails().toString());
					});
				}

				@Override
				public void onFailure(Call<List<Item>> call, Throwable t) {
				}
			});
		} catch (NullPointerException | GuildWars2Exception e2) {
			e2.printStackTrace();
		}
		lbl_source.setText(String.valueOf(recipe.getFlags()));
		lbl_type.setText(String.valueOf(recipe.getType()));
		lbl_outputQty.setText(String.valueOf(recipe.getOutputItemCount()));
		for (CraftingDisciplines discipline : recipe.getDisciplines()) {
			vbox_discipline.getChildren().add(new Label(String.valueOf(discipline.name())));
		}
		lbl_reqRating.setText(String.valueOf(recipe.getMinRating()));
	}

}
