package dicemc.testapp;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import dicemc.gnclib.guilds.LogicGuilds;
import dicemc.gnclib.guilds.entries.Guild;
import dicemc.gnclib.guilds.entries.RankPerms;
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
import dicemc.gnclib.trade.entries.EntryStorage;
import dicemc.gnclib.trade.entries.IMarketEntry;
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.Agent.Type;

public class Menu {
	private static final Scanner input = new Scanner(System.in);	
	private static List<IMarketEntry> marketList;
	private static List<EntryOffer> offers= new ArrayList<EntryOffer>();
	private static Map<FilterType, String> filters = new HashMap<FilterType, String>();
	private static int rowIndex, rowCount;
	private static DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	private static UUID locality = UUID.randomUUID();
	
	private static List<Guild> openGuilds = new ArrayList<Guild>();

	public static void main() {
		boolean looping = true;
		while (looping) {
		System.out.println("Test Player Set as: " + GnCLibConsole.testPlayerName);
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
		locality = UUID.randomUUID();
		System.out.println("locality changed to new random");
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
			System.out.println("6. Print Tables");
			int selection = input.nextInt();
			if (selection==0) {looping = false;}
			else if (selection==5) {marketPersonalActions();}
			else if (selection==6) {LogicTrade.get().printTables();}
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
			System.out.println("7. Accept offer");
			int selection = input.nextInt();
			if (selection == 0) {return;}
			switch (selection) {
			case 1: {marketActionViewListings(market, false); break;}
			case 2: {marketActionNewTransaction(market); break;}
			case 3: {marketActionExecuteTransaction(market); break;}
			case 4: {marketActionViewListings(market, true); break;}
			case 5: {if (!market.equals(IDBImplTrade.MarketType.SERVER)) marketActionSubmitOffer(market); break;}
			case 6: {marketActionViewDetail(market); break;}
			case 7: {marketActionAcceptOffer(market); break;}
			default: return;}
		}
	}
	
	private static void marketPersonalActions() {
		boolean looping = true;
		while (looping) {
			System.out.println("Please select an action: ");
			System.out.println("0. Return");
			System.out.println("1. View sales");
			System.out.println("2. view storage");
			System.out.println("3. view History");
			System.out.println("4. view my offers");
			System.out.println("5. change transaction supply");
			System.out.println("6. remove item from storage");
			int selection = input.nextInt();
			switch (selection) {
			case 1: {
				marketList = getAllMarketItems(GnCLibConsole.testPlayer, false);
				for (int i = 0; i < marketList.size(); i++) {
					String itemLine = i + marketList.get(i).getClass().getSimpleName()+":"+marketList.get(i).getID()+ " " + marketList.get(i).getStack() +
							(marketList.get(i).getGiveItem() ? " Offered for $" : " Requested for $") +
							marketList.get(i).getPrice() + " by:" + marketList.get(i).getVendor().name +
							" " + (marketList.get(i).getStock() >= 0 ? marketList.get(i).getStock() : "Unlimited") + " Supply " + 
							(marketList.get(i) instanceof EntryLocal ? "\n available at " + marketList.get(i).getLocality() : "") +
							(marketList.get(i) instanceof EntryAuction ? "\n Until:" + new Timestamp(marketList.get(i).getBidEnd()) : "") +
							" Placed on:" + new Timestamp(marketList.get(i).getDTGPlaced());
					System.out.println(itemLine);
				}
				continue;
			}
			case 2: {
				input.nextLine();
				System.out.println("Input Index");
				int index = input.nextInt();
				System.out.println("Input Row Count");
				int count = input.nextInt();
				List<EntryStorage> list = LogicTrade.get().getStorageList(index, count, LogicTrade.get().getTransactor(GnCLibConsole.testPlayer, Type.PLAYER, GnCLibConsole.testPlayerName));
				System.out.println("============PLAYER STORAGE================");
				for (int i = 0; i < list.size(); i++) {
					String itemLine = "ID:"+list.get(i).getID()+" Item: " + list.get(i).stack + " Count: " + list.get(i).count;
					System.out.println(itemLine);
				}
				System.out.println("==========================================");
				continue;
			}
			case 3: {
				marketList = getAllMarketItems(GnCLibConsole.testPlayer, true);
				for (int i = 0; i < marketList.size(); i++) {
					String itemLine = i + marketList.get(i).getClass().getSimpleName()+":"+marketList.get(i).getID()+ " " + marketList.get(i).getStack() +
							(marketList.get(i).getGiveItem() ? " Offered for $" : " Requested for $") +
							marketList.get(i).getPrice() + " by:" + marketList.get(i).getVendor().name +
							" " + (marketList.get(i).getStock() >= 0 ? marketList.get(i).getStock() : "Unlimited") + " Supply " + 
							(marketList.get(i) instanceof EntryLocal ? "\n available at " + marketList.get(i).getLocality() : "") +
							(marketList.get(i) instanceof EntryAuction ? "\n Until:" + new Timestamp(marketList.get(i).getBidEnd()) : "") +
							" Placed on:" + new Timestamp(marketList.get(i).getDTGPlaced());
					System.out.println(itemLine);
				}
				continue;
			}
			case 4: {
				List<IMarketEntry> myItems = getAllMarketItems(GnCLibConsole.testPlayer, false);
				for (int i = 0; i < myItems.size(); i++) {
					if (!myItems.get(i).getGiveItem()) continue;
					if (myItems.get(i) instanceof EntryAuction) continue;
					MarketType type = (myItems.get(i) instanceof EntryLocal ? MarketType.LOCAL : MarketType.GLOBAL);
					System.out.println("On Market: "+type.toString());
					System.out.println("=====OFFERS========");
					LogicTrade.get().getOfferList(myItems.get(i).getID(), type).forEach(e -> {
						System.out.println(e.stack+" offered:"+e.offeredAmount+" for:"+e.requestedAmount+" by:"+e.offerer.name);
					});;
					System.out.println("===================");
				}
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
				else if (entry instanceof EntryGlobal) market = entry.getVendor().type.equals(Type.SERVER) ? MarketType.SERVER : MarketType.GLOBAL;
				else if (entry instanceof EntryAuction) market = MarketType.AUCTION;
				System.out.println(new Translation(
						LogicTrade.get().changeTransactionSupply(market, entry, newSupply)
						.translationKey)
						.print());
				continue;
			}
			case 6: {
				input.nextLine();
				System.out.println("Enter item ID");
				int id = input.nextInt();
				System.out.println("Enter quantity being removed");
				int count = input.nextInt();
				EntryStorage entry = new EntryStorage(id, new Agent(Type.PLAYER, GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName), "", count);
				System.out.println(new Translation(
						LogicTrade.get().pullFromStorage(entry, count)
						.translationKey)
						.print());
				continue;
			}
			default: looping=false;}
		}
	}
	
	private static List<IMarketEntry> getAllMarketItems(UUID player, boolean isHistory) {
		List<IMarketEntry> list = new ArrayList<IMarketEntry>();
		Map<FilterType, String> xfilters = Collections.singletonMap(FilterType.SOURCE_VENDOR, player.toString());
		list.addAll(LogicTrade.get().getMarketList(MarketType.LOCAL, 0, -1, xfilters, isHistory));
		list.addAll(LogicTrade.get().getMarketList(MarketType.GLOBAL, 0, -1, xfilters, isHistory));
		list.addAll(LogicTrade.get().getMarketList(MarketType.AUCTION, 0, -1, xfilters, isHistory));
		return list;
	}
	
	private static void marketActionViewListings(IDBImplTrade.MarketType market, boolean isHistory) {
		filters = new HashMap<FilterType, String>();
		input.nextLine();
		Map<FilterType, String> filters = new HashMap<FilterType, String>();
		System.out.println("Please enter your filter preferences:");
		System.out.println("(1)requests or (2)offers: ");
		int x = input.nextInt();
		filters.put(FilterType.IS_OFFER, (x==1 ? "false": "true"));
		System.out.println("enter row count: ");
		rowCount = input.nextInt();
		System.out.println("enter row start: ");
		rowIndex = input.nextInt() * rowCount;		
		System.out.println("(1)filter or (2)all: ");
		x = input.nextInt();
		if (x == 1) {
			input.nextLine();
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
		}
		//sort preferences
		System.out.println("Sort by price 1.no 2.ASC 3.DESC");
		int priceOrder = input.nextInt();
		String in = (priceOrder==2 ? "ASC" : "DESC");
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
					" by:" + marketList.get(i).getVendor().name +
					" " + (marketList.get(i).getStock() >= 0 ? marketList.get(i).getStock() : "Unlimited") + " Supply " + 
					(marketList.get(i) instanceof EntryLocal ? "\n available at " + LogicGuilds.getGuildByID(marketList.get(i).getLocality()).name : "") +
					(marketList.get(i) instanceof EntryAuction ? "\n Until:" + new Timestamp(marketList.get(i).getBidEnd()) : "") +
					" Placed on:" + new Timestamp(marketList.get(i).getDTGPlaced()) +
					(isHistory ? "\n Purchased by: "+marketList.get(i).getBuyer().name+ " on:" + new Timestamp(marketList.get(i).getDTGClosed()) : "");
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
			EntryLocal entry = new EntryLocal(locality, new Agent(Type.PLAYER, GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName),
					stack, stock, price, giveItem);
			System.out.println(new Translation(LogicTrade.get().createTransaction(entry, market).translationKey).print());
			return;
		}
		case GLOBAL: {
			EntryGlobal entry = new EntryGlobal(new Agent(Type.PLAYER, GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName), stack, stock, price, giveItem);
			System.out.println(new Translation(LogicTrade.get().createTransaction(entry, market).translationKey).print());
			return;
		}
		case AUCTION: {
			EntryAuction entry = new EntryAuction(new Agent(Type.PLAYER, GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName), stack, price);
			System.out.println(new Translation(LogicTrade.get().createTransaction(entry, market).translationKey).print());
			return;
		}
		case SERVER: {
			EntryGlobal entry = new EntryGlobal(new Agent(Type.SERVER, ComVars.NIL, "Server"), stack, stock, price, giveItem);
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
		Agent buyer = new Agent(Type.PLAYER, GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName);
		System.out.println(new Translation(
				LogicTrade.get().executeTransaction(entry, market, buyer, quant)
					.translationKey).print());
		marketList = LogicTrade.get().getMarketList(market, rowIndex, rowCount, filters, false);
	}
	private static void marketActionSubmitOffer(IDBImplTrade.MarketType market) {
		input.nextLine();
		System.out.println("Enter item number: ");
		int id = input.nextInt();
		
		if (market.equals(MarketType.AUCTION)) {
			System.out.println("Enter Bid Amount");
			double price = input.nextDouble();
			EntryBid bid = new EntryBid(marketList.get(id).getID(), new Agent(Type.PLAYER, GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName), price);
			System.out.println(new Translation(
					LogicTrade.get().placeBid(bid, marketList.get(id).getPrice()).translationKey)
						.print());
		}
		else {
			input.nextLine();
			System.out.println("Enter your ItemStack Name:");
			String stack = input.nextLine();
			System.out.println("Enter requested amount: ");
			int requested = input.nextInt();
			System.out.println("Enter offerred amount: ");
			int offered = input.nextInt();
			EntryOffer offer = new EntryOffer(marketList.get(id).getID(), LogicTrade.get().getMarketName(market), stack
					, new Agent(Type.PLAYER, GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName), requested, offered);
			System.out.println(new Translation(
					LogicTrade.get().submitOffer(marketList.get(id), offer, market).translationKey).print());
		}
	}
	private static void marketActionViewDetail(IDBImplTrade.MarketType market) {
		
		input.nextLine();
		System.out.println("Enter item number: ");
		int id = input.nextInt();
		IMarketEntry entry = marketList.get(id);
		if (entry.getGiveItem() && !market.equals(MarketType.AUCTION)) {
			System.out.println("Get offers");
			offers = LogicTrade.get().getOfferList(entry.getID(), market);
			System.out.println(offers.size());
		}
		double price = entry.getPrice();
		List<EntryBid> bids = new ArrayList<EntryBid>();
		if (market.equals(MarketType.AUCTION)) {
			bids = LogicTrade.get().getBidList(entry.getID());
			price = (bids.size()==0 ? entry.getPrice() : bids.get(bids.size()-1).value); 
		}
		
		System.out.println("===========DETAIL VIEW================");
		System.out.println(entry.getGiveItem() ? "OFFERING:" : "REQUESTING");
		System.out.println("Item  : "+ entry.getStack());
		System.out.println(" supply="+ entry.getStock());
		System.out.println("for   :$"+ df.format(price));
		System.out.println("Vendor: "+ entry.getVendor().name +"["+entry.getVendor().refID.toString()+"]");				
		System.out.println("Placed on: "+ new Timestamp(entry.getDTGPlaced()));
		if (market.equals(MarketType.AUCTION)) System.out.println("Bidding Ends on: "+ new Timestamp(entry.getBidEnd()));
		if (market.equals(MarketType.LOCAL)) System.out.println("Available at: "+LogicGuilds.getGuildByID(entry.getLocality()).name);
		System.out.println("======================================");
		if (!market.equals(MarketType.AUCTION)) {
			System.out.println("---------------OFFERS-----------------");
			for (int i = 0; i < offers.size(); i++) {
				EntryOffer o = offers.get(i);
				System.out.println("["+i+"] "+o.offerer.name+" offers");
				System.out.println(" Item: "+o.stack);
				System.out.println("Give:For  "+o.offeredAmount+":"+o.requestedAmount);
				System.out.println("_______________________________________");
			}
		}
		else {
			System.out.println("---------------BIDS-----------------");
			for (int i = 0; i < bids.size(); i++) {
				EntryBid b = bids.get(i);
				System.out.println(b.bidder.name+" bid $"+df.format(b.value)+" on "+new Timestamp(b.placedDate));
				System.out.println("_______________________________________");
			}
		}
	}
	private static void marketActionAcceptOffer(IDBImplTrade.MarketType market) {
		input.nextLine();
		System.out.println("Input item index");
		int iIndex = input.nextInt();
		System.out.println("Input offer index");
		int oIndex = input.nextInt();
		System.out.println(new Translation(
				LogicTrade.get().acceptOffer(marketList.get(iIndex), market, offers.get(oIndex))
				.translationKey)
				.print());
	}
	
	private static void guildMain() {
		boolean looping = true;
		while (looping) {
			System.out.println("Guild Menu");
			System.out.println("Please select a guild option: ");
			System.out.println("0. Back");
			boolean hasNoGuild = LogicGuilds.getGuildByMember(GnCLibConsole.testPlayer).equals(Guild.getDefault());
			if (hasNoGuild) {
				System.out.println("1. Create a guild");
				System.out.println("2. view invites");
				System.out.println("3. view all guilds");
				System.out.println("4. Admin menu");
				System.out.println("5. Print Tables");
				switch (input.nextInt()) {
				case 1: {
					input.nextLine();
					System.out.println("Enter the Guild's Name:");
					String name = input.nextLine();
					System.out.println(new Translation(
							LogicGuilds.playerCreateGuild(new Agent(Type.PLAYER, GnCLibConsole.testPlayer, GnCLibConsole.testPlayerName), name, false)
							.translationKey)
							.print());
					break;
				}
				case 2: {
					openGuilds = LogicGuilds.getJoinableGuilds(GnCLibConsole.testPlayer);
					System.out.println("======Open Guilds=========");
					for (int i = 0; i< openGuilds.size(); i++) {
						System.out.println(i +": "+ openGuilds.get(i).name);
					}
					System.out.println("==========================");
					System.out.println("Join guild? -1=no, #=guild to Join");
					int selection = input.nextInt();
					if (selection >= 0 && selection < openGuilds.size()) {
						Guild g = openGuilds.get(selection);
						System.out.println(new Translation(
								LogicGuilds.addMember(g.guildID, GnCLibConsole.testPlayer, LogicGuilds.getBottomRank(g.guildID))
								.translationKey)
								.print());
					}
					break;
				}
				case 3: {
					Map<UUID, Guild> allGuilds = LogicGuilds.getGuilds();
					System.out.println("======All Guilds=========");
					for (Map.Entry<UUID, Guild> g : allGuilds.entrySet()) {
						System.out.println(g.getValue().name);
					}
					System.out.println("=========================");
					break;
				}
				case 4: {guildAdminMenu(); break;}
				case 5: {LogicGuilds.printTables(); break;}
				default: looping = false;}
			}
			//player has guild options
			else {
				Guild gRef = LogicGuilds.getGuildByMember(GnCLibConsole.testPlayer);
				Agent agent = LogicTrade.get().getTransactor(GnCLibConsole.testPlayer, Type.PLAYER, GnCLibConsole.testPlayerName);
				System.out.println("1. view all guilds");
				System.out.println("2. view guild members");
				System.out.println("3. invite member");
				System.out.println("4. kick member");
				System.out.println("5. open rank manager");
				System.out.println("6. open perms manager");
				System.out.println("7. open guild manager");
				System.out.println("8. Print Tables");
				switch (input.nextInt()) {
				case 1: {
					Map<UUID, Guild> allGuilds = LogicGuilds.getGuilds();
					System.out.println("======All Guilds=========");
					for (Map.Entry<UUID, Guild> g : allGuilds.entrySet()) {
						System.out.println(g.getValue().name);
					}
					System.out.println("=========================");
					break;
				}
				case 2: { 
					Map<UUID, Integer> members = LogicGuilds.getMembers(gRef.guildID);
					System.out.println("======Guild Members=========");
					for (Map.Entry<UUID, Integer> m : members.entrySet()) {
						String name = RunVars.playerMap.get(m.getKey());
						String rank = LogicGuilds.getRanks(gRef.guildID).getOrDefault(m.getValue(), "Invited");
						System.out.println(name +" : "+ rank);
					}
					System.out.println("============================");
					break;
				}
				case 3: { 
					input.nextLine();
					System.out.println("Enter player name to Invite:");
					String plyr = input.nextLine();
					System.out.println(new Translation(
							LogicGuilds.inviteMember(gRef.guildID, agent, LogicTrade.get().getTransactor(RunVars.getPlayerByName(plyr), Type.PLAYER, plyr))
							.translationKey)
							.print());
					break;
				}
				case 4: { 
					input.nextLine();
					System.out.println("Enter name of member to kick");
					String plyr = input.nextLine();
					System.out.println(new Translation(
							LogicGuilds.kickMember(gRef.guildID, agent, RunVars.getPlayerByName(plyr))
							.translationKey)
							.print());
					break;
				}
				case 5: {ranksMgr(gRef.guildID, agent); break;}
				case 6: {permsMgr(gRef.guildID, agent); break;}
				case 7: {guildMgr(gRef.guildID, agent); break;}
				case 8: {LogicGuilds.printTables(); break;}
				default: looping = false;}
			}
		}
	}
	
	private static void ranksMgr(UUID guildID, Agent exec) {
		boolean looping = true;
		while (looping) {
			System.out.println("============Rank Manager===========");
			//print ranks with membership underneath
			int rank = 0;
			while (LogicGuilds.getRanks(guildID).containsKey(rank)) {
				System.out.println(rank+ ": "+LogicGuilds.getRanks(guildID).get(rank));
				for (Map.Entry<UUID, Integer> mbr : LogicGuilds.getMembers(guildID).entrySet()) {
					if (mbr.getValue() == rank) System.out.println("     -"+RunVars.playerMap.get(mbr.getKey()));
				}
				rank++;
			}			
			System.out.println("Select an option:");
			System.out.println("0. Back");
			System.out.println("1. Buy Rank");
			System.out.println("2. Rename Rank");
			System.out.println("3. Assign Member to Rank");
			switch (input.nextInt()) {
			case 1: {
				input.nextLine();
				System.out.println("Enter new Rank Title:");
				String title = input.nextLine();
				System.out.println(new Translation(
						LogicGuilds.buyNewRank(guildID, exec, title)
						.translationKey)
						.print());				
				break;
			}
			case 2: {
				System.out.println("Enter number of rank to be renamed:");
				int selectedRank = input.nextInt();
				input.nextLine();
				System.out.println("Enter rank's new title");
				String title = input.nextLine();
				System.out.println(new Translation(
						LogicGuilds.setRankTitle(guildID, exec, selectedRank, title)
						.translationKey)
						.print());
				break;
			}
			case 3: {
				System.out.println("Enter number of rank to be assigned to:");
				int selectedRank = input.nextInt();
				input.nextLine();
				System.out.println("Enter name of member to be reassigned:");
				UUID tgt = RunVars.getPlayerByName(input.nextLine());
				System.out.println(new Translation(
						LogicGuilds.updateMember(guildID, exec, tgt, selectedRank)
						.translationKey)
						.print());
				break;
			}
			default: looping = false;}
		}
	}
	
	private static void permsMgr(UUID guildID, Agent exec) {
		boolean looping = true;
		while (looping) {
			System.out.println("============Permission Manager===========");
			for (Map.Entry<String, List<RankPerms>> perms : LogicGuilds.getPerms(guildID).entrySet()) {
				List<RankPerms> members = new ArrayList<RankPerms>();
				List<RankPerms> groups = new ArrayList<RankPerms>();
				for (int i = 0; i < perms.getValue().size(); i++) {
					if (perms.getValue().get(i).rank == -2) members.add(perms.getValue().get(i));
					else groups.add(perms.getValue().get(i));
				}
				System.out.println(perms.getKey());
				for (int i = 0; i < groups.size(); i++) {
					System.out.println("    "+LogicGuilds.getRanks(guildID).get(groups.get(i).rank)+ "[CASCADES:"+groups.get(i).cascades+"]");}
				for (int i = 0; i < members.size(); i++) {
					System.out.println("    "+RunVars.playerMap.get(members.get(i).player));}
				System.out.println("");
			}
			System.out.println("Please select an option below:");
			System.out.println("0. Back");
			System.out.println("1. Add Player Perm");
			System.out.println("2. Add Rank Perm");
			System.out.println("3. Remove Player Perm");
			System.out.println("4. Remove Rank Perm");
			switch (input.nextInt()) {
			case 1: {
				input.nextLine();
				System.out.println("Enter perm key:  (verbatim from above)");
				String key = input.nextLine();
				System.out.println("Enter player name to be permitted");
				String name = input.nextLine();
				RankPerms perm = new RankPerms(guildID, key, RunVars.getPlayerByName(name));
				System.out.println(new Translation(
						LogicGuilds.changePermission(perm, exec, true)
						.translationKey)
						.print());
				break;
			}
			case 2: {
				input.nextLine();
				System.out.println("Enter perm key:  (verbatim from above)");
				String key = input.nextLine();
				System.out.println("Enter rank number to be permitted");
				int rank = input.nextInt();
				input.nextLine();
				System.out.println("Should this permission cascade? ['yes' or 'no] (eg. all ranks below it also are permitted)");
				boolean cascades = input.nextLine().equalsIgnoreCase("yes");
				RankPerms perm = new RankPerms(guildID, key, rank, cascades);
				System.out.println(new Translation(
						LogicGuilds.changePermission(perm, exec, true)
						.translationKey)
						.print());
				break;
			}
			case 3: {
				input.nextLine();
				System.out.println("Enter perm key:  (verbatim from above)");
				String key = input.nextLine();
				System.out.println("Enter player name to be removed");
				String name = input.nextLine();
				RankPerms perm = new RankPerms(guildID, key, RunVars.getPlayerByName(name));
				System.out.println(new Translation(
						LogicGuilds.changePermission(perm, exec, false)
						.translationKey)
						.print());
				break;
			}
			case 4: {
				input.nextLine();
				System.out.println("Enter perm key:  (verbatim from above)");
				String key = input.nextLine();
				System.out.println("Enter rank number to be removed");
				int rank = input.nextInt();
				RankPerms perm = new RankPerms(guildID, key, rank, true);
				System.out.println(new Translation(
						LogicGuilds.changePermission(perm, exec, false)
						.translationKey)
						.print());
				break;
			}
			default: looping = false;}
		}
	}
	
	private static void guildMgr(UUID guildID, Agent exec) {
		DecimalFormat df = new DecimalFormat("0.000");
		boolean looping = true;
		while (looping) {
			Guild guild = LogicGuilds.getGuildByID(guildID);
			System.out.println("============Guild Manager===========");
			System.out.println("<< "+guild.name+" >> "+ (guild.isAdmin ? "[ADMIN]": ""));
			System.out.println("Join Status: "+ (guild.open ? "Open" : "Invite Only"));
			System.out.println(" Member Tax: "+df.format(guild.tax*100)+"%");
			System.out.println("Market Size: "+guild.getMarketSize());
			System.out.println("Market TP Location: ["+guild.getTPX()+", "+guild.getTPY()+", "+guild.getTPZ()+"]");
			System.out.println("====================================");
			System.out.println("Selct an option:");
			System.out.println("0. back");
			System.out.println("1. change guild name");
			System.out.println("2. change member tax");
			System.out.println("3. toggle join status");
			System.out.println("4. change market TP location");
			switch (input.nextInt()) {
			case 1: {
				input.nextLine();
				System.out.println("Enter new name");
				String newName = input.nextLine();
				System.out.println(new Translation(
						LogicGuilds.setGuildName(guildID, exec, newName)
						.translationKey)
						.print());
				break;
			}
			case 2: {
				input.nextLine();
				System.out.println("Enter new tax rate: (in decimal format)");
				double newTax = input.nextDouble();
				System.out.println(new Translation(
						LogicGuilds.setGuildTax(guildID, exec, newTax)
						.translationKey)
						.print());
				break;
			}
			case 3: {
				System.out.println(new Translation(
						LogicGuilds.setGuildOpen(guildID, exec, !guild.open)
						.translationKey)
						.print());
				break;
			}
			case 4: {
				input.nextLine();
				System.out.println("Enter TP X:");
				int newTPx = input.nextInt();
				System.out.println("Enter TP Y:");
				int newTPy = input.nextInt();
				System.out.println("Enter TP Z:");
				int newTPz = input.nextInt();
				System.out.println(new Translation(
						LogicGuilds.setGuildShopLoc(guildID, exec, newTPx, newTPy, newTPz)
						.translationKey)
						.print());
				break;
			}
			default: looping = false;}
		}
	}
	
	private static void guildAdminMenu() {
		
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
			System.out.println("5. Print Table");
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
				System.out.println("Enter name of Sender");
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
			else if (selection == 5) {
				LogicMoney.printTable();
				continue;
			}
			else return;
		}
	}
	
	private static void adminMain() {
		System.out.println("Admin Menu");
		System.out.println("Select User:");
		System.out.println("1. Steve");
		System.out.println("2. Alex");
		System.out.println("3. Enderman");
		System.out.println("4. Rumm");
		int selection = input.nextInt();
		switch (selection) {
		case 1: {GnCLibConsole.testPlayerName="Steve"; break;}
		case 2: {GnCLibConsole.testPlayerName="Alex"; break;}
		case 3: {GnCLibConsole.testPlayerName="Enderman"; break;}
		case 4: {GnCLibConsole.testPlayerName="Rumm"; break;}
		default: {GnCLibConsole.testPlayerName = "Steve";}}
		GnCLibConsole.testPlayer = RunVars.getPlayerByName(GnCLibConsole.testPlayerName);
	}
}
