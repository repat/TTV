/*
 * Added by INET
 */
package de.uniba.wiai.lspi.chord.service;

import de.uniba.wiai.lspi.chord.data.ID;

public interface NotifyCallback {
	
	public void retrieved(ID target);
	
	public void broadcast(ID source, ID target, Boolean hit);

}
