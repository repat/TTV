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
    private GameLogic gameLogic = null;
    private ChordImpl chordImpl = null;
    private int shipsLeft = 10;
    final List<BroadcastLog> broadcastLog = new ArrayList<>();
    final List<ID> uniquePlayers = new ArrayList<>();
    final Map<ID, ID[]> uniquePlayersSectors = new HashMap<>();
    List<ID> shootableSectors = new ArrayList<>();
    private final List<ID> dumbPlayers = new ArrayList<>(); // müssen wir noch nutzen
    private final Map<ID, Integer> hitForID = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    public void setChordClient(GameLogic chordClient, ChordImpl chordImpl) {
        this.gameLogic = chordClient;
        this.chordImpl = chordImpl;
    }

    @Override
    public void retrieved(ID target) {
        handleHit(target);
        calculateUniquePlayersSectors();
        calculateShootableSectors();
        gameLogic.shoot();
    }

    private void handleHit(ID target) {
        ID[] sectors = gameLogic.mySectors;
        System.out.println("Ships left: " + shipsLeft);
        for (int i = 0; i < sectors.length - 1; i++) {
            if (target.compareTo(sectors[i]) >= 0 && target.compareTo(sectors[i + 1]) < 0) {
                if (gameLogic.ships[i]) {
                    System.out.println("Ship " + shipsLeft + " destroyed in sector " + (i + 1));
                    shipsLeft--;
                    gameLogic.ships[i] = false;
                    chordImpl.broadcast(target, Boolean.TRUE);
                    break;
                } else {
                    System.out.println("no Ship" + " in sector " + (i + 1));
                    chordImpl.broadcast(target, Boolean.FALSE);
                    break;
                }
            }
        }

        if (target.compareTo(sectors[sectors.length - 1]) >= 0 && target.compareTo(gameLogic.myID) <= 0) {
            if (gameLogic.ships[sectors.length - 1]) {
                System.out.println("Ship " + shipsLeft + " destroyed in sector 100");
                shipsLeft--;
                gameLogic.ships[sectors.length - 1] = false;
                chordImpl.broadcast(target, Boolean.TRUE);
            } else {
                System.out.println("no Ship" + " in sector 100");
                chordImpl.broadcast(target, Boolean.FALSE);
            }
        }

        if (shipsLeft < 1) {
            System.out.println("I LOST!");
        }
    }

    @Override
    public void broadcast(ID source, ID target, Boolean hit) {
//        System.out.println("Broadcast empfangen: src:" + source.toHexString() + ", target:" + target.toHexString()
//                + " hit:" + hit);
        int transactionID = chordImpl.getLastSeenTransactionID();

        // chordImpl.setLastSeenTransactionID(transactionID);
        broadcastLog.add(new BroadcastLog(source, target, hit, transactionID));

        if (hit) {
            if (hitForID.containsKey(source)) {
                int tmp = hitForID.get(source);
                hitForID.put(source, ++tmp);

                if (tmp == 10) {
                    System.out.println("Player " + target + " lost!\nlast seen transaction ID: " + chordImpl.getLastSeenTransactionID());
                }
            } else {
                hitForID.put(source, 1);
            }
        }

        if (!uniquePlayers.contains(source)) {
            uniquePlayers.add(source);
        }
    }

    void calculateUniquePlayersSectors() {
        if (uniquePlayers.size() == uniquePlayersSectors.size()) {
            return;
        }

        for (int i = 0; i < uniquePlayers.size() - 1; i++) {
            ID[] newSectors = gameLogic.calculateSectors(
                    uniquePlayers.get(i), uniquePlayers.get(i + 1));
            uniquePlayersSectors.put(uniquePlayers.get(i), newSectors);
        }

        // case for the last player
        ID[] newSectors = gameLogic.calculateSectors(
                uniquePlayers.get(uniquePlayers.size() - 1), uniquePlayers.get(0));
        uniquePlayersSectors.put(uniquePlayers.get(uniquePlayers.size() - 1), newSectors);
    }

    void calculateShootableSectors() {

        shootableSectors = new ArrayList<>();

        // fill List
        for (ID uniquePlayer : uniquePlayers) {
            for (int j = 0; j < gameLogic.I; j++) {
                shootableSectors.add(uniquePlayersSectors.get(uniquePlayer)[j]);
            }
        }

        // remove fields, that are destroyed
        for (BroadcastLog bl : broadcastLog.toArray(new BroadcastLog[0])) {
            for (ID uniquePlayer : uniquePlayers) {
                ID[] sectors = uniquePlayersSectors.get(uniquePlayer);
                int index = gameLogic.isInSector(bl.getTarget(), sectors);
                if (index != -1) {
                    shootableSectors.remove(sectors[index]);
                }
            }
        }

        // remove own fielde
        for (ID id : gameLogic.mySectors) {
            shootableSectors.remove(id);
        }

        System.out.println("Number of shootable sectors: " + shootableSectors.size());
    }

}
