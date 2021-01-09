package dicemc.gnclib.trade;

public class Marketplace{
	public String marketName;
	public double buyFee, sellFee;
	
	public Marketplace(String name, double buyFee, double sellFee) {
		this.marketName = name;
		this.buyFee = buyFee;
		this.sellFee = sellFee;
	}
	public Marketplace(String name) {
		this(name, 0d, 0d);
	}
}
