package presentation;

import domain.Directory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import util.ByteUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import static util.Constants.WARNING_PANE_TITLE;

/**
 * Represents a filename item tableview.
 * <p>
 * Created by danielGoncalves on 13/05/17.
 */
public class DirectoryTableView extends TableView<File> implements Observer {


    // FIXME: Fix if not needed
    private final Directory dir;

    private ObservableList<File> data;

    private TableColumn<File, String> nameColumn;
    private TableColumn<File, String> sizeColumn;

    public DirectoryTableView(Directory directory) {

        dir = directory;
        dir.addObserver(this);

        // Set data
        setData();

        // define the table columns.
        createColumns();
    }

    public void setData() {

        try {
            data = FXCollections.observableArrayList(Arrays.asList(dir.getFiles()));
            setItems(data);
        } catch (IOException e) {
            // PopUp Warning
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to load " + dir.getDirectoryName() + " directory.",
                    WARNING_PANE_TITLE,
                    JOptionPane.WARNING_MESSAGE);
            // Set empty list
            setItems(FXCollections.observableArrayList());
        }
    }


    @SuppressWarnings("unchecked")
    private void createColumns() {

        nameColumn = new TableColumn<>("File");
        nameColumn.setCellValueFactory(new Callback<CellDataFeatures<File, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<File, String> f) {

                // f.getValue() returns the RemoteFilename instance for a particular TableView row

                return new SimpleStringProperty(f.getValue().getName());
            }
        });
        nameColumn.setMinWidth(200);

        sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(new Callback<CellDataFeatures<File, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<File, String> f) {

                // f.getValue() returns the RemoteFilename instance for a particular TableView row

                return new SimpleStringProperty(ByteUtil.readableByteCount(f.getValue().length(), true));
            }
        });
        sizeColumn.setMinWidth(50);

        setMinWidth(nameColumn.getMinWidth() + sizeColumn.getMinWidth());

        getColumns().setAll(nameColumn, sizeColumn);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @Override
    public void update(Observable o, Object arg) {

        // Refresh data
        setData();
        refresh();
    }
}
