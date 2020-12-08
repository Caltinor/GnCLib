package dicemc.gnclib.realestate;

public class WhitelistEntry {
	private boolean isEntity, canBreak, canInteract;
	/*TODO if more specific protection features are added, include them and update constructors and getters/setters
	* Examples include "canPlace", "canDamage", "canTrample", "canBurn"
	*/
	public WhitelistEntry(boolean isEntity) {
		this(isEntity, false, false);
	}
	
	public WhitelistEntry(boolean isEntity, boolean canBreak, boolean canInteract) {
		this.isEntity = isEntity;
		this.canBreak = canBreak;
		this.canInteract = canInteract;
	}
	
	public boolean getCanBreak() {return canBreak;}
	public void setCanBreak(boolean bool) {canBreak = bool;}
	public boolean getCanInteract() {return canInteract;}
	public void setCanInteract(boolean bool) {canInteract = bool;}
	public boolean isEntity() {return isEntity;}
}
