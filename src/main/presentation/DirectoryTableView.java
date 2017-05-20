package presentation;

import domain.Directory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import util.ByteUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

/**
 * Represents a filename item tableview.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class DirectoryTableView extends TableView<File> implements Observer {

    private final Directory dir;

    /**
     * Create a directory table view
     *
     * @param directory the directory to list
     */
    public DirectoryTableView(Directory directory) {

        dir = directory;
        dir.addObserver(this);

        // Set data
        setData();

        // define the table columns.
        createColumns();
    }

    /**
     * Sets the table data
     */
    public void setData() {

        ObservableList<File> data = FXCollections.observableArrayList(Arrays.asList(dir.getFiles()));
        setItems(data);
    }


    @SuppressWarnings("unchecked")
    /*
      Creates the table columns
     */
    private void createColumns() {

        TableColumn<File, String> nameColumn = new TableColumn<>("File");
        nameColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getName()));
        nameColumn.setMinWidth(200);

        TableColumn<File, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(f -> new SimpleStringProperty(ByteUtil.readableByteCount(f.getValue().length(), true)));
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
