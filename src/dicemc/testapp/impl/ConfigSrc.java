package dicemc.testapp.impl;

import java.util.ArrayList;

import dicemc.gnclib.configs.ConfigCore;

public class ConfigSrc {

	public static void init() {
		ConfigCore.defineDataStorageConfigValues(
				"",  							//port
				"GNC_test1", 					//name
				"h2", 							//service
				System.getProperty("user.dir"), //url
				"sa", 							//user
				"");							//password
		ConfigCore.defineGuildConfigValues(
				0.1, 		//globalTaxRate, 
				864000L,	//globalTaxInterval, 
				2500d, 		//guildCreateCost, 
				1500d, 		//guildNameChangeCost, 
				1000d); 	//guildRankAddCost
		ConfigCore.defineMoneyConfigValues(
				"test World 1", //worldName, 
				1000d, 			//startingFunds, 
				0d);			//guildStartingFunds
		ConfigCore.defineProtectionConfigValues(
				true, 						//unownedProtected, 
				new ArrayList<String>(), 	//unownedWhitelist, 
				new ArrayList<String>());	//protectionBlacklist
		ConfigCore.defineRealEstateConfigValues(
				100d, 						//defaultLandPrice, 
				43200000L, 					//tempclaimDuration, 
				9, 							//chunksPerMember, 
				0.1,						//tempclaimRate, 
				0.75,						//landAbandonRefundRate, 
				2000d,						//outpostCreateCost, 
				true);						//autoTempclaim
		ConfigCore.defineTradeConfigValues(
				"test World 1",	//saveName
				0.1, 			//globalTaxBuy, 
				0.1, 			//globalTaxSell, 
				0.3,			//auctionTaxSell, 
				600000L,		//auctionOpenDuration
				25);			//pageSize
	}
}
