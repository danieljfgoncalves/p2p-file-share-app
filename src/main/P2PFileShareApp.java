import application.CommunicationsController;
import application.EditConfigurationsController;
import application.ManageSharedFilesController;
import domain.Directory;
import domain.RemoteFilename;
import domain.RemoteFilenameList;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import presentation.*;
import settings.Application;
import util.Constants;
import util.OsUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static util.Constants.WARNING_PANE_TITLE;

/**
 * Represents the main app class
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class P2PFileShareApp extends javafx.application.Application {

    // Label strings
    private static final String APP_TITLE = "P2P File Share App";
    private static final String USERNAME_LBL = "Username: ";
    private static final String UDP_LBL = "UDP Port: ";
    private static final String TCP_LBL = "TCP Port: ";
    private static final String DATE_LBL = "Date: ";
    private static final String GROUP_LBL = "Group: ";

    // Components
    private static Stage mainStage;
    private final Region veil = new Region();
    private Desktop desktop;
    private RemoteFilenamesTableView remoteTableView;
    private DirectoryTableView sharedTableView;
    private DirectoryTableView downloadTableView;
    private DownloadingDialog wd = null;
    private FileChooser openFileChooser;
    private FileChooser downloadFileChooser;
    private BooleanBinding shdTableSelectionBind;
    private BooleanBinding downloadTableSelectionBind;

    // Controllers
    private CommunicationsController communicationsController;
    private ManageSharedFilesController manageSharedFilesController;
    private EditConfigurationsController editConfigurationController;

    // Objects
    private RemoteFilenameList filenames;
    private Directory shdDir;
    private Directory dwlDir;
    private Integer tcpPort = 0;

    /**
     * Main method of the app.
     *
     * @param args args
     */
    public static void main(String[] args) {

        try {
            Thread.sleep(5 * 100);
        } catch (InterruptedException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.INFO, "Start delay failed.", e);
        }

        System.out.println("\n|****************************************|");
        System.out.println("| Launched Serverless P2P File Share App |");
        System.out.println("|****************************************|\n");

        // Launch Main UI
        launch(args);

        System.out.println("\n|******************************************|");
        System.out.println("| Terminated Serverless P2P File Share App |");
        System.out.println("|******************************************|\n");

        System.exit(Constants.EXIT_SUCCESS); // Kills all threads
    }

    /**
     * Initial setup before launching the GUI
     */
    private void setup() {
        // Open UDP/TCP Sockets
        DatagramSocket udpSocket = null;
        ServerSocket tcpSocket = null;
        try {
            udpSocket = new DatagramSocket(Application.settings().getUdpPort());
            tcpSocket = new ServerSocket(Application.settings().getTcpPort());
        } catch (IOException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.SEVERE, "Open sockets failed.", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(WARNING_PANE_TITLE);
            alert.setHeaderText("Open sockets failed.");
            alert.setContentText("The app will exit!");
            alert.showAndWait();

            System.exit(Constants.SOCKET_FAILED);
        }

        // Create List of file to share & shared/download directories
        filenames = new RemoteFilenameList();
        shdDir = null;
        dwlDir = null;
        try {
            shdDir = new Directory(Application.settings().getShdDir());
            dwlDir = new Directory(Application.settings().getDownloadsDir());
            shdDir.watch(); // Activate watch service
            dwlDir.watch();
        } catch (IOException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.SEVERE, "Open directories failed.", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(WARNING_PANE_TITLE);
            alert.setHeaderText("Open directories failed. Relaunch app.");
            alert.setContentText("The app will exit!");
            alert.showAndWait();

            System.exit(Constants.WATCH_DIR_FAILED);
        }
        // Get TCP Port
        tcpPort = tcpSocket.getLocalPort();

        // Open Communications Controller
        communicationsController = null;
        try {
            communicationsController = new CommunicationsController(udpSocket, shdDir, filenames, tcpPort, tcpSocket, dwlDir);
        } catch (SocketException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.SEVERE, "Open communications failed.", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(WARNING_PANE_TITLE);
            alert.setHeaderText("Open communications failed. Relaunch app.");
            alert.setContentText("The app will exit!");
            alert.showAndWait();

            System.exit(Constants.SOCKET_FAILED);
        }
        // Load Known IPs
        try {
            communicationsController.loadKnownIps();
        } catch (UnknownHostException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.WARNING, "Load known ips failed.", e);

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(WARNING_PANE_TITLE);
            alert.setHeaderText("loading known ips failed.");
            alert.setContentText("Known ips will not be loaded.");
            alert.showAndWait();
        }
        // Start communications
        communicationsController.openUdpCommunications();
        communicationsController.openTcpCommunications();

        // Manage Controller
        manageSharedFilesController = new ManageSharedFilesController();
        // Edit Config Controller
        editConfigurationController = new EditConfigurationsController();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        mainStage = primaryStage;

        // Initial setup
        setup();
        sharedTableView = new DirectoryTableView(shdDir);
        sharedTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        downloadTableView = new DirectoryTableView(dwlDir);
        downloadTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteTableView = new RemoteFilenamesTableView(filenames.getList());

        // Setup file choosers
        configureFileChoosers();
        // Set main scene
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(createMainScene());
        primaryStage.show();
    }

    /**
     * Create main scene
     *
     * @return the main scene
     */
    private Scene createMainScene() {

        BorderPane bp = new BorderPane();
        bp.setPadding(new Insets(20, 20, 10, 20));
        bp.setStyle("-fx-background-color: #F0F0F0;");

        // Create top pane (Username, UDP Port, TCP Port)
        bp.setTop(createTopPane());
        // Create left pane (Shared Directory, Add & Remove Buttons)
        bp.setLeft(createLeftPane());
        // Create centre pane (Remote Files, Download & Download to..)
        bp.setCenter(createCenterPane());
        // Create right pane (Download Directory)
        bp.setRight(createRightPane());
        // Create bottom pane (IP Address, System Date & "Copyright" Label)
        bp.setBottom(createBottomPane());

        // Veil when downloading
        veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5)");
        veil.setVisible(false);

        // MenuBar
        VBox mainPane = new VBox(createMenuBar(), bp);

        // Create desktop
        desktop = null;
        if (Desktop.isDesktopSupported()) desktop = Desktop.getDesktop();

        StackPane root = new StackPane();
        root.getChildren().addAll(mainPane, veil);

        return new Scene(root, Color.BLACK);
    }

    /**
     * Creates the menu bar
     *
     * @return the menu bar
     */
    private MenuBar createMenuBar() {

        MenuBar bar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem addIpItem = new MenuItem("Add Known IPv4 Addresses");
        addIpItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
        addIpItem.setOnAction(event -> {
            System.out.println("[MenuItem] Add IP");
            veil.setVisible(true);
            addIpDialog();
            veil.setVisible(false);
        });
        MenuItem settingsItem = new MenuItem("Edit Settings");
        settingsItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        settingsItem.setOnAction(event -> {
            System.out.println("[MenuItem] Edit Settings");
            veil.setVisible(true);
            EditConfigurationDialog editDialog = new EditConfigurationDialog(mainStage);
            Optional<Map<String, String>> result = editDialog.showAndWait();
            result.ifPresent(config -> {
                if (!config.isEmpty()) {
                    try {
                        editConfigurationController.edit(config);
                        communicationsController.saveKnownIpsList();
                    } catch (IOException e) {
                        System.out.println("Couldn't save config file. :(");
                    }
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Edit Configurations");
                    alert.setHeaderText("Need to restart for the changes to take place.");
                    alert.setContentText("Do you wish to restart?\n(If not the changes will take place the next time you start the app.)");
                    Optional<ButtonType> confirm = alert.showAndWait();
                    confirm.ifPresent(button -> {
                        if (button == ButtonType.OK) {
                            try {
                                restart();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            veil.setVisible(false);
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE, KeyCombination.CONTROL_DOWN));
        exitItem.setOnAction(event -> {
            System.out.println("[MenuItem] Exit");
            Platform.exit();
        });
        fileMenu.getItems().addAll(settingsItem, addIpItem, new SeparatorMenuItem(), exitItem);

        Menu shareMenu = new Menu("Share");
        MenuItem addItem = new MenuItem("Add File");
        addItem.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        addItem.setOnAction(event -> addShdFile());
        MenuItem removeItem = new MenuItem("Remove File");
        removeItem.disableProperty().bind(shdTableSelectionBind);
        removeItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
        removeItem.setOnAction(event -> removeShdFile());
        MenuItem downloadItem = new MenuItem("Download");
        downloadItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        downloadItem.disableProperty().bind(downloadTableSelectionBind);
        downloadItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Download Confirmation");
            alert.setHeaderText("Downloading file: " + remoteTableView.getSelectionModel().getSelectedItem().getFilename());
            alert.setContentText("Are you sure you want to download?");
            Optional<ButtonType> result = alert.showAndWait();
            result.ifPresent(button -> {
                if (button == ButtonType.OK) {
                    download();
                }
            });
        });

        MenuItem downloadToItem = new MenuItem("Download to..");
        downloadToItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        downloadToItem.disableProperty().bind(downloadTableSelectionBind);
        downloadToItem.setOnAction(event -> downloadTo());
        shareMenu.getItems().addAll(addItem, removeItem, new SeparatorMenuItem(), downloadItem, downloadToItem);

        bar.getMenus().addAll(fileMenu, shareMenu);

        return bar;
    }

    /**
     * Create top pane
     *
     * @return top pane
     */
    private Pane createTopPane() {

        Pane usernamePane = createLabelAndText(USERNAME_LBL, settings.Application.settings().getUsername());
        Pane udpPane = createLabelAndText(UDP_LBL, settings.Application.settings().getUdpPort().toString());
        Pane tcpPane = createLabelAndText(TCP_LBL, tcpPort.toString());

        HBox hbox = new HBox(20d, usernamePane, udpPane, tcpPane);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(10, 10, 40, 10));

        return hbox;
    }

    /**
     * Create left pane
     *
     * @return left pane
     */
    private Pane createLeftPane() {

        Button addBtn = new Button("Add");
        addBtn.setStyle("-fx-font: bold 16 arial; -fx-base: #4EC22D;" +
                "-fx-text-fill:white;");
        addBtn.setOnAction(event -> addShdFile());
        // Table View selection boolean binding
        shdTableSelectionBind = Bindings.isEmpty(sharedTableView.getSelectionModel().getSelectedItems());
        Button removeBtn = new Button("Remove");
        removeBtn.disableProperty().bind(shdTableSelectionBind);
        removeBtn.setStyle("-fx-font: normal 16 arial; -fx-base: #F5A2A2;" +
                "-fx-text-fill:#991818;");
        removeBtn.setOnAction(event -> removeShdFile());
        HBox buttonBox = new HBox(10, addBtn, removeBtn);

        Label paneLbl = new Label("Shared Files");
        paneLbl.setAlignment(Pos.CENTER);
        paneLbl.setStyle("-fx-font: bold 14 arial;");

        VBox vBox = new VBox(10, paneLbl, sharedTableView, buttonBox);
        vBox.setPadding(new Insets(10, 30, 20, 30));

        return vBox;
    }

    /**
     * Create center pane
     *
     * @return center pane
     */
    private Pane createCenterPane() {

        // Table View selection boolean binding
        downloadTableSelectionBind = Bindings.isEmpty(remoteTableView.getSelectionModel().getSelectedItems());

        Button downloadBtn = new Button("Download");
        downloadBtn.disableProperty().bind(downloadTableSelectionBind);
        downloadBtn.setStyle("-fx-font: bold 16 arial; -fx-base: #0E4EAD;" +
                "-fx-text-fill:white;");
        downloadBtn.setOnAction(event -> download());
        Button downloadToBtn = new Button("Download to..");
        downloadToBtn.disableProperty().bind(downloadTableSelectionBind);
        downloadToBtn.setStyle("-fx-font: normal 16 arial; -fx-base: #07093D;" +
                "-fx-text-fill:#D9DBDB;");
        downloadToBtn.setOnAction(event -> downloadTo());
        HBox buttonBox = new HBox(10, downloadBtn, downloadToBtn);

        Label paneLbl = new Label("Remote Files");
        paneLbl.setAlignment(Pos.CENTER);
        paneLbl.setStyle("-fx-font: bold 14 arial;");

        VBox vBox = new VBox(10, paneLbl, remoteTableView, buttonBox);
        vBox.setPadding(new Insets(10, 30, 20, 20));

        return vBox;
    }

    /**
     * Create right pane
     *
     * @return right pane
     */
    private Pane createRightPane() {

        Button openBtn = new Button("Open File");
        openBtn.setStyle("-fx-font: bold 16 arial; -fx-base: #0CA2B4;" +
                "-fx-text-fill:white;");
        openBtn.setOnAction(event -> {
            System.out.println("Open a downloaded file");
            try {
                if (!desktop.isSupported(Desktop.Action.OPEN)) {
                    throw new IllegalArgumentException("Unsupported open operation.");
                }
                desktop.open(downloadTableView.getSelectionModel().getSelectedItem());
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(WARNING_PANE_TITLE);
                alert.setHeaderText("Failed to open file");
                alert.setContentText("The file wasn't open.");
                alert.showAndWait();
            } catch (IllegalArgumentException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(WARNING_PANE_TITLE);
                alert.setHeaderText("Open file unsupported");
                alert.setContentText("The operating system doesn't support the open file action.");
                alert.showAndWait();
            }
        });
        // Table View selection boolean binding
        BooleanBinding selectionBind = Bindings.isEmpty(downloadTableView.getSelectionModel().getSelectedItems());
        openBtn.disableProperty().bind(selectionBind);

        HBox buttonBox = new HBox(10, openBtn);

        Label paneLbl = new Label("Downloads Folder");
        paneLbl.setAlignment(Pos.CENTER);
        paneLbl.setStyle("-fx-font: bold 14 arial;");

        VBox vBox = new VBox(10, paneLbl, downloadTableView, buttonBox);
        vBox.setPadding(new Insets(10, 30, 20, 20));

        return vBox;
    }

    /**
     * Create bottom pane
     *
     * @return bottom pane
     */
    private Pane createBottomPane() {

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Pane dataPane = createLabelAndText(DATE_LBL, dateFormat.format(today));

        Pane rcompPane = createLabelAndText("RCOMP: ", "2DD");

        Pane groupPane = createLabelAndText(GROUP_LBL, "SNOW WHITE");

        HBox hbox = new HBox(20d, dataPane, rcompPane, groupPane);
        hbox.setAlignment(Pos.BASELINE_RIGHT);
        hbox.setPadding(new Insets(10, 10, 0, 10));

        return hbox;
    }

    /**
     * Create a title label & text label pair
     *
     * @param title the title text
     * @param text  the body text
     * @return a pane with the pair
     */
    private Pane createLabelAndText(String title, String text) {

        Label lbl = new Label(title);
        lbl.setAlignment(Pos.CENTER_RIGHT);
        lbl.setStyle("-fx-font: bold 12 verdana;");

        Label txt = new Label(text);
        lbl.setAlignment(Pos.CENTER_LEFT);

        return new HBox(lbl, txt);
    }

    /**
     * Creates an Add Known IPs dialog
     */
    private void addIpDialog() {

        // Create the ip dialog.
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add peers IP Addresses Dialog");
        dialog.setHeaderText("Please enter a valid IPv4 addresses");

        // Set the button types.
        ButtonType addButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField textField = ipTxtField();
        textField.setPromptText("ex. 192.168.1.1");

        ObservableList<String> items = FXCollections.observableArrayList(communicationsController.getKnownIps());
        items.remove(Constants.BROADCAST_STRING);
        Collections.sort(items);
        ListView<String> listView = new ListView<>(items);
        listView.setPrefSize(70, 150);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        javafx.scene.image.Image imageAdd = new javafx.scene.image.Image(this.getClass().getClassLoader().getResourceAsStream("add.png"));
        Button addBtn = new Button();
        addBtn.setGraphic(new ImageView(imageAdd));
        addBtn.setDisable(true);
        addBtn.setOnAction(new AddToListEventHandler(items, textField));

        BooleanBinding binding = Bindings.isEmpty(listView.getSelectionModel().getSelectedItems());
        javafx.scene.image.Image imageRemove = new javafx.scene.image.Image(this.getClass().getClassLoader().getResourceAsStream("remove.png"));
        Button removeBtn = new Button();
        removeBtn.setGraphic(new ImageView(imageRemove));
        removeBtn.disableProperty().bind(binding);
        removeBtn.setOnAction(event -> {
            items.removeAll(listView.getSelectionModel().getSelectedItems());
            Collections.sort(items);
        });
        HBox buttonBox = new HBox(textField, addBtn, removeBtn);
        buttonBox.setAlignment(Pos.BASELINE_CENTER);

        Label validationLbl = new Label("");
        validationLbl.setStyle("-fx-font: normal 10 arial; -fx-text-fill: #F5A2A2;");

        grid.add(new Label("IPv4 Known Addresses:"), 0, 0);
        grid.add(listView, 0, 1);
        grid.add(buttonBox, 0, 2);
        grid.add(validationLbl, 0, 3);

        String ipv4Regex = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";
        Pattern ipv4Pattern = Pattern.compile(ipv4Regex, Pattern.CASE_INSENSITIVE);

        // Do some validation (using the Java 8 lambda syntax).
        textField.textProperty().addListener((observable, oldValue, newValue) -> {

            boolean sameAsBroadcast = newValue.equalsIgnoreCase("255.255.255.255");
            boolean sameAsZeros = newValue.matches("0+\\.0+\\.0+\\.0+");

            Matcher match = ipv4Pattern.matcher(newValue);

            addBtn.setDisable(newValue.trim().isEmpty() || sameAsBroadcast || sameAsZeros || !match.matches());

            if (sameAsBroadcast || sameAsZeros) {
                validationLbl.setText("Invalid IPv4!");
            } else {
                validationLbl.setText("");
            }
        });
        dialog.getDialogPane().setContent(grid);

        // Request focus on the textfield by default.
        Platform.runLater(textField::requestFocus);

        // Convert the result when the add button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                items.add(Constants.BROADCAST_STRING);
                String[] strings = new String[listView.getItems().size()];
                return listView.getItems().toArray(strings);
            }
            return null;
        });
        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(ipAddresses -> {
            try {
                communicationsController.addPeerAddresses(ipAddresses);
            } catch (UnknownHostException e) {

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(WARNING_PANE_TITLE);
                alert.setHeaderText("Failed to add IPv4 Addresses");
                alert.setContentText("Unknown IPv4 Address.");
                alert.showAndWait();
            }
        });
    }

    /**
     * Creates a IPv4 Validation Textfield
     *
     * @return a IPv4 Validation Textfield
     */
    private TextField ipTxtField() {
        TextField ipTextField = new TextField();

        String partialBlock = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))";
        String subsequentPartialBlock = "(\\." + partialBlock + ")";
        String regex = partialBlock + "?" + subsequentPartialBlock + "{0,3}";

        final UnaryOperator<TextFormatter.Change> ipAddressFilter = c -> {
            String text = c.getControlNewText();
            if (text.matches(regex)) {
                return c;
            } else {
                return null;
            }
        };
        ipTextField.setTextFormatter(new TextFormatter<>(ipAddressFilter));

        return ipTextField;
    }

    /**
     * Creates a downloading dialog while the download is happening
     *
     * @param filename the file's name to download
     * @param newFile  the downloaded file location
     */
    private void downloadDialog(RemoteFilename filename, File newFile) {

        String tmp = filename.getFilename();
        if (tmp.length() > 20) tmp = tmp.substring(0, 20);
        wd = new DownloadingDialog(mainStage.getOwner(), "Downloading " + tmp + "...");
        wd.addTaskEndNotification(result -> {
            System.out.println("DOWNLOAD STATUS: " + result);
            wd = null; // don't keep the object, cleanup
            if ((Integer) result < 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(WARNING_PANE_TITLE);
                alert.setHeaderText("Failed to download file");
                alert.setContentText("The file wasn't downloaded.");
                alert.showAndWait();
            }
        });
        //noinspection unchecked
        wd.exec(1, inputParam -> {
            veil.setVisible(true);
            // Download
            try {
                communicationsController.downloadFile(filename, newFile);
                downloadTableView.setData();
                downloadTableView.refresh();
                Thread.sleep(2 * 1000); // So we can see the dialog if download is to fast ;)
            } catch (Exception e) {
                System.out.println(e.getMessage());
                veil.setVisible(false);
                return -1;
            }
            veil.setVisible(false);
            return 0;
        });
    }

    /**
     * Configures the ALL PERMITTED file chooser extension filter
     *
     * @return the extension filter
     */
    private FileChooser.ExtensionFilter configureAllExtensionFilter() {

        String[] predefined = Application.settings().getFileExtensions();
        String[] exts = new String[predefined.length];
        for (int i = 0; i < exts.length; i++) {
            exts[i] = "*.".concat(predefined[i]);
        }
        return new FileChooser.ExtensionFilter("All Permitted", exts);
    }

    /**
     * Configures the file choosers
     */
    private void configureFileChoosers() {

        ObservableList<FileChooser.ExtensionFilter> filters = FXCollections.observableArrayList();
        for (String title :
                Application.settings().getFileExtensions()) {

            String ext = "*.".concat(title);

            filters.add(new FileChooser.ExtensionFilter(title.toUpperCase(), ext));
        }
        openFileChooser = new FileChooser();
        openFileChooser.setTitle("Share File..");
        openFileChooser.getExtensionFilters().add(configureAllExtensionFilter());
        openFileChooser.getExtensionFilters().addAll(filters);

        downloadFileChooser = new FileChooser();
        downloadFileChooser.setTitle("Download To..");
        downloadFileChooser.getExtensionFilters().add(configureAllExtensionFilter());
        downloadFileChooser.getExtensionFilters().addAll(filters);
    }

    /**
     * Adds a list of files to share
     */
    private void addShdFile() {
        System.out.println("Added a Shared File");
        veil.setVisible(true);
        List<File> filesToAdd = openFileChooser.showOpenMultipleDialog(mainStage);
        if (filesToAdd != null) {
            for (File file :
                    filesToAdd) {
                try {
                    manageSharedFilesController.addShareFile(file);
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle(WARNING_PANE_TITLE);
                    alert.setHeaderText("Failed to copy file");
                    alert.setContentText("The file wasn't copied.");
                    alert.showAndWait();
                }
            }
            sharedTableView.setData();
            sharedTableView.refresh();
        }
        veil.setVisible(false);
    }

    /**
     * Removes a the selected shared files
     */
    private void removeShdFile() {
        System.out.println("Removed a Shared File");
        veil.setVisible(true);
        ObservableList<File> filesToRemove = sharedTableView.getSelectionModel().getSelectedItems();
        String tmp = (filesToRemove.size() > 1) ? filesToRemove.size() + " files" : "file: " + filesToRemove.iterator().next().getName();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Confirmation");
        alert.setHeaderText("Removing " + tmp);
        alert.setContentText("Are you sure you want to remove?");
        Optional<ButtonType> result = alert.showAndWait();
        result.ifPresent(button -> {
            if (button == ButtonType.OK) {
                for (File file :
                        filesToRemove) {
                    manageSharedFilesController.removeSharedFile(file);
                }
                sharedTableView.setData();
                sharedTableView.refresh();
            }
        });
        veil.setVisible(false);
    }

    /**
     * Downloads a remote file to the default folder
     */
    private void download() {
        System.out.println("Download");
        // Download
        veil.setVisible(true);
        RemoteFilename item = remoteTableView.getSelectionModel().getSelectedItem();
        downloadDialog(item, null);
        veil.setVisible(false);
    }

    /**
     * Downloads a remote file to a specific folder
     */
    private void downloadTo() {
        System.out.println("Download to");
        veil.setVisible(true);
        File file = downloadFileChooser.showSaveDialog(mainStage);
        if (file != null) {
            RemoteFilename item = remoteTableView.getSelectionModel().getSelectedItem();
            downloadDialog(item, file);
        }
        veil.setVisible(false);
    }

    /**
     * Restart the current Java application
     */
    private void restart() throws IOException {

        String path = P2PFileShareApp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = path;
        try {
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(Directory.class.getName()).log(Level.WARNING, "Couldn't retrieve running executable's path", e);
        }
        if (OsUtils.isWindows()) {
            decodedPath = decodedPath.substring(1);
        }

        String command = "java -jar " + decodedPath;
        Runtime.getRuntime().exec(command);
        Platform.exit();
    }

    @Override
    public void stop() {
        System.out.println("Stage is closing");
        // Save ips
        try {
            communicationsController.saveKnownIpsList();
        } catch (IOException e) {
            Logger.getLogger(Directory.class.getName()).log(Level.WARNING, "Couldn't save known IPs", e);
        }
    }
}
