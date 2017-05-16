import application.AddRemoveSharedFileController;
import application.CommunicationsController;
import domain.Directory;
import domain.FilenameItem;
import domain.FilenameItemList;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import presentation.DirectoryTableViewUI;
import presentation.FilenameItemTableViewUI;
import presentation.WorkIndicatorDialog;
import settings.Application;
import util.Constants;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.Constants.WARNING_PANE_TITLE;

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
    private Desktop desktop;
    private FilenameItemTableViewUI remoteTableView;
    private DirectoryTableViewUI sharedTableView;
    private DirectoryTableViewUI downloadTableView;
    private WorkIndicatorDialog wd = null;
    private Region veil = new Region();
    private FileChooser openFileChooser;
    private FileChooser downloadFileChooser;

    // Controllers
    private CommunicationsController communicationsController;
    private AddRemoveSharedFileController addRemoveController;

    // Objects
    private FilenameItemList filenames;
    private Directory shdDir;
    private Directory dwlDir;
    private Integer tcpPort = 0;

    public static void main(String[] args) {

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
        filenames = new FilenameItemList();
        shdDir = null;
        dwlDir = null;
        try { // FIXME : Refactor to a controller ??
            shdDir = new Directory(Application.settings().getShdDir());
            dwlDir = new Directory(Application.settings().getDownloadsDir());
            shdDir.watch(); // Activate watch service
            dwlDir.watch();
        } catch (IOException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.SEVERE, "Open directories failed.", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(WARNING_PANE_TITLE);
            alert.setHeaderText("Open directories failed.");
            alert.setContentText("The app will exit!");
            alert.showAndWait();

            System.exit(Constants.SOCKET_FAILED);
        }

        tcpPort = tcpSocket.getLocalPort();

        // Open Communications Controller
        communicationsController = null;
        try {
            communicationsController = new CommunicationsController(udpSocket, shdDir, filenames, tcpPort, tcpSocket, dwlDir);
        } catch (SocketException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.WARNING, "Send broadcast packet failed.", e);

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(WARNING_PANE_TITLE);
            alert.setHeaderText("Send broadcast packet failed.");
            alert.setContentText("Sending files crashed. Relaunch app or you can still download files.");
            alert.showAndWait();
        }
        // Open communications
        communicationsController.openUdpCommunications();
        communicationsController.openTcpCommunications();

        // Add/Remove Controller
        addRemoveController = new AddRemoveSharedFileController();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        mainStage = primaryStage;

        setup();
        sharedTableView = new DirectoryTableViewUI(shdDir);
        downloadTableView = new DirectoryTableViewUI(dwlDir);
        remoteTableView = new FilenameItemTableViewUI(filenames.getList());

        // Setup file choosers
        configureFileChoosers();

        primaryStage.setTitle(APP_TITLE);
        // Set main scene
        primaryStage.setScene(createMainScene());

        primaryStage.show();

//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Welcome");
//        alert.setHeaderText("Welcome to P2P File Share");
//        alert.setContentText("Enjoy!");
//        alert.showAndWait();
    }

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
        veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3)");
        veil.setVisible(false);

        // Create desktop
        desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }

        StackPane root = new StackPane();
        root.getChildren().addAll(bp, veil);

        Scene mainScene = new Scene(root, Color.BLACK); //Color.rgb(240,240,240));

        return mainScene;
    }

    private Pane createTopPane() {

        Pane usernamePane = createLabelAndText(USERNAME_LBL, settings.Application.settings().getUsername());
        Pane udpPane = createLabelAndText(UDP_LBL, settings.Application.settings().getUdpPort().toString());
        Pane tcpPane = createLabelAndText(TCP_LBL, tcpPort.toString());

        HBox hbox = new HBox(20d, usernamePane, udpPane, tcpPane);
        hbox.setAlignment(Pos.CENTER);

        hbox.setPadding(new Insets(10, 10, 40, 10));

        return hbox;
    }

    private Pane createLeftPane() {

        Button addBtn = new Button("Add");
        addBtn.setStyle("-fx-font: bold 16 arial; -fx-base: #4EC22D;" +
                "-fx-text-fill:white;");
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Added a Shared File");
                // TODO: add file
                veil.setVisible(true);
                List<File> filesToAdd = openFileChooser.showOpenMultipleDialog(mainStage);
                if (filesToAdd != null) {

                    for (File file :
                            filesToAdd) {
                        try {
                            addRemoveController.addShareFile(file);
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
        });
        // Table View selection boolean binding
        BooleanBinding selectionBind = Bindings.isEmpty(sharedTableView.getSelectionModel().getSelectedItems());
        Button removeBtn = new Button("Remove");
        removeBtn.disableProperty().bind(selectionBind);
        removeBtn.setStyle("-fx-font: normal 16 arial; -fx-base: #F5A2A2;" +
                "-fx-text-fill:#991818;");
        removeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Removed a Shared File");
                // TODO: remove file
                ObservableList<File> filesToRemove = sharedTableView.getSelectionModel().getSelectedItems();
                for (File file :
                        filesToRemove) {
                    addRemoveController.removeSharedFile(file);
                }
                sharedTableView.setData();
                sharedTableView.refresh();
            }
        });
        HBox buttonBox = new HBox(10, addBtn, removeBtn);

        Label paneLbl = new Label("Shared Files");
        paneLbl.setAlignment(Pos.CENTER);
        paneLbl.setStyle("-fx-font: bold 14 arial;");

        VBox vBox = new VBox(10, paneLbl, sharedTableView, buttonBox);
        vBox.setPadding(new Insets(10, 30, 20, 30));

        return vBox;
    }

    private Pane createCenterPane() {

        // Table View selection boolean binding
        BooleanBinding selectionBind = Bindings.isEmpty(remoteTableView.getSelectionModel().getSelectedItems());

        Button downloadBtn = new Button("Download");
        downloadBtn.disableProperty().bind(selectionBind);
        downloadBtn.setStyle("-fx-font: bold 16 arial; -fx-base: #0E4EAD;" +
                "-fx-text-fill:white;");
        downloadBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Download");
                // Download
                veil.setVisible(true);
                FilenameItem item = remoteTableView.getSelectionModel().getSelectedItem();
                downloadDialog(item, null);
                veil.setVisible(false);
            }
        });
        Button downloadToBtn = new Button("Download to..");
        downloadToBtn.disableProperty().bind(selectionBind);
        downloadToBtn.setStyle("-fx-font: normal 16 arial; -fx-base: #07093D;" +
                "-fx-text-fill:#D9DBDB;");
        downloadToBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Download to");
                // TODO: download to file
                veil.setVisible(true);
                File file = downloadFileChooser.showSaveDialog(mainStage);
                if (file != null) {
                    FilenameItem item = remoteTableView.getSelectionModel().getSelectedItem();
                    downloadDialog(item, file);
                }
                veil.setVisible(false);
            }
        });
        HBox buttonBox = new HBox(10, downloadBtn, downloadToBtn);

        Label paneLbl = new Label("Remote Files");
        paneLbl.setAlignment(Pos.CENTER);
        paneLbl.setStyle("-fx-font: bold 14 arial;");

        VBox vBox = new VBox(10, paneLbl, remoteTableView, buttonBox);
        vBox.setPadding(new Insets(10, 30, 20, 20));

        return vBox;
    }

    private Pane createRightPane() {

        Button openBtn = new Button("Open File");
        openBtn.setStyle("-fx-font: bold 16 arial; -fx-base: #0CA2B4;" +
                "-fx-text-fill:white;");
        openBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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

    private Pane createLabelAndText(String label, String text) {

        Label lbl = new Label(label);
        lbl.setAlignment(Pos.CENTER_RIGHT);
        lbl.setStyle("-fx-font: bold 12 verdana;");

        Label txt = new Label(text);
        lbl.setAlignment(Pos.CENTER_LEFT);

        return new HBox(lbl, txt);
    }

    private Integer downloadDialog(FilenameItem filename, File newFile) {

        // TODO: If file doesn't exist
        String tmp = filename.getFilename();
        if (tmp.length() > 20) tmp = tmp.substring(0, 20);
        wd = new WorkIndicatorDialog(mainStage.getOwner(), "Downloading " + tmp + "...");

        wd.addTaskEndNotification(result -> {
            System.out.println(result);
            wd = null; // don't keep the object, cleanup
        });

        wd.exec("done", inputParam -> {

            veil.setVisible(true);
            // Download
            try {
                communicationsController.downloadFile(filename, newFile);
                downloadTableView.setData();
                downloadTableView.refresh();
                Thread.sleep(2 * 1000); // So we can see the dialog if download is to fast ;) FIXME
            } catch (IOException e) {
                e.printStackTrace(); // FIXME : Add option pane failed download

                return -1;
            } catch (IllegalArgumentException e) {
                e.printStackTrace(); // FIXME : Add option pane file not available

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            veil.setVisible(false);

            return 0;
        });

        return wd.getResultValue();
    }

    private FileChooser.ExtensionFilter configureAllExtensionFilter() {

        String[] predefined = Application.settings().getFileExtensions();

        String[] exts = new String[predefined.length];

        for (int i = 0; i < exts.length; i++) {

            exts[i] = "*.".concat(predefined[i]);
        }

        return new FileChooser.ExtensionFilter("All Permitted", exts);
    }

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
}
