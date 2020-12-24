package dicemc.gnclib.realestate.items;

import java.util.Map;

import dicemc.gnclib.realestate.WhitelistEntry;

public interface IDefaultWhitelister {
	Map<String, WhitelistEntry> getWhitelist(Object stack);
	void setWhitelister(Object stack, Map<String, WhitelistEntry> whitelist);
	void addToWhitelister(Object stack, String item, WhitelistEntry.UpdateType type);
}
