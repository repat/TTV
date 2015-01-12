package main;

import java.math.BigInteger;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;

public class GameNotify implements NotifyCallback {

	ChordClient chordClient = null;

	public void setChordClient(ChordClient c) {
		this.chordClient = c;
	}

	@Override
	public void retrieved(ID target) {
		System.out.println("retrieved()[ID(target): " + target + "]");

		BigInteger targetBi = new BigInteger(target.getBytes());
		// if (chordClient.sectors ...)

	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		System.out.println("broadcast()[ID(source): " + source
				+ ", ID(target): " + target + ", Boolean(hit): " + hit + "]");
	}

}
