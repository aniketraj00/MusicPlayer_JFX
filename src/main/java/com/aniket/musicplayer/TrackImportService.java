package com.aniket.musicplayer;

import com.aniket.musicplayer.datamodel.Playlist;
import com.aniket.musicplayer.datamodel.Track;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

import java.io.File;
import java.util.List;

public class TrackImportService extends Service<ObservableList<Track>> {
    private List<File> files;

    public TrackImportService(List<File> files) {
        this.files = files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    @Override
    protected Task<ObservableList<Track>> createTask() {
        return new Task<>() {
            @Override
            protected ObservableList<Track> call() throws Exception {
                ObservableList<Track> playlist = FXCollections.observableArrayList();
                for(File file : files){
                    playlist.add(new Track());
                    int playListCurrentSize = playlist.size();
                    Track trackAdded = playlist.get(playListCurrentSize - 1);
                    Media curMedia = new Media(file.toURI().toString());
                    trackAdded.setTitle(file.getName());
                    trackAdded.setFilePath(file.getPath());
                    Platform.runLater(() -> {
                        curMedia.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
                            if(change.wasAdded()){
                                if(change.getKey().equals("album")){
                                    trackAdded.setAlbum(change.getValueAdded().toString());
                                }
                                if(change.getKey().equals("artist")){
                                    trackAdded.setArtist(change.getValueAdded().toString());
                                }
                                if(change.getKey().equals("year")){
                                    trackAdded.setYear(change.getValueAdded().toString());
                                }
                                if(change.getKey().equals("image")){
                                    trackAdded.setAlbumCover(((Image) change.getValueAdded()));
                                }
                            }
                        });
                    });
                    updateProgress(playListCurrentSize,files.size());
                }
                return playlist;
            }
        };
    }
}
