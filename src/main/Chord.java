package main;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.net.MalformedURLException;

public class Chord implements Runnable {

    private String joinOrCreate = "join";

    private ChordImpl chordImpl;
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
        return chordClient;
    }

    public void setChordClient(GameLogic chordClient) {
        this.chordClient = chordClient;
    }

    final static String PLAYER_NAME = "Rene";
    static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    static final String HOST_PORT = "8080";
    static final String HOST_IP = "192.168.1.1";
    static final String LOCAL_IP = "192.168.1.2";
    private GameLogic chordClient = null;
    private final String LOCAL_PORT = "8181";

    public Chord(String joinOrCreate) {
        this.joinOrCreate = joinOrCreate;
    }

    @Override
    public void run() {
        if (joinOrCreate.equals("join")) {
            joinChord();
        } else if (joinOrCreate.equals("create")) {
            createChord();
        }

    }

    private void createChord() {
        if (!PropertiesLoader.isLoaded()) {
            PropertiesLoader.loadPropertyFile();
        }

        URL localURL = null;
        try {
            localURL = new URL(PROTOCOL + "://" + HOST_IP + ":" + HOST_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        chordImpl = new ChordImpl();
        myNotifyCallback = new GameNotify();
        myNotifyCallback.setChordClient(chordClient, chordImpl);
        chordImpl.setCallback(myNotifyCallback);

        try {
            chordImpl.create(localURL);
        } catch (ServiceException e) {
            throw new RuntimeException("Could not create DHT!", e);
        }

        System.out.println(PLAYER_NAME + ": Chord listens on: " + localURL);
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
            // localURL = new URL(PROTOCOL + "://" +
            // Inet4Address.getLocalHost().getHostAddress() + ":" + localPort +
            // "/");
            localURL = new URL(PROTOCOL + "://" + LOCAL_IP + ":" + LOCAL_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        // } catch (UnknownHostException e) {
        // Logger.getLogger(ChordClient.class.getName()).log(Level.SEVERE, null,
        // e);
        // }

        chordImpl = new ChordImpl();
        myNotifyCallback = new GameNotify();
        myNotifyCallback.setChordClient(chordClient, chordImpl);
        chordImpl.setCallback(myNotifyCallback);

        URL bootstrapURL = null;
        try {
            bootstrapURL = new URL(PROTOCOL + "://" + HOST_IP + ":" + HOST_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            chordImpl.join(localURL, bootstrapURL);
        } catch (ServiceException e) {
            throw new RuntimeException("Could not join DHT!", e);
        }

        System.out.println(PLAYER_NAME + ": Chord running on: " + localURL);
        // System.out.println("Chord joined: " + bootstrapURL + "\n");
    }

}
