package com.aniket.musicplayer;

import com.aniket.musicplayer.datamodel.Playlist;
import com.aniket.musicplayer.datamodel.Track;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.function.Predicate;

public class SearchController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Track> searchPlaylistTableView;
    private Predicate<Track> FILTER_OFF;
    private final FilteredList<Track> filteredTrackList = new FilteredList<>(Playlist.getInstance().getPlaylist());

    public void initialize(){
        this.FILTER_OFF = track -> true;
        this.filteredTrackList.setPredicate(FILTER_OFF);
        this.searchPlaylistTableView.setItems(this.filteredTrackList);
        this.searchField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            this.searchTrack(newValue);
        });
    }

    private void searchTrack(String param){
        Predicate<Track> FILTER_ON = track -> {
            if(track.getTitle().toLowerCase().contains(param.toLowerCase())){
                return true;
            }
            return false;
        };

        if(param.isEmpty()){
            this.filteredTrackList.setPredicate(this.FILTER_OFF);
        }else{
            this.filteredTrackList.setPredicate(FILTER_ON);
        }
    }

    public Track fetchSelectedTrack(){
        return this.searchPlaylistTableView.getSelectionModel().getSelectedItem();
    }

}
