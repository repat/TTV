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

public class JoinChord {

	private static final String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
	private static final BigInteger biggestNodeKey = new BigDecimal(2.0).pow(160).toBigInteger().subtract(BigInteger.ONE);
	private static final String ip = "localhost";

	public static void main(String[] args) {
		PropertiesLoader.loadPropertyFile();
		URL localURL = null;
		try {
			localURL = new URL(protocol + "://" + Inet4Address.getLocalHost().getHostAddress() + ":8181/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			Logger.getLogger(JoinChord.class.getName()).log(Level.SEVERE, null, e);
		}

		ChordImpl chordImpl = new ChordImpl();
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

		System.out.println("MaxID:         " + biggestNodeKey);
		System.out.println("NodeId:        " + chordImpl.getID());
		System.out.println("PredecessorID: " + chordImpl.getPredecessorID());

		String data = "Just an example.";
		StringKey myKey = new StringKey(data);

		chordImpl.insert(myKey, data);
	}
}
