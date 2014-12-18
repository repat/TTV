package main;

import java.net.MalformedURLException;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Game {

	private static ChordImpl chordImpl;
	private static NotifyCallback myNotifyCallback;
	private static final String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);

	public static void main(String[] args) {
		create();
//		join("192.168.1.1");
	}

	/**
	 * initialises the create() or init() method and should not be called manually
	 *
	 * @param port the port of the local chord service
	 * @return	the URL the local chord runs on
	 * @throws RuntimeException on connection error
	 */
	private static URL init(int port) throws RuntimeException {
		PropertiesLoader.loadPropertyFile();
		URL localURL = null;
		try {
			localURL = new URL(protocol + "://localhost:" + port + "/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		chordImpl = new ChordImpl();
		myNotifyCallback = new GameNotify();
		chordImpl.setCallback(myNotifyCallback);

		return localURL;
	}

	/**
	 * creates an open chord service a user can connect to
	 */
	private static void create() {
		URL localURL = init(8080);

		try {
			chordImpl.create(localURL);
		} catch (ServiceException e) {
			throw new RuntimeException("Could not create DHT!", e);
		}

		String data = "Just an example.";
		StringKey myKey = new StringKey(data);

		while (true) {
			try {
				System.out.println(chordImpl.retrieve(myKey));
			} catch (Exception e) {
				System.out.println("stacktrace:");
				System.out.println(e.getStackTrace().toString());
			}
		}
	}

	/**
	 * joins an existing chord instance
	 *
	 * @param ip the target IP you want to connect to in dot notation
	 * @throws RuntimeException of connection error
	 */
	private static void join(String ip) throws RuntimeException {
		URL localURL = init(8181);

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

		String data = "Just an example .";
		StringKey myKey = new StringKey(data);
		chordImpl.insert(myKey, data);
	}
}
