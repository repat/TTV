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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordHost {

	private static final String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    private static final BigInteger biggestNodeKey = new BigDecimal(2.0).pow(160).toBigInteger().subtract(BigInteger.ONE);
    private static int numberOfShips = 0;

	public static void main(String[] args) {
		PropertiesLoader.loadPropertyFile();
		URL localURL = null;
		try {
			localURL = new URL(protocol + "://" + Inet4Address.getLocalHost().getHostAddress() + ":8080/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
            Logger.getLogger(ChordHost.class.getName()).log(Level.SEVERE, null, e);
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

//		System.out.println("MaxID:  " + biggestNodeKey);
//        System.out.println("NodeId: " + chordImpl.getID().toBigInteger());
//        System.out.println("PredecessorID: " + chordImpl.getPredecessorID());

//		String data = "Just an example.";
//		StringKey myKey = new StringKey(data);
//
//		while (true) {
//			try {
//                Thread.sleep(1000);
                // just prints result, when the number of entries has changed (stops spaming console)
//                if (chordImpl.retrieve(myKey).size() != numberOfShips) {
//                    System.out.println(chordImpl.retrieve(myKey));
//                    numberOfShips = chordImpl.retrieve(myKey).size();
//                }
//			} catch (Exception e) {
//				System.out.println("stacktrace:");
//				System.out.println(Arrays.toString(e.getStackTrace()));
//			}
//		}
	}
}
