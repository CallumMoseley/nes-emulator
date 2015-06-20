import java.io.File;
import java.io.FileInputStream;

public class GamePak
{
	private int mapper;
	boolean fourScreen;
	boolean trainer;
	boolean batteryBacked;
	boolean mirroring;
	
	boolean pc10;
	boolean vs;

	public int readMemory(int addr)
	{
		return 0;
	}
	
	public void loadGame(File f)
	{
		try
		{
			FileInputStream fis = new FileInputStream(f);
			for (int i = 0; i < 4; i++)
			{
				fis.read();
			}
			
			int prgROM = fis.read();
			int chrROM = fis.read();
			
			int flags6 = fis.read();
			mapper = flags6 >> 4;
			fourScreen = ((flags6 >> 3) & 0x01) == 1;
			trainer = ((flags6 >> 2) & 0x01) == 1;
			batteryBacked = ((flags6 >> 1) & 0x01) == 1;
			mirroring = (flags6 & 0x01) == 1;
			
			int flags7 = fis.read();
			mapper |= flags7 & 0xF0;
			pc10 = ((flags7 >> 1) & 0x01) == 1;
			vs = (flags7 & 0x01) == 1;
			
			for (int i = 0; i < 8; i++)
			{
				fis.read();
			}
			
			fis.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}