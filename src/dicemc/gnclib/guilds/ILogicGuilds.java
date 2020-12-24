package dicemc.gnclib.guilds;

import java.util.Map;
import java.util.UUID;

public interface ILogicGuilds {
	
	public Map<UUID, Guild> getGuilds();

	public default Guild getGuildByID(UUID guild) {
		// TODO Auto-generated method stub
		return null;
	}

}
