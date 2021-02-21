package dicemc.gnclib.realestate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.guilds.LogicGuilds;
import dicemc.gnclib.guilds.LogicGuilds.PermKey;
import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.money.LogicMoney.AccountType;
import dicemc.gnclib.trade.LogicTrade;
import dicemc.gnclib.util.ChunkPos3D;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.Duo;
import dicemc.gnclib.util.TranslatableResult;
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.Agent.Type;

public interface ILogicRealEstate {
	public enum RealEstateResult {SUCCESS, FAILURE}
	public enum UpdateType {PRICE, LEASE_PRICE, LEASE_DURATION, PERM_MIN, PUBLIC, FOR_SALE, EXPLODE}
	
	public Map<ChunkPos3D, ChunkData> getCap();
	
	public default TranslatableResult<RealEstateResult> updateChunk(ChunkPos3D ck, Map<UpdateType, String> values) {
		ChunkData cd = getCap().getOrDefault(ck, new ChunkData(ck));
		for (Map.Entry<UpdateType, String> vals : values.entrySet()) {
			switch(vals.getKey()) {
			case PRICE: { cd.price = Double.valueOf(vals.getValue()); break;}
			case LEASE_PRICE: {cd.leasePrice = Double.valueOf(vals.getValue()); break;}
			case LEASE_DURATION: {cd.leaseDuration = Integer.valueOf(vals.getValue()); break;}
			case PERM_MIN: {cd.permMin = Integer.valueOf(vals.getValue()); break;}			
			case PUBLIC: {cd.isPublic = Boolean.valueOf(vals.getValue()); break;}
			case FOR_SALE: {cd.isForSale = Boolean.valueOf(vals.getValue()); break;}
			case EXPLODE: {cd.canExplode = Boolean.valueOf(vals.getValue()); break;}
			default: return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.updatechunk.failure.key");
			}
		}
		getCap().put(ck, cd);
		saveChunkData();
		return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.updatechunk.success");
	}
	
	public default ChunkData getChunk(ChunkPos3D pos) {return getCap().get(pos);}
	
	public default TranslatableResult<RealEstateResult> setWhitelist(ChunkPos3D ck, Map<String, WhitelistEntry> whitelist) {
		getCap().get(ck).whitelist = whitelist;
		saveChunkData();
		if (whitelist.size() == 0) return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.setwhitelist.success.clear");
		return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.setwhitelist.success.set");
	}
	
	public default TranslatableResult<RealEstateResult> updateWhitelistItem(ChunkPos3D ck, String itemRef, WhitelistEntry wlItem) {
		getCap().get(ck).whitelist.put(itemRef, wlItem);
		saveChunkData();
		return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.updatewhitelist.success");
	}
	
	public default TranslatableResult<RealEstateResult> removeWhitelistItem(ChunkPos3D ck, String item) {
		WhitelistEntry wle = getCap().get(ck).whitelist.remove(item);
		saveChunkData();
		if (wle == null) return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.removewhitelistitem.failure.missing");
		return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.removewhitelistitem.success");
	}
	
	public default Map<String, WhitelistEntry> getWhitelist(ChunkPos3D ck) {return getCap().get(ck).whitelist;}
	
	public default TranslatableResult<RealEstateResult> addPlayer(ChunkPos3D ck, Agent agent) {
		for (Agent agents : getCap().get(ck).permittedPlayers) {
			if (agents.equals(agent)) return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.addplayer.failure");
		}
		getCap().get(ck).permittedPlayers.add(agent);
		saveChunkData();
		return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.addplayer.success");
	}
	
	public default TranslatableResult<RealEstateResult> removePlayer(ChunkPos3D ck, Agent agent) {
		int id = -1;
		List<Agent> list = getChunk(ck).permittedPlayers;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(agent)) {id = i; break;}
		}
		if (id >= 0 ) {
			getCap().get(ck).permittedPlayers.remove(id);
			saveChunkData();
			return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.subplayer.success");
		}
		return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.subplayer.failure");
	}
	
	public default List<Agent> getPlayers(ChunkPos3D ck) {return getCap().get(ck).permittedPlayers;}
	
	/**
	 * This method is called by the default methods to indicate data
	 * has been changed and should be saved.  for a WSD implementation
	 * this may be just a call to the WSD to mark dirty
	 */
	public void saveChunkData();
	
	//BEGIN GAME LOGIC SECTION
	public default TranslatableResult<RealEstateResult> tempClaim(ChunkPos3D ck, Agent agent) {
		if (!getCap().get(ck).owner.equals(agent) || !getCap().get(ck).renter.equals(agent)) 
			return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.tempclaim.failure.occupied");
		double balP = LogicMoney.getBalance(agent.refID, LogicMoney.agentType(agent.type));
		if (balP >= getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE) {
			LogicMoney.changeBalance(agent.refID, LogicMoney.agentType(agent.type), (-1 * (getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE)));
			getCap().get(ck).renter = agent;
			getCap().get(ck).permittedPlayers.add(agent);
			getCap().get(ck).rentEnd = System.currentTimeMillis() + ConfigCore.TEMPCLAIM_DURATION;
			saveChunkData();
			return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.tempclaim.success");
		}
		else return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.tempclaim.failure.funds");
	}
	/**
	 * Claims land for the guild.  Checks if the land borders existing land as
	 * a check on permissibility to proceed.  if it does not border guild land
	 * the additional outpost creation cost is applied.
	 * @param ck the chunk being claimed
	 * @param guild the guild attempting to claim
	 * @return a textual result statement.
	 */
	public default TranslatableResult<RealEstateResult> guildClaim(ChunkPos3D ck, Agent agent) {
		Agent guild = agent.type.equals(Type.PLAYER) 
				? LogicGuilds.getGuildByMember(agent.refID).asAgent() 
				: LogicGuilds.getGuildByID(agent.refID).asAgent();
		if (!LogicGuilds.hasPermission(guild.refID, PermKey.CLAIM_LAND.rl, agent.refID))
			return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.guildclaim.failure.permission");
		if (!getCap().get(ck).owner.refID.equals(ComVars.NIL) && !getCap().get(ck).isForSale) 
			return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.guildclaim.failure.occupied");
		boolean bordersCore = bordersCoreLand(ck, guild.refID);
		//Verify funds available for transaction
		double outpostFee = (bordersCore ? 0d : ConfigCore.OUTPOST_CREATE_COST);
		double balG = LogicMoney.getBalance(guild.refID, LogicMoney.agentType(guild.type));
		if (balG >= getCap().get(ck).price + outpostFee) {
			LogicMoney.changeBalance(guild.refID, LogicMoney.agentType(guild.type), -(getCap().get(ck).price + outpostFee));
			if (!getCap().get(ck).renter.refID.equals(ComVars.NIL)) {
				Agent renter = getCap().get(ck).renter;
				LogicMoney.changeBalance(renter.refID, LogicMoney.agentType(renter.type), getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE);
				getCap().get(ck).renter = new Agent();
				getCap().get(ck).permittedPlayers = new ArrayList<Agent>();
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
			getCap().get(ck).permittedPlayers = new ArrayList<Agent>();
		}
		else return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.guildclaim.failure.funds");
		return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.guildclaim.success");
	}
	
	/**
	 * Returns a value based on the type of claim.
	 * @param ck the chunk being checked
	 * @param guild the guild whose ownership is being evaluated
	 * @return 0=normal claim, 1=new outpost, 2=outpostclaim
	 */
	default boolean bordersCoreLand(ChunkPos3D ck, UUID guild) {
		Agent aguild = LogicTrade.get().getTransactor(guild, Type.GUILD, LogicGuilds.getGuildByID(guild).name);
		return getCap().get(new ChunkPos3D(ck.x-1, ck.y, ck.z)).owner.equals(aguild) ||
			getCap().get(new ChunkPos3D(ck.x+1, ck.y, ck.z)).owner.equals(aguild) ||
			getCap().get(new ChunkPos3D(ck.x, ck.y, ck.z-1)).owner.equals(aguild) ||
			getCap().get(new ChunkPos3D(ck.x, ck.y, ck.z+1)).owner.equals(aguild) ||
			getCap().get(new ChunkPos3D(ck.x, ck.y-1, ck.z)).owner.equals(aguild) ||
			getCap().get(new ChunkPos3D(ck.x, ck.y+1, ck.z)).owner.equals(aguild); 
	}
	
	public default TranslatableResult<RealEstateResult> extendClaim(ChunkPos3D ck, UUID player) {
		double balP = LogicMoney.getBalance(player, AccountType.PLAYER.rl);
		double cost = (getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE * getCap().get(ck).permittedPlayers.size());
		if (balP >= cost) {
			LogicMoney.changeBalance(player, AccountType.PLAYER.rl, (-1 * cost));
			getCap().get(ck).rentEnd += ConfigCore.TEMPCLAIM_DURATION;
			return new TranslatableResult<RealEstateResult>(RealEstateResult.SUCCESS, "lib.realestate.extendclaim.success");
		}
		return new TranslatableResult<RealEstateResult>(RealEstateResult.FAILURE, "lib.realestate.extendclaim.failure");
	}
	
	public default Map<UUID, Duo<Integer, Double>> getTaxData() {
		Map<UUID, Duo<Integer, Double>> count = new HashMap<UUID, Duo<Integer, Double>>();
		for (Map.Entry<ChunkPos3D, ChunkData> cap : getCap().entrySet()) {
			if (cap.getValue().owner.refID.equals(ComVars.NIL)) continue;
			Duo<Integer, Double> d = count.getOrDefault(cap.getValue().owner, new Duo<Integer, Double>(0, 0d));
			d.setL(d.getL() + 1);
			d.setR(d.getR() + cap.getValue().price);
			count.put(cap.getValue().owner.refID, d);
		}
		return count;
	}
}
