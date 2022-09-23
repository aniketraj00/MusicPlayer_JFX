package com.aniket.musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        primaryStage.setTitle("AR MP3 Player");
        primaryStage.getIcons().addAll(new Image(new File("src/main/resources/com/aniket/musicplayer/resources/img/defaultPlayerIcon.png").toURI().toString()));
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.show();
    }


}
