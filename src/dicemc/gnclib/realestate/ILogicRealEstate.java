package dicemc.gnclib.realestate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.guilds.LogicGuilds;
import dicemc.gnclib.guilds.LogicGuilds.PermKey;
import dicemc.gnclib.guilds.entries.Guild;
import dicemc.gnclib.money.LogicMoney;
import dicemc.gnclib.money.LogicMoney.AccountType;
import dicemc.gnclib.trade.LogicTrade;
import dicemc.gnclib.util.ChunkPos3D;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.Duo;
import dicemc.gnclib.util.ResultType;
import dicemc.gnclib.util.TranslatableResult;
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.Agent.Type;

public interface ILogicRealEstate {
	
	public Map<ChunkPos3D, ChunkData> getCap();
	
	public default TranslatableResult<ResultType> setWhitelist(ChunkPos3D ck, Map<String, WhitelistEntry> whitelist) {
		getCap().get(ck).whitelist = whitelist;
		saveChunkData();
		if (whitelist.size() == 0) return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.setwhitelist.success.clear");
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.setwhitelist.success.set");
	}
	
	public default TranslatableResult<ResultType> clearWhitelist(ChunkPos3D ck) {
		return setWhitelist(ck, new HashMap<String, WhitelistEntry>());
	}
	
	public default TranslatableResult<ResultType> updateWhitelistItem(ChunkPos3D ck, String itemRef, WhitelistEntry wlItem) {
		getCap().get(ck).whitelist.put(itemRef, wlItem);
		saveChunkData();
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.updatewhitelist.success");
	}
	
	public default TranslatableResult<ResultType> removeWhitelistItem(ChunkPos3D ck, String item) {
		WhitelistEntry wle = getCap().get(ck).whitelist.remove(item);
		saveChunkData();
		if (wle == null) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.removewhitelistitem.failure.missing");
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.removewhitelistitem.success");
	}
	
	public default Map<String, WhitelistEntry> getWhitelist(ChunkPos3D ck) {return getCap().get(ck).whitelist;}
	
	public default TranslatableResult<ResultType> addPlayer(ChunkPos3D ck, Agent agent) {
		for (Agent agents : getCap().get(ck).permittedPlayers) {
			if (agents.equals(agent)) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.addplayer.failure");
		}
		getCap().get(ck).permittedPlayers.add(agent);
		saveChunkData();
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.addplayer.success");
	}
	
	public default TranslatableResult<ResultType> removePlayer(ChunkPos3D ck, Agent agent) {
		int id = -1;
		List<Agent> list = getCap().get(ck).permittedPlayers;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(agent)) {id = i; break;}
		}
		if (id >= 0 ) {
			getCap().get(ck).permittedPlayers.remove(id);
			saveChunkData();
			return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.subplayer.success");
		}
		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.subplayer.failure");
	}
	
	public default List<Agent> getPlayers(ChunkPos3D ck) {return getCap().get(ck).permittedPlayers;}
	
	public default TranslatableResult<ResultType> expireSublet(ChunkPos3D ck) {
		getCap().get(ck).renter = new Agent();
		getCap().get(ck).permittedPlayers = new ArrayList<Agent>();
		getCap().get(ck).isPublic = false;
		getCap().get(ck).canExplode = false;
		saveChunkData();
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.expiresublet.success");
	}
	
	/**
	 * This method is called by the default methods to indicate data
	 * has been changed and should be saved.  for a WSD implementation
	 * this may be just a call to the WSD to mark dirty
	 */
	public void saveChunkData();
	
	//BEGIN GAME LOGIC SECTION
	public default TranslatableResult<ResultType> tempClaim(ChunkPos3D ck, Agent agent) {
		if (!getCap().get(ck).owner.refID.equals(ComVars.NIL) || !getCap().get(ck).renter.refID.equals(ComVars.NIL)) 
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.tempclaim.failure.occupied");
		double balP = LogicMoney.getBalance(agent.refID, LogicMoney.agentType(agent.type));
		if (balP >= getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE) {
			LogicMoney.changeBalance(agent.refID, LogicMoney.agentType(agent.type), (-1 * (getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE)));
			getCap().get(ck).renter = agent;
			getCap().get(ck).permittedPlayers.add(agent);
			getCap().get(ck).rentEnd = System.currentTimeMillis() + ConfigCore.TEMPCLAIM_DURATION;
			saveChunkData();
			return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.tempclaim.success");
		}
		else return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.tempclaim.failure.funds");
	}
	/**
	 * Claims land for the guild.  Checks if the land borders existing land as
	 * a check on permissibility to proceed.  if it does not border guild land
	 * the additional outpost creation cost is applied.
	 * @param ck the chunk being claimed
	 * @param guild the guild attempting to claim
	 * @return a textual result statement.
	 */
	public default TranslatableResult<ResultType> guildClaim(ChunkPos3D ck, Agent agent) {
		Guild guild = LogicGuilds.getGuildByMember(agent.refID);
		if (!LogicGuilds.hasPermission(guild.guildID, PermKey.CLAIM_LAND.rl, agent.refID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.guildclaim.failure.permission");
		if (!getCap().get(ck).owner.refID.equals(ComVars.NIL) && !getCap().get(ck).isForSale) 
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.guildclaim.failure.occupied");
		boolean bordersCore = bordersCoreLand(ck, guild.guildID);
		//Verify funds available for transaction
		double outpostFee = (bordersCore ? 0d : ConfigCore.OUTPOST_CREATE_COST);
		double balG = LogicMoney.getBalance(guild.guildID, LogicMoney.agentType(Type.GUILD));
		if (balG >= getCap().get(ck).price + outpostFee) {
			//check if this is a guild to guild sale or purchase from the server
			if (getCap().get(ck).isForSale && !getCap().get(ck).owner.refID.equals(ComVars.NIL))
				LogicMoney.transferFunds(guild.guildID, LogicMoney.agentType(Type.GUILD)
						, getCap().get(ck).owner.refID, LogicMoney.agentType(Type.GUILD), getCap().get(ck).price + outpostFee);
			else LogicMoney.changeBalance(guild.guildID, LogicMoney.agentType(Type.GUILD), -(getCap().get(ck).price + outpostFee));
			//determine if this is a tempclaim refund otherise leave rental status as is
			if (!getCap().get(ck).renter.refID.equals(ComVars.NIL) && getCap().get(ck).owner.refID.equals(ComVars.NIL)) {
				Agent renter = getCap().get(ck).renter;
				LogicMoney.changeBalance(renter.refID, LogicMoney.agentType(renter.type), getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE);
				getCap().get(ck).renter = new Agent();
				getCap().get(ck).rentEnd = System.currentTimeMillis();
				getCap().get(ck).leasePrice = -1;
				getCap().get(ck).leaseDuration = 0;
				getCap().get(ck).permittedPlayers = new ArrayList<Agent>();
				getCap().get(ck).whitelist = new HashMap<String, WhitelistEntry>();
			}
			getCap().get(ck).owner = guild.asAgent();			
			getCap().get(ck).permMin = 0;			
			getCap().get(ck).isPublic = false;
			getCap().get(ck).isForSale = false;
			getCap().get(ck).canExplode = false;
			saveChunkData();
		}
		else return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.guildclaim.failure.funds");
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.guildclaim.success");
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
	
	public default TranslatableResult<ResultType> extendTemporaryTime(ChunkPos3D ck, UUID player) {
		boolean isSublet = !getCap().get(ck).owner.refID.equals(ComVars.NIL);
		double balP = LogicMoney.getBalance(player, AccountType.PLAYER.rl);
		double cost = (isSublet 
				? (getCap().get(ck).leasePrice)
				: (getCap().get(ck).price * ConfigCore.TEMPCLAIM_RATE)) 
				*getCap().get(ck).permittedPlayers.size();
		if (balP >= cost) {
			LogicMoney.changeBalance(player, AccountType.PLAYER.rl, -cost);
			getCap().get(ck).rentEnd += isSublet ? getCap().get(ck).leaseDuration * 3600000l : ConfigCore.TEMPCLAIM_DURATION;
			saveChunkData();
			return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.extendclaim.success");
		}
		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.extendclaim.failure");
	}
	
	public default TranslatableResult<ResultType> setSubletData(ChunkPos3D ck, Agent agent, double price, int duration) {
		Guild gid = LogicGuilds.getGuildByID(getCap().get(ck).owner.refID);
		Agent guild = LogicTrade.get().getTransactor(gid.guildID, Type.GUILD, gid.name);
		if (!LogicGuilds.hasPermission(guild.refID, PermKey.SUBLET_MANAGE.rl, agent.refID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.updatesublet.failure.permission");
		if (price >= 0 && !getCap().get(ck).renter.refID.equals(ComVars.NIL))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.updatesublet.failure.contracted");
		getCap().get(ck).leasePrice = price;
		getCap().get(ck).leaseDuration = duration;
		saveChunkData();
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.updatesublet.success");
	}
	
	public default TranslatableResult<ResultType> disableSublet(ChunkPos3D ck, Agent agent) {
		long gracePeriod = (long)(getCap().get(ck).leaseDuration * 3600000 * ConfigCore.TENANT_PROTECTION_RATIO);
		long expiration = getCap().get(ck).rentEnd;
		if (expiration > System.currentTimeMillis() && System.currentTimeMillis() > (expiration - gracePeriod))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.disablesublet.failure.protected");
		return setSubletData(ck, agent, -1, 0);
	}	
	
	public default TranslatableResult<ResultType> rentSublet(ChunkPos3D ck, Agent agent) {
		if (!getCap().get(ck).renter.refID.equals(ComVars.NIL))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.expiresublet.failure.occupied");
		double pbal = LogicMoney.getBalance(agent.refID, LogicMoney.agentType(agent.type));
		if (pbal < getCap().get(ck).leasePrice)
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.expiresublet.failure.funds");
		LogicMoney.changeBalance(agent.refID, LogicMoney.agentType(agent.type), -getCap().get(ck).leasePrice);
		getCap().get(ck).renter = agent;
		getCap().get(ck).rentEnd = System.currentTimeMillis() + ((long)getCap().get(ck).leaseDuration * 3600000l);
		addPlayer(ck, agent); //save executed in this method. no need to duplicate
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.rentsublet.success");
	}
	
	public default TranslatableResult<ResultType> addPermittedPlayer(ChunkPos3D ck, Agent exec, Agent ref) {
		boolean isPermitted = (getCap().get(ck).renter.equals(exec) ||
				LogicGuilds.hasPermission(getCap().get(ck).owner.refID, PermKey.SUBLET_MANAGE.rl, exec.refID));
		if (!isPermitted) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.addsubletmembers.failure.permission");
		return addPlayer(ck, ref);
	}
	
	public default TranslatableResult<ResultType> removePermittedPlayer(ChunkPos3D ck, Agent exec, Agent ref) {
		boolean isPermitted = (getCap().get(ck).renter.equals(exec) ||
				LogicGuilds.hasPermission(getCap().get(ck).owner.refID, PermKey.SUBLET_MANAGE.rl, exec.refID));
		if (!isPermitted) return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.removesubletmembers.failure.permission");
		return removePlayer(ck, ref);
	}
	
	public default TranslatableResult<ResultType> setPublic(ChunkPos3D ck, Agent exec, boolean setting) {
		if (LogicGuilds.hasPermission(LogicGuilds.getGuildByMember(exec.refID).guildID, PermKey.SUBLET_MANAGE.rl, exec.refID)) {
			getCap().get(ck).isPublic = setting;
			saveChunkData();
			return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.setpublic.success");
		}
		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.setpublic.success");
	}
	
	public default TranslatableResult<ResultType> setExplosions(ChunkPos3D ck, Agent exec, boolean setting) {
		if (LogicGuilds.hasPermission(LogicGuilds.getGuildByMember(exec.refID).guildID, PermKey.SUBLET_MANAGE.rl, exec.refID)) {
			getCap().get(ck).canExplode = setting;
			saveChunkData();
			return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.setexplode.success");
		}
		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.setexplode.success");
	}
	
	public default TranslatableResult<ResultType> setMinRank(ChunkPos3D ck, Agent exec, int setting) {
		if (LogicGuilds.hasPermission(LogicGuilds.getGuildByMember(exec.refID).guildID, PermKey.SUBLET_MANAGE.rl, exec.refID)) {
			getCap().get(ck).permMin = setting;
			saveChunkData();
			return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.setminrank.success");
		}
		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.setminrank.success");
	}
	
	public default TranslatableResult<ResultType> setChunkForSale(ChunkPos3D ck, Agent exec, double value) {
		if (!LogicGuilds.hasPermission(LogicGuilds.getGuildByMember(exec.refID).guildID, PermKey.CLAIM_SELL.rl, exec.refID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.setforsale.failure.permission");
		getCap().get(ck).isForSale = true;
		getCap().get(ck).price = value;
		return new TranslatableResult<ResultType>(ResultType.SUCCESS, "lib.realestate.setforsale.success");
	}
	
	public default TranslatableResult<ResultType> abandonChunk(ChunkPos3D ck, Agent exec) {
		if (!LogicGuilds.hasPermission(LogicGuilds.getGuildByMember(exec.refID).guildID, PermKey.CLAIM_ABANDON.rl, exec.refID))
			return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.abandon.failure.permission");
		getCap().get(ck).owner = new Agent();
		getCap().get(ck).leasePrice = -1;
		getCap().get(ck).leaseDuration = 0;
		getCap().get(ck).permMin = 0;
		getCap().get(ck).isPublic = false;
		getCap().get(ck).isForSale = false;
		getCap().get(ck).canExplode = false;
		getCap().get(ck).whitelist = new HashMap<String, WhitelistEntry>();
		return new TranslatableResult<ResultType>(ResultType.FAILURE, "lib.realestate.abandon.success");
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
