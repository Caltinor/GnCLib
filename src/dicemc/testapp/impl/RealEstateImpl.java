package dicemc.testapp.impl;

import java.util.HashMap;
import java.util.Map;

import dicemc.gnclib.realestate.ChunkData;
import dicemc.gnclib.realestate.ILogicRealEstate;
import dicemc.gnclib.util.ChunkPos3D;

public class RealEstateImpl implements ILogicRealEstate{
	Map<ChunkPos3D, ChunkData> cap = new HashMap<ChunkPos3D, ChunkData>();

	@Override
	public Map<ChunkPos3D, ChunkData> getCap() {return cap;}

	@Override
	public void saveChunkData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadChunkData(ChunkPos3D ck) {
		// TODO Auto-generated method stub
		
	}

}
