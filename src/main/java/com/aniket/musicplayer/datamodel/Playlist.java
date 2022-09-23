package com.aniket.musicplayer.datamodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Playlist {
    public static final String defaultName = "My Playlist";
    private final SimpleStringProperty playlistName = new SimpleStringProperty(defaultName);
    private final String filePath = "src/main/resources/playlistXMLSource";
    private final ObservableList<Track> playlist;
    private static final Playlist instance = new Playlist();

    public Playlist() {
        this.playlist = FXCollections.observableArrayList();
    }

    public static Playlist getInstance() {
        return instance;
    }

    public String getPlaylistName() {
        return playlistName.get();
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName.set(playlistName);
    }

    public ObservableList<Track> getPlaylist() {
        return playlist;
    }

    public List<File> readData() {

        List<File> files = FXCollections.observableArrayList();

        try{
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.filePath);
            doc.getDocumentElement().normalize();
            this.playlistName.set(doc.getDocumentElement().getElementsByTagName("Name").item(0).getTextContent());
            NodeList nodeList = doc.getElementsByTagName("Track");
            Node node;
            Element nodeEl;
            for(int i = 0; i < nodeList.getLength(); i++){
                node = nodeList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    nodeEl = ((Element) node.getChildNodes());
                    files.add(new File(nodeEl.getElementsByTagName("Path").item(0).getTextContent()));
                }
                node.getNextSibling();
            }
        }catch (ParserConfigurationException | SAXException | IOException e){
            this.writeData();
        }
        return files;
    }

    public void writeData() {

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootEl = doc.createElement("Playlist");
            Element playlistNameEl = doc.createElement("Name");
            playlistNameEl.setTextContent(this.getPlaylistName());
            rootEl.appendChild(playlistNameEl);
            Element tracksEl = doc.createElement("Tracks");
            for (Track track : playlist) {
                Element trackEl = doc.createElement("Track");
                Element pathEl = doc.createElement("Path");
                pathEl.setTextContent(track.getFilePath());
                trackEl.appendChild(pathEl);
                tracksEl.appendChild(trackEl);
            }
            rootEl.appendChild(tracksEl);
            doc.appendChild(rootEl);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(this.filePath)));
            } catch (TransformerException | IOException tr) {
                System.out.println(tr.getMessage());
            }
        }catch (ParserConfigurationException p){
            System.out.println(p.getMessage());
        }
    }
}
