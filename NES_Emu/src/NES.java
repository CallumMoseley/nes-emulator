import java.io.File;

public class NES
{
	CPU cpu;
	PPU ppu;
	APU apu;
	GamePak game;
	
	public NES()
	{
		cpu = new CPU();
		ppu = new PPU();
		apu = new APU();
		game = new GamePak();
		game.loadGame(new File("SMB/smb.nes"));
		
		cpu.setPPU(ppu);
		cpu.setAPU(apu);
		cpu.setGame(game);
		
		ppu.setCPU(cpu);
		ppu.setGame(game);
	}
	
	public void startCPU()
	{
		for (;;)
		{
			cpu.op();
		}
	}
}