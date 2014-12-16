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

	public static void main(String[] args) {

		PropertiesLoader.loadPropertyFile();
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
		URL localURL = null;
		try {
			localURL = new URL(protocol + "://192.168.1.1:8080/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		chordImpl = new ChordImpl();
		myNotifyCallback = new GameNotify();
		chordImpl.setCallback(myNotifyCallback);

		try {
			chordImpl.create(localURL);
		} catch (ServiceException e) {
			throw new RuntimeException(" Could not create DHT ! ", e);
		}

		// chordImpl.broadcast(iD, true);

		String data = " Just an example . ";
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
}