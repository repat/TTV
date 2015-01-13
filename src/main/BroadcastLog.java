package main;

import de.uniba.wiai.lspi.chord.data.ID;

public class BroadcastLog {

    private ID source;
    private ID target;
    private boolean hit;

    public BroadcastLog(ID source2, ID target2, Boolean hit2) {
        this.source = source2;
        this.target = target2;
        this.hit = hit2;
    }

    public ID getSrc() {
        return source;
    }

    public void setSrc(ID src) {
        this.source = src;
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

}
