package dicemc.testapp;

import java.util.UUID;

import dicemc.gnclib.realestate.ChunkData;
import dicemc.gnclib.util.ChunkPos3D;
import dicemc.testapp.impl.ConfigSrc;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GnCLibConsole {
	public static UUID id = UUID.randomUUID();
	public static String testPlayerName = "Rumm";
	public static UUID testPlayer;	
	
	public static void main(String []args) {
		/*Map<String, RealEstateImpl> wMgr = new HashMap<String, RealEstateImpl>();
		wMgr.put("Overworld", new RealEstateImpl());
		wMgr.put("Nether", new RealEstateImpl());
		wMgr.put("End", new RealEstateImpl());*/
		ConfigSrc.init();
		RunVars.init();
		testPlayer = RunVars.getPlayerByName(testPlayerName);
		ByteBuf buf = Unpooled.buffer();
		ChunkData data = new ChunkData(new ChunkPos3D(0,0,0));
		System.out.println(data.pos.x);
		buf.writeBytes(data.writeBytes(buf));
		ChunkData outData = new ChunkData(new ChunkPos3D(1,1,1));
		System.out.println(outData.pos.x);
		outData.readBytes(buf);
		System.out.println(outData.pos.x);
		Menu.main();
	}
	
}
