public class PPU
{
	private CPU cpu;
	private GamePak game;
	
	private char[] memory;
	private char addr;
	private char memInc;
	private char oamaddr;
	private char[] registers;
	
	private int scanline;
	private int x;
	private int frameNo;
	private int xScroll;
	private int yScroll;
	
	private int scrollWrites;
	private int addrWrites;
	
	private byte nametable;
	private byte spritePatternTable;
	private byte bgPatternTable;
	private byte spriteSize;
	
	private boolean slave;
	private boolean vBlankNMI;
	private boolean vBlank;
	private boolean spriteHit;
	private boolean spriteOverflow;
	private boolean render;
	
	public PPU()
	{
		registers = new char[8];
		memory = new char[0x4000];
		powerUp();
	}

	public void tick()
	{
		if (scanline == 0 && x == 0 && frameNo % 2 == 1)
		{
			x++;
		}
		if (scanline == 241 && x == 1)
		{
			vBlank = true;
		}
		if (vBlankNMI && vBlank)
		{
			cpu.triggerNMI();
		}
		
		// Render pixel
		
		x++;
		if (x == 341)
		{
			x = 0;
			scanline++;
			if (scanline == 262)
			{
				scanline = 0;
				frameNo++;
				vBlank = false;
			}
		}
	}
	
	public void powerUp()
	{
		registers[0] = 0x00;
		registers[1] = 0x00;
		registers[2] = 0xA0;
		registers[3] = 0x00;
		registers[5] = 0x00;
		registers[6] = 0x00;
		registers[7] = 0x00;
		frameNo = 0;
		scanline = -1;
		x = 0;
		scrollWrites = 0;
		addrWrites = 0;
	}
	
	public void reset()
	{
		registers[0] = 0x00;
		registers[1] = 0x00;
		registers[2] = (char) (registers[2] & 0x80);
		registers[5] = 0x00;
		registers[7] = 0x00;
		scrollWrites = 0;
		addrWrites = 0;
	}

	public void setCPU(CPU cpu2)
	{
		cpu = cpu2;
	}
	
	public char access(boolean write, char addr, char b)
	{
		// Pattern table access from GamePak
		if (addr < 0x2000)
		{
			 
		}
		return 0;
	}
	
	public char accessRegisters(boolean write, char addr, char b)
	{
		// Writing to registers
		if (write)
		{
			// PPUCTRL
			if (addr == 0x2000)
			{
				nametable = (byte) (b & 0x03);
				if (((nametable & 0x04) >> 2) == 1)
				{
					memInc = 32;
				}
				else
				{
					memInc = 1;
				}
				spritePatternTable = (byte) ((b & 0x08) >> 3);
				bgPatternTable = (byte) ((b & 0x10) >> 4);
				spriteSize = (byte) ((b & 0x20) >> 5);
				slave = ((byte) ((b & 0x40) >> 6) == 1);
				vBlankNMI = ((byte) (b >> 7) == 1);
			}
			// PPUMASK
			if (addr == 0x2001)
			{
				render = !(Utils.getBit(b, 3) == 0) && (Utils.getBit(b, 4) == 0);
			}
			// OAMADDR
			if (addr == 0x2003)
			{
				oamaddr = b;
			}
			// OAMDATA
			else if (addr == 0x2004)
			{
				// Write to OAM data
			}
			// PPUSCROLL
			else if (addr == 0x2005)
			{
				if (scrollWrites == 0)
				{
					xScroll = b;
					scrollWrites++;
				}
				else if (scrollWrites == 1)
				{
					yScroll = b;
					scrollWrites++;
				}
			}
			// PPUADDR
			else if (addr == 0x2006)
			{
				if (addrWrites == 0)
				{
					this.addr = (char) (b << 8);
					addrWrites++;
				}
				else if (addrWrites == 1)
				{
					this.addr |= b;
					addrWrites++;
				}
			}
			// PPUDATA
			else if (addr == 0x2007)
			{
				access(write, this.addr, b);
				this.addr += memInc;
				
			}
		}
		// Read registers
		else
		{
			// PPUSTATUS
			if (addr == 0x2002)
			{
				if (vBlank)
				{
					vBlank = false;
					return (char) ((1 << 7) | ((spriteHit ? 1 : 0) << 6) | ((spriteOverflow ? 1 : 0) << 5));
				}
			}
			//PPUDATA
			else if (addr == 0x2007)
			{
				char q = memory[this.addr];
				this.addr += memInc;
				return q;
			}
		}
		return 0;
	}

	public void setGamePak(GamePak g)
	{
		
	}
}