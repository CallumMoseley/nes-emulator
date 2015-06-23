import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

public class GamePak
{
	private int mapper;
	private boolean fourScreen;
	private boolean trainer;
	private boolean batteryBacked;
	private boolean vertMirroring;

	private boolean pc10;
	private boolean vs;

	private int[] sram;
	private int[][] prgRom;
	private int[][] chrRom;

	public void writeMemory(int addr, int v)
	{
		if (addr >= 0x6000 && addr < 0x8000)
		{
			sram[addr - 0x6000] = v;
			// Write to a file here?
		}
		else if (addr >= 0x8000 && addr < 0xC000)
		{
			prgRom[0][addr - 0x8000] = v;
		}
		else if (addr >= 0xC000)
		{
			prgRom[1][addr - 0xC000] = v;
		}
	}

	public int readMemory(int addr)
	{
		if (addr >= 0x6000 && addr < 0x8000)
		{
			return sram[addr - 0x6000];
		}
		else if (addr >= 0x8000 && addr < 0xC000)
		{
			return prgRom[0][addr - 0x8000];
		}
		else if (addr >= 0xC000)
		{
			return prgRom[1][addr - 0xC000];
		}
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

			int prgROMPages = fis.read();
			int chrROMPages = fis.read();

			prgRom = new int[prgROMPages][0x4000];
			chrRom = new int[chrROMPages][0x2000];
			sram = new int[0x2000];

			int flags6 = fis.read();
			mapper = flags6 >> 4;
			fourScreen = ((flags6 >> 3) & 0x01) == 1;
			trainer = ((flags6 >> 2) & 0x01) == 1;
			batteryBacked = ((flags6 >> 1) & 0x01) == 1;
			vertMirroring = (flags6 & 0x01) == 1;

			int flags7 = fis.read();
			mapper |= flags7 & 0xF0;
			pc10 = ((flags7 >> 1) & 0x01) == 1;
			vs = (flags7 & 0x01) == 1;

			for (int i = 0; i < 8; i++)
			{
				fis.read();
			}

			for (int i = 0; i < prgROMPages; i++)
			{
				for (int j = 0; j < 0x4000; j++)
				{
					prgRom[i][j] = fis.read();
				}
			}

			for (int i = 0; i < chrROMPages; i++)
			{
				for (int j = 0; j < 0x2000; j++)
				{
					chrRom[i][j] = fis.read();
				}
			}

			fis.close();

			String sav = f.getAbsolutePath().substring(0,
					f.getAbsolutePath().length() - 3)
					+ "sav";
			if (Files.exists(Paths.get(sav), new LinkOption[] {}))
			{
				fis = new FileInputStream(new File(sav));
				for (int i = 0; i < 0x2000; i++)
				{
					sram[i] = fis.read();
				}
				fis.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}