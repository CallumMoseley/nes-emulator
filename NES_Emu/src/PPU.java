public class PPU
{
	private CPU cpu;
	
	private boolean evenFrame;
	private boolean warm;

	private int scanline;
	private int x;
	private int tickCount;

	private int grey;
	
	private int bg8;
	private int spr8;
	private int bg;
	private int spr;
	
	private int r;
	private int g;
	private int b;
	
	private int vBlank;
	private int sprite0Hit;
	private int spriteOverflow;
	
	private int nmiOutput;
	
	private int addrInc;
	private int nametable;

	public PPU()
	{
		scanline = 0;
		x = 0;
		tickCount = 0;
		evenFrame = true;
		warm = false;
	}
	
	public void setCPU(CPU c)
	{
		cpu = c;
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
		
		// VBlank
		if (scanline == 241 && x == 1)
		{
			vBlank = 1;
			if (nmiOutput == 1)
			{
				cpu.triggerNMI();
			}
		}
		if (scanline == 261 && x == 1)
		{
			vBlank = 0;
			sprite0Hit = 0;
		}
		
		
		// RENDER
		

		x++;
		tickCount++;
	}

	public void writeRegister(int i, int d)
	{
		// PPUCTRL
		if (i == 0 && warm)
		{
			nametable = 0x2000 + 0x400 * (d & 0x03);
			addrInc = ((d >> 2) & 0x01) == 1 ? 32 : 1;
			
			nmiOutput = d >> 7;
			if (nmiOutput == 1 && vBlank == 1)
			{
				cpu.triggerNMI();
			}
		}
		// PPUMASK
		else if (i == 1 && warm)
		{
			grey = d & 0x01;
			bg8 = (d >> 1) & 0x01;
			spr8 = (d >> 2) & 0x01;
			bg = (d >> 3) & 0x01;
			spr = (d >> 4) & 0x01;
			r = (d >> 5) & 0x01;
			g = (d >> 6) & 0x01;
			b = (d >> 7) & 0x01;
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