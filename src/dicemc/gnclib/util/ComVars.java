package dicemc.gnclib.util;

import java.util.Map;
import java.util.UUID;

public class ComVars {
	public static final UUID NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
	public static final String MOD_ID = "gnc";
	
	public static UUID unrepeatedUUIDs(Map<UUID, ? extends Object> map) {
    	UUID out = UUID.randomUUID();
    	if (map.get(out) == null) return out;
    	else return unrepeatedUUIDs(map);
    }
}
