package dicemc.gnclib.util;

import io.netty.buffer.ByteBuf;

public interface IBufferable {
	ByteBuf writeBytes(ByteBuf buf);
	void readBytes(ByteBuf buf);
}
