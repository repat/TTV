package main;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordClient {
    // constants
    private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    private static final String PORT = "8080";
    public static final BigInteger BIGGESTID = BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE);
    private static final String IP = "localhost";
    private static final int S = 10;
    private static final int I = 100;
	private final boolean DEBUG = false;

    private static ChordImpl chordImpl;
    final BigInteger[] sectors = new BigInteger[I];
    final boolean[] ships = new boolean[I];
    private static BigInteger myID;
    private static BigInteger predecessorID;

    public static void main(String[] args) {
        ChordClient cc = new ChordClient();
        cc.startGame();
    }

    public void startGame() {
        joinChord();
        calculateSectors();
        setShips();

        if (DEBUG) {
            System.out.println("maxID :   " + BIGGESTID);
        }
        for (int i = 0; i < I; i++) {
			shoot(getIdUnsigned(sectors[i].toByteArray()));
        }
    }

    private static ID getIdUnsigned(byte[] id) {
		ID result;
		if (id.length == 21 && id[0] == 0) {
			byte[] tmp = new byte[id.length - 1];
            System.arraycopy(id, 1, tmp, 0, tmp.length);
            result = new ID(tmp);
        } else {
            result = new ID(id);
        }
        return result;
    }

    /**
     * joins the Chord Network
     *
     * @throws RuntimeException
     */
    private void joinChord() throws RuntimeException {
        PropertiesLoader.loadPropertyFile();
        URL localURL = null;
        try {
            localURL = new URL(PROTOCOL + "://" + Inet4Address.getLocalHost().getHostAddress() + ":8181/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            Logger.getLogger(ChordClient.class.getName()).log(Level.SEVERE, null, e);
        }

        chordImpl = new ChordImpl();
        GameNotify myNotifyCallback = new GameNotify();
        myNotifyCallback.setChordClient(this, chordImpl);
        chordImpl.setCallback(myNotifyCallback);

        URL bootstrapURL = null;
        try {
            bootstrapURL = new URL(PROTOCOL + "://" + IP + ":" + PORT + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            chordImpl.join(localURL, bootstrapURL);
        } catch (ServiceException e) {
            throw new RuntimeException("Could not join DHT!", e);
        }

        System.out.println("Chord joined: " + bootstrapURL + "\n");

        if (DEBUG) {
            System.out.println("MaxID:         " + BIGGESTID);
            System.out.println("NodeId:        " + chordImpl.getID());
            System.out.println("PredecessorID: " + chordImpl.getPredecessorID());
        }
    }

    /**
     * Calculates the sectores between the predecessor and our id and splits
     * this into I Sectors. Also handles the case if the predecessor has a
     * bigger id than we do.
     */
    private void calculateSectors() {
        BigInteger distance;
        myID = chordImpl.getID().toBigInteger();
        predecessorID = chordImpl.getPredecessorID().toBigInteger();

        // System.out.println("myID:          " + myID);
        // System.out.println("predecessorID: " + predecessorID);

		// predecessorID might be bigger than our ID, due to Chord circle1
        if (myID.compareTo(predecessorID) > 0) {
            distance = myID.subtract(predecessorID);
        } else {
            distance = BIGGESTID.subtract(predecessorID).add(myID);
        }
        // System.out.println("distance:      " + distance);

        BigInteger step = distance.divide(BigInteger.valueOf(I));

        for (int i = 0; i < I; i++) {
            // (predecessorID + (i * step) + 1) % biggestID
            sectors[i] = predecessorID.add(BigInteger.valueOf((long) i).multiply(step)).add(BigInteger.ONE)
                    .mod(BIGGESTID);

            // System.out.println("sector " + (i + 1) + ": " + sectors[i]);
        }
    }

    /**
     * setzt zufällig S Schiffe auf einen der I Sektoren
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

        if (DEBUG) {
            for (int i = 0; i < I; i++) {
                System.out.println("ship " + (i + 1) + ": " + ships[i]);
            }
        }
    }

    /**
     * Laeuft
     *
     * @param sector
     */
    private void shoot(ID sector) {
        chordImpl.retrieve(sector);
    }

    public BigInteger getMyID() {
        return myID;
    }

}
