package presentation;

import domain.FilenameItem;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * Represents a filename item tableview.
 * <p>
 * Created by danielGoncalves on 13/05/17.
 */
public class FilenameItemTableViewUI extends TableView<FilenameItem> {


    // FIXME: Fix if not needed
    private final ObservableList<FilenameItem> data;
    private TableColumn<FilenameItem, String> fileColumn;
    private TableColumn<FilenameItem, String> usernameColumn;

    public FilenameItemTableViewUI(ObservableList<FilenameItem> set) {

        data = set;

        setItems(data);

        // define the table columns.
        createColumns();
    }


    @SuppressWarnings("unchecked")
    private void createColumns() {

        fileColumn = new TableColumn<>("File");
        fileColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FilenameItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<FilenameItem, String> f) {

                // f.getValue() returns the FilenameItem instance for a particular TableView row

                return f.getValue().filenameProperty();
            }
        });
        usernameColumn = new TableColumn<>("User");
        usernameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FilenameItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<FilenameItem, String> f) {

                // f.getValue() returns the FilenameItem instance for a particular TableView row

                return f.getValue().usernameProperty();
            }
        });

        getColumns().setAll(fileColumn, usernameColumn);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
