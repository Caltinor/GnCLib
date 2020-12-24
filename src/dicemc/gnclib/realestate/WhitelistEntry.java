package dicemc.gnclib.realestate;

public class WhitelistEntry {
	public static enum UpdateType {BREAK, INTERACT}
	private boolean canBreak, canInteract;
	/*TODO if more specific protection features are added, include them and update constructors and getters/setters
	* Examples include "canPlace", "canDamage", "canTrample", "canBurn"
	*/
	public WhitelistEntry() {
		this(false, false);
	}
	
	public WhitelistEntry(boolean canBreak, boolean canInteract) {
		this.canBreak = canBreak;
		this.canInteract = canInteract;
	}
	
	public boolean getCanBreak() {return canBreak;}
	public void setCanBreak(boolean bool) {canBreak = bool;}
	public boolean getCanInteract() {return canInteract;}
	public void setCanInteract(boolean bool) {canInteract = bool;}
}
