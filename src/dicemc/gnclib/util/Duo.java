package dicemc.gnclib.util;

public class Duo <L, R extends Object>{
	private L left;
	private R right;
	
	public Duo(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	public L getL() {return left;}
	public R getR() {return right;}
	public void setL(L newValue) {left = newValue;}
	public void setR(R newValue) {right = newValue;}
}
