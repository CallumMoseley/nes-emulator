public class NES
{
	private CPU cpu;
	private PPU ppu;
	private APU apu;
	
	public NES()
	{
		cpu = new CPU();
		ppu = new PPU();
		cpu.setPPU(ppu);
		ppu.setCPU(cpu);
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