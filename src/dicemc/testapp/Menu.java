package dicemc.testapp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.trade.LogicTrade;
import dicemc.gnclib.trade.dbref.IDBImplTrade;
import dicemc.gnclib.trade.dbref.IDBImplTrade.FilterType;
import dicemc.gnclib.trade.dbref.IDBImplTrade.MarketType;
import dicemc.gnclib.trade.entries.EntryAuction;
import dicemc.gnclib.trade.entries.EntryBid;
import dicemc.gnclib.trade.entries.EntryGlobal;
import dicemc.gnclib.trade.entries.EntryLocal;
import dicemc.gnclib.trade.entries.EntryOffer;
import dicemc.gnclib.trade.entries.EntryServer;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.ComVars;

public class Menu {
	private static final Scanner input = new Scanner(System.in);	
	private static List<IMarketEntry> marketList;

	public static void main() {
		boolean looping = true;
		while (looping) {
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
		else looping = false;
		}
	}
	
	private static void landMain() {
		System.out.println("Land Menu");
	}
	
	private static void marketMain() {
		boolean looping = true;
		while (looping) {
			System.out.println("Market Menu");
			System.out.println("Please select a market menu to begin: ");
			System.out.println("1. LOCAL");
			System.out.println("2. GLOBAL");
			System.out.println("3. AUCTION");
			System.out.println("4. SERVER");
			System.out.println("5. PERSONAL");
			int selection = input.nextInt();
			if (selection==0) {looping = false;}
			else if (selection==5) {marketPersonalActions();}
			else {marketActions(IDBImplTrade.MarketType.values()[selection-1]);	}
		}
	}
	
	private static void marketActions(IDBImplTrade.MarketType market) {
		boolean looping = true;
		while (looping) {
			System.out.println("Please select an action: ");
			System.out.println("0. Return");
			System.out.println("1. View Listings");
			System.out.println("2. new transaction");
			System.out.println("3. execute transaction");
			System.out.println("4. view History");
			System.out.println("5. submit offer/bid");
			System.out.println("6. Detail View");
			int selection = input.nextInt();
			if (selection == 0) {return;}
			switch (selection) {
			case 1: {marketActionViewListings(market, false); break;}
			case 2: {marketActionNewTransaction(market); break;}
			case 3: {marketActionExecuteTransaction(market); break;}
			case 4: {marketActionViewListings(market, true); break;}
			case 5: {if (!market.equals(IDBImplTrade.MarketType.SERVER)) marketActionSubmitOffer(market); break;}
			case 6: {marketActionViewDetail(market); break;}
			default: return;}
		}
	}
	
	private static void marketPersonalActions() {
		boolean looping = true;
		while (looping) {
			Map<FilterType, String> filters = new HashMap<FilterType, String>();
			System.out.println("Please select an action: ");
			System.out.println("0. Return");
			System.out.println("1. View sales");
			System.out.println("2. view storage");
			System.out.println("3. view History");
			System.out.println("4. view my offers");
			System.out.println("5. change transaction supply");
			int selection = input.nextInt();
			switch (selection) {
			case 1: {
				List<IMarketEntry> list = new ArrayList<IMarketEntry>();
				filters.put(FilterType.SOURCE_VENDOR, GnCLibConsole.testPlayer.toString());
				list.addAll(LogicTrade.get().getMarketList(MarketType.LOCAL, 0, -1, filters, false));
				list.addAll(LogicTrade.get().getMarketList(MarketType.GLOBAL, 0, -1, filters, false));
				list.addAll(LogicTrade.get().getMarketList(MarketType.AUCTION, 0, -1, filters, false));
				marketList = list;
				for (int i = 0; i < marketList.size(); i++) {
					String itemLine = i + marketList.get(i).getClass().getSimpleName()+":"+marketList.get(i).getID()+ " " + marketList.get(i).getStack() +
							(marketList.get(i).getGiveItem() ? " Offered for $" : " Requested for $") +
							marketList.get(i).getPrice() + " by:" + marketList.get(i).getVendorName() +
							" " + (marketList.get(i).getStock() >= 0 ? marketList.get(i).getStock() : "Unlimited") + " Supply " + 
							(marketList.get(i) instanceof EntryLocal ? "\n available at " + marketList.get(i).getLocality() : "") +
							(marketList.get(i) instanceof EntryAuction ? "\n Until:" + new Timestamp(marketList.get(i).getBidEnd()) : "") +
							" Placed on:" + new Timestamp(marketList.get(i).getDTGPlaced());
					System.out.println(itemLine);
				}
				continue;
			}
			case 2: {
				continue;
			}
			case 3: {
				continue;
			}
			case 4: {
				continue;
			}
			case 5: {
				input.nextLine();
				System.out.println("Enter item number: ");
				int id = input.nextInt();
				System.out.println("Enter new amount: ");
				int newSupply = input.nextInt();
				IMarketEntry entry = marketList.get(id);
				MarketType market = null;
				if (entry instanceof EntryLocal) market = MarketType.LOCAL;
				else if (entry instanceof EntryGlobal) market = MarketType.GLOBAL;
				else if (entry instanceof EntryAuction) market = MarketType.AUCTION;
				else if (entry instanceof EntryServer) market = MarketType.SERVER;
				LogicTrade.get().changeTransactionSupply(market, entry, newSupply);
				continue;
			}
			default: looping=false;}
		}
	}
	
	private static void marketActionViewListings(IDBImplTrade.MarketType market, boolean isHistory) {
		input.nextLine();
		Map<FilterType, String> filters = new HashMap<FilterType, String>();
		System.out.println("Please enter your filter preferences:");
		System.out.println("Vendor Filter: ");
		String in = input.nextLine();
		if (in.length()>0) filters.put(FilterType.SOURCE_VENDOR, in);
		System.out.println("Locality Filter: ");
		in = input.nextLine();
		if (in.length()>0) filters.put(FilterType.SOURCE_LOCALITY, in);
		System.out.println("Price Minimum Filter: ");
		in = input.nextLine();
		if (in.length()>0) filters.put(FilterType.PRICE_FROM, in);
		System.out.println("Price Maximum Filter: ");
		in = input.nextLine();
		if (in.length()>0) filters.put(FilterType.PRICE_TO, in);
		System.out.println("(1)requests or (2)offers: ");
		int x = input.nextInt();
		filters.put(FilterType.IS_OFFER, (x==1 ? "false": "true"));
		System.out.println("enter row start: ");
		int rowIndex = input.nextInt();
		System.out.println("enter row count: ");
		int rowCount = input.nextInt();
		//sort preferences
		System.out.println("Sort by price 1.no 2.ASC 3.DESC");
		int priceOrder = input.nextInt();
		in = (priceOrder==2 ? "ASC" : "DESC");
		if (priceOrder >= 2) filters.put(FilterType.ORDER_PRICE, in);
		System.out.println("include my sales 1.yes 2.no");
		int includeOwnSales = input.nextInt();
		if (includeOwnSales == 2) filters.put(FilterType.INCLUDE_MY_SALES, GnCLibConsole.testPlayer.toString());
		
		marketList = LogicTrade.get().getMarketList(market, rowIndex, rowCount, filters, isHistory);
		Map<Integer, Double> highestBids = new HashMap<Integer, Double>();
		if (market.equals(MarketType.AUCTION)) {
			for (int i = 0; i < marketList.size(); i++) {
				List<EntryBid> bids = LogicTrade.get().getBidList(marketList.get(i).getID());
				highestBids.put(i, (bids.size()==0 ? marketList.get(i).getPrice() : bids.get(bids.size()-1).value)); 
			}
		}
		System.out.println("============LISTINGS==============");
		for (int i = 0; i < marketList.size(); i++) {
			String itemLine = i +":"+marketList.get(i).getID()+ " " + marketList.get(i).getStack() +
					(marketList.get(i).getGiveItem() ? " Offered for $" : " Requested for $") +
					(market.equals(MarketType.AUCTION) ? highestBids.get(i) : marketList.get(i).getPrice()) +
					" by:" + marketList.get(i).getVendorName() +
					" " + (marketList.get(i).getStock() >= 0 ? marketList.get(i).getStock() : "Unlimited") + " Supply " + 
					(marketList.get(i) instanceof EntryLocal ? "\n available at " + marketList.get(i).getLocality() : "") +
					(marketList.get(i) instanceof EntryAuction ? "\n Until:" + new Timestamp(marketList.get(i).getBidEnd()) : "") +
					" Placed on:" + new Timestamp(marketList.get(i).getDTGPlaced()) +
					(isHistory ? "\n Purchased by: "+marketList.get(i).getBuyerName()+ " on:" + new Timestamp(marketList.get(i).getDTGClosed()) : "");
			System.out.println(itemLine);
		}
		System.out.println("==================================");
	}
	private static void marketActionNewTransaction(IDBImplTrade.MarketType market) {
		input.nextLine();
		System.out.println("Are you requesting and item or offering one?");
		System.out.println("1. Offering");
		System.out.println("2. Requesting");
		boolean giveItem = (input.nextInt() == 1 ? true : false);
		input.nextLine();
		System.out.println("Enter your ItemStack Name:");
		String stack = input.nextLine();
		System.out.println("Enter the Price for this item:");
		double price = input.nextDouble();
		input.nextLine();
		System.out.println("Enter the item quantity:");
		int stock = input.nextInt();
		input.nextLine();
		switch (market) {
		case LOCAL: {
			EntryLocal entry = new EntryLocal(UUID.randomUUID(), GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName,
					stack, stock, price, giveItem);
			System.out.println(new Translation(LogicTrade.get().createTransaction(entry, market).translationKey).print());
			return;
		}
		case GLOBAL: {
			EntryGlobal entry = new EntryGlobal(GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName, stack, stock, price, giveItem);
			System.out.println(new Translation(LogicTrade.get().createTransaction(entry, market).translationKey).print());
			return;
		}
		case AUCTION: {
			EntryAuction entry = new EntryAuction(GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName, stack, price);
			System.out.println(new Translation(LogicTrade.get().createTransaction(entry, market).translationKey).print());
			return;
		}
		case SERVER: {
			EntryServer entry = new EntryServer(stack, price, giveItem, stock);
			System.out.println(new Translation(LogicTrade.get().createTransaction(entry, market).translationKey).print());
			return;
		}
		default:}
	}
	private static void marketActionExecuteTransaction(IDBImplTrade.MarketType market) {
		input.nextLine();
		System.out.println("Enter item number: ");
		int id = input.nextInt();
		System.out.println("Enter desired quantity: ");
		int quant = input.nextInt();

		IMarketEntry entry = marketList.get(id);
		UUID buyer = GnCLibConsole.testPlayer;
		String buyerName = GnCLibConsole.testPlayerName;
		System.out.println(new Translation(
				LogicTrade.get().executeTransaction(entry, market, buyer, buyerName, quant)
					.translationKey).print());
	}
	private static void marketActionSubmitOffer(IDBImplTrade.MarketType market) {
		input.nextLine();
		System.out.println("Enter item number: ");
		int id = input.nextInt();
		
		if (market.equals(MarketType.AUCTION)) {
			System.out.println("Enter Bid Amount");
			double price = input.nextDouble();
			EntryBid bid = new EntryBid(marketList.get(id).getID(), GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName, price);
			System.out.println(new Translation(
					LogicTrade.get().placeBid(bid, marketList.get(id).getPrice()).translationKey)
						.print());
		}
		else {
			System.out.println("Enter your ItemStack Name:");
			String stack = input.nextLine();
			System.out.println("Enter requested amount: ");
			int requested = input.nextInt();
			System.out.println("Enter offerred amount: ");
			int offered = input.nextInt();
			EntryOffer offer = new EntryOffer(marketList.get(id).getID(), market.toString(), stack, GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName, requested, offered);
			System.out.println(new Translation(
					LogicTrade.get().submitOffer(marketList.get(id), offer).translationKey).print());
		}
	}
	private static void marketActionViewDetail(IDBImplTrade.MarketType market) {}
	
	private static void guildMain() {
		System.out.println("Guild Menu");
	}
	
	private static void accountMain() {
		boolean looping = true;
		while (looping) {
			System.out.println("Account Menu");
			System.out.println("Choose an action:");
			System.out.println("1. get balance of testPlayer");
			System.out.println("2. set balance of testPlayer");
			System.out.println("3. change balance of testPlayer");
			System.out.println("4. transfer funds");
			System.out.println("0. Back");
			int selection = input.nextInt();
			if (selection == 1) {
				System.out.println("Player Balance is: $"+LogicMoney.getBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl));
				continue;
			}
			else if (selection == 2) {
				double value = input.nextDouble();
				LogicMoney.setBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl, value);
				System.out.println("Player Balance is now: $"+LogicMoney.getBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl));
				continue;
			}
			else if (selection == 3) {
				double value = input.nextDouble();
				LogicMoney.changeBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl, value);
				System.out.println("Player Balance is now: $"+LogicMoney.getBalance(GnCLibConsole.testPlayer, LogicMoney.AccountType.PLAYER.rl));
				continue;
			}
			else if (selection == 4) {
				input.nextLine();
				System.out.println("Enter name of recipient");
				UUID sender = RunVars.getPlayerByName(input.nextLine());			
				if (sender.equals(ComVars.NIL)) {
					System.out.println("Unable to find sender.");
					continue;
				}
				System.out.println("Enter name of recipient");
				UUID recipient = RunVars.getPlayerByName(input.nextLine());			
				if (recipient.equals(ComVars.NIL)) {
					System.out.println("Unable to find recipient.");
					continue;
				}
				else {
					System.out.println("Enter Value to be transfered:");
					double value = input.nextDouble();
					if (LogicMoney.transferFunds(sender, LogicMoney.AccountType.PLAYER.rl, recipient, LogicMoney.AccountType.PLAYER.rl, value)) {
						System.out.println("$"+ value +" transferred from "+RunVars.playerMap.get(sender)+" to "+RunVars.playerMap.get(recipient));
						System.out.println(RunVars.playerMap.get(sender)+" Balance is now: $"+LogicMoney.getBalance(sender, LogicMoney.AccountType.PLAYER.rl));
						System.out.println(RunVars.playerMap.get(recipient)+ " Balance is now: $"+LogicMoney.getBalance(recipient, LogicMoney.AccountType.PLAYER.rl));
						continue;
					}
				}
			}
			else return;
		}
	}
	
	private static void adminMain() {
		System.out.println("Admin Menu");
	}
}
