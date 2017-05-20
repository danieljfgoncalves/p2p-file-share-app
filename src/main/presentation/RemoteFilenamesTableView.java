package presentation;

import domain.RemoteFilename;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Represents a filename item tableview.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class RemoteFilenamesTableView extends TableView<RemoteFilename> {


    /**
     * Creates a remote file table view.
     *
     * @param set remote filenames
     */
    public RemoteFilenamesTableView(ObservableList<RemoteFilename> set) {

        // define the table columns.
        createColumns();
        // Set table's items
        setItems(set);
    }


    @SuppressWarnings("unchecked")
    /*
      Creates the table view columns
     */
    private void createColumns() {

        TableColumn<RemoteFilename, String> fileColumn = new TableColumn<>("File");
        fileColumn.setCellValueFactory(cellData -> cellData.getValue().filenameProperty());
        fileColumn.setMinWidth(250);

        TableColumn<RemoteFilename, String> usernameColumn = new TableColumn<>("User");
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        usernameColumn.setMinWidth(250);

        setMinWidth(fileColumn.getMinWidth() + usernameColumn.getMinWidth());

        getColumns().setAll(fileColumn, usernameColumn);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
