public class NES
{
	private CPU cpu;
	
	public NES()
	{
		cpu = new CPU();
	}
	
	public void startCPU()
	{
		Thread cpuThread = new Thread()
		{
			public void run()
			{
				for (;;)
				{
					cpu.op();
				}
			}
		};
		cpuThread.start();
	}
}