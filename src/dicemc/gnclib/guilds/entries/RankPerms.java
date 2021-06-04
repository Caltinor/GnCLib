package dicemc.gnclib.guilds.entries;

import java.nio.charset.Charset;
import java.util.UUID;

import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.IBufferable;
import io.netty.buffer.ByteBuf;

public class RankPerms implements IBufferable{
	public int id;
	public UUID guildID, player;
	public String key;
	public int rank;
	public boolean cascades;
	
	public RankPerms(int id, UUID guildID, String key, UUID player , int rank, boolean cascades) {
		this.id = id;
		this.guildID = guildID;
		this.player = player;
		this.key = key;
		this.rank = rank;
		this.cascades = cascades;
	}
	public RankPerms(UUID guildID, String key, UUID player, int rank, boolean cascades) {
		this(0, guildID, key, player, rank, cascades);
	}	
	public RankPerms(UUID guildID, String key, UUID player) {
		this(0, guildID, key, player, -2, false);
	}
	public RankPerms(UUID guildID, String key, int rank, boolean cascades) {
		this(0, guildID, key, ComVars.NIL, rank, cascades);
	}
	
	public boolean matches(RankPerms other) {
		return guildID.equals(other.guildID) && player.equals(other.player) && key.equalsIgnoreCase(other.key) && rank == other.rank;
	}
	
	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(rank);
		buf.writeBoolean(cascades);
		buf.writeInt(guildID.toString().length());
		buf.writeCharSequence(guildID.toString(), Charset.defaultCharset());
		buf.writeInt(player.toString().length());
		buf.writeCharSequence(player.toString(), Charset.defaultCharset());
		buf.writeInt(key.length());
		buf.writeCharSequence(key, Charset.defaultCharset());
		return buf;
	}
	@Override
	public void readBytes(ByteBuf buf) {
		id = buf.readInt();
		rank = buf.readInt();
		cascades= buf.readBoolean();
		int len = buf.readInt();
		guildID = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		len = buf.readInt();
		player = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
		len = buf.readInt();
		key = buf.readCharSequence(len, Charset.defaultCharset()).toString();
	}
}
