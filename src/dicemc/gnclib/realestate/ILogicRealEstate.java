package dicemc.gnclib.realestate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.guilds.Guild.permKey;
import dicemc.gnclib.guilds.LogicGuilds;
import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.util.ChunkPos3D;
import dicemc.gnclib.util.ComVars;

public interface LogicRealEstate {
	Map<ChunkPos3D, ChunkData> cap = new HashMap<ChunkPos3D, ChunkData>();
	
	public default String updateChunk(ChunkPos3D ck, Map<String, String> values) {
		//TODO populate switch statement
		ChunkData cd = cap.getOrDefault(ck, new ChunkData(ck));
		for (Map.Entry<String, String> vals : values.entrySet()) {
			switch(vals.getKey()) {
			case "price": {
				cd.price = Double.valueOf(vals.getValue());
				System.out.println("price updated" +String.valueOf(cap.get(ck).price));
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
				WhitelistEntry wlEntry = new WhitelistEntry(j.get("isentity").getAsBoolean(), j.get("canbreak").getAsBoolean(), j.get("caninteract").getAsBoolean());
				updateWhitelistItem(ck, wlItem, wlEntry);
				} catch (JsonSyntaxException e) {e.printStackTrace();}
				break;
			}
			default: return "Key Unrecognized" + vals.getKey();
			}
		}
		cap.put(ck, cd);
		return "Success";
	}
	
	public default ChunkData getChunk(ChunkPos3D pos) {return cap.get(pos);}
	
	public default String setWhitelist(ChunkPos3D ck, Map<String, WhitelistEntry> whitelist) {
		cap.get(ck).whitelist = whitelist;
		if (whitelist.size() == 0) return "Whitelist Cleared";
		return "Whitelist Set";
	}
	
	public default String updateWhitelistItem(ChunkPos3D ck, String itemRef, WhitelistEntry wlItem) {
		cap.get(ck).whitelist.put(itemRef, wlItem);
		return "WLItem Added";
	}
	
	public default String removeWhitelistItem(ChunkPos3D ck, String item) {
		WhitelistEntry wle = cap.get(ck).whitelist.remove(item);
		if (wle == null) return "Item Not Found.";
		return "Whitelist Item Removed";

	}
	
	public default Map<String, WhitelistEntry> getWhitelist(ChunkPos3D ck) {return cap.get(ck).whitelist;}
	
	public default String addPlayer(ChunkPos3D ck, UUID player, String playerName) {		
		cap.get(ck).permittedPlayers.put(player, playerName);
		return "Player Added";
	}
	
	public default String removePlayer(ChunkPos3D ck, UUID player) {
		cap.get(ck).permittedPlayers.remove(player);
		return "Player Removed";
	}
	
	public default Map<UUID, String> getPlayers(ChunkPos3D ck) {return cap.get(ck).permittedPlayers;}
	
	/*TODO think on these as to how they should would be implemented
	*this has to do with WSDs and how I want to interface with them.
	*I might have to move these to another class.
	*/
	public void saveChunkData();
	public void loadChunkData(ChunkPos3D ck);
	
	//BEGIN GAME LOGIC SECTION
	public static String tempClaim(ChunkPos3D ck, UUID player, String playerName) {
		if (!cap.get(ck).owner.equals(ComVars.NIL) || !cap.get(ck).renter.equals(ComVars.NIL)) return "Chunk Already Claimed";
		double balP = LogicMoney.getBalance(player);
		if (balP >= cap.get(ck).price * ConfigCore.TEMPCLAIM_RATE) {
			LogicMoney.changeBalance(player, (-1 * (cap.get(ck).price * ConfigCore.TEMPCLAIM_RATE)));
			cap.get(ck).renter = player;
			cap.get(ck).permittedPlayers.put(player, playerName);
			cap.get(ck).rentEnd = System.currentTimeMillis() + ConfigCore.TEMPCLAIM_DURATION;
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
	public static String guildClaim(ChunkPos3D ck, UUID guild) {
		double outpostFee = (bordersCoreLand(ck, guild) ? 0d : ConfigCore.OUTPOST_CREATE_COST);
		if (!cap.get(ck).owner.equals(ComVars.NIL)) return "Chunk Already Claimed";
		double balG = LogicMoney.getBalance(guild);
		if (balG >= cap.get(ck).price + outpostFee) {
			LogicMoney.changeBalance(guild, (-1 * (cap.get(ck).price)));
			if (!cap.get(ck).renter.equals(ComVars.NIL)) {
				LogicMoney.changeBalance(cap.get(ck).renter, cap.get(ck).price * ConfigCore.TEMPCLAIM_RATE);
				cap.get(ck).renter = ComVars.NIL;
				cap.get(ck).permittedPlayers = new HashMap<UUID, String>();
			}
			cap.get(ck).owner = guild;
			cap.get(ck).leasePrice = -1;
			cap.get(ck).leaseDuration = 0;
			cap.get(ck).permMin = (outpostFee > 0 ? LogicGuilds.getGuildByID(guild).permissions.get(permKey.OUTPOST_CREATE) : LogicGuilds.getGuildByID(guild).permissions.get(permKey.CORE_CLAIM));
			cap.get(ck).rentEnd = System.currentTimeMillis();
			cap.get(ck).isPublic = false;
			cap.get(ck).isForSale = false;
			cap.get(ck).canExplode = false;
			cap.get(ck).whitelist = new HashMap<String, WhitelistEntry>();
			cap.get(ck).permittedPlayers = new HashMap<UUID, String>();
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
	static boolean bordersCoreLand(ChunkPos3D ck, UUID guild) {
		return cap.get(new ChunkPos3D(ck.x-1, ck.y, ck.z)).owner.equals(guild) ||
			cap.get(new ChunkPos3D(ck.x+1, ck.y, ck.z)).owner.equals(guild) ||
			cap.get(new ChunkPos3D(ck.x, ck.y, ck.z-1)).owner.equals(guild) ||
			cap.get(new ChunkPos3D(ck.x, ck.y, ck.z+1)).owner.equals(guild) ||
			cap.get(new ChunkPos3D(ck.x, ck.y-1, ck.z)).owner.equals(guild) ||
			cap.get(new ChunkPos3D(ck.x, ck.y+1, ck.z)).owner.equals(guild); 
	}
	
	public default String extendClaim(ChunkPos3D ck, UUID player) {
		double balP = LogicMoney.getBalance(player);
		double cost = (cap.get(ck).price * ConfigCore.TEMPCLAIM_RATE * cap.get(ck).permittedPlayers.size());
		if (balP >= cost) {
			LogicMoney.changeBalance(player, (-1 * cost));
			cap.get(ck).rentEnd += ConfigCore.TEMPCLAIM_DURATION;
			return "Claim Extended";
		}
		return "Insufficient Funds";
	}
	
	public default String publicToggle(ChunkPos3D ck, boolean value) {
		cap.get(ck).isPublic = value;
		return "Access Updated";
	}
}
