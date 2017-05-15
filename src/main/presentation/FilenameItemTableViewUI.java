package presentation;

import domain.FilenameItem;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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

        // define the table columns.
        createColumns();
        // Set table's items
        setItems(data);
    }


    @SuppressWarnings("unchecked")
    private void createColumns() {

        fileColumn = new TableColumn<>("File");
        fileColumn.setCellValueFactory(cellData -> cellData.getValue().filenameProperty());
        fileColumn.setMinWidth(250);

        usernameColumn = new TableColumn<>("User");
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        usernameColumn.setMinWidth(250);

        setMinWidth(fileColumn.getMinWidth() + usernameColumn.getMinWidth());

        getColumns().setAll(fileColumn, usernameColumn);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
