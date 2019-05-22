package de.rohmio.gw2.tools.view.main;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class IntOnlyTextFieldChangeListener implements ChangeListener<String> {

	private TextField textField;
	
	public IntOnlyTextFieldChangeListener(TextField textField) {
		this.textField = textField;
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		if (!newValue.matches("\\d*")) {
			textField.setText(newValue.replaceAll("[^\\d]", ""));
		}
	}

}
