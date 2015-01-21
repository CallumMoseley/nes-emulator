public class Utils
{
	public static byte getBit(char n, int bit)
	{
		return (byte) ((n >> bit) & 0x01);
	}
}