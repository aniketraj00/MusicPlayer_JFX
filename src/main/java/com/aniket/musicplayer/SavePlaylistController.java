package com.aniket.musicplayer;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SavePlaylistController {

    @FXML
    private TextField playlistNameField;


    public String getPlaylistNameInput(){
        return playlistNameField.getText();
    }
}
