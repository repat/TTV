package main;

import de.uniba.wiai.lspi.chord.data.ID;

/**
 * stores the broadcasts we received. It is just a helper class with constructor and getter/setter
 */
public class BroadcastLog {

    private ID source;
    private ID target;
    private boolean hit;
    private int transactionID;

    public BroadcastLog(ID source2, ID target2, Boolean hit2, int transactionID2) {
        this.source = source2;
        this.target = target2;
        this.hit = hit2;
        this.transactionID = transactionID2;
        //System.out.println(this);
    }

    public ID getTarget() {
        return target;
    }

    public void setTarget(ID target) {
        this.target = target;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public ID getSource() {
        return source;
    }

    public void setSource(ID source) {
        this.source = source;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    @Override
    public String toString() {
        return "BroadcastLog [source=" + source + ", target=" + target + ", hit=" + hit + ", transactionID="
                + transactionID + "]";
    }

}
