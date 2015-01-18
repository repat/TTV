package main;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameLogic {
    // constants for config
    private static final int S = 10; // number of ships
    static final int I = 100; // number of mySectors
    private static final int WAITING_TIME_CHORD_JOIN = 5000;
    private static final int MIDDLE = 2;
    private static final ID BIGGEST_ID = new ID(BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE).toByteArray());

    // package private
    ID myID;
    ID[] mySectors = new ID[I];
    final boolean[] ships = new boolean[I];
    private static Chord chord = null;

    GameLogic getChordClient() {
        return this;
    }

    /**
     * initialises the game
     *
     * @param args
     * @throws InterruptedException might be called by Thread.sleep
     */
    public static void main(String[] args) throws InterruptedException {
        GameLogic cc = new GameLogic();
        chord = new Chord(cc);
        chord.init();
        // sleep prevents calling "loadPropertyFile()" at the same time.
        Thread.sleep(100);
        cc.startGame();
    }

    /**
     * starts up the actual game
     */
    private void startGame() {
        Scanner scanner = new Scanner(System.in);
            // start the game after s was typed in
            String input;
            do {
                System.out.print("type \"s\" to begin: ");
                input = scanner.next();
            } while (!input.equals("s"));
            scanner.close();

        // waits 5 sec to make sure others have joined chord
        try {
                Thread.sleep(WAITING_TIME_CHORD_JOIN);
        } catch (InterruptedException ex) {
            Logger.getLogger(GameLogic.class.getName()).log(Level.SEVERE, null, ex);
        }

        myID = chord.getChordImpl().getID();
        mySectors = calculateSectors(chord.getChordImpl().getPredecessorID(), myID);
        setShips();

        // adds us to the unicePlayer sectors
        chord.getMyNotifyCallback().uniquePlayers.add(myID);

        // add the fingertables to
        Set<Node> fingerSet = new HashSet<>(chord.getChordImpl().getFingerTable());
        for (Node n : fingerSet) {
            chord.getMyNotifyCallback().uniquePlayers.add(n.getNodeID());
        }
        Collections.sort(chord.getMyNotifyCallback().uniquePlayers);

        // we start the game if we have the BIGGESTID
        if (chord.getChordImpl().getPredecessorID().compareTo(chord.getChordImpl().getID()) > 0) {
            System.out.println("I start!");

            chord.getMyNotifyCallback().calculateUniquePlayersSectors();
            chord.getMyNotifyCallback().calculateShootableSectors();
            shoot();
        }
    }

    /**
     * Calculates the mySectors between the predecessor and our id and splits
     * this into I Sectors. Also handles the case if the predecessor has a
     * bigger id than we do.
     *
     * @param from the first id, thinking circle clockwise
     * @param to the second id,
     * @return array of BigInteger
     */
    ID[] calculateSectors(ID from, ID to) {
        ID[] result = new ID[I];
        ID distance;

        // predecessorID might be bigger than our ID, due to Chord circle
        if (from.compareTo(to) < 0) {
            distance = to.subtract(from);
        } else {
            distance = BIGGEST_ID.subtract(from).add(to);
        }

        ID step = distance.divide(I);

        for (int i = 0; i < I; i++) {
            // (from + 1 + (i * step)) % biggestID
            result[i] = from.add(1).add(step.multiply(i)).mod(BIGGEST_ID);

            // System.out.println("sector " + (i + 1) + ": " + result[i]);
        }
        return result;
    }

    /**
     * Places S ships in I mySectors
     */
    private void setShips() {
        Random rnd = new Random();
        int random;

        for (int i = 0; i < S; i++) {
            do {
                random = rnd.nextInt(I);
            } while (ships[random] == true);

            ships[random] = true;
        }
    }

    /**
     * handles the shooting of our enemies
     */
    void shoot() {
        System.out.println();
        Random rnd = new Random();

        List<ID> shootableSectors = chord.getMyNotifyCallback().shootableSectors;

        int sectorNumber = rnd.nextInt(shootableSectors.size());
        ID target = shootableSectors.get(sectorNumber);

        // doesn't work
//        ID targetNext = calculateNextSector(target);
//        ID distance;
//
//        if (target.compareTo(targetNext) < 0) {
//            distance = targetNext.subtract(target).divide(MIDDLE);
//        } else {
//            distance = BIGGEST_ID.subtract(target).add(targetNext).divide(MIDDLE);
//        }
//        ID middleOfSector = target.add(distance).mod(BIGGEST_ID);
        ID middleOfSector = target.add(10).mod(BIGGEST_ID);

        System.out.println("shooting at: " + target.toBigInteger());
        RetrieveThread retrieve = new RetrieveThread(chord.getChordImpl(), middleOfSector);
        retrieve.start();
    }

    /**
     * calculates the next sector for an ID. This is needed to be able to shoot in the middle of a
     * sector.
     *
     * @param sector
     * @return
     */
    ID calculateNextSector(ID sector) {
        List<ID> uniquePlayers = chord.getMyNotifyCallback().uniquePlayers;

        for (int i = 0; i < uniquePlayers.size(); i++) {
            ID[] uniquePlayersSectors = chord.getMyNotifyCallback().uniquePlayersSectors.get(uniquePlayers.get(i));
            int sectorNumber = isInSector(sector, uniquePlayersSectors);

            System.out.println("Sector nr: " + sectorNumber);

            if (sectorNumber != -1) {
                if (sectorNumber < uniquePlayersSectors.length - 1) {
                    // next sector of the same player
                    return uniquePlayersSectors[i + 1];
                } else if (i < uniquePlayers.size() - 1) {
                    // first sector of the next player
                    return uniquePlayers.get(i + 1);
                } else {
                    // return the first sector of the first player
                    return uniquePlayers.get(0);
                }
            }
        }
        // should not happen
        System.out.println("ERROR: next Sector is null");
        return null;
    }

    /**
     * Checks if the target is inside the Sector boundaries.
     *
     * @param target target ID
     * @param sector Array of IDs
     * @return
     */
    int isInSector(ID target, ID[] sector) {
        if (!target.isInInterval(sector[0], sector[sector.length - 1])) {
            return -1;
        }

        for (int i = 0; i < sector.length - 1; i++) {
            if (target.compareTo(sector[i]) >= 0 && target.compareTo(sector[i + 1]) < 0) {
                return i;
            }
        }

        if (target.compareTo(sector[sector.length - 1]) >= 0 && target.compareTo(findNextPlayer(target)) < 0) {
            return sector.length - 1;
        }

        return -1;
    }

    /**
     * returns the ID of the next player
     *
     * @param target
     * @return
     */
    public ID findNextPlayer(ID target) {
        List<ID> uniquePlayers = chord.getMyNotifyCallback().uniquePlayers;
        Collections.sort(uniquePlayers);

        for (int i = 0; i < uniquePlayers.size() - 1; i++) {
            if (target.isInInterval(uniquePlayers.get(i), uniquePlayers.get(i + 1))) {
                return uniquePlayers.get(i + 1);
            }
        }
        return uniquePlayers.get(0);
    }

}
