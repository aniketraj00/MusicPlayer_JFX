package com.aniket.musicplayer;

import com.aniket.musicplayer.datamodel.Playlist;
import com.aniket.musicplayer.datamodel.Track;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Controller {
    @FXML
    private BorderPane mainWindow;
    @FXML
    private TableView<Track> playListTableView;
    @FXML
    private Button importTrackBtn;
    @FXML
    private Button searchTrackBtn;
    @FXML
    private Button displayCurrentTrackBtn;
    @FXML
    private Button savePlaylistBtn;
    @FXML
    private Button deletePlaylistBtn;
    @FXML
    private ImageView albumArtView;
    @FXML
    private ToggleButton muteVolumeBtn;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Tooltip volumeSliderTooltip;
    @FXML
    private Button prevTrackBtn;
    @FXML
    private Button playPauseBtn;
    @FXML
    private Button nextTrackBtn;
    @FXML
    private Button playlistShuffleBtn;
    @FXML
    private Slider seekSlider;
    @FXML
    private Tooltip seekSliderTooltip;
    @FXML
    private Label playlistNameLabel;
    @FXML
    private Label nowPlayingLabel;
    @FXML
    private Label trackTitleLabel;
    @FXML
    private Label trackAlbumLabel;
    @FXML
    private Label trackArtistLabel;
    @FXML
    private Label trackDurationLabel;
    @FXML
    private Label trackYearLabel;
    @FXML
    private ProgressBar importProgressBar;
    @FXML
    private VBox progressVBox;

    private final Image defaultAlbumArtImg = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/img/defaultAlbumArt.png").toURI().toString());
    private final Image importIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/General/importIcon24.png").toURI().toString());
    private final Image musicIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/Media/musicIcon24.png").toURI().toString());
    private final Image searchIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/Media/searchMusicIcon24.png").toURI().toString());
    private final Image saveIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/General/saveIcon24.png").toURI().toString());
    private final Image trashIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/General/deleteIcon24.png").toURI().toString());
    private final Image muteIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/Media/muteIcon16.png").toURI().toString());
    private final Image volumeIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/Media/volumeIcon16.png").toURI().toString());
    private final Image playIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/Media/playIcon24.png").toURI().toString());
    private final Image pauseIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/Media/pauseIcon24.png").toURI().toString());
    private final Image nextIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/Media/nextIcon24.png").toURI().toString());
    private final Image prevIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/Media/prevIcon24.png").toURI().toString());
    private final Image shuffleIcon = new Image(new File("src/main/resources/com/aniket/musicplayer/resources/icon/Media/shuffleIcon24.png").toURI().toString());
    private final ContextMenu playlistContextMenu = new ContextMenu(new MenuItem("Play"), new MenuItem("Delete"));
    private final TrackImportService importService = new TrackImportService(null);
    private double volumeLevel = 1.0;
    private MediaPlayer player;
    private Track currentTrack;


    public void initialize() {
        //Setup Player Defaults (properties and event handlers (if any)).
        this.playerPresets();

        this.mainWindow.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.SPACE)){
                this.playPauseBtn.fire();
            }
        });

        this.importService.setOnSucceeded(workerStateEvent -> {
            Playlist.getInstance().getPlaylist().addAll(this.importService.getValue());
            this.playlistNameLabel.setText(Playlist.getInstance().getPlaylistName());
            this.playListTableView.getSelectionModel().selectFirst();
            this.firePlayBtn();
        });

        this.playlistContextMenu.getItems().get(0).setOnAction(actionEvent -> this.initPlayer(this.playListTableView.getSelectionModel().getSelectedItem()));
        this.playlistContextMenu.getItems().get(1).setOnAction(actionEvent -> this.deleteTrack(this.playListTableView.getSelectionModel().getSelectedItem()));

        //Prepare the playlist TableView by adding row and column factories as well as event handlers (if any).
        for(TableColumn<Track,?> tableColumn : this.playListTableView.getColumns()){
            tableColumn.setCellFactory(this.playlistTableCellFactory());
        }

        this.playListTableView.setPlaceholder(new Label(""));

        this.playListTableView.setRowFactory(trackTableView -> {
            TableRow<Track> row = new TableRow<>();
            row.emptyProperty().addListener((observableValue, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(this.playlistContextMenu);
                    row.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                        if(mouseEvent.getClickCount() == 2){
                            this.firePlayBtn();
                        }
                    });
                }
            });
            return row;
        });

        this.playListTableView.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                this.deleteTrack(this.playListTableView.getSelectionModel().getSelectedItem());
            }

            if(keyEvent.getCode().equals(KeyCode.ENTER)){
                this.firePlayBtn();
            }
        });


        //Bind progress bar with import service progress property to display progress while importing tracks.
        this.importProgressBar.progressProperty().bind(this.importService.progressProperty());
        this.progressVBox.visibleProperty().bind(this.importService.runningProperty());

        //Load Saved Data (if any).
        List<File> fileList = Playlist.getInstance().readData();
        if(!fileList.isEmpty()){
            this.importService.setFiles(fileList);
            this.importService.start();
        }

        //Bind the playlist data model to the playlist Table View.
        this.playListTableView.setItems(Playlist.getInstance().getPlaylist());
    }


    public void playerPresets() {
        this.renderAlbumArtView();
        this.renderToolbarIcons();
        this.setupPlayerControls();
        this.renderCurrentTrackView(false, "", "", "", "", Duration.seconds(0), null);
        this.disablePlayerControls(true);
    }

    public void initPlayer(Track track) {
        this.currentTrack = track;

        if (this.player != null) {
            this.player.dispose();
        }

        Media media = new Media(new File(track.getFilePath()).toURI().toString());
        this.player = new MediaPlayer(media);
        this.player.setAutoPlay(true);

        player.setOnReady(() -> {
            this.disablePlayerControls(false);
            this.setupPlayerControls();

            this.playPauseBtn.setOnAction(actionEvent -> {
                if (this.player.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                    this.player.pause();
                    this.playPauseBtn.setGraphic(new ImageView(this.playIcon));
                } else if (this.player.getStatus().equals(MediaPlayer.Status.PAUSED)) {
                    this.player.play();
                    this.playPauseBtn.setGraphic(new ImageView(this.pauseIcon));
                }
            });

            this.nextTrackBtn.setOnAction(actionEvent -> {
                int currentTrackIndex = this.playListTableView.getItems().indexOf(this.currentTrack);
                if (currentTrackIndex != (this.playListTableView.getItems().size() - 1)) {
                    this.initPlayer(this.playListTableView.getItems().get(currentTrackIndex + 1));
                }
            });

            this.prevTrackBtn.setOnAction(actionEvent -> {
                int currentTrackIndex = this.playListTableView.getItems().indexOf(this.currentTrack);
                if (currentTrackIndex != 0) {
                    this.initPlayer(this.playListTableView.getItems().get(currentTrackIndex - 1));
                }
            });

            this.playlistShuffleBtn.setOnAction(actionEvent -> {
                FXCollections.shuffle(this.playListTableView.getItems());
                Platform.runLater(() -> {
                    this.playListTableView.getSelectionModel().selectFirst();
                    this.playListTableView.scrollTo(this.playListTableView.getSelectionModel().getSelectedItem());
                    this.playListTableView.requestFocus();
                    this.firePlayBtn();
                });
            });

            this.player.setOnEndOfMedia(() -> this.nextTrackBtn.fire());

            this.player.currentTimeProperty().addListener((observableValue, oldValue, newValue) -> this.seekSlider.setValue(this.player.getCurrentTime().toSeconds()));

            this.muteVolumeBtn.selectedProperty().bindBidirectional(this.player.muteProperty());

            this.renderCurrentTrackView(true, this.currentTrack.getTitle(),
                    this.currentTrack.getAlbum(),
                    this.currentTrack.getArtist(),
                    this.currentTrack.getYear(),
                    this.player.getMedia().getDuration(),
                    this.currentTrack.getAlbumCover()
            );
        });

        if (player.getStatus().equals(MediaPlayer.Status.UNKNOWN)) {
            this.playerPresets();
        }
    }

    public void renderAlbumArtView() {
        this.albumArtView.setFitWidth(200);
        this.albumArtView.setPreserveRatio(true);
        this.albumArtView.setSmooth(true);
    }

    public void renderToolbarIcons() {
        this.importTrackBtn.setGraphic(new ImageView(this.importIcon));
        this.searchTrackBtn.setGraphic(new ImageView(this.searchIcon));
        this.displayCurrentTrackBtn.setGraphic(new ImageView(this.musicIcon));
        this.savePlaylistBtn.setGraphic(new ImageView(this.saveIcon));
        this.deletePlaylistBtn.setGraphic(new ImageView(this.trashIcon));
    }

    public void renderVolumeMuteToggleBtn() {
        this.muteVolumeBtn.setGraphic(new ImageView(this.volumeIcon));
        this.muteVolumeBtn.selectedProperty().addListener((observableValue, wasSelected, isSelectedNow) -> {
            if (isSelectedNow) {
                this.muteVolumeBtn.setGraphic(new ImageView(this.muteIcon));
            } else {
                this.muteVolumeBtn.setGraphic(new ImageView(this.volumeIcon));
            }
        });
    }

    public void renderPrevBtn() {
        this.prevTrackBtn.setGraphic(new ImageView(this.prevIcon));
        this.prevTrackBtn.setDisable(this.playListTableView.getItems().indexOf(this.currentTrack) == 0);
    }

    public void renderNextBtn() {
        this.nextTrackBtn.setGraphic(new ImageView(this.nextIcon));
        this.nextTrackBtn.setDisable(this.playListTableView.getItems().indexOf(this.currentTrack) == (this.playListTableView.getItems().size() - 1));
    }

    public void renderPlayPauseBtn() {
        this.playPauseBtn.setGraphic(new ImageView(this.pauseIcon));
    }

    public void renderShuffleBtn() {
        this.playlistShuffleBtn.setGraphic(new ImageView(this.shuffleIcon));
    }

    public void renderCurrentTrackView(boolean isNowPlaying, String trackTitle, String trackAlbum, String trackArtist, String trackYear, Duration duration, Image albumArt) {
        this.albumArtView.setImage(albumArt == null ? this.defaultAlbumArtImg : albumArt);
        this.nowPlayingLabel.setText((isNowPlaying ? "Now Playing ..." : ""));
        this.trackTitleLabel.setText(trackTitle.isEmpty() ? "" : ("Title :  " + trackTitle));
        this.trackAlbumLabel.setText(trackAlbum.isEmpty() ? "" : ("Album :  " + trackAlbum));
        this.trackArtistLabel.setText(trackArtist.isEmpty() ? "" : ("Artist  : " + trackArtist));
        this.trackYearLabel.setText(trackYear.isEmpty() ? "" : ("Year :  " + trackYear));
        this.trackDurationLabel.setText((duration.toSeconds() == 0) ? "" : ("Duration :  " + this.parseDuration(duration)));
    }

    public void renderVolumeSlider() {
        if (this.player != null) {
            if (this.player.getStatus().equals(MediaPlayer.Status.READY)) {
                this.volumeSlider.setMin(0.0);
                this.volumeSlider.setMax(1.0);
                this.volumeSlider.setValue(this.volumeLevel);
                this.volumeSliderTooltip.setText("Volume : " + ((int)(this.volumeLevel * 100)) + "%");
                this.player.setVolume(this.volumeLevel);

                this.volumeSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
                    this.player.setVolume(newValue.doubleValue());
                    this.volumeSliderTooltip.setText("Volume : " + ((int)(newValue.doubleValue() * 100)) + "%");
                    this.volumeLevel = newValue.doubleValue();
                });
            }
        }

    }

    public void renderSeekSlider() {
        if (this.player != null) {
            if (this.player.getStatus().equals(MediaPlayer.Status.READY)) {
                this.seekSlider.setMin(0.0);
                this.seekSlider.setMax(this.player.getMedia().getDuration().toSeconds());
                this.seekSlider.setValue(0.0);
                this.seekSlider.setValueChanging(true);

                ChangeListener<Number> changeListener = (observableValue, oldValue, newValue) -> this.player.seek(Duration.seconds(newValue.doubleValue()));

                this.seekSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> this.seekSliderTooltip.setText(this.parseDuration(Duration.seconds(newValue.doubleValue()))));

                this.seekSlider.setOnMousePressed(mouseEvent -> {
                    this.player.pause();
                    this.seekSlider.valueProperty().addListener(changeListener);
                });

                this.seekSlider.setOnMouseClicked(mouseEvent -> this.player.seek(Duration.seconds(this.seekSlider.getValue())));

                this.seekSlider.setOnMouseReleased(mouseEvent -> {
                    this.seekSlider.valueProperty().removeListener(changeListener);
                    this.player.play();
                });
            }
        }
    }

    public void setupPlayerControls() {
        this.renderPlayPauseBtn();
        this.renderNextBtn();
        this.renderPrevBtn();
        this.renderVolumeMuteToggleBtn();
        this.renderShuffleBtn();
        this.renderVolumeSlider();
        this.renderSeekSlider();
    }

    public void disablePlayerControls(Boolean disableControls) {
        this.playPauseBtn.setDisable(disableControls);
        this.prevTrackBtn.setDisable(disableControls);
        this.nextTrackBtn.setDisable(disableControls);
        this.playlistShuffleBtn.setDisable(disableControls);
        this.muteVolumeBtn.setDisable(disableControls);
        this.volumeSlider.setDisable(disableControls);
        this.seekSlider.setDisable(disableControls);
    }

    public void firePlayBtn() {
        this.playListTableView.setContextMenu(this.playlistContextMenu);
        this.playlistContextMenu.getItems().get(0).fire();
    }

    public String parseDuration(Duration duration) {
        int minutes = ((int) duration.toMinutes());
        int seconds = ((int) (duration.toSeconds() - (minutes * 60)));
        if ((seconds + "").length() == 1) {
            return (minutes + ":0" + seconds);
        }
        return (minutes + ":" + seconds);
    }

    public void deleteTrack(Track track) {
        Optional<ButtonType> userResponse = this.popupAlertWindow(
                Alert.AlertType.CONFIRMATION,
                "Confirm",
                "Do you want to delete this Track ?",
                "Title : " + track.getTitle()
        );
        if (userResponse.isPresent() && userResponse.get() == ButtonType.OK) {
            this.playListTableView.getItems().remove(track);
            if (track.equals(this.currentTrack)) {
                this.firePlayBtn();
            }
        }

    }

    public Optional<ButtonType> popupAlertWindow(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        if (alertType.equals(Alert.AlertType.CONFIRMATION)) {
            return alert.showAndWait();
        } else {
            alert.show();
            return Optional.empty();
        }
    }

    public <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> playlistTableCellFactory(){
        return tableColumn -> new TableCell<>(){
            @Override
            protected void updateItem(T item, boolean isEmpty) {
                super.updateItem(item, isEmpty);
                if(isEmpty){
                    setText(null);
                }else{
                    if(item.toString().isEmpty()){
                        setText("Not Available");
                    }else{
                        setText(item.toString());
                    }
                }
            }
        };
    }

    @FXML
    public void importTracks() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Music Files", "*.mp3","*.wav"));
        List<File> files = chooser.showOpenMultipleDialog(this.mainWindow.getScene().getWindow());
        if (files != null) {
            this.importService.setFiles(files);
            if(this.importService.getState().equals(Worker.State.SUCCEEDED)){
                this.importService.restart();
                return;
            }
            this.importService.start();
        }
    }

    @FXML
    public void savePlaylist(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(this.mainWindow.getScene().getWindow());
        dialog.setTitle("Confirm");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("savePlaylist.fxml"));
        try {
            dialog.setDialogPane(fxmlLoader.load());
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        dialog.getDialogPane().setHeaderText("Save Playlist to Local Disk");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> userResponse = dialog.showAndWait();
        if(userResponse.isPresent() && userResponse.get() == ButtonType.OK){
            SavePlaylistController controller = fxmlLoader.getController();
            String userInput = controller.getPlaylistNameInput();
            if(!userInput.isEmpty() && userInput.length() < 50){
                Playlist.getInstance().setPlaylistName(userInput);
                this.playlistNameLabel.setText(userInput);
                this.saveData();
            }
        }
    }

    @FXML
    public void searchTrack() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(this.mainWindow.getScene().getWindow());
        dialog.setTitle("Search");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("search.fxml"));
        try {
            dialog.setDialogPane(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        dialog.getDialogPane().setHeaderText("Search and Play Track from the Playlist");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Optional<ButtonType> userResponse = dialog.showAndWait();
        if (userResponse.isPresent() && userResponse.get() == ButtonType.OK) {
            SearchController searchController = fxmlLoader.getController();
            Track foundTrack = searchController.fetchSelectedTrack();
            if (foundTrack != null) {
                Optional<ButtonType> userResponseToPlayFoundTrack = this.popupAlertWindow(
                        Alert.AlertType.CONFIRMATION,
                        "Confirm",
                        "Do you want to play this track ?",
                        "Title : " + foundTrack.getTitle());
                if (userResponseToPlayFoundTrack.isPresent() && userResponseToPlayFoundTrack.get().equals(ButtonType.OK)) {
                    this.playListTableView.getSelectionModel().select(foundTrack);
                    this.playListTableView.scrollTo(foundTrack);
                    this.playListTableView.requestFocus();
                    this.firePlayBtn();
                }
            }
        }
    }

    @FXML
    public void navigateToPlayingTrack() {
        this.playListTableView.getSelectionModel().select(this.currentTrack);
        this.playListTableView.scrollTo(this.currentTrack);
        this.playListTableView.requestFocus();
    }

    @FXML
    public void saveData(){
        Playlist.getInstance().writeData();
    }

    @FXML
    public void deletePlaylist(){
        if (!this.playListTableView.getItems().isEmpty()) {
            Optional<ButtonType> userResponse = this.popupAlertWindow(
                    Alert.AlertType.CONFIRMATION,
                    "Confirm",
                    "Do you want to delete the playlist ?",
                    ""
            );
            if (userResponse.isPresent() && userResponse.get() == ButtonType.OK) {
                if(this.player != null){
                    this.player.dispose();
                }
                this.playerPresets();
                this.playListTableView.getItems().clear();
                this.playlistNameLabel.setText("");
                Playlist.getInstance().setPlaylistName(Playlist.defaultName);
                this.saveData();
            }
        }
    }

    @FXML
    public void about() {
        this.popupAlertWindow(Alert.AlertType.INFORMATION, "About", "AR Music Player", "Author : Aniket Raj \nCopyright \u00A9 2020");
    }

    @FXML
    public void quit() {
        Optional<ButtonType> userResponse = this.popupAlertWindow(Alert.AlertType.CONFIRMATION, "Exit", "Do you want to quit the player ?", "");
        if(userResponse.isPresent() && userResponse.get() == ButtonType.OK){
            Platform.exit();
        }
    }


}
































