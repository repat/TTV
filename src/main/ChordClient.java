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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordClient {

	private static ChordImpl chordImpl;
	// private static final StringKey myKey = new StringKey("Just an example.");
	private static final String protocol = URL.KNOWN_PROTOCOLS
			.get(URL.SOCKET_PROTOCOL);
	private static final BigInteger biggestNodeKey = new BigDecimal(2.0)
			.pow(160).toBigInteger().subtract(BigInteger.ONE);
	private static final String ip = "localhost";

	public static void main(String[] args) {
		joinChord();

		setShips();

		shoot("1");
		shoot("2");

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

	// man müsste die Schiffe natürlich auf den zugewiesenen Bereich verteilen.
	private static void setShips() {
		chordImpl.insert(new StringKey("1"), "Schiff");
		chordImpl.insert(new StringKey("3"), "Schiff");
		chordImpl.insert(new StringKey("7"), "Schiff");
		chordImpl.insert(new StringKey("9"), "Schiff");
	}

	// Man schießt noch auf sich selber, aber geht das irgendwie in die richtige
	// Richtung?
	private static void shoot(String sektor) {
		StringKey key = new StringKey(sektor);

		if (chordImpl.retrieve(key).size() > 0) {
			chordImpl.remove(key, "Schiff");
			System.out.println("Schiff in Sektor " + sektor + " getroffen!");// Broadcast
			// wird noch nicht ausgewertet
			chordImpl.broadcast(chordImpl.getID(), Boolean.TRUE);
		} else {
			System.out.println("Kein Schiff in Sektor " + sektor + "!");
			chordImpl.broadcast(chordImpl.getID(), Boolean.FALSE);
		}
	}

}
