package presentation;

import domain.RemoteFilename;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Represents a filename item tableview.
 * <p>
 * Created by danielGoncalves on 13/05/17.
 */
public class RemoteFilenamesTableView extends TableView<RemoteFilename> {


    // FIXME: Fix if not needed
    private final ObservableList<RemoteFilename> data;
    private TableColumn<RemoteFilename, String> fileColumn;
    private TableColumn<RemoteFilename, String> usernameColumn;

    public RemoteFilenamesTableView(ObservableList<RemoteFilename> set) {

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
