package dicemc.gnclib.realestate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.guilds.LogicGuilds;
import dicemc.gnclib.guilds.LogicGuilds.PermKey;
import dicemc.gnclib.guilds.entries.Guild;
import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.money.LogicMoney.AccountType;
import dicemc.gnclib.util.ChunkPos3D;
import dicemc.gnclib.util.ComVars;

public interface ILogicRealEstate {
	
	public Map<ChunkPos3D, ChunkData> getCap();
	
	public default String updateChunk(ChunkPos3D ck, Map<String, String> values) {
		//TODO populate switch statement
		ChunkData cd = getCap().getOrDefault(ck, new ChunkData(ck));
		for (Map.Entry<String, String> vals : values.entrySet()) {
			switch(vals.getKey()) {
			case "price": {
				cd.price = Double.valueOf(vals.getValue());
				System.out.println("price updated" +String.valueOf(getCap().get(ck).price));
				break;
			}
			case "player": {
				cd.permittedPlayers.put(UUID.fromString(vals.getValue().substring(0, 36)), vals.getValue().substring(36));
				break;
			}
			case "whitelist": {
				try {
				JsonObject j = JsonParser.parseString(vals.getValue()).getAsJsonObject();
				String wlItem = j.get("item").getAsString();
				WhitelistEntry wlEntry = new WhitelistEntry(j.get("canbreak").getAsBoolean(), j.get("caninteract").getAsBoolean());
				updateWhitelistItem(ck, wlItem, wlEntry);
				} catch (JsonSyntaxException e) {e.printStackTrace();}
				break;
			}
			default: return "Key Unrecognized" + vals.getKey();
			}
		}
		getCap().put(ck, cd);
		return "Success";
	}
	
	public default ChunkData getChunk(ChunkPos3D pos) {return getCap().get(pos);}
	
	public default String setWhitelist(ChunkPos3D ck, Map<String, WhitelistEntry> whitelist) {
		getCap().get(ck).whitelist = whitelist;
		if (whitelist.size() == 0) return "Whitelist Cleared";
		return "Whitelist Set";
	}
	
	public default String updateWhitelistItem(ChunkPos3D ck, String itemRef, WhitelistEntry wlItem) {
		getCap().get(ck).whitelist.put(itemRef, wlItem);
		return "WLItem Added";
	}
	
	public default String removeWhitelistItem(ChunkPos3D ck, String item) {
		WhitelistEntry wle = getCap().get(ck).whitelist.remove(item);
		if (wle == null) return "Item Not Found.";
		return "Whitelist Item Removed";

	}
	
	public default Map<String, WhitelistEntry> getWhitelist(ChunkPos3D ck) {return getCap().get(ck).whitelist;}
	
	public default String addPlayer(ChunkPos3D ck, UUID player, String playerName) {		
		getCap().get(ck).permittedPlayers.put(player, playerName);
		return "Player Added";
	}
	
	public default String removePlayer(ChunkPos3D ck, UUID player) {
		getCap().get(ck).permittedPlayers.remove(player);
		return "Player Removed";
	}
	
	public default Map<UUID, String> getPlayers(ChunkPos3D ck) {return getCap().get(ck).permittedPlayers;}
	
	/*TODO think on these as to how they should would be implemented
	*this has to do with WSDs and how I want to interface with them.
	*I might have to move these to another class.
	*/
	public void saveChunkData();
	public void loadChunkData(ChunkPos3D ck);
	
	//BEGIN GAME LOGIC SECTION
	public default String tempClaim(ChunkPos3D ck, UUID player, String playerName) {
		if (!getCap().get(ck).owner.equals(ComVars.NIL) || !getCap().get(ck).renter.equals(ComVars.NIL)) return "Chunk Already Claimed";
		double balP = LogicMoney.getBalance(player, AccountType.PLAYER.rl);
		if (balP >= getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE) {
			LogicMoney.changeBalance(player, AccountType.PLAYER.rl, (-1 * (getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE)));
			getCap().get(ck).renter = player;
			getCap().get(ck).permittedPlayers.put(player, playerName);
			getCap().get(ck).rentEnd = System.currentTimeMillis() + ConfigCore.TEMPCLAIM_DURATION;
			return "Temp Claim Successful";
		}
		else return "Insufficient funds to claim";
	}
	/**
	 * Used to claim land for a guild.  Method logic checks whether the land can be
	 * claimed as core land or if it should be claimed as an outpost.  If outpost,
	 * the method checks if this is adding to an outpost or creating a new one.
	 * If a new one, the outpost cost is added to the cost.  Additionally, after
	 * a successful claim a recursive method is called to procedurally check each
	 * adjacent chunk to see if it borders core land and update it if it is an outpost.
	 * @param ck the chunk being claimed
	 * @param guild the guild attempting to claim
	 * @return a textual result statement.
	 */
	public default String guildClaim(ChunkPos3D ck, UUID guild, UUID transactor, LogicGuilds guildImpl) {
		if (!getCap().get(ck).owner.equals(ComVars.NIL) && !getCap().get(ck).isForSale) 
			return "Chunk Already Claimed";
		boolean bordersCore = bordersCoreLand(ck, guild);
		Guild gindex = guildImpl.getGuildByID(guild);
		//Verify actor is permitted to perform action
		/*TODO this needs a BIG rework
		if (!bordersCore && gindex.ranks.get(gindex.members.getOrDefault(transactor, ComVars.INV)).sequence < gindex.permissions.get(PermKey.OUTPOST_CREATE))
			return "Rank Permission Inadequate";
		if (bordersCore && gindex.ranks.get(gindex.members.getOrDefault(transactor, ComVars.INV)).sequence < gindex.permissions.get(PermKey.CLAIM_LAND))
			return "Rank Permission Inadequate";*/
		//Verify funds available for transaction
		//TODO was still working through this block.  it looks like it works, but it's garbage.
		double outpostFee = (bordersCore ? 0d : ConfigCore.OUTPOST_CREATE_COST);
		double balG = LogicMoney.getBalance(guild, AccountType.GUILD.rl);
		if (balG >= getCap().get(ck).price + outpostFee) {
			LogicMoney.changeBalance(guild, AccountType.GUILD.rl, (-1 * (getCap().get(ck).price)));
			if (!getCap().get(ck).renter.equals(ComVars.NIL)) {
				LogicMoney.changeBalance(getCap().get(ck).renter, AccountType.PLAYER.rl, getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE);
				getCap().get(ck).renter = ComVars.NIL;
				getCap().get(ck).permittedPlayers = new HashMap<UUID, String>();
			}
			getCap().get(ck).owner = guild;
			getCap().get(ck).leasePrice = -1;
			getCap().get(ck).leaseDuration = 0;
			getCap().get(ck).permMin = 0;
			getCap().get(ck).rentEnd = System.currentTimeMillis();
			getCap().get(ck).isPublic = false;
			getCap().get(ck).isForSale = false;
			getCap().get(ck).canExplode = false;
			getCap().get(ck).whitelist = new HashMap<String, WhitelistEntry>();
			getCap().get(ck).permittedPlayers = new HashMap<UUID, String>();
		}
		else return "Insufficient Guild Funds";
		return "Claim Successful";
	}
	
	/**
	 * Returns a value based on the type of claim.
	 * @param ck the chunk being checked
	 * @param guild the guild whose ownership is being evaluated
	 * @return 0=normal claim, 1=new outpost, 2=outpostclaim
	 */
	default boolean bordersCoreLand(ChunkPos3D ck, UUID guild) {
		return getCap().get(new ChunkPos3D(ck.x-1, ck.y, ck.z)).owner.equals(guild) ||
			getCap().get(new ChunkPos3D(ck.x+1, ck.y, ck.z)).owner.equals(guild) ||
			getCap().get(new ChunkPos3D(ck.x, ck.y, ck.z-1)).owner.equals(guild) ||
			getCap().get(new ChunkPos3D(ck.x, ck.y, ck.z+1)).owner.equals(guild) ||
			getCap().get(new ChunkPos3D(ck.x, ck.y-1, ck.z)).owner.equals(guild) ||
			getCap().get(new ChunkPos3D(ck.x, ck.y+1, ck.z)).owner.equals(guild); 
	}
	
	public default String extendClaim(ChunkPos3D ck, UUID player) {
		double balP = LogicMoney.getBalance(player, AccountType.PLAYER.rl);
		double cost = (getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE * getCap().get(ck).permittedPlayers.size());
		if (balP >= cost) {
			LogicMoney.changeBalance(player, AccountType.PLAYER.rl, (-1 * cost));
			getCap().get(ck).rentEnd += ConfigCore.TEMPCLAIM_DURATION;
			return "Claim Extended";
		}
		return "Insufficient Funds";
	}
	
	public default String publicToggle(ChunkPos3D ck, boolean value) {
		getCap().get(ck).isPublic = value;
		return "Access Updated";
	}
}
