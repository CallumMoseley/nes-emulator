public class PPU
{
	private boolean evenFrame;
	private boolean warm;

	private int scanline;
	private int x;
	private int tickCount;

	private int bg;
	private int spr;
	
	private int vBlank;
	private int sprite0Hit;
	private int spriteOverflow;

	public PPU()
	{
		scanline = 0;
		x = 0;
		tickCount = 0;
		evenFrame = true;
		warm = false;
	}

	public void tick()
	{
		// Handle scanlines and x position, as well as weird tick skipping on
		// odd frames
		if ((bg == 1 || spr == 1) && !evenFrame && x == 340 && scanline == 261)
		{
			x = 0;
			scanline = 0;
			evenFrame = !evenFrame;
		}
		if (x == 341)
		{
			scanline++;
			x = 0;
		}
		if (scanline == 262)
		{
			scanline = 0;
			evenFrame = !evenFrame;
		}

		x++;
		tickCount++;
	}

	public void writeRegister(int i, int d)
	{
		// PPUCTRL
		if (i == 0 && warm)
		{
			
		}
		// PPUMASK
		else if (i == 1 && warm)
		{
			
		}
		// OAMADDR
		else if (i == 3)
		{
			
		}
		// OAMDATA
		else if (i == 4)
		{
			
		}
		// PPUSCROLL
		else if (i == 5 && warm)
		{
			
		}
		// PPUADDR
		else if (i == 6 && warm)
		{
			
		}
		// PPUDATA
		else if (i == 7)
		{
			
		}
		// OAMDMA
		else if (i == 0x4014)
		{
			
		}
	}

	public int readRegister(int i)
	{
		// PPUSTATUS
		if (i == 2)
		{
			int val = (vBlank << 7) | (sprite0Hit << 6) | (spriteOverflow << 5); 
			vBlank = 0;
			return val;
		}
		// OAMDATA
		else if (i == 4)
		{
			
		}
		// PPUDATA
		else if (i == 7)
		{
			
		}
		return 0;
	}
	
	public void setWarm()
	{
		warm = true;
	}
}