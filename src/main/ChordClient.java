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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordClient implements Runnable {
    // constants
    private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    private static final String PORT = "8080";
	private static final ID BIGGESTID = new ID(BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE).toByteArray());
	private static final String IP = "localhost";
	private static final int S = 10; // number of ships
	private static final int I = 100; // number of mySectors
	private static final boolean DEBUG = false;
	private static final boolean TESTING_MODE = true;

	private final String PORT_LOCAL;
	private ChordImpl chordImpl;
	ID[] mySectors = new ID[I];
	final boolean[] ships = new boolean[I];
	private ID myID;
	private ID distance;
	private final String PLAYER_NAME; // just for local testing purposos
	private final Scanner scanner = new Scanner(System.in);

	public ChordClient(String PORT_LOCAL, String playerName) {
		this.PORT_LOCAL = PORT_LOCAL;
		this.PLAYER_NAME = playerName;
	}

	public static void main(String[] args) throws InterruptedException {

		// spawns x numbers of clients
		for (int i = 0; i < 5; i++) {
			String port = String.valueOf(8181 + i);
			new Thread(new ChordClient(port, "Player " + (i + 1))).start();
			// sleep prevents calling "loadPropertyFile()" at the same time.
			Thread.sleep(100);
		}
	}

	@Override
	public void run() {
        joinChord();

		if (!TESTING_MODE) {
			// start the game after s was typed in
			String input;
			do {
				System.out.print("type \"s\" to begin: ");
				input = scanner.next();
			} while (!input.equals("s"));
		}

		// waits 10000 sec to make sure others have joined chord
		try {
			if (!TESTING_MODE) {
				Thread.sleep(10000);
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
//		if (getUnsignedId(BIGGESTID.toByteArray())
//			.isInInterval(getUnsignedId(mySectors[0].toByteArray()), getUnsignedId(myID.toByteArray()))) {
			System.out.println(PLAYER_NAME + ": I start!");

			// If we start we shoot in front of us to find out our postPrecessors area.
			// @Rene: ne bessere Idee?
			shoot(distance
				.add(myID.multiply((long) 1.5))
				.mod(BIGGESTID));
		}
	}

//	ID getUnsignedId(byte[] id) {
//		ID result;
//		if (id.length == 21 && id[0] == 0) {
//			byte[] tmp = new byte[id.length - 1];
//            System.arraycopy(id, 1, tmp, 0, tmp.length);
//            result = new ID(tmp);
//        } else {
//            result = new ID(id);
//        }
//        return result;
//	}

	boolean isMyID(ID id) {
		boolean result = id.isInInterval(myID, mySectors[0]);
//		if (result) {
//			System.out.println(PLAYER_NAME + ": " + id + " is my own ID");
//		}

		return result;
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
			localURL = new URL(PROTOCOL + "://" + Inet4Address.getLocalHost().getHostAddress() + ":" + PORT_LOCAL + "/");
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

		System.out.println(PLAYER_NAME + ": Chord running on: " + localURL);
//		System.out.println("Chord joined: " + bootstrapURL + "\n");

        if (DEBUG) {
            System.out.println("MaxID:         " + BIGGESTID);
            System.out.println("NodeId:        " + chordImpl.getID());
            System.out.println("PredecessorID: " + chordImpl.getPredecessorID());
        }
    }

	/**
	 * Calculates the mySectors between the predecessor and our id and splits this into I Sectors. Also handles the case if the predecessor has a bigger id than we do.
	 *
	 * @param from the first id, thinking circle clockwise
	 * @param to the second id,
	 * @return array of BigInteger
	 */
	private ID[] calculateSectors(ID from, ID to) {
		ID[] result = new ID[I];

		// predecessorID might be bigger than our ID, due to Chord circlel
		if (from.compareTo(to) < 0) {
			distance = to.subtract(from);
        } else {
			distance = BIGGESTID.subtract(from).add(to);
        }

		ID step = distance.divide(I);

        for (int i = 0; i < I; i++) {
			// (from + 1 + (i * step)) % biggestID
			result[i] = from
				.add(1)
				.add(step.multiply(i))
				.mod(BIGGESTID);

//			System.out.println("sector " + (i + 1) + ": " + result[i]);
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

        if (DEBUG) {
            for (int i = 0; i < I; i++) {
                System.out.println("ship " + (i + 1) + ": " + ships[i]);
            }
        }
    }

    /**
     * shoots into a sector
     * @param sector to shoot into
	 */
	void shoot(ID sector) {
		System.out.println(PLAYER_NAME + ": shooting at: " + sector);
		chordImpl.retrieve(sector);
    }

	public ID getMyID() {
        return myID;
	}

	public String getPLAYER_NAME() {
		return PLAYER_NAME;
	}

	public ID getBIGGESTID() {
		return BIGGESTID;
	}


}
