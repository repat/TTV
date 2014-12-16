/*
 * Added by INET
 */
package de.uniba.wiai.lspi.chord.com;

import java.io.Serializable;

import de.uniba.wiai.lspi.chord.data.ID;

public final class Broadcast implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 480670215611101785L;
	private ID range;
	private ID source;
	private ID target;
	private Integer transaction;
	private Boolean hit;
	
	public Broadcast (ID rng, ID src, ID trg, Integer trn, Boolean hit) {
		this.range = rng;
		this.source = src;
		this.target = trg;
		this.transaction = trn;
		this.hit = hit;
	}
	
	public ID getRange () {
		return this.range;
	}
	
	public ID getSource () {
		return this.source;
	}
	
	public ID getTarget () {
		return this.target;
	}
	
	public Integer getTransaction () {
		return this.transaction;
	}
	
	public Boolean getHit () {
		return this.hit;
	}
	
	public String toString() {
		return "( Broadcast: range = " + this.range.toString() 
				+ ", source = " + this.source.toString()
				+ ", target = " + this.target.toString()
				+ ", transaction = " + this.transaction.toString()
				+ ", hit = " + this.hit.toString()
				+ ")";
	}

	public int hashCode() {
		int result = 17;
		result += 37 * this.range.hashCode();
		result += 37 * this.source.hashCode();
		result += 37 * this.target.hashCode();
		result += 37 * this.transaction.hashCode();
		result += 37 * this.hit.hashCode();
		return result;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Entry)) {
			return false;
		}
		Broadcast bc = (Broadcast) o;
		return (bc.range.equals(this.range) &&
				bc.source.equals(this.source) &&
				bc.target.equals(this.target) &&
				bc.transaction.equals(this.transaction) &&
				(bc.hit == this.hit));
	}
}
