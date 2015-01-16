package main;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.net.MalformedURLException;

public class Chord {

    // constants for config
    private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    private static final String SERVER_IP = "192.168.1.1";
    private static final String SERVER_PORT = "8080";
    private static final String CLIENT_IP = "192.168.1.2";
    private static final String CLIENT_PORT = "8181";

    // instances of things
    private ChordImpl chordImpl;
    private final String joinOrCreate;
    private GameLogic gameLogic;

    public Chord(String joinOrCreate,GameLogic gameLogic) {
        this.joinOrCreate = joinOrCreate;
        this.gameLogic = gameLogic;
    }

    public void init() {
        if (joinOrCreate.equals("join")) {
            joinChord();
        } else if (joinOrCreate.equals("create")) {
            createChord();
        } else {
            System.out.println("ERROR: choose if you want to be server of client!");
        }
    }

    private void createChord() {
        if (!PropertiesLoader.isLoaded()) {
            PropertiesLoader.loadPropertyFile();
        }

        URL localURL = null;
        try {
            localURL = new URL(PROTOCOL + "://" + SERVER_IP + ":" + SERVER_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        chordImpl = new ChordImpl();
        myNotifyCallback = new GameNotify();
        myNotifyCallback.setChordClient(gameLogic, chordImpl);
        chordImpl.setCallback(myNotifyCallback);

        try {
            chordImpl.create(localURL);
        } catch (ServiceException e) {
            throw new RuntimeException("Could not create DHT!", e);
        }

        System.out.println("Chord listens on: " + localURL);
    }

    /**
     * joins the Chord Network
     *
     * @throws RuntimeException
     */
    private void joinChord() throws RuntimeException {
        if (!PropertiesLoader.isLoaded()) {
            PropertiesLoader.loadPropertyFile();
        }
        URL localURL = null;
        try {
            localURL = new URL(PROTOCOL + "://" + CLIENT_IP + ":" + CLIENT_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        chordImpl = new ChordImpl();
        myNotifyCallback = new GameNotify();
        myNotifyCallback.setChordClient(gameLogic, chordImpl);
        chordImpl.setCallback(myNotifyCallback);

        URL bootstrapURL = null;
        try {
            bootstrapURL = new URL(PROTOCOL + "://" + SERVER_IP + ":" + SERVER_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            chordImpl.join(localURL, bootstrapURL);
        } catch (ServiceException e) {
            throw new RuntimeException("Could not join DHT!", e);
        }

        System.out.println("Chord running on: " + localURL);
        // System.out.println("Chord joined: " + bootstrapURL + "\n");
    }

    public ChordImpl getChordImpl() {
        return chordImpl;
    }

    public void setChordImpl(ChordImpl chordImpl) {
        this.chordImpl = chordImpl;
    }

    private GameNotify myNotifyCallback;

    public GameNotify getMyNotifyCallback() {
        return myNotifyCallback;
    }

    public void setMyNotifyCallback(GameNotify myNotifyCallback) {
        this.myNotifyCallback = myNotifyCallback;
    }

    public GameLogic getChordClient() {
        return gameLogic;
    }

    public void setChordClient(GameLogic chordClient) {
        this.gameLogic = chordClient;
    }
}
