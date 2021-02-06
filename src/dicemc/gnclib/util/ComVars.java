package dicemc.gnclib.util;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

public class ComVars {
	public static final UUID NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
	public static final String MOD_ID = "gnc";
	
	public static UUID unrepeatedUUIDs(Map<UUID, ? extends Object> map) {
    	UUID out = UUID.randomUUID();
    	if (map.get(out) == null) return out;
    	else return unrepeatedUUIDs(map);
    }
	
	public static final NavigableMap<Long, String> formatMap = new TreeMap<>(); 
	static {
		formatMap.put(1_000L, "K");
		formatMap.put(1_000_000L, "M");
		formatMap.put(1_000_000_000L, "B");
	}
	
	public static String storageCount(int count) {
		DecimalFormat df = new DecimalFormat("##0.#");
		if (count < 0) return "-"+storageCount(-count);
		if (count < 1000) return String.valueOf(count);
		Entry<Long, String> e = formatMap.floorEntry((long)count);
		long divideBy = e.getKey();
		String suffix = e.getValue();
		long truncated = (long)count / (divideBy / 10);
		boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
		return hasDecimal ? df.format(truncated/10d) + suffix : df.format(truncated/10) + suffix;
	}
}
