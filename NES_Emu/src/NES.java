import java.io.File;

public class NES {
	CPU cpu;
	PPU ppu;
	APU apu;
	GamePak game;
	private boolean paused;

	public NES() {
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

	public void startCPU() {
		Thread cpuThread = new Thread() {
			public void run() {
				for (;;) {
					if (!paused) {
						cpu.op();
					}
				}
			}
		};

		cpuThread.start();
	}

	public void reset() {
		cpu.reset();
		ppu = new PPU();
		ppu.setCPU(cpu);
		ppu.setGame(game);
		cpu.setPPU(ppu);
	}

	public void restart() {

	}

	public void pause() {
		paused = true;
	}

	public void resume() {
		paused = false;
	}

	public void step() {
		cpu.op();
	}

	public CPU getCPU() {
		return cpu;
	}

	public PPU getPPU() {
		return ppu;
	}

	public void attachDebugger(Debugger debugger) {
		cpu.attachDebugger(debugger);
		ppu.attachDebugger(debugger);
	}
}