package main;

import java.math.BigInteger;

public class ShipsAndSectors {

	private static ShipsAndSectors singleton = null;
	private static BigInteger[] sectors = new BigInteger[100];
	private static boolean[] ships = new boolean[100];

	private ShipsAndSectors() {

	}

	public static ShipsAndSectors getInstance() {
		if (singleton == null)
			singleton = new ShipsAndSectors();
		return singleton;
	}

	public BigInteger[] getSectors() {
		return sectors;
	}

	public boolean[] getShips() {
		return ships;
	}
}
