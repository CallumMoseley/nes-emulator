import java.io.File;

public class NES
{
	private CPU cpu;
	private PPU ppu;
	private APU apu;
	
	public NES()
	{
		GamePak g = new GamePak();
		try
		{
			g.load(new File("SMB/smb.nes"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		cpu = new CPU();
		ppu = new PPU();
		cpu.setPPU(ppu);
		cpu.setGamePak(g);
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