package dicemc.testapp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.util.ComVars;

public class RunVars {
	public static Map<UUID, String> playerMap = new HashMap<UUID, String>();
	
	public static void init() {
		playerSetup();
		worldGenerate();
	}
	
	private static void playerSetup() {
		playerMap.put(GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName);
		playerMap.put(UUID.fromString("3b2e6a34-043f-4059-87b3-e243af392d69"), "Madood");
		for (Map.Entry<UUID, String> entries : playerMap.entrySet()) {
			System.out.println("Balance "+entries.getValue()+ "= $"+LogicMoney.getBalance(entries.getKey(), LogicMoney.AccountType.PLAYER.rl));
		}
	}
	
	private static void worldGenerate() {
		
	}
	
	public static UUID getPlayerByName(String name) {
		for (Map.Entry<UUID, String> entry : playerMap.entrySet()) {
			if (entry.getValue().equals(name)) {
				return entry.getKey();
			}
		}
		return ComVars.NIL;
	}
}
