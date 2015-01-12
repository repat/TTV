package main;

import java.math.BigInteger;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class GameNotify implements NotifyCallback {

    private ChordClient chordClient = null;
    private ChordImpl chordImpl = null;
    private int ship = 0;

    public void setChordClient(ChordClient chordClient, ChordImpl chordImpl) {
        this.chordClient = chordClient;
        this.chordImpl = chordImpl;
	}

	@Override
	public void retrieved(ID target) {
        BigInteger sector = target.toBigInteger();
        BigInteger[] sectors = chordClient.getSectors();

        for (int i = 0; i < 99; i++) {
            if (sector.compareTo(sectors[i]) >= 0 && sector.compareTo(sectors[i + 1]) < 0) {
                if (chordClient.getShips()[i]) {
                    ship++;
                    System.out.println("Ship " + ship + " destroyed in sector " + i);
                    chordImpl.broadcast(target, Boolean.TRUE);
                } else {
                    chordImpl.broadcast(target, Boolean.FALSE);
                }
            }
        }

        if (sector.compareTo(sectors[99]) >= 0 && sector.compareTo(chordClient.getMyID()) <= 0) {
            if (chordClient.getShips()[99]) {
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
		System.out.println("broadcast()[ID(source): " + source
				+ ", ID(target): " + target + ", Boolean(hit): " + hit + "]");
	}
}
