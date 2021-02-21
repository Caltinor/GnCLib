package dicemc.gnclib.realestate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.ChunkPos3D;

public class ChunkData {
	public ChunkPos3D pos;
	public Agent owner = new Agent();
	public Agent renter = new Agent();
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
	public List<Agent> permittedPlayers = new ArrayList<Agent>();
	
	public ChunkData(ChunkPos3D pos) {this.pos = pos;}
}
