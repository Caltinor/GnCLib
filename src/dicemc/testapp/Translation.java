package dicemc.testapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Translation {
	private String output;
	public static Map<String, String> translationMap = readFromFile();

	public Translation(String key) {
		output = translationMap.get(key);
	}
	
	public String print() {
		return output;
	}
	
	private static Map<String, String> readFromFile() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(Translation.class.getResourceAsStream("/dicemc/gnclib/util/translations/en_us.lang")));
		Map<String, String> map = new HashMap<String, String>();
		reader.lines().forEach((e) -> {
			if (e.indexOf("#") == 0) return;
			if (e.length() == 0) return;
			int splitter = e.indexOf("=");
			String key = e.substring(0, splitter);
			String value = e.substring(splitter + 1, e.length());
			map.put(key, value);
		});
		return map;
	}
}
