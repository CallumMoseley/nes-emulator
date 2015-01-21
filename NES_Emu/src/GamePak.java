import java.io.File;
import java.io.FileReader;

public class GamePak {
	
	private char[][] PRG_ROM;
	private char[][] CHR_ROM;
	
	private int mapper;
	
	public void load(File f) throws Exception
	{
		FileReader fr = new FileReader(f);
		for (int i = 0; i < 4; i++)
			fr.read();
		int prgSize = fr.read();
		int chrSize = fr.read();
		
		PRG_ROM = new char[prgSize][0x4000];
		CHR_ROM = new char[chrSize][0x2000];
		
		char flags1 = (char) fr.read();
		char flags2 = (char) fr.read();
		
		mapper = (flags2 & 0xF0) | (flags1 >> 4);
		for (int i = 0; i < 8; i++)
		{
			fr.read();
		}
		
		for (int i = 0; i < prgSize; i++)
		{
			fr.read(PRG_ROM[i], 0, 0x4000);
		}
		for (int i = 0; i < chrSize; i++)
		{
			fr.read(CHR_ROM[i], 0, 0x2000);
		}
		fr.close();
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
				PRG_ROM[1][addr - 0x8000] = b;
			}
		}
		return 0;
	}
}