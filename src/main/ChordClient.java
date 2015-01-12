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

	private static ChordImpl chordImpl;
	private static final String protocol = URL.KNOWN_PROTOCOLS
			.get(URL.SOCKET_PROTOCOL);
	private static final BigInteger biggestID = BigInteger.valueOf(2).pow(160)
			.subtract(BigInteger.ONE);
	private static final String ip = "localhost";

    private static final BigInteger[] sectors = new BigInteger[100];
    private static final boolean[] ships = new boolean[100];
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

//		System.out.println("maxID :   " + biggestID);
        for (int i = 0; i < 100; i++) {
            shoot(getIdUnsigned(sectors[i].toByteArray()));
        }
    }

    private static ID getIdUnsigned(byte[] id) {
        ID result;
        if (id[0] == 0) {
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
			localURL = new URL(protocol + "://"
					+ Inet4Address.getLocalHost().getHostAddress() + ":8181/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			Logger.getLogger(ChordClient.class.getName()).log(Level.SEVERE,
					null, e);
		}

		chordImpl = new ChordImpl();
		GameNotify myNotifyCallback = new GameNotify();
        myNotifyCallback.setChordClient(this, chordImpl);
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
	 * Calculates the sectores between the predecessor and our id and splits
	 * this into 100 Sectors. Also handles the case if the predecessor has a
	 * bigger id than we do.
	 */
	private void calculateSectors() {
		BigInteger distance;
		myID = chordImpl.getID().toBigInteger();
		predecessorID = chordImpl.getPredecessorID().toBigInteger();
		// System.out.println("myID:          " + myID);
		// System.out.println("predecessorID: " + predecessorID);

		// predecessorID might be bigger than our ID, due to Chord circle
		if (myID.compareTo(predecessorID) > 0) {
			distance = myID.subtract(predecessorID);
		} else {
			distance = biggestID.subtract(predecessorID).add(myID);
		}
//		 System.out.println("distance:      " + distance);

		BigInteger step = distance.divide(BigInteger.valueOf(100L));

		for (int i = 0; i < 100; i++) {
			// (predecessorID + (i * step) + 1) % biggestID
			sectors[i] = predecessorID
					.add(BigInteger.valueOf((long) i).multiply(step))
					.add(BigInteger.ONE).mod(biggestID);

//			 System.out.println("sector " + (i + 1) + ": " + sectors[i]);
		}
	}

	/**
	 * setzt zufällig 10 Schiffe auf einen der 100 Sektoren
	 */
	private void setShips() {

		Random rnd = new Random();
		int random;

		for (int i = 0; i < 10; i++) {
			do {
				random = rnd.nextInt(100);
			} while (ships[random] == true);

			ships[random] = true;
		}

		// for (int i = 0; i < 100; i++) {
		// System.out.println("ship " + (i + 1) + ": " + ships[i]);
		// }
	}

	/**
	 * Passt so noch überhaupt nicht
	 *
	 * @param sector
	 */
	private void shoot(ID sector) {
        chordImpl.retrieve(sector);
	}

	private void gotHit() {
		// StringKey key = new StringKey(sectors[sector].toString());
		//
		// if (ships[sector]) {
		// chordImpl.remove(key, "Schiff");
		// System.out.println("Schiff in Sektor " + (sector + 1) +
		// " getroffen!");
		// //wird noch nicht ausgewertet
		// chordImpl.broadcast(chordImpl.getID(), Boolean.TRUE);
		// } else {
		// System.out.println("Kein Schiff in Sektor " + (sector + 1) + "!");
		// chordImpl.broadcast(chordImpl.getID(), Boolean.FALSE);
		// }
	}

    public BigInteger[] getSectors() {
        return sectors;
    }

    public boolean[] getShips() {
        return ships;
    }

    public BigInteger getMyID() {
        return myID;
    }


}
