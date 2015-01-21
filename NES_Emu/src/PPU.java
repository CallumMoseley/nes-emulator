public class PPU
{
	private CPU cpu;
	private char[] registers;
	private int scanline;
	private int x;
	
	public PPU()
	{
		
	}

	public void tick()
	{
		
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