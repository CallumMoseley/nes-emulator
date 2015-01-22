public class Utils
{
	public static char getBit(char n, int bit)
	{
		return (char) ((n >> bit) & 0x01);
	}
}