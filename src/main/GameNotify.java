package main;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;

public class GameNotify implements NotifyCallback {

	@Override
	public void retrieved(ID target) {
		System.out.println("retrieved()[ID(target): " + target + "]");

	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		System.out.println("broadcast()[ID(source): " + source
				+ ", ID(target): " + target + ", Boolean(hit): " + hit + "]");
	}

}
