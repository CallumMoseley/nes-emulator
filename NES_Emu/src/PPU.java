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
	
	private boolean vBlankNMI;
	
	public PPU()
	{
		registers = new char[8];
		memory = new char[0x4000];
		powerUp();
	}

	public void tick()
	{
		
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
		scanline = 0;
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
				spriteSize = (byte) ((b & 0x20) >> 4);
				
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
			
		}
		return 0;
	}

	public void setGamePak(GamePak g)
	{
		
	}
}