package dicemc.gnclib.protection;

import java.util.UUID;

import dicemc.gnclib.configs.ConfigCore;
import dicemc.gnclib.guilds.LogicGuilds;
import dicemc.gnclib.guilds.LogicGuilds.PermKey;
import dicemc.gnclib.realestate.ChunkData;
import dicemc.gnclib.realestate.WhitelistEntry;
import dicemc.gnclib.realestate.items.IDefaultWhitelister;
import dicemc.gnclib.realestate.items.IWhitelister;
import dicemc.gnclib.util.ComVars;
import dicemc.gnclib.util.TranslatableResult;

public class LogicProtection {
	public static enum ActionType {BREAK, PLACE, INTERACT}
	public static enum MatchType {FULL, DENY, WHITELIST}
	enum PlayerType {UNSET, ULNM, LNM, LM, HRM, LRM}
	//elongated: UnListed Non-Member, Listed Non-Member, Listed Member, High Rank Member, Low Rank Member
	public static enum ResultType {TRUE, FALSE, PACKET}
	
	/* NOTES FOR IMPLEMENTATION
	 * 
	 * 
	 */
	
	public static MatchType ownerMatch(UUID player, ChunkData data) {
		//return full perms if the claim is public
		if (data.isPublic) return MatchType.FULL;
		//return full perms if the player is the tempclaim owner
		if (data.owner.equals(ComVars.NIL) && data.renter.equals(player)) return MatchType.FULL;
		//return full perms if the player is an added member but not the tempclaim owner
		if (data.owner.equals(ComVars.NIL) && !data.renter.equals(ComVars.NIL)) {
			if (data.permittedPlayers.containsKey(player)) return MatchType.FULL;}
		//conduct checks if the land is guild owned
		else if (!data.owner.equals(ComVars.NIL) && !data.isForSale) {
			//Idenitfy the proper playerType for the player's relation to the chunk
			PlayerType pt = PlayerType.UNSET;
			//check if player is non-member by checking for invited members and defaulting to the same code if not present
			if (LogicGuilds.getMembers(data.owner).getOrDefault(player, Integer.MAX_VALUE).equals(Integer.MAX_VALUE)) {
				if (data.renter.equals(player) || data.permittedPlayers.containsKey(player)) pt = PlayerType.LNM;
				if (pt == PlayerType.UNSET) pt = PlayerType.ULNM;
			}
			else {
				if (data.renter.equals(player) || data.permittedPlayers.containsKey(player)) pt = PlayerType.LM;
				if (pt == PlayerType.UNSET && LogicGuilds.getMembers(data.owner).get(player) <= data.permMin) pt = PlayerType.HRM;
				else pt = PlayerType.LRM;
			} 
			//Use playerType to identify which whitelist setup results in which result matchType
			if (data.renter.equals(ComVars.NIL)) {
				if (data.permittedPlayers.size() == 0) {
					if (data.leasePrice >= 0) {
						return MatchType.DENY;
					}
					else {//not up for lease
						if (data.whitelist.size() == 0) {
							switch (pt) {
							case ULNM: {return MatchType.DENY;}
							case LRM: case HRM: {return MatchType.FULL;}
							default:}
						}
						else {//whitelist has items
							switch (pt) {
							case ULNM: case LRM: {return MatchType.WHITELIST;}
							case HRM: {return MatchType.FULL;}
							default:}
						}
					}
				}
				else {//members on list but no renter
					//renting should not be possible if members on list
					if (data.whitelist.size() == 0) {
						switch (pt) {
						case ULNM: case LRM: {return MatchType.DENY;}
						case LM: {return MatchType.FULL;}
						case HRM: {return MatchType.FULL;}
						default:}
					}
					else {//whitelist has items
						switch (pt) {
						case ULNM: case LRM: {return MatchType.DENY;}
						case LM: {return MatchType.WHITELIST;}
						case HRM: {return MatchType.FULL;}
						default:}
					}
				}
			}
			else {//renter exists
				if (data.whitelist.size() == 0) {
					switch (pt) {
					case ULNM: case LRM: case HRM: {return MatchType.DENY;}
					case LNM:case LM:  {return MatchType.FULL;}
					default:}
				}
				else {//whitelist has items
					switch (pt) {
					case ULNM: case LRM: case HRM: {return MatchType.DENY;}
					case LNM: case LM: {return MatchType.WHITELIST;}
					default:}
				}
			}
		}
		return MatchType.DENY;
	}

	public static boolean whitelistCheck(String item, ChunkData data, ActionType type) {
		if (data.whitelist.size() == 0) return true;
		if (data.whitelist.containsKey(item)) {
			switch (type) {
			case BREAK: {return data.whitelist.get(item).getCanBreak();}
			case INTERACT: {return data.whitelist.get(item).getCanInteract();}
			default:}
		}
		return false;
	}
	
	public static enum WhitelisterType {NOT_WHITELISTER, DEFAULT, GREEN_STICK, RED_STICK}	
	public static TranslatableResult<ResultType> isWhitelisterAction(ChunkData data, UUID player, String item, WhitelisterType type, IWhitelister stack, boolean isLeftClick, boolean isSneaking) {
		switch (type) {
		case NOT_WHITELISTER: {return new TranslatableResult<ResultType>(ResultType.FALSE, "");} 
		case DEFAULT: {
			if (isLeftClick) {
				if (isSneaking) {
					((IDefaultWhitelister)stack).setWhitelister(stack, data.whitelist);
					return new TranslatableResult<ResultType>(ResultType.TRUE, "");
				}
				else {//not sneaking
					((IDefaultWhitelister)stack).addToWhitelister(stack, item, WhitelistEntry.UpdateType.BREAK);
					return new TranslatableResult<ResultType>(ResultType.TRUE, "");
				}
			}
			else {//is right click
				if (isSneaking && !data.owner.equals(ComVars.NIL)) {
					if (!isSubletPermitted(data, player)) {
						data.whitelist = ((IDefaultWhitelister)stack).getWhitelist(stack);
						return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.wlapply");
					}
					else {return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.wlapply.deny");}
				}
				else {//not sneaking
					((IDefaultWhitelister)stack).addToWhitelister(stack, item, WhitelistEntry.UpdateType.INTERACT);
					return new TranslatableResult<ResultType>(ResultType.TRUE, "");
				}
			}
		}
		case GREEN_STICK: {
			if (!isSubletPermitted(data, player)) {return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.stick.deny");}
			if (isLeftClick) {
				data.whitelist.computeIfAbsent("block", key -> new WhitelistEntry()).setCanBreak(true);
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.stick.newsetting");
			}
			else {//is right click
				data.whitelist.computeIfAbsent("block", key -> new WhitelistEntry()).setCanInteract(true);
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.stick.newsetting");
			}
		}
		case RED_STICK: {
			if (!isSubletPermitted(data, player)) {return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.stick.deny");} 
			if (isLeftClick) {
				data.whitelist.computeIfAbsent("block", key -> new WhitelistEntry()).setCanBreak(false);
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.stick.newsetting");
			}
			else {//is right click
				data.whitelist.computeIfAbsent("block", key -> new WhitelistEntry()).setCanInteract(false);
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.stick.newsetting");
			}
		}
		default:}
		return new TranslatableResult<ResultType>(ResultType.TRUE, "");
	}
	
	/** checks to see if the player has the required guild permission
	 * to interact with a sublet function.
	 * 
	 * do not call this function on a chunk that is not owned by a guild
	 * 
	 * @param data  the data for the chunk being checked
	 * @param player the player being checked for
	 * @return true if the player is sublet permitted by their guild.
	 */
	private static boolean isSubletPermitted(ChunkData data, UUID player) {
		return LogicGuilds.hasPermission(data.owner, PermKey.SUBLET_MANAGE, player);
	}
	
	public static boolean unownedWLBreakCheck(String block) {
		if (ConfigCore.UNOWNED_WHITELIST.size() == 0) return false;
		for (int i = 0; i < ConfigCore.UNOWNED_WHITELIST.size(); i++) {
			if (block.equalsIgnoreCase(ConfigCore.UNOWNED_WHITELIST.get(i))) return true;
		}
		return false;
	}
	
	public static TranslatableResult<ResultType> onEntityEnterChunkLogic() {
		//TODO check for fallingentities that should be denied entry
		// Lays foundation for entry denial permissions.
		return null;
	}
	
	/**Logic to be applied to BreakEvent on both sides
	 * 
	 * Implementation should check for exempt cases like creative before executing this method
	 * 
	 * @param data the chunk data for the target block
	 * @param item the block object's registry name
	 * @param player the player entity triggering the event
	 * @param guildImpl the guild logic implementation which stores the guild data
	 * @return the appropriate action as a result of this method.  true/false for the event cancellation status or packet if one should be sent to the client
	 */
	public static TranslatableResult<ResultType> onBlockBreakLogic(ChunkData data, String item, UUID player) {
		if (data.owner.equals(ComVars.NIL) && !ConfigCore.UNOWNED_PROTECTED) 
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		if (data.owner.equals(ComVars.NIL) && ConfigCore.UNOWNED_PROTECTED && unownedWLBreakCheck(item)) 
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		if (data.owner.equals(ComVars.NIL) && ConfigCore.AUTO_TEMPCLAIM) 
			return new TranslatableResult<ResultType>(ResultType.PACKET, "");
		switch (ownerMatch(player, data)) {
		case DENY: {
			return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.breakdeny");
		}
		case WHITELIST: {
			if (!whitelistCheck(item, data, ActionType.BREAK)) {
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.breakdeny");
			}
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		}
		default: {return new TranslatableResult<ResultType>(ResultType.FALSE, "");}
		}
	}
	
	public static TranslatableResult<ResultType> onBlockLeftClickLogic(ChunkData data, UUID player, String item, WhitelisterType type, IWhitelister stack, boolean isLeftClick, boolean isSneaking) {
		return isWhitelisterAction(data, player, item, type, stack, isLeftClick, isSneaking);
	}
	
	/**CALL ONLY ON THE SERVER SIDE OF THE EVENT
	 * 
	 * Implement block notifications to the client when cancelled.
	 * 
	 * @param data
	 * @param player
	 * @param item
	 * @param type
	 * @param stack
	 * @param isLeftClick
	 * @param isSneaking
	 * @param guildImpl
	 * @return the appropriate action as a result of this method.  true/false for the event cancellation status or packet if one should be sent to the client
	 */
	public static TranslatableResult<ResultType> onBlockRightClickLogic(ChunkData data, UUID player, String item, WhitelisterType type, IWhitelister stack, boolean isLeftClick, boolean isSneaking) {
		//Whitelister interact toggle
		TranslatableResult<ResultType> result = isWhitelisterAction(data, player, item, type, stack, isLeftClick, isSneaking);
		if (result.result.equals(ResultType.TRUE)) {return result;}
		//normal protection checks
		switch (ownerMatch(player, data)) {
		case DENY: {
			return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.block.breakdeny");
		}
		case WHITELIST: {
			if (!whitelistCheck(item, data, ActionType.INTERACT)) {
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.block.breakdeny");
			}
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		}
		default: {return new TranslatableResult<ResultType>(ResultType.FALSE, "");}
		}
	}
	
	/**CHECK IF ENTITY IS PLAYER BEFORE CALLING
	 * 
	 * @param data
	 * @param player
	 * @param item
	 * @param guildImpl
	 * @return the appropriate action as a result of this method.  true/false for the event cancellation status or packet if one should be sent to the client
	 */
	public static TranslatableResult<ResultType> onBlockPlaceLogic(ChunkData data, UUID player, String item) {
		if (!ConfigCore.UNOWNED_PROTECTED && data.owner.equals(ComVars.NIL)) return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		if (data.owner.equals(ComVars.NIL) && ConfigCore.AUTO_TEMPCLAIM)
			return new TranslatableResult<ResultType>(ResultType.PACKET, "");
		switch (ownerMatch(player, data)) {
		case DENY: {
			return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.block.placedeny");
		}
		case WHITELIST: {
			if (!whitelistCheck(item, data, ActionType.PLACE))
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.block.placedeny");
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		}
		default:}
		return new TranslatableResult<ResultType>(ResultType.TRUE, "");
	}
	
	//Check if player is exempt before calling this method
	public static TranslatableResult<ResultType> onEntityInteractLogic(ChunkData data, UUID player, String item, WhitelisterType type, IWhitelister stack, boolean isLeftClick, boolean isSneaking) {
		TranslatableResult<ResultType> result = isWhitelisterAction(data, player, item, type, stack, isLeftClick, isSneaking);
		if (result.result.equals(ResultType.TRUE)) return new TranslatableResult<ResultType>(ResultType.TRUE, "");
		switch (ownerMatch(player, data)) {
		case DENY: {
			return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.entity.interactdeny");
		}
		case WHITELIST: {
			if (!whitelistCheck(item, data, ActionType.INTERACT)) {
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.entity.interactdeny");
			}
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		}
		default: }
		return new TranslatableResult<ResultType>(ResultType.TRUE, "");
	}
	
	/** this method checks the logic of LivingDamageEvent and AttackEntityEvents
	 * 
	 * - Do not call if player is exempt, such as creative or operator.
	 * - Do not call if target entity is exempt from protections, such as mobs.
	 * - Call {@link isWhitelisterAction} on the logical server before calling this method on both sides
	 * 
	 * @param data the chunk data for the target's location
	 * @param player the player our damage source player causing the harm
	 * @param guildImpl the guild logic implementation
	 * @param target the entity receiving the harm.
	 * @return the appropriate action as a result of this method.  true/false for the event cancellation status or packet if one should be sent to the client
	 */
	public static TranslatableResult<ResultType> onEntityHarmLogic(ChunkData data, UUID player, String target) {
		switch (ownerMatch(player, data)) {
		case DENY :{ return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.entity.breakdeny");}
		case WHITELIST: {
			if (!whitelistCheck(target, data, ActionType.BREAK)) { return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.whitelist.entity.breakdeny");} 
		}
		default: return new TranslatableResult<ResultType>(ResultType.FALSE, "");}
	}
	
	//Explosion cannot reasonably be default.  this needs to be done on the implementation
	//public static TranslatableResult<ResultType> onExplosionLogic(ILogicRealEstate realEstateImpl) {}

	public static TranslatableResult<ResultType> onTrampleLogic(ChunkData data, UUID player, String item) {
		if (data.owner.equals(ComVars.NIL) && !ConfigCore.UNOWNED_PROTECTED) 
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		if (data.owner.equals(ComVars.NIL) && ConfigCore.UNOWNED_PROTECTED && unownedWLBreakCheck(item)) 
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		if (data.owner.equals(ComVars.NIL) && ConfigCore.AUTO_TEMPCLAIM) 
			return new TranslatableResult<ResultType>(ResultType.PACKET, "");
		switch (ownerMatch(player, data)) {
		case DENY: {
			return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.trampledeny");
		}
		case WHITELIST: {
			if (!whitelistCheck(item, data, ActionType.BREAK)) {
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.trampledeny");
			}
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		}
		default: {return new TranslatableResult<ResultType>(ResultType.FALSE, "");}
		}
	}

	public static TranslatableResult<ResultType> onBucketUseLogic(ChunkData data, UUID player, String item) {
		if (data.owner.equals(ComVars.NIL) && !ConfigCore.UNOWNED_PROTECTED) 
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		if (data.owner.equals(ComVars.NIL) && ConfigCore.UNOWNED_PROTECTED && unownedWLBreakCheck(item)) 
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		if (data.owner.equals(ComVars.NIL) && ConfigCore.AUTO_TEMPCLAIM) 
			return new TranslatableResult<ResultType>(ResultType.PACKET, "");
		switch (ownerMatch(player, data)) {
		case DENY: {
			return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.bucketdeny");
		}
		case WHITELIST: {
			if (!whitelistCheck(item, data, ActionType.BREAK)) {
				return new TranslatableResult<ResultType>(ResultType.TRUE, "event.chunk.bucketdeny");
			}
			return new TranslatableResult<ResultType>(ResultType.FALSE, "");
		}
		default: {return new TranslatableResult<ResultType>(ResultType.FALSE, "");}
		}
	}
	
	public static TranslatableResult<ResultType> onBonemealUseLogic() {
		//TODO add bonemeal event logic
		return null;
	}
}
