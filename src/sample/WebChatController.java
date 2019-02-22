package sample;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class WebChatController {

    public ListView TheChat;
    public ImageView UserOneImage;
    public ImageView UserTwoImage;
    public TextField UserOneText;
    public TextField yourNameText;
    public TextField IPAddressText;
    public TextField portText;
    public TextField statusText;
    public Button UserOneSend;
    public Button UserOneFile;
    public Button startButton;
    public MediaView UserOneMedia;
    public MediaView UserTwoMedia;
    private SynchronizedQueue inQueue;
    private SynchronizedQueue outQueue;

    private SynchronizedQueue Queue;
    private Stage stage;

    private boolean serverMode;
    static boolean connected;

    public void initialize() {
        Queue = new SynchronizedQueue();
        inQueue = new SynchronizedQueue();
        outQueue = new SynchronizedQueue();
        connected = false;

        // Create and start the GUI updater thread
        UpdateGUI updater = new UpdateGUI(inQueue, UserOneText, UserOneImage, UserOneMedia, TheChat, yourNameText);
        Thread updaterThread = new Thread(updater);
        updaterThread.start();
        //GUI Updates text, image, and file to either people.
    }

    public void setStage(Stage theStage) {
        stage = theStage;
    }

    void setServerMode() {
        serverMode = true;
        startButton.setText("Start");
        try {
            // display the computer's IP address
            IPAddressText.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception ex) {
            ex.printStackTrace();
            statusText.setText("Server start: getLocalHost failed. Exiting....");
        }
    }

    void setClientMode() {
        serverMode = false;
        startButton.setText("Connect");
        // display the IP address for the local computer
        IPAddressText.setText("127.0.0.1");
    }

    public void startButtonPressed() {
        // If we're already connected, start button should be disabled
        if (connected) {
            // don't do anything else; the threads will stop and everything will be cleaned up by them.
            return;
        }

        // We can't start network connection if Port number is unknown
        if (portText.getText().isEmpty()) {
            // user did not enter a Port number, so we can't connect.
            statusText.setText("Type a port number BEFORE connecting.");
            return;
        }

        // We're gonna start network connection!
        connected = true;
        startButton.setDisable(true);

        if (serverMode) {

            // We're a server: create a thread for listening for connecting clients
            ConnectToNewClients connectToNewClients = new ConnectToNewClients(Integer.parseInt(portText.getText()), inQueue, outQueue, statusText, yourNameText);
            Thread connectThread = new Thread(connectToNewClients);
            connectThread.start();

        } else {

            // We're a client: connect to a server
            try {
                Socket socketClientSide = new Socket(IPAddressText.getText(), Integer.parseInt(portText.getText()));
                statusText.setText("Connected to server at IP address " + IPAddressText.getText() + " on port " + portText.getText());

                // The socketClientSide provides 2 separate streams for 2-way communication
                //   the InputStream is for communication FROM server TO client
                //   the OutputStream is for communication TO server FROM client
                // Create data reader and writer from those stream (NOTE: ObjectOutputStream MUST be created FIRST)

                // Every client prepares for communication with its server by creating 2 new threads:
                //   Thread 1: handles communication TO server FROM client
                CommunicationOut communicationOut = new CommunicationOut(socketClientSide, new ObjectOutputStream(socketClientSide.getOutputStream()), outQueue, statusText);
                Thread communicationOutThread = new Thread(communicationOut);
                communicationOutThread.start();

                //   Thread 2: handles communication FROM server TO client
                CommunicationIn communicationIn = new CommunicationIn(socketClientSide, new ObjectInputStream(socketClientSide.getInputStream()), inQueue, null, statusText, yourNameText);
                Thread communicationInThread = new Thread(communicationIn);
                communicationInThread.start();

            } catch (Exception ex) {
                ex.printStackTrace();
                statusText.setText("Client start: networking failed. Exiting....");
            }

            // We connected!
        }
    }

    public void OpenFile() {
        //Open file and put it into imageview
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);

        // If user chose an image file via FileChooser
        if (file != null) {
            if (file.getPath().endsWith(".png") || file.getPath().endsWith(".tif")
                    || file.getPath().endsWith(".jpeg") || file.getPath().endsWith(".gif")
                    || file.getPath().endsWith(".jpg")) {
                Image newImage = new Image(file.toURI().toString());
                UserOneImage.setImage(newImage);
            }

            // Check if the file is a media file
            if (file.getPath().endsWith(".avi") || file.getPath().endsWith(".flv")
                    || file.getPath().endsWith(".wmv") || file.getPath().endsWith(".mov")
                    || file.getPath().endsWith(".mp4")) {

                Media UOM = new Media(file.toURI().toString());
                MediaPlayer UOMP = new MediaPlayer(UOM);
                UOMP.setCycleCount(MediaPlayer.INDEFINITE);
                UOMP.setAutoPlay(true);
                UserOneMedia.setMediaPlayer(UOMP);
            }
        }
    }

    public void SendUserOne() {
        Message message = new Message(yourNameText.getText(), UserOneText.getText(), UserOneImage.getImage());
        boolean putSucceeded = outQueue.put(message);
        while (!putSucceeded) {
            Thread.currentThread().yield();
            putSucceeded = outQueue.put(message);
        }

        /*Image userOneImg = UserOneImage.getImage();
        if (userOneImg != null) {
            while (!outQueue.put(userOneImg)) {
                Thread.currentThread().yield();
            }
        } else {
            String empty = "E.M.P.T.Y";
            while (!outQueue.put(empty)) {
                Thread.currentThread().yield();
            }
        }
        System.out.println("SendMessage: PUT " + userOneImg);

        MediaPlayer userOneMed = UserOneMedia.getMediaPlayer();
        if (userOneMed != null) {
            while (!outQueue.put(userOneMed)) {
                Thread.currentThread().yield();
            }
        } else {
            String empty = "E.M.P.T.Y";
            while (!outQueue.put(empty)) {
                Thread.currentThread().yield();
            }
        }
        System.out.println("SendMessage: PUT " + userOneMed);


        String userOneText = "User 1: " + UserOneText.getText();
        UserOneText.setText("");

        if (userOneText != null) {
            while (!outQueue.put(userOneText)) {
                Thread.currentThread().yield();
            }
        } else {
            String empty = "E.M.P.T.Y";
            while (!outQueue.put(empty)) {
                Thread.currentThread().yield();
            }
        }
        System.out.println("SendMessage: PUT " + userOneText);*/
    }
}
