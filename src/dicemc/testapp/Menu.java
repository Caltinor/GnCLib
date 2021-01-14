package dicemc.testapp;

import java.util.Scanner;
import java.util.UUID;

import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.util.ComVars;

public class Menu {
	private static final Scanner input = new Scanner(System.in);

	public static void main() {
		System.out.println("Which Menu to Open?");
		System.out.println("1. Land");
		System.out.println("2. Market");
		System.out.println("3. Guild");
		System.out.println("4. Accounts");
		System.out.println("5. Admin");
		int selection = input.nextInt();
		if (selection == 1) landMain();
		else if (selection == 2) marketMain();
		else if (selection == 3) guildMain();
		else if (selection == 4) accountMain();
		else if (selection == 5) adminMain();
	}
	
	private static void landMain() {
		System.out.println("Land Menu");
		main();
	}
	
	private static void marketMain() {
		System.out.println("Market Menu");
		main();
	}
	
	private static void guildMain() {
		System.out.println("Guild Menu");
		main();
	}
	
	private static void accountMain() {
		System.out.println("Account Menu");
		System.out.println("Choose an action:");
		System.out.println("1. get balance of testPlayer");
		System.out.println("2. set balance of testPlayer");
		System.out.println("3. change balance of testPlayer");
		System.out.println("4. transfer funds");
		System.out.println("0. Back");
		int selection = input.nextInt();
		if (selection == 1) {
			System.out.println("Player Balance is now: $"+LogicMoney.getBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl));
			accountMain();
		}
		else if (selection == 2) {
			double value = input.nextDouble();
			LogicMoney.setBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl, value);
			System.out.println("Player Balance is now: $"+LogicMoney.getBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl));
			accountMain();
		}
		else if (selection == 3) {
			double value = input.nextDouble();
			LogicMoney.changeBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl, value);
			System.out.println("Player Balance is now: $"+LogicMoney.getBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl));
			accountMain();
		}
		else if (selection == 4) {
			input.nextLine();
			System.out.println("Enter name of recipient");
			UUID sender = RunVars.getPlayerByName(input.nextLine());			
			if (sender.equals(ComVars.NIL)) {
				System.out.println("Unable to find sender.");
				accountMain();
			}
			System.out.println("Enter name of recipient");
			UUID recipient = RunVars.getPlayerByName(input.nextLine());			
			if (recipient.equals(ComVars.NIL)) {
				System.out.println("Unable to find recipient.");
				accountMain();
			}
			else {
				System.out.println("Enter Value to be transfered:");
				double value = input.nextDouble();
				if (LogicMoney.transferFunds(sender, LogicMoney.AccountType.PLAYER.rl, recipient, LogicMoney.AccountType.PLAYER.rl, value)) {
					System.out.println("$"+ value +" transferred from "+RunVars.playerMap.get(sender)+" to "+RunVars.playerMap.get(recipient));
					System.out.println(RunVars.playerMap.get(sender)+" Balance is now: $"+LogicMoney.getBalance(sender, LogicMoney.AccountType.PLAYER.rl));
					System.out.println(RunVars.playerMap.get(recipient)+ " Balance is now: $"+LogicMoney.getBalance(recipient, LogicMoney.AccountType.PLAYER.rl));
					accountMain();
				}
			}
		}
		else main();
	}
	
	private static void adminMain() {
		System.out.println("Admin Menu");
		main();
	}
}
