package dicemc.gnclib.util;

@SuppressWarnings("rawtypes")
public class TranslatableResult<T extends Enum> {
	public T result;
	public String translationKey;
	
	public TranslatableResult(T result, String translationKey) {
		this.result = result;
		this.translationKey = translationKey;
	}
}
