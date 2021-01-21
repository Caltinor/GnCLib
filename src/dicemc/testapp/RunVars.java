package dicemc.testapp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.money.LogicMoney;

public class RunVars {
	public static Map<UUID, String> playerMap = new HashMap<UUID, String>();
	
	public static void init() {
		playerSetup();
		worldGenerate();
	}
	
	private static void playerSetup() {
		playerMap.put(UUID.fromString("12d9481c-280f-4b70-b93c-c82934cd8c13"), "Caltinor");
		playerMap.put(UUID.fromString("3b2e6a34-043f-4059-87b3-e243af392d69"), "Madood");
		playerMap.put(UUID.fromString("f7079724-6d4d-4983-8721-cb96fa46db58"), "Verazor");
		playerMap.put(UUID.fromString("16e7414d-c7bb-444e-ad1a-62ae34ba5552"), "Darxen");
		System.out.println("=====ACCOUNT PRINTOUT======");
		for (Map.Entry<UUID, String> entries : playerMap.entrySet()) {
			System.out.println("Balance "+entries.getValue()+ "= $"+LogicMoney.getBalance(entries.getKey(), LogicMoney.AccountType.PLAYER.rl));
		}
		System.out.println();
	}
	
	private static void worldGenerate() {
		
	}
	
	public static UUID getPlayerByName(String name) {
		for (Map.Entry<UUID, String> entry : playerMap.entrySet()) {
			if (entry.getValue().equals(name)) {
				return entry.getKey();
			}
		}
		return UUID.randomUUID();
	}
}
