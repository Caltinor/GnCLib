package dicemc.testapp;

import java.nio.charset.Charset;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GnCLibConsole {
	public static UUID id = UUID.randomUUID();
	
	public static void main(String []args) {
		System.out.println(id.toString());
		ByteBuf buffer = write(Unpooled.buffer());
    	read(buffer);
    	System.out.println(id.toString());
		//javax.swing.SwingUtilities.invokeLater(new Runnable() {public void run() {ScreenRoot.openGUI();}});
	}
	
	private static ByteBuf write(ByteBuf buf) {
		buf.writeInt(id.toString().length());
		buf.writeCharSequence(id.toString(), Charset.defaultCharset());
		return buf;
	}
	
	private static void read(ByteBuf buf) {
		int len = buf.readInt();
		id = UUID.fromString(buf.readCharSequence(len, Charset.defaultCharset()).toString());
	}
	
}
