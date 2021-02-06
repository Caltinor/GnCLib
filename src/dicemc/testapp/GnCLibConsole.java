package dicemc.testapp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dicemc.testapp.impl.ConfigSrc;
import dicemc.testapp.impl.RealEstateImpl;

public class GnCLibConsole {
	public static UUID id = UUID.randomUUID();
	public static String testPlayerName = "Madood";
	public static UUID testPlayer;	
	
	public static void main(String []args) {
		Map<String, RealEstateImpl> wMgr = new HashMap<String, RealEstateImpl>();
		wMgr.put("Overworld", new RealEstateImpl());
		wMgr.put("Nether", new RealEstateImpl());
		wMgr.put("End", new RealEstateImpl());
		ConfigSrc.init();
		RunVars.init();
		testPlayer = RunVars.getPlayerByName(testPlayerName);
		System.out.println("Test Player Set as: " + testPlayerName);
		Menu.main();
	}
	
}
