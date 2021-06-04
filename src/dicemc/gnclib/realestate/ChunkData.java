package dicemc.gnclib.realestate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.util.Agent;
import dicemc.gnclib.util.ChunkPos3D;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class ChunkData implements IBufferable{
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
	
	public static ChunkData getPlaceholder() {return new ChunkData(new ChunkPos3D(0,0,0));}

	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeBytes(pos.writeBytes(buf));
		buf.writeBytes(owner.writeBytes(buf));
		buf.writeBytes(renter.writeBytes(buf));
		buf.writeDouble(price);
		buf.writeDouble(leasePrice);
		buf.writeInt(leaseDuration);
		buf.writeInt(permMin);
		buf.writeLong(rentEnd);
		buf.writeBoolean(isPublic);
		buf.writeBoolean(isForSale);
		buf.writeBoolean(canExplode);
		buf.writeInt(whitelist.size());
		for (Map.Entry<String, WhitelistEntry> map : whitelist.entrySet()) {
			buf.writeInt(map.getKey().length());
			buf.writeCharSequence(map.getKey(), Charset.defaultCharset());
			buf.writeBytes(map.getValue().writeBytes(buf));
		}
		buf.writeInt(permittedPlayers.size());
		for (int i = 0; i < permittedPlayers.size(); i++) {
			buf.writeBytes(permittedPlayers.get(i).writeBytes(buf));
		}
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		pos.readBytes(buf);
		owner.readBytes(buf);
		renter.readBytes(buf);
		price = buf.readDouble();
		leasePrice = buf.readDouble();
		leaseDuration = buf.readInt();
		permMin = buf.readInt();
		rentEnd = buf.readLong();
		isPublic = buf.readBoolean();
		isForSale = buf.readBoolean();
		canExplode = buf.readBoolean();
		int len = buf.readInt();
		for (int i = 0; i < len; i++) {
			int slen = buf.readInt();
			String key = buf.readCharSequence(slen, Charset.defaultCharset()).toString();
			WhitelistEntry wle = new WhitelistEntry();
			wle.readBytes(buf);
			whitelist.put(key, wle);
		}
		len = buf.readInt();
		for (int i = 0; i < len; i++) {
			Agent a = new Agent();
			a.readBytes(buf);
			permittedPlayers.add(a);
		}
	}
}
