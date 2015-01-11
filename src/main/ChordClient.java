package main;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordClient {

	private static ChordImpl chordImpl;
	private static final String protocol = URL.KNOWN_PROTOCOLS
			.get(URL.SOCKET_PROTOCOL);
	private static final BigInteger biggestNodeKey = new BigDecimal(2.0)
			.pow(160).toBigInteger().subtract(BigInteger.ONE);
    private static final String ip = "localhost";

    private static final BigInteger[] sectors = new BigInteger[100];
    private static final boolean[] ships = new boolean[100];

	public static void main(String[] args) {
        joinChord();
        calculateSectors();
        setShips();


        shoot(0);
        shoot(1);
        shoot(2);
        shoot(3);
        shoot(4);
        shoot(5);
        shoot(6);
        shoot(7);
        shoot(8);
        shoot(9);
        shoot(10);
    }

	private static void joinChord() throws RuntimeException {
		PropertiesLoader.loadPropertyFile();
		URL localURL = null;
		try {
			localURL = new URL(protocol + "://"
					+ Inet4Address.getLocalHost().getHostAddress() + ":8181/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			Logger.getLogger(ChordClient.class.getName()).log(Level.SEVERE,
					null, e);
		}

		chordImpl = new ChordImpl();
		NotifyCallback myNotifyCallback = new GameNotify();
		chordImpl.setCallback(myNotifyCallback);

		URL bootstrapURL = null;
		try {
			bootstrapURL = new URL(protocol + "://" + ip + ":8080/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		try {
			chordImpl.join(localURL, bootstrapURL);
		} catch (ServiceException e) {
			throw new RuntimeException("Could not join DHT!", e);
		}

		System.out.println("Chord joined: " + bootstrapURL + "\n");

		// System.out.println("MaxID:         " + biggestNodeKey);
		// System.out.println("NodeId:        " + chordImpl.getID());
		// System.out.println("PredecessorID: " + chordImpl.getPredecessorID());

		// Vielleicht hilfe das irgendwie
		// Wikipedia:
		// "Every key is assigned to (stored at) its successor node, so looking
		// up a key k is to query successor(k)."
		System.out.println(chordImpl.printSuccessorList());
	}

    /**
     * Habe mit Mitja geredet, er sagte mir, dass wir den Bereich von unsere ID zu der des
     * vorherigen nutzen sollen. Wir müssen wohl diesen Bereich in 100 Teile teilen und 10 davon mit
     * Schiffen besetzen.
     */
    private static void calculateSectors() {

        BigInteger myID = chordImpl.getID().toBigInteger();
        BigInteger predecessorID = chordImpl.getPredecessorID().toBigInteger();
//        System.out.println("myID:          " + myID);
//        System.out.println("predecessorID: " + predecessorID);

        BigInteger distance = myID.subtract(predecessorID);
        BigInteger step = distance.divide(BigInteger.valueOf(100L));

        for (int i = 0; i < 100; i++) {
            sectors[i] = BigInteger.valueOf((long) i).multiply(step);
//            System.out.println("sector " + (i + 1) + ": " + sectors[i]);
        }

    }

    /**
     * setzt zufällig 10 Schiffe auf einen der 100 Sektoren
     */
    private static void setShips() {

        Random rnd = new Random();
        int random;

        for (int i = 0; i < 10; i++) {

            do {
                random = rnd.nextInt(100);
            } while (ships[random] == true);

            ships[random] = true;
            chordImpl.insert(new StringKey(sectors[random].toString()), "ship");
        }

        for (int i = 0; i < 100; i++) {
            System.out.println("ship " + (i + 1) + ": " + ships[i]);
        }

//		chordImpl.insert(new StringKey("1"), "Schiff");
//		chordImpl.insert(new StringKey("3"), "Schiff");
//		chordImpl.insert(new StringKey("7"), "Schiff");
//		chordImpl.insert(new StringKey("9"), "Schiff");
    }

    /**
     * Passt so noch überhaupt nicht
     *
     * @param sector
     */
    private static void shoot(int sector) {
        StringKey key = new StringKey(sectors[sector].toString());

        if (ships[sector]) {
			chordImpl.remove(key, "Schiff");
            System.out.println("Schiff in Sektor " + (sector + 1) + " getroffen!");
			//wird noch nicht ausgewertet
			chordImpl.broadcast(chordImpl.getID(), Boolean.TRUE);
		} else {
            System.out.println("Kein Schiff in Sektor " + (sector + 1) + "!");
			chordImpl.broadcast(chordImpl.getID(), Boolean.FALSE);
		}
	}

}
