package dicemc.gnclib.util;

import io.netty.buffer.ByteBuf;

public class ChunkPos3D implements IBufferable{
	public int x, y, z;
	private static final int xSize = 1 + doMath(smallestPower(30000000));
	private static final int zSize = xSize;
	private static final int ySize = 64 - xSize - zSize;
	private static final long xComp = (1L << xSize) - 1L;
	private static final long yComp = (1L << ySize) - 1L;
	private static final long zComp = (1L << zSize) - 1L;
	private static final int zInverseStart = ySize;
	private static final int xInverseStart = ySize + zSize;
	
	public ChunkPos3D(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public ChunkPos3D(long pos) {
		this.x = (int)(pos << 64 - xInverseStart - xSize >> 64 - xSize);
		this.y = (int)(pos << 64 - ySize >> 64 - ySize);
		this.z = (int)(pos << 64 - zInverseStart - zSize >> 64 - zSize);
	}
	
	public long toLong() {
		long i = 0L;
	    i = i | ((long)x & xComp) << xInverseStart;
	    i = i | ((long)y & yComp) << 0;
	    return i | ((long)z & zComp) << zInverseStart;
	}
	
	public static int chunkYFromAltitude(int altitude) {return altitude/16;}
	
	private static int doMath(int value) {
		boolean powerOfTwo = true;
		int innerValue = value;
		if (!(value != 0 && (value & value - 1) == 0)) {
		     innerValue =  smallestPower(value);
		     powerOfTwo = false;
		}
		int[] posArray = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
		int bitPos = posArray[(int)((long)innerValue * 125613361L >> 27) & 31];
		return bitPos - (powerOfTwo ? 0 : 1);
	}
	
	private static int smallestPower(int value) {
		 int i = value - 1;
	     i = i | i >> 1;
	     i = i | i >> 2;
	     i = i | i >> 4;
	     i = i | i >> 8;
	     i = i | i >> 16;
	     return  i + 1;
	}

	@Override
	public ByteBuf writeBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		return buf;
	}

	@Override
	public void readBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}
}
