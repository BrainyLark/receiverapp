/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filereceiver.view;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * FXML Controller class
 *
 * @author erdenebileg
 */
public class ReceiverController implements Initializable {

    @FXML
    Button downloadBtn;
    @FXML
    Label nameLabel;
    @FXML
    Label extensionLabel;
    @FXML
    Label sizeLabel;
    @FXML
    ProgressBar progressBar;

    private Socket client;
    private DataInputStream is;
    private BufferedOutputStream out;

    private SimpleStringProperty nameProperty;
    private SimpleStringProperty extensionProperty;
    private SimpleStringProperty sizeProperty;

    private SimpleDoubleProperty percentProperty;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nameProperty = new SimpleStringProperty("unknown");
        extensionProperty = new SimpleStringProperty("unknown");
        sizeProperty = new SimpleStringProperty("unknown");
        percentProperty = new SimpleDoubleProperty(0);
        initBtn();
    }

    private void initBtn() {
        progressBar.progressProperty().bind(percentProperty);
        nameLabel.textProperty().bindBidirectional(nameProperty);
        extensionLabel.textProperty().bind(extensionProperty);
        sizeLabel.textProperty().bind(sizeProperty);

        downloadBtn.setOnAction((e) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    downloadFile();
                }

            }).start();
        });
    }

    public void downloadFile() {
        try {
            client = new Socket("localhost", 8001);
            is = new DataInputStream(client.getInputStream());
            String filename = is.readUTF();
            String extension = is.readUTF();
            long size = is.readLong();
            final long totalSize = size;

            double percent = 0;
             
            Platform.runLater(() -> {
                nameProperty.set(filename);
                extensionProperty.set(extension);
                sizeProperty.set(Long.toString(totalSize));
            });

            out = new BufferedOutputStream(new DataOutputStream(new FileOutputStream("/home/erdenebileg/Desktop/" + filename + "." + extension)));

            while (size > 0) {
                out.write(is.read());
                percent = 1 - (double) size / totalSize;
                percentProperty.set(percent);
                size--;
            }

            out.close();
            is.close();
            client.close();

        } catch (IOException ex) {
            System.out.println("Error occured while connecting sockets : " + ex.getMessage());
        }
    }

}
