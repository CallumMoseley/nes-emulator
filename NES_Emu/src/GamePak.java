import java.io.File;
import java.io.FileInputStream;

public class GamePak {
	
	private char[][] PRG_ROM;
	private char[][] CHR_ROM;
	
	private int mapper;
	
	public void load(File f) throws Exception
	{
		FileInputStream fis = new FileInputStream(f);
		for (int i = 0; i < 4; i++)
			fis.read();
		int prgSize = fis.read();
		int chrSize = fis.read();
		
		PRG_ROM = new char[prgSize][0x4000];
		CHR_ROM = new char[chrSize][0x2000];
		
		char flags1 = (char) fis.read();
		char flags2 = (char) fis.read();
		
		mapper = (flags2 & 0xF0) | (flags1 >> 4);
		
		// Blank bytes
		for (int i = 0; i < 8; i++)
		{
			fis.read();
		}
		
		// Read in PRG_ROM banks
		for (int i = 0; i < prgSize; i++)
		{
			for (int j = 0; j < 0x4000; j++)
			{
				PRG_ROM[i][j] = (char) fis.read();
			}
		}
		
		// Read in CHR_ROM (VROM)
		for (int i = 0; i < chrSize; i++)
		{
			for (int j = 0; j < 0x2000; j++)
			{
				CHR_ROM[i][j] = (char) fis.read();
			}
		}
		fis.close();
	}
	
	public char access(boolean write, char addr, char b)
	{
		if (addr >= 0x8000 && addr < 0xC000)
		{
			if (!write)
			{
				return PRG_ROM[0][addr - 0x8000];
			}
			else
			{
				PRG_ROM[0][addr - 0x8000] = b;
			}
		}
		if (addr >= 0xC000 && addr < 0x10000)
		{
			if (!write)
			{
				return PRG_ROM[1][addr - 0xC000];
			}
			else
			{
				PRG_ROM[1][addr - 0xC000] = b;
			}
		}
		return 0;
	}
}