package dicemc.testapp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.testapp.impl.RealEstateImpl;

public class GnCLibConsole {
	public static UUID id = UUID.randomUUID();
	
	public static void main(String []args) {
		Map<String, RealEstateImpl> wMgr = new HashMap<String, RealEstateImpl>();
		wMgr.put("Overworld", new RealEstateImpl());
		wMgr.put("Nether", new RealEstateImpl());
		wMgr.put("End", new RealEstateImpl());
		System.out.println(-(ConfigCore.GUILD_NAME_CHANGE_COST));
		//javax.swing.SwingUtilities.invokeLater(new Runnable() {public void run() {ScreenRoot.openGUI();}});
	}
	
}
