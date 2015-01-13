package main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class GameNotify implements NotifyCallback {

    private ChordClient chordClient = null;
    private ChordImpl chordImpl = null;
    private int ship = 0;
	private final List<BroadcastLog> bl = new ArrayList<>();
	private final Map<ID, ID> uniquePlayers = new HashMap<>();
	private final List<ID> dumbPlayers = new ArrayList<>();

    public void setChordClient(ChordClient chordClient, ChordImpl chordImpl) {
        this.chordClient = chordClient;
        this.chordImpl = chordImpl;
    }

    @Override
    public void retrieved(ID target) {
        BigInteger sector = target.toBigInteger();
        BigInteger[] sectors = chordClient.sectors;
        for (int i = 0; i < sectors.length - 1; i++) {
            if (sector.compareTo(sectors[i]) >= 0 && sector.compareTo(sectors[i + 1]) < 0) {
                if (chordClient.ships[i]) {
                    ship++;
					System.out.println("Ship " + ship + " destroyed in sector " + (i + 1));
                    chordImpl.broadcast(target, Boolean.TRUE);
                } else {
                    chordImpl.broadcast(target, Boolean.FALSE);
                }
            }
        }

        if (sector.compareTo(sectors[sectors.length - 1]) >= 0 && sector.compareTo(chordClient.getMyID()) <= 0) {
            if (chordClient.ships[sectors.length - 1]) {
                ship++;
                System.out.println("Ship" + ship + "destroyed in sector 99");
                chordImpl.broadcast(target, Boolean.TRUE);
            } else {
                chordImpl.broadcast(target, Boolean.FALSE);
            }
        }
    }

    @Override
    public void broadcast(ID source, ID target, Boolean hit) {
        bl.add(new BroadcastLog(source, target, hit));

        // wenn wir den spieler schon kennen
        if (uniquePlayers.containsKey(source)) {
            // wenn target zwischen dem bekannten target und source sitzt
            if (target.isInInterval(uniquePlayers.get(source), source)) {
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
}
