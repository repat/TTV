package main;

import de.uniba.wiai.lspi.chord.data.ID;
import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameLogic {
    // constants for config
    

    static final int NUMBER_OF_PLAYERS = 2;
    private static final int S = 10; // number of ships
    private static final int I = 100; // number of mySectors
    private static final boolean TESTING_MODE = true;
    private static final int WAITING_TIME_CHORD_JOIN = 5000;
    private static final int MIDDLE = 2;

    // instances of things
    //private ChordImpl chordImpl;
    private ID distance;
    private final Scanner scanner = new Scanner(System.in);
    //private GameNotify myNotifyCallback;
    private static String JOIN_OR_CREATE_FOR_THIS_CLIENT = "join";

    // package private
    ID myID;
    ID[] mySectors = new ID[I];
    final boolean[] ships = new boolean[I];
    static final ID BIGGEST_ID = new ID(BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE).toByteArray());
    private static Chord chord = null;
    
    GameLogic getChordClient() {
        return this;
    } 

    public static void main(String[] args) throws InterruptedException {
        
        GameLogic cc = new GameLogic();
        chord = new Chord(JOIN_OR_CREATE_FOR_THIS_CLIENT);
        new Thread(chord).start();
        // sleep prevents calling "loadPropertyFile()" at the same time.
        Thread.sleep(100);
        cc.startGame();

    }

    public void startGame() {
        

        if (TESTING_MODE) {
            // start the game after s was typed in
            String input;
            do {
                System.out.print("type \"s\" to begin: ");
                input = scanner.next();
            } while (!input.equals("s"));
            scanner.close();
        }

        // waits 10000 msec to make sure others have joined chord
        try {
            if (TESTING_MODE) {
                Thread.sleep(WAITING_TIME_CHORD_JOIN);
            } else {
                Thread.sleep(3000);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(GameLogic.class.getName()).log(Level.SEVERE, null, ex);
        }

        myID = chord.getChordImpl().getID();
        mySectors = calculateSectors(chord.getChordImpl().getPredecessorID(), myID);
        setShips();

        // we start the game if we have the BIGGESTID
        if (chord.getChordImpl().getPredecessorID().compareTo(chord.getChordImpl().getID()) > 0) {
            System.out.println("I start!");

            shoot();
        }
    }

    /**
     * Calculates the mySectors between the predecessor and our id and splits
     * this into I Sectors. Also handles the case if the predecessor has a
     * bigger id than we do.
     * 
     * @param from
     *            the first id, thinking circle clockwise
     * @param to
     *            the second id,
     * @return array of BigInteger
     */
    private ID[] calculateSectors(ID from, ID to) {
        ID[] result = new ID[I];

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

    void shoot() {

        System.out.println();
        Random rnd = new Random();
        long sectorNumber;
        ID sectorSize = BIGGEST_ID.divide(I * NUMBER_OF_PLAYERS);
        ID target;

        do {
            // number of sectors * players
            sectorNumber = rnd.nextInt(I * NUMBER_OF_PLAYERS);
            target = sectorSize.multiply(sectorNumber).add((sectorSize.divide(MIDDLE))).mod(BIGGEST_ID);
            // System.out.println(".");
        } while (isInMyInterval(target) || isAlreadyHit(target));

        System.out.println("shooting at: " + target.toBigInteger());
        chord.getChordImpl().retrieve(target);
    }

    boolean isInMyInterval(ID id) {
        boolean result = id.isInInterval(mySectors[0], myID);
        // if (result) {
        // System.out.println(PLAYER_NAME + ": " + id + " is my own ID");
        // }
        return result;
    }

    private boolean isAlreadyHit(ID id) {

        for (BroadcastLog b : chord.getMyNotifyCallback().getBroadcastLog()) {
            if (b.getTarget().equals(id)) {
                System.out
                        .println("looking for a target but already shot at this particular target, no I'm not shooting there again!");
                return true;
            }
        }
        return false;
    }

}
