package dicemc.gnclib.realestate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.util.ChunkPos3D;
import dicemc.gnclib.util.ComVars;

public class ChunkData {
	public ChunkPos3D pos;
	public UUID owner = ComVars.NIL;
	public UUID renter = ComVars.NIL;
	public double price = ConfigCore.DEFAULT_LAND_PRICE;
	public double leasePrice = -1;
	public int leaseDuration = 0;
	public int permMin = 0;
	public long rentEnd = System.currentTimeMillis() + ConfigCore.TEMPCLAIM_DURATION;
	public boolean isPublic = false;
	public boolean isForSale = false;
	public boolean canExplode = true;
	//String should be an MC ResourceLocation unless Impl dictates otherwise
	public Map<String, WhitelistEntry> whitelist = new HashMap<String, WhitelistEntry>();
	public Map<UUID, String> permittedPlayers = new HashMap<UUID, String>();
	
	public ChunkData(ChunkPos3D pos) {this.pos = pos;}
}
