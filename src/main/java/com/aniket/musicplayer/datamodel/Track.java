package com.aniket.musicplayer.datamodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

public class Track {
    private String filePath;
    private final SimpleStringProperty title = new SimpleStringProperty("");
    private final SimpleStringProperty album = new SimpleStringProperty("");
    private final SimpleStringProperty artist = new SimpleStringProperty("");
    private final SimpleStringProperty year = new SimpleStringProperty("");
    private Image albumCover;


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getAlbum() {
        return album.get();
    }

    public SimpleStringProperty albumProperty() {
        return album;
    }

    public void setAlbum(String album) {
        this.album.set(album);
    }

    public String getArtist() {
        return artist.get();
    }

    public SimpleStringProperty artistProperty() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist.set(artist);
    }

    public String getYear() {
        return year.get();
    }

    public SimpleStringProperty yearProperty() {
        return year;
    }

    public void setYear(String year) {
        this.year.set(year);
    }

    public Image getAlbumCover() {
        return albumCover;
    }

    public void setAlbumCover(Image albumCover) {
        this.albumCover = albumCover;
    }
}
