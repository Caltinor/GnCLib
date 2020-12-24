package dicemc.testapp;

import java.util.HashMap;
import java.util.Map;

import dicemc.gnclib.realestate.WhitelistEntry;

public class GnCLibConsole {
	public static void main(String []args){
		Map<String, WhitelistEntry> whitelist = new HashMap<String, WhitelistEntry>();
		whitelist.put("block", new WhitelistEntry(true, true));
		whitelist.computeIfAbsent("block", key -> new WhitelistEntry()).setCanBreak(false);
		System.out.println(String.valueOf(whitelist.get("block").getCanBreak())+":"+
				String.valueOf(whitelist.get("block").getCanInteract()));
	}
	
}
