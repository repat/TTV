package main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.util.Random;
import java.util.Scanner;

public class GameNotify implements NotifyCallback {

	private ChordClient chordClient = null;
    private ChordImpl chordImpl = null;
	private int ship = 10;
	private final List<BroadcastLog> bl = new ArrayList<>();
	private final Map<ID, ID> uniquePlayers = new HashMap<>();
	private final List<ID> dumbPlayers = new ArrayList<>();

	public void setChordClient(ChordClient chordClient, ChordImpl chordImpl) {
        this.chordClient = chordClient;
        this.chordImpl = chordImpl;
	}

    @Override
    public void retrieved(ID target) {
		handleHit(target);
		calculateRanges();
		shootEmUp();
	}

	private void handleHit(ID target) {
		BigInteger sector = target.toBigInteger();
		BigInteger[] sectors = chordClient.mySectors;
		for (int i = 0; i < sectors.length - 1; i++) {
			if (sector.compareTo(sectors[i]) >= 0 && sector.compareTo(sectors[i + 1]) < 0) {
				if (chordClient.ships[i]) {
					System.out.println(chordClient.getPLAYER_NAME() + ": Ship " + ship + " destroyed in sector " + (i + 1));
					ship--;
					chordImpl.broadcast(target, Boolean.TRUE);
					break;
				} else {
					System.out.println(chordClient.getPLAYER_NAME() + ": no Ship" + " in sector " + (i + 1));
					chordImpl.broadcast(target, Boolean.FALSE);
					break;
				}
			}
		}

		if (sector.compareTo(sectors[sectors.length - 1]) >= 0 && sector.compareTo(chordClient.getMyID()) <= 0) {
			if (chordClient.ships[sectors.length - 1]) {
				System.out.println(chordClient.getPLAYER_NAME() + ": Ship " + ship + " destroyed in sector 100");
				ship--;
				chordImpl.broadcast(target, Boolean.TRUE);
			} else {
				System.out.println(chordClient.getPLAYER_NAME() + ": no Ship" + " in sector 100");
				chordImpl.broadcast(target, Boolean.FALSE);
			}
		}

		if (ship < 1) {
			System.out.println(chordClient.getPLAYER_NAME() + ": I LOST!");
			Scanner scanner = new Scanner(System.in);
			scanner.next();
		}
	}

	private void calculateRanges() {
		// to be implemented
	}

	/**
	 * shoots completely random right now
	 */
	private void shootEmUp() {
		System.out.println();
		Random rnd = new Random();
		long sectorNumber;
		BigInteger sectorSize = chordClient.getBIGGESTID().divide(BigInteger.valueOf(3 * 100));
		BigInteger targetSector;

		do {
			sectorNumber = rnd.nextInt(100 * 3); // number of sectors * players
			targetSector = BigInteger.valueOf(sectorNumber).multiply(sectorSize).mod(chordClient.getBIGGESTID());
		} while (chordClient.isMyID(targetSector) || !allreadyHit(targetSector));

		chordClient.shoot(targetSector);
	}

	private boolean allreadyHit(BigInteger sector) {

		for (BroadcastLog b : bl) {
			if (b.getTarget().equals(chordClient.getUnsignedId(sector.toByteArray()))) {
				System.out.println(chordClient.getPLAYER_NAME() + ": allready hit this target!");
				return true;
			}
		}
		return true;
	}

    @Override
	public void broadcast(ID source, ID target, Boolean hit) {
		System.out.println(chordClient.getPLAYER_NAME()
			+ ": [Broadcast]:"
			+ " source: " + source.toBigInteger()
			+ " target: " + target.toBigInteger()
			+ " hit: " + hit);

        bl.add(new BroadcastLog(source, target, hit));

        // wenn wir den spieler schon kennen
        if (uniquePlayers.containsKey(source)) {
            // wenn target zwischen dem bekannten target und source sitzt
            if (!target.isInInterval(uniquePlayers.get(source), source)) {
                // source hat schon mal auf's target geschossen -> bloede
                // strategie -> auf den schiessen wir als naechstes
                if (uniquePlayers.get(source).equals(target)) {
                    dumbPlayers.add(source);
                } else {
                    // update target
                    uniquePlayers.put(source, target);
                }
            }
        } else {
            // baue liste mit unique spielern
            uniquePlayers.put(source, target);
        }

    }
}
