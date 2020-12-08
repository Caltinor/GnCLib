package dicemc.gnclib.util;

public class ChunkPos3D {
	public int x, y, z;
	
	public ChunkPos3D(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public ChunkPos3D(long pos) {
		//TODO complete bitwise conversionS
	}
	
	public long toLong() {
		//TODO flesh out
		return 1L;
	}
	
	public static int chunkYFromAltitude(int altitude) {return altitude/16;}
}
