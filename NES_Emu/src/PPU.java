public class PPU
{
	private CPU cpu;
	
	private char[] registers;
	
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
	
	public void readRegister(int n)
	{
		registers[n] = CPU.readRegister(n);
	}
}