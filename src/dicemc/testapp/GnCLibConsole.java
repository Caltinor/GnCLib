package dicemc.testapp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dicemc.testapp.impl.ConfigSrc;
import dicemc.testapp.impl.RealEstateImpl;

public class GnCLibConsole {
	public static UUID id = UUID.randomUUID();
	public static UUID testPlayer = UUID.fromString("12d9481c-280f-4b70-b93c-c82934cd8c13");
	public static String testPlayerName = "Caltinor";
	
	public static void main(String []args) {
		Map<String, RealEstateImpl> wMgr = new HashMap<String, RealEstateImpl>();
		wMgr.put("Overworld", new RealEstateImpl());
		wMgr.put("Nether", new RealEstateImpl());
		wMgr.put("End", new RealEstateImpl());
		ConfigSrc.init();
		RunVars.init();
		Menu.main();
	}
	
}
