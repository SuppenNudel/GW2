package de.rohmio.gw2.tools.view.recipe;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;
import me.xhsun.guildwars2wrapper.model.v2.Recipe.Ingredient;
import me.xhsun.guildwars2wrapper.model.v2.util.comm.CraftingDisciplines;

public class RecipeViewController extends VBox implements Initializable {

	@FXML
	private Label lbl_outputName;
	@FXML
	private Label lbl_source;
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
		lbl_outputName.setText(String.valueOf(recipe.getOutputItemId()));
		lbl_source.setText(String.valueOf(recipe.getFlags()));
		lbl_type.setText(String.valueOf(recipe.getType()));
		lbl_outputQty.setText(String.valueOf(recipe.getOutputItemCount()));
		for (CraftingDisciplines discipline : recipe.getDisciplines()) {
			vbox_discipline.getChildren().add(new Label(String.valueOf(discipline.name())));
		}
		lbl_reqRating.setText(String.valueOf(recipe.getMinRating()));
		for (Ingredient ingredient : recipe.getIngredients()) {
			vbox_ingredients.getChildren()
					.add(new Label(String.format("%dx %s", ingredient.getCount(), ingredient.getItemId())));
		}
	}

}
