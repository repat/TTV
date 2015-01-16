package main;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordClient implements Runnable {
    // constants for config
    private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    private static final String HOST_PORT = "8080";
    private static final String STARTING_PORT = "8181";
    private static final String HOST_IP = "192.168.1.1";
    private static final String LOCAL_IP = "192.168.1.2";
    private static final int NUMBER_OF_PLAYERS = 2;
    private static final int NUMBER_OF_THREADS = 1;
    private static final int S = 10; // number of ships
    private static final int I = 100; // number of mySectors
    private static final boolean TESTING_MODE = true;
    private static final int WAITING_TIME_CHORD_JOIN = 10000;
    private static final int MIDDLE = 2;

    // instances of things
    private ChordImpl chordImpl;
    private ID distance;
    private final Scanner scanner = new Scanner(System.in);
    private GameNotify myNotifyCallback;
    private String localPort = "0";
    private String joinOrCreate = "join";
    private static String JOIN_OR_CREATE_FOR_THIS_CLIENT = "create";

    // package private
    ID myID;
    ID[] mySectors = new ID[I];
    final boolean[] ships = new boolean[I];
    static final ID BIGGEST_ID = new ID(BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE).toByteArray());
    final String PLAYER_NAME; // just for local testing purposos

    public ChordClient(String localPort, String playerName, String joinOrCreate) {
        this.localPort = localPort;
        this.PLAYER_NAME = playerName;
        this.joinOrCreate = joinOrCreate;
    }

    public static void main(String[] args) throws InterruptedException {

        // spawns x numbers of clients
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            // TODO: unsinn
            String port = String.valueOf(Integer.valueOf(STARTING_PORT) + i);
            
            new Thread(new ChordClient(port, "Player " + (i + 1), JOIN_OR_CREATE_FOR_THIS_CLIENT)).start();
            // sleep prevents calling "loadPropertyFile()" at the same time.
            Thread.sleep(100);
        }
    }

    @Override
    public void run() {

        if (joinOrCreate == "join") {
            joinChord();
        } else if (joinOrCreate == "create") {
            createChord();
        }

        if (TESTING_MODE) {
            // start the game after s was typed in
            String input;
            do {
                System.out.print("type \"s\" to begin: ");
                input = scanner.next();
            } while (!input.equals("s"));
            scanner.close();
        }

        // waits 10000 msec to make sure others have joined chord
        try {
            if (TESTING_MODE) {
                Thread.sleep(WAITING_TIME_CHORD_JOIN);
            } else {
                Thread.sleep(3000);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ChordClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        myID = chordImpl.getID();
        mySectors = calculateSectors(chordImpl.getPredecessorID(), myID);
        setShips();

        // we start the game if we have the BIGGESTID
        if (chordImpl.getPredecessorID().compareTo(chordImpl.getID()) > 0) {
            System.out.println(PLAYER_NAME + ": I start!");

            shoot();
        }
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
            localURL = new URL(PROTOCOL + "://" + LOCAL_IP + ":" + localPort + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        // } catch (UnknownHostException e) {
        // Logger.getLogger(ChordClient.class.getName()).log(Level.SEVERE, null,
        // e);
        // }

        chordImpl = new ChordImpl();
        myNotifyCallback = new GameNotify();
        myNotifyCallback.setChordClient(this, chordImpl);
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

    /**
     * Calculates the mySectors between the predecessor and our id and splits
     * this into I Sectors. Also handles the case if the predecessor has a
     * bigger id than we do.
     * 
     * @param from
     *            the first id, thinking circle clockwise
     * @param to
     *            the second id,
     * @return array of BigInteger
     */
    private ID[] calculateSectors(ID from, ID to) {
        ID[] result = new ID[I];

        // predecessorID might be bigger than our ID, due to Chord circle
        if (from.compareTo(to) < 0) {
            distance = to.subtract(from);
        } else {
            distance = BIGGEST_ID.subtract(from).add(to);
        }

        ID step = distance.divide(I);

        for (int i = 0; i < I; i++) {
            // (from + 1 + (i * step)) % biggestID
            result[i] = from.add(1).add(step.multiply(i)).mod(BIGGEST_ID);

            // System.out.println("sector " + (i + 1) + ": " + result[i]);
        }
        return result;
    }

    /**
     * Places S ships in I mySectors
     */
    private void setShips() {

        Random rnd = new Random();
        int random;

        for (int i = 0; i < S; i++) {
            do {
                random = rnd.nextInt(I);
            } while (ships[random] == true);

            ships[random] = true;
        }
    }

    void shoot() {

        System.out.println();
        Random rnd = new Random();
        long sectorNumber;
        ID sectorSize = BIGGEST_ID.divide(I * NUMBER_OF_PLAYERS);
        ID target;

        do {
            // number of sectors * players
            sectorNumber = rnd.nextInt(I * NUMBER_OF_PLAYERS);
            target = sectorSize.multiply(sectorNumber).add((sectorSize.divide(MIDDLE))).mod(BIGGEST_ID);
        } while (isInMyInterval(target) || isAlreadyHit(target));

        System.out.println(PLAYER_NAME + ": shooting at: " + target.toBigInteger());
        chordImpl.retrieve(target);
    }

    boolean isInMyInterval(ID id) {
        boolean result = id.isInInterval(mySectors[0], myID);
        // if (result) {
        // System.out.println(PLAYER_NAME + ": " + id + " is my own ID");
        // }
        return result;
    }

    private boolean isAlreadyHit(ID id) {

        for (BroadcastLog b : myNotifyCallback.getBroadcastLog()) {
            if (b.getTarget().equals(id)) {
                // System.out.println(getPLAYER_NAME() +
                // ": already hit this target!");
                return true;
            }
        }
        return false;
    }

    private void createChord() {
        URL localURL = null;
        try {
            localURL = new URL(PROTOCOL + "://" + HOST_IP + ":" + HOST_PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        
        ChordImpl chordImpl = new ChordImpl();
        NotifyCallback myNotifyCallback = new GameNotify();
        chordImpl.setCallback(myNotifyCallback);

        try {
            chordImpl.create(localURL);
        } catch (ServiceException e) {
            throw new RuntimeException("Could not create DHT!", e);
        }

        System.out.println("Chord listens on: " + localURL + "\n");
    }
}
