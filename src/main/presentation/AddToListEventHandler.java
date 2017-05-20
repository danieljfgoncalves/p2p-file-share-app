package presentation;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

import java.util.Collections;

/**
 * An event handler to add from textfield to a related list
 * <p>
 * Created by danielGoncalves on 20/05/17.
 */
public class AddToListEventHandler implements EventHandler<ActionEvent> {

    private final ObservableList<String> aList;
    private final TextField aTextField;

    public AddToListEventHandler(ObservableList<String> list, TextField textField) {

        aList = list;
        aTextField = textField;
    }

    @Override
    public void handle(ActionEvent event) {
        String extension = aTextField.getText().trim().toLowerCase();
        aTextField.setText("");
        if (!extension.isEmpty()) {
            aList.add(extension);
            Collections.sort(aList);
        }
    }
}
