public class PPU
{
	private CPU cpu;
	private char[] registers;
	private int scanline;
	private int x;
	private int frameNo;
	
	public PPU()
	{
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
	}
	
	public void reset()
	{
		registers[0] = 0x00;
		registers[1] = 0x00;
		registers[2] = (char) (registers[2] & 0x80);
		registers[5] = 0x00;
		registers[7] = 0x00;
	}

	public void setCPU(CPU cpu2)
	{
		cpu = cpu2;
	}
	
	public char access(boolean write, char addr, char b)
	{
		addr -= 0x2000;
		if (write)
		{
			registers[addr] = b;
		}
		else
		{
			return registers[addr];
		}
		return 0;
	}
}