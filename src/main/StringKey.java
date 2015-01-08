package main;

import de.uniba.wiai.lspi.chord.service.Key;

public class StringKey implements Key {

	private final String theString;

	public StringKey(String theString) {
		this.theString = theString;
	}

	@Override
	public byte[] getBytes() {
		return this.theString.getBytes();
	}

	@Override
	public int hashCode() {
		return this.theString.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof StringKey) {
			return ((StringKey) o).theString.equals(this.theString);
		}
		return false;
	}
}
