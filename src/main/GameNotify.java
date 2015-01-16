package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.util.Scanner;

public class GameNotify implements NotifyCallback {

    private ChordClient chordClient = null;
    private ChordImpl chordImpl = null;
	private int ship = 10;
	private final List<BroadcastLog> broadcastLog = new ArrayList<>();
	private final Map<ID, ID> uniquePlayers = new HashMap<>();
	private final List<ID> dumbPlayers = new ArrayList<>();
	private final Map<ID, Integer> hitForID = new HashMap<>();

    public void setChordClient(ChordClient chordClient, ChordImpl chordImpl) {
        this.chordClient = chordClient;
        this.chordImpl = chordImpl;
    }

    @Override
    public void retrieved(ID target) {
        handleHit(target);
		chordClient.shoot();
    }

    private void handleHit(ID target) {
		ID[] sectors = chordClient.mySectors;
        for (int i = 0; i < sectors.length - 1; i++) {
			if (target.compareTo(sectors[i]) >= 0 && target.compareTo(sectors[i + 1]) < 0) {
                if (chordClient.ships[i]) {
					System.out.println(chordClient.PLAYER_NAME + ": Ship " + ship + " destroyed in sector " + (i + 1));
                    ship--;
                    chordImpl.broadcast(target, Boolean.TRUE);
                    break;
                } else {
                    System.out.println(chordClient.PLAYER_NAME + ": no Ship" + " in sector " + (i + 1));
                    chordImpl.broadcast(target, Boolean.FALSE);
                    break;
                }
            }
        }

		if (target.compareTo(sectors[sectors.length - 1]) >= 0 && target.compareTo(chordClient.myID) <= 0) {
			if (chordClient.ships[sectors.length - 1]) {
				System.out.println(chordClient.PLAYER_NAME + ": Ship " + ship + " destroyed in sector 100");
				ship--;
				chordImpl.broadcast(target, Boolean.TRUE);
			} else {
				System.out.println(chordClient.PLAYER_NAME + ": no Ship" + " in sector 100");
				chordImpl.broadcast(target, Boolean.FALSE);
			}
		}
		
        if (ship < 1) {
            System.out.println(chordClient.PLAYER_NAME + ": I LOST!");
            Scanner scanner = new Scanner(System.in);
            scanner.next();
            scanner.close();
        }
        
        
    }

    @Override
    public void broadcast(ID source, ID target, Boolean hit) {
        int transactionID = chordImpl.getLastSeenTransactionID();

        chordImpl.setLastSeenTransactionID(transactionID);
		broadcastLog.add(new BroadcastLog(source, target, hit, transactionID));

        if (hit) {
            if (hitForID.containsKey(source)) {
                int tmp = hitForID.get(source);
                hitForID.put(source, tmp++);
            } else {
                hitForID.put(source, 1);
            }
        }

        // wenn wir den spieler schon kennen
        if (uniquePlayers.containsKey(source)) {
            // wenn target zwischen dem bekannten target und source sitzt
            if (!(target.isInInterval(uniquePlayers.get(source), source))) {
                // source hat schon mal auf's target geschossen -> bloede
                // strategie -> auf den schiessen wir als naechstes
                if (uniquePlayers.get(source).equals(target)) {
                    dumbPlayers.add(source);
                } else {
                    // update target
                    uniquePlayers.remove(source);
                    uniquePlayers.put(source, target);
                }
            }
        } else {
            // baue liste mit unique spielern
            uniquePlayers.put(source, target);
        }

	}

	public List<BroadcastLog> getBroadcastLog() {
		return broadcastLog;
	}

}
