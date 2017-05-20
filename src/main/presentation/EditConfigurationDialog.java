package presentation;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import settings.AppSettings;
import settings.Application;
import util.StringUtil;

import java.io.File;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Represents a dialog to edit configurations.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class EditConfigurationDialog extends Dialog<Map<String, String>> {

    private static final Integer PORT_MIN = 1;
    private static final Integer PORT_MAX = 65535;
    private static final Integer MIN_WIDTH_TXTFDS = 150;
    private ToggleGroup tcpToggle;
    private ToggleGroup udpToggle;
    private Spinner<Integer> maxUpSpinner;
    private Spinner<Integer> refreshSpinner;
    private Spinner<Integer> sendSpinner;
    private TextField textfieldShared;
    private TextField textfieldDownload;
    private TextField textfieldUsername;
    private ListView<String> extsListView;

    /**
     * Creates an edit configuration dialog
     *
     * @param owner the window that owns this dialog
     */
    public EditConfigurationDialog(Window owner) {
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);

        setTitle("Edit Configurations");
        GridPane root = new GridPane();
        root.setPadding(new Insets(20, 20, 0, 20));
        root.setVgap(15);
        root.setHgap(15);

        root.addRow(0, createInfoPane(), createFileExtsPane());
        root.add(createNetworkPane(), 0, 1, 2, 1);

        getDialogPane().setContent(root);

        // Set the button types.
        ButtonType addButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {

                Map<String, String> result = new HashMap<>();

                String username = textfieldUsername.getText().trim().toLowerCase();
                if (!(username.isEmpty() || username.equalsIgnoreCase(Application.settings().getUsername()))) {
                    result.put(AppSettings.USERNAME_KEY, username);
                }

                String shdDir = textfieldShared.getText().trim();
                if (!(shdDir.isEmpty() || shdDir.equalsIgnoreCase(Application.settings().getShdDir()))) {
                    result.put(AppSettings.SHD_DIR_KEY, shdDir);
                }

                String dwlDir = textfieldDownload.getText().trim();
                if (!(dwlDir.isEmpty() || dwlDir.equalsIgnoreCase(Application.settings().getDownloadsDir()))) {
                    result.put(AppSettings.DOWNLOADS_DIR_KEY, dwlDir);
                }

                if (tcpToggle.getSelectedToggle() != null) {

                    Integer value = 0;
                    Object object = tcpToggle.getSelectedToggle().getUserData();
                    if (object instanceof Spinner) {
                        Object data = ((Spinner) object).getValue();
                        if (data instanceof Integer) {
                            value = (Integer) data;
                        }
                    }
                    if (!value.equals(Application.settings().getTcpPort())) {
                        result.put(AppSettings.TCP_PORT_KEY, value.toString());
                    }
                }

                if (udpToggle.getSelectedToggle() != null) {

                    Integer value = 0;
                    Object object = udpToggle.getSelectedToggle().getUserData();
                    if (object instanceof Spinner) {
                        Object data = ((Spinner) object).getValue();
                        if (data instanceof Integer) {
                            value = (Integer) data;
                        }
                    }
                    if (!value.equals(Application.settings().getUdpPort())) {
                        result.put(AppSettings.UDP_PORT_KEY, value.toString());
                    }
                }

                Integer maxUps = maxUpSpinner.getValue();
                if (maxUps != null) {
                    if (maxUps > 0 && maxUps <= 20) {
                        if (!maxUps.equals(Application.settings().getMaxUploads())) {
                            result.put(AppSettings.MAX_UPLOADS_KEY, maxUps.toString());
                        }
                    }
                }

                Integer refresh = refreshSpinner.getValue();
                if (refresh != null) {
                    if (refresh > 0 && refresh <= 120) {
                        if (!refresh.equals(Application.settings().getFileRefreshTime())) {
                            result.put(AppSettings.FILE_REFRESH_TIME_KEY, refresh.toString());
                        }
                    }
                }

                Integer sendTime = sendSpinner.getValue();
                if (sendTime != null) {
                    if (sendTime > 0 && sendTime <= 120) {
                        if (!sendTime.equals(Application.settings().getBroadcastTimeInterval())) {
                            result.put(AppSettings.BROADCAST_TIME_INTERVAL_KEY, sendTime.toString());
                        }
                    }
                }

                List<String> items = extsListView.getItems();
                String[] exts = new String[items.size()];
                exts = items.toArray(exts);
                String extString = StringUtil.arrayToString(exts);

                if (!Arrays.equals(exts, Application.settings().getFileExtensions())) {

                    result.put(AppSettings.FILE_EXTENSIONS_KEY, extString);
                }
                return result;
            }
            return null;
        });
    }

    /**
     * Creates a pane with the network configurations
     *
     * @return a pane with the network configurations
     */
    private Pane createNetworkPane() {

        Label labelTcp = new Label("TCP Port: ");
        labelTcp.setAlignment(Pos.CENTER_RIGHT);
        Label labelUdp = new Label("UDP Port: ");
        labelUdp.setAlignment(Pos.CENTER_RIGHT);
        Label labelMaxUp = new Label("Max Uploads (at once): ");
        labelMaxUp.setAlignment(Pos.CENTER_RIGHT);
        Label labelRefresh = new Label("Remote File Refresh Limit (in seconds): ");
        Label labelSend = new Label("Send Remote File Announcement Time Interval (in seconds): ");

        tcpToggle = new ToggleGroup();
        Pane tcpPane = createToggleSelection(tcpToggle, Application.settings().getTcpPort());
        udpToggle = new ToggleGroup();
        Pane udpPane = createToggleSelection(udpToggle, Application.settings().getUdpPort());
        maxUpSpinner = createSpinner(1, 20, Application.settings().getMaxUploads());
        refreshSpinner = createSpinner(1, 120, Application.settings().getFileRefreshTime());
        sendSpinner = createSpinner(1, 120, Application.settings().getBroadcastTimeInterval());

        GridPane grid = new GridPane();
        Label title = new Label("Network Configuration");
        title.setStyle("-fx-font: bold 16 verdana;");

        title.setPadding(new Insets(10, 0, 10, 0));
        grid.add(title, 0, 0, 2, 1);
        grid.add(labelTcp, 0, 1);
        grid.add(labelUdp, 0, 2);
        grid.add(labelMaxUp, 0, 3);
        grid.add(labelRefresh, 0, 4, 2, 1);
        grid.add(refreshSpinner, 0, 5);
        grid.add(labelSend, 0, 6, 2, 1);
        grid.add(sendSpinner, 0, 7);
        grid.add(tcpPane, 1, 1);
        grid.add(udpPane, 1, 2);
        grid.add(maxUpSpinner, 1, 3);

        grid.setVgap(10);
        grid.setPadding(new Insets(0, 20, 20, 20));
        grid.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

        return grid;
    }

    /**
     * Creates a pane with the information configurations
     *
     * @return a pane with the information configurations
     */
    private Pane createInfoPane() {

        Label title = new Label("Info Configuration");
        title.setStyle("-fx-font: bold 16 verdana;");
        title.setPadding(new Insets(10, 0, 10, 0));

        Label labelUsername = new Label("Username:");
        Label labelDwlFolder = new Label("Downloads Folder:");
        Label labelShdFolder = new Label("Shared Folder:");
        textfieldUsername = new TextField(Application.settings().getUsername());

        GridPane grid = new GridPane();
        grid.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));


        textfieldShared = new TextField(Application.settings().getShdDir());
        textfieldDownload = new TextField(Application.settings().getDownloadsDir());
        grid.addColumn(0, title, labelUsername, textfieldUsername, labelShdFolder,
                createDirectoryChooser(textfieldShared), labelDwlFolder,
                createDirectoryChooser(textfieldDownload));
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 20, 20, 20));

        return grid;
    }

    /**
     * Creates a pane with the file extensions selector
     *
     * @return a pane with the file extensions selector
     */
    private Pane createFileExtsPane() {

        ObservableList<String> items = FXCollections.observableArrayList(Application.settings().getFileExtensions());
        Collections.sort(items);
        extsListView = new ListView<>(items);
        extsListView.setPrefSize(70, 150);
        extsListView.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE);

        TextField extTxtfield = new TextField();
        String regex = "[a-zA-Z0-9\\-]+";
        final UnaryOperator<TextFormatter.Change> txtFilter = c -> {

            String txt = c.getControlNewText();
            if (txt.matches(regex) || txt.isEmpty()) {
                return c;
            } else {
                return null;
            }
        };
        extTxtfield.setTextFormatter(new TextFormatter<>(txtFilter));

        Image imageAdd = new Image(this.getClass().getClassLoader().getResourceAsStream("add.png"));
        Button addBtn = new Button();
        addBtn.setGraphic(new ImageView(imageAdd));
        addBtn.setOnAction(new AddToListEventHandler(items, extTxtfield));
        BooleanBinding binding = Bindings.isEmpty(extsListView.getSelectionModel().getSelectedItems());
        Image imageRemove = new Image(this.getClass().getClassLoader().getResourceAsStream("remove.png"));
        Button removeBtn = new Button();
        removeBtn.setGraphic(new ImageView(imageRemove));
        removeBtn.disableProperty().bind(binding);
        removeBtn.setOnAction(event -> {
            items.removeAll(extsListView.getSelectionModel().getSelectedItems());
            Collections.sort(items);
        });
        HBox buttonBox = new HBox(extTxtfield, addBtn, removeBtn);
        buttonBox.setAlignment(Pos.BASELINE_CENTER);

        GridPane grid = new GridPane();
        Label title = new Label("File Extensions");
        title.setStyle("-fx-font: bold 16 verdana;");
        title.setPadding(new Insets(10, 0, 10, 0));
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 20, 20, 20));
        grid.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

        grid.add(title, 0, 0, 1, 1);
        grid.add(extsListView, 0, 1, 1, 6);
        grid.add(buttonBox, 0, 7, 1, 1);

        return grid;
    }

    /**
     * Creates a directory chooser selector
     *
     * @param textField the textfield related to the selector
     * @return a directory chooser selector pane
     */
    private Pane createDirectoryChooser(TextField textField) {

        textField.setMinWidth(MIN_WIDTH_TXTFDS);

        Button btnOpenDirectoryChooser = new Button();
        btnOpenDirectoryChooser.setText("...");
        btnOpenDirectoryChooser.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory =
                    directoryChooser.showDialog(getOwner());

            if (selectedDirectory != null && selectedDirectory.isDirectory()) {
                textField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        return new HBox(0, textField, btnOpenDirectoryChooser);
    }

    /**
     * Creates a radio button group
     *
     * @param group        the group to associate
     * @param initialValue the initial value
     * @return a pane with the toggle group
     */
    private Pane createToggleSelection(ToggleGroup group, int initialValue) {

        RadioButton rbDynamic = new RadioButton("Dynamic");
        rbDynamic.setToggleGroup(group);
        rbDynamic.setSelected(true);
        rbDynamic.setUserData(0);
        RadioButton rbSpecific = new RadioButton("Specific:");
        rbSpecific.setToggleGroup(group);

        Spinner<Integer> spinner = createSpinner(PORT_MIN, PORT_MAX, initialValue);
        spinner.disableProperty().bind(rbSpecific.selectedProperty().not());

        rbSpecific.setUserData(spinner);

        if (initialValue != 0) {
            rbDynamic.setSelected(false);
            rbSpecific.setSelected(true);
        }

        HBox box = new HBox(0, rbSpecific, spinner);
        box.setAlignment(Pos.CENTER_LEFT);

        HBox root = new HBox(10, rbDynamic, box);

        root.setAlignment(Pos.CENTER_LEFT);

        return root;
    }

    /**
     * Creates an integer spinner
     *
     * @param min          minimum value
     * @param max          maximum value
     * @param initialValue initial value
     * @return the spinner
     */
    private Spinner<Integer> createSpinner(int min, int max, int initialValue) {

        Spinner<Integer> spinner = new Spinner<>();
        // Editable.
        spinner.setEditable(true);
        // Value Factory
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue);
        spinner.setValueFactory(valueFactory);

        return spinner;
    }
}
