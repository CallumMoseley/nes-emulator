public class NES
{
	private CPU cpu;
	
	public NES()
	{
		cpu = new CPU();
		
		for (;;)
		{
			cpu.op();
		}
	}
}