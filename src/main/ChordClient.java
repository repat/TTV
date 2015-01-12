package main;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordClient {

	private static ChordImpl chordImpl;
	private static final String protocol = URL.KNOWN_PROTOCOLS
			.get(URL.SOCKET_PROTOCOL);
	private static final BigInteger biggestID = BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE);
    private static final String ip = "localhost";

	private static final BigInteger[] sectors = new BigInteger[100];
    private static final boolean[] ships = new boolean[100];

	public static void main(String[] args) {
        joinChord();
        calculateSectors();
		setShips();

		System.out.println("maxID :   " + biggestID);


//		System.out.println("biggestID length:  " + biggestID.bitCount());
//
//		byte[] test = new byte[biggestID.toByteArray().length];
//		System.arraycopy(biggestID.toByteArray(), 0, test, 0, biggestID.toByteArray().length);
//		System.out.println("biggestID length2: " + test.length);
//
//		ID test = new ID(biggestID.toByteArray());
//
//		System.out.println("value: " + test);
//		System.out.println("value: " + biggestID.toByteArray().length);
//		System.out.println("length: " + test.getLength());

//		shoot(test);

//		shoot(new ID(sectors[0].toByteArray()));
//		shoot(new ID(sectors[1].toByteArray()));
//		shoot(new ID(sectors[2].toByteArray()));
//		shoot(new ID(sectors[3].toByteArray()));
//		shoot(new ID(sectors[4].toByteArray()));
//		shoot(new ID(sectors[5].toByteArray()));
//		shoot(new ID(sectors[6].toByteArray()));
//		shoot(new ID(sectors[7].toByteArray()));
//		shoot(new ID(sectors[8].toByteArray()));
//		shoot(new ID(sectors[9].toByteArray()));
//		shoot(new ID(sectors[10].toByteArray()));
    }

	public static int byteArrayToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i] & 0x000000FF) << shift;
		}
		return value;
	}

	/**
	 * joins the Chord Network
	 *
	 * @throws RuntimeException
	 */
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
     * Calculates the sectores between the predecessor and our id and splits this into 100 Sectors. Also handles the case if the predecessor has a bigger id than we do.
	 */
    private static void calculateSectors() {
		BigInteger distance;
        BigInteger myID = chordImpl.getID().toBigInteger();
        BigInteger predecessorID = chordImpl.getPredecessorID().toBigInteger();
//		System.out.println("myID:          " + myID);
//		System.out.println("predecessorID: " + predecessorID);

		// predecessorID might be bigger than our ID, due to Chord circle
		if (myID.compareTo(predecessorID) > 0) {
			distance = myID.subtract(predecessorID);
		} else {
			distance = biggestID.subtract(predecessorID).add(myID);
		}
//		System.out.println("distance:      " + distance);

		BigInteger step = distance.divide(BigInteger.valueOf(100L));

		for (int i = 0; i < 100; i++) {
			// (predecessorID + (i * step) + 1) % biggestID
			sectors[i] = predecessorID.add(BigInteger.valueOf((long) i).multiply(step)).add(BigInteger.ONE).mod(biggestID);

//			System.out.println("sector " + (i + 1) + ": " + sectors[i]);
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
        }

//        for (int i = 0; i < 100; i++) {
//            System.out.println("ship " + (i + 1) + ": " + ships[i]);
//        }
    }

    /**
     * Passt so noch überhaupt nicht
     *
     * @param sector
     */
	private static void shoot(ID sector) {
		Set<Serializable> result = chordImpl.retrieve(sector);

		result.stream().forEach((s) -> {
			System.out.println(s);
		});
	}

	private static void gotHit() {
//        StringKey key = new StringKey(sectors[sector].toString());
//
//        if (ships[sector]) {
//			chordImpl.remove(key, "Schiff");
//            System.out.println("Schiff in Sektor " + (sector + 1) + " getroffen!");
//			//wird noch nicht ausgewertet
//			chordImpl.broadcast(chordImpl.getID(), Boolean.TRUE);
//		} else {
//            System.out.println("Kein Schiff in Sektor " + (sector + 1) + "!");
//			chordImpl.broadcast(chordImpl.getID(), Boolean.FALSE);
//		}
	}

}
