package main;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordHost {

    private static final String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    private static final String PORT = "8080";   

    public static void main(String[] args) {
        PropertiesLoader.loadPropertyFile();
        URL localURL = null;
        try {
            localURL = new URL(protocol + "://" + Inet4Address.getLocalHost().getHostAddress() + ":" + PORT + "/");
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
    }

    public static String getPort() {
        return PORT;
    }
    
    
}
