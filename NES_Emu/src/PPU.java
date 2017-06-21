import java.awt.image.BufferedImage;

public class PPU {
	private CPU cpu;
	private GamePak game;

	private BufferedImage screen;

	private int[] memory;
	private int[] oam;

	private boolean evenFrame;
	private boolean warm;

	private int scanline;
	private int x;
	private int tickCount;

	private int grey;

	private int bg8;
	private int spr8;
	private int bg;
	private int spr;

	private int r;
	private int g;
	private int b;

	private int vBlank;
	private int sprite0Hit;
	private int spriteOverflow;

	private int nmiOutput;

	private int addrInc;
	private int addr;
	private int addrLatch;

	private int oamAddr;

	private int nametable;

	public PPU() {
		scanline = 0;
		x = 0;
		tickCount = 0;
		evenFrame = true;
		warm = false;
		memory = new int[0x4000];
		screen = new BufferedImage(256, 240, BufferedImage.TYPE_INT_RGB);
	}

	public void setCPU(CPU c) {
		cpu = c;
	}

	public void setGame(GamePak g) {
		game = g;
	}

	public void tick() {
		// Handle scanlines and x position, as well as weird tick skipping on
		// odd frames
		if ((bg == 1 || spr == 1) && !evenFrame && x == 340 && scanline == 261) {
			x = 0;
			scanline = 0;
			evenFrame = !evenFrame;
		}
		if (x == 341) {
			scanline++;
			x = 0;
		}
		if (scanline == 262) {
			scanline = 0;
			evenFrame = !evenFrame;
		}

		// VBlank
		if (scanline == 241 && x == 1) {
			vBlank = 1;
			if (nmiOutput == 1) {
				cpu.triggerNMI();
			}
		}
		if (scanline == 261 && x == 1) {
			vBlank = 0;
			sprite0Hit = 0;
		}

		// TODO Render

		x++;
		tickCount++;
	}

	public void writeRegister(int i, int d) {
		// PPUCTRL
		if (i == 0 && warm) {
			nametable = 0x2000 + 0x400 * (d & 0x03);
			addrInc = ((d >> 2) & 0x01) == 1 ? 32 : 1;

			nmiOutput = d >> 7;
			if (nmiOutput == 1 && vBlank == 1) {
				cpu.triggerNMI();
			}
		}
		// PPUMASK
		else if (i == 1 && warm) {
			grey = d & 0x01;
			bg8 = (d >> 1) & 0x01;
			spr8 = (d >> 2) & 0x01;
			bg = (d >> 3) & 0x01;
			spr = (d >> 4) & 0x01;
			r = (d >> 5) & 0x01;
			g = (d >> 6) & 0x01;
			b = (d >> 7) & 0x01;
		}
		// OAMADDR
		else if (i == 3) {
			oamAddr = d;
		}
		// OAMDATA
		else if (i == 4) {
			oamAddr &= 0xFF;
			oam[oamAddr++] = d;
		}
		// PPUSCROLL
		else if (i == 5 && warm) {
			// TODO scrolling
		}
		// PPUADDR
		else if (i == 6 && warm) {
			if (addrLatch == 1) {
				addr &= d;
			} else {
				addrLatch = 1;
				addr = d << 8;
			}
		}
		// PPUDATA
		else if (i == 7) {
			accessMemory(addr, d);
			addr += addrInc;
		}
	}

	public int readRegister(int i) {
		// PPUSTATUS
		if (i == 2) {
			int val = (vBlank << 7) | (sprite0Hit << 6) | (spriteOverflow << 5);
			vBlank = 0;
			addrLatch = 0;
			return val;
		}
		// OAMDATA
		else if (i == 4) {
			oamAddr &= 0xFF;
			return oam[oamAddr++];
		}
		// PPUDATA
		else if (i == 7) {
			int val = accessMemory(addr);
			addr += addrInc;
			return val;
		}
		return 0;
	}

	private int accessMemory(int a) {
		if (a < 0x2000) {
			return game.readCHR(a);
		} else {
			if (a >= 0x3F10 && a % 4 == 0) {
				a -= 0x10;
			}
			return memory[a];
		}
	}

	private void accessMemory(int a, int v) {
		if (a < 0x2000) {
			game.writeCHR(a, v);
		} else {
			if (a >= 0x3F10 && a % 4 == 0) {
				a -= 0x10;
			}
			memory[a] = v;
		}
	}

	public void setWarm() {
		warm = true;
	}

	public BufferedImage getScreen() {
		return screen;
	}

	public void attachDebugger(Debugger debugger) {

	}
}