package dicemc.testapp;

import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;

public class GnCLibConsole {
	public static UUID id = UUID.randomUUID();
	
	public static void main(String []args) {
		System.out.println(-(ConfigCore.GUILD_NAME_CHANGE_COST));
		//javax.swing.SwingUtilities.invokeLater(new Runnable() {public void run() {ScreenRoot.openGUI();}});
	}
	
}
