import application.CommunicationsController;
import domain.Directory;
import domain.FilenameItem;
import domain.FilenameItemList;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import presentation.DirectoryTableViewUI;
import presentation.FilenameItemTableViewUI;
import presentation.WorkIndicatorDialog;
import settings.Application;
import util.Constants;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    // Controllers
    CommunicationsController communicationsController;
    private FilenameItemTableViewUI remoteTableView;
    private DirectoryTableViewUI sharedTableView;
    private DirectoryTableViewUI downloadTableView;
    private WorkIndicatorDialog wd = null;
    private Region veil = new Region();
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

            // PopUp Warning
            JOptionPane.showMessageDialog(
                    null,
                    "Open sockets failed.",
                    WARNING_PANE_TITLE,
                    JOptionPane.WARNING_MESSAGE);

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

            // PopUp Warning
            JOptionPane.showMessageDialog(
                    null,
                    "Open directories failed.",
                    WARNING_PANE_TITLE,
                    JOptionPane.WARNING_MESSAGE);

            System.exit(Constants.SOCKET_FAILED);
        }

        tcpPort = tcpSocket.getLocalPort();

        // Open Communications Controller
        communicationsController = null;
        try {
            communicationsController = new CommunicationsController(udpSocket, shdDir, filenames, tcpPort, tcpSocket, dwlDir);
        } catch (SocketException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.WARNING, "Send broadcast packet failed.", e);

            // PopUp Warning
            JOptionPane.showMessageDialog(
                    null,
                    "Sending files crashed. Relaunch app or you can still download files.",
                    WARNING_PANE_TITLE,
                    JOptionPane.WARNING_MESSAGE);
        }
        // Open communications
        communicationsController.openUdpCommunications();
        communicationsController.openTcpCommunications();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        mainStage = primaryStage;

        setup();
        sharedTableView = new DirectoryTableViewUI(shdDir);
        downloadTableView = new DirectoryTableViewUI(dwlDir);
        remoteTableView = new FilenameItemTableViewUI(filenames.getList());

        primaryStage.setTitle(APP_TITLE);
        // Set main scene
        primaryStage.setScene(createMainScene());
        primaryStage.show();
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
            }
        });
        HBox buttonBox = new HBox(10, addBtn, removeBtn);

        VBox vBox = new VBox(10, sharedTableView, buttonBox);
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
                FilenameItem item = remoteTableView.getSelectionModel().getSelectedItem();
                downloadDialog(item, null);
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
            }
        });
        HBox buttonBox = new HBox(10, downloadBtn, downloadToBtn);

        VBox vBox = new VBox(10, remoteTableView, buttonBox);
        vBox.setPadding(new Insets(10, 30, 20, 20));

        return vBox;
    }

    private Pane createRightPane() {

        HBox buttonBox = new HBox(10);

        VBox vBox = new VBox(10, downloadTableView, buttonBox);
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

        wd = new WorkIndicatorDialog(mainStage.getOwner(), "Downloading " + filename.getFilename() + "...");

        wd.addTaskEndNotification(result -> {
            System.out.println(result);
            wd = null; // don't keep the object, cleanup
            veil.setVisible(false);
        });

        wd.exec("done", inputParam -> {

            veil.setVisible(true);
            // Download
            try {
                communicationsController.downloadFile(filename, newFile);
                Thread.sleep(5 * 1000);
            } catch (IOException e) {
                e.printStackTrace(); // FIXME
                return -1;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 0;
        });

        return wd.getResultValue();
    }
}
