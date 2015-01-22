import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// TEST CODE: https://code.google.com/p/hmc-6502/source/browse/trunk/emu/testvectors/AllSuiteA.asm

public class CPU
{
	private char a, x, y, sp;
	private byte c, z, i, d, v, n;
	private char pc;

	private char[] memory = new char[0x10000];

	private GamePak game;
	private PPU ppu;
	private APU apu;

	private boolean reset, nmi, irq;

	public CPU()
	{
		ppu = new PPU();
		apu = new APU();

		try
		{
			byte[] q = Files.readAllBytes(Paths.get("ehbasic.bin"));
			for (int i = 0; i < q.length; i++)
			{
				memory[i + 0xC000] = (char) (q[i] & 0xFF);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		reset = true;
		nmi = false;
		irq = false;

		a = 0x00;
		x = 0x00;
		y = 0x00;

		c = 0x00;
		z = 0x00;
		i = 0x01;
		d = 0x00;
		v = 0x00;
		n = 0x00;

		sp = 0xFF;
		pc = 0x00;
	}
	
	public void setPPU(PPU ppu2)
	{
		ppu = ppu2;
	}
	
	public void setGamePak(GamePak g)
	{
		game = g;
	}

	public void triggerNMI()
	{
		nmi = true;
	}
	
	public void reset()
	{
		reset = true;
	}

	public void tick()
	{
		tick(1);
	}

	public void tick(int n)
	{
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				ppu.tick();
			}
			apu.tick();
		}
	}

	public void op()
	{
		char opcode = accessMemory(pc);
		if (reset)
		{
			reset = false;
			pc = (char) (accessMemory((char) 0xFFFC) | (accessMemory((char) 0xFFFD) << 8));
			pc--;
			i = 1;
			d = (byte) ((int) (Math.random() * 2));
			tick(5);
		}
		else if (nmi)
		{
			NMI();
		}
		else if (irq && i != 1)
		{
			BRK();
		}
		else if (opcode == 0x69 || opcode == 0x65 || opcode == 0x75
				|| opcode == 0x6D || opcode == 0x7D || opcode == 0x79
				|| opcode == 0x61 || opcode == 0x71)
		{
			char operand = 0;
			char zpAddr = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0x69:
				operand = accessMemory(++pc);
				break;
			case 0x65:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0x75:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick();
				break;
			case 0x6D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			case 0x7D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + x));
				if ((loc & 0x0F00) != ((loc + x) & 0xF00))
				{
					tick();
				}
				break;
			case 0x79:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			case 0x61:
				zpAddr = (char) ((accessMemory(++pc) + x) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0x71:
				zpAddr = (char) (accessMemory(++pc) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			}
			ADC(operand);
		}
		else if (opcode == 0x29 || opcode == 0x25 || opcode == 0x35
				|| opcode == 0x2D || opcode == 0x3D || opcode == 0x39
				|| opcode == 0x21 || opcode == 0x31)
		{
			char operand = 0;
			char zpAddr = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0x29:
				operand = accessMemory(++pc);
				break;
			case 0x25:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0x35:
				loc = (char) ((accessMemory(++pc) + x) % 256);
				operand = accessMemory(loc);
				tick();
				break;
			case 0x2D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			case 0x3D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + x));
				if ((loc & 0x0F00) != ((loc + x) & 0xF00))
				{
					tick();
				}
				break;
			case 0x39:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			case 0x21:
				zpAddr = (char) ((accessMemory(++pc) + x) % 256);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0x31:
				zpAddr = (char) (accessMemory(++pc) % 256);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			}
			AND(operand);
		}
		else if (opcode == 0x0A || opcode == 0x06 || opcode == 0x16
				|| opcode == 0x0E || opcode == 0x1E)
		{
			char operand = 0;
			char loc = 0;
			boolean go = true;
			switch (opcode)
			{
			case 0x0A:
				tick();
				ASL();
				go = false;
				break;
			case 0x06:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				tick();
				break;
			case 0x16:
				loc = (char) ((accessMemory(++pc) + x) % 256);
				operand = accessMemory(loc);
				tick(2);
				break;
			case 0x0E:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0x1E:
				loc = (char) ((accessMemory(++pc) | (accessMemory(++pc) << 8)) + x);
				operand = accessMemory(loc);
				tick(2);
				break;
			}
			if (go)
				ASL(operand, loc);
		}
		else if (opcode == 0x90)
		{
			BCC(accessMemory(++pc));
		}
		else if (opcode == 0xB0)
		{
			BCS(accessMemory(++pc));
		}
		else if (opcode == 0xF0)
		{
			BEQ(accessMemory(++pc));
		}
		else if (opcode == 0x24 || opcode == 0x2C)
		{
			char operand = 0;
			switch (opcode)
			{
			case 0x24:
				operand = accessMemory(accessMemory(++pc));
				break;
			case 0x2C:
				operand = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				tick();
				break;
			}
			BIT(operand);
		}
		else if (opcode == 0x30)
		{
			BMI(accessMemory(++pc));
		}
		else if (opcode == 0xD0)
		{
			BNE(accessMemory(++pc));
		}
		else if (opcode == 0x10)
		{
			BPL(accessMemory(++pc));
		}
		else if (opcode == 0x00)
		{
			BRK();
		}
		else if (opcode == 0x50)
		{
			BVC(accessMemory(++pc));
		}
		else if (opcode == 0x70)
		{
			BVS(accessMemory(++pc));
		}
		else if (opcode == 0x18)
		{
			tick(2);
			CLC();
		}
		else if (opcode == 0xD8)
		{
			tick(2);
			CLD();
		}
		else if (opcode == 0x58)
		{
			tick(2);
			CLI();
		}
		else if (opcode == 0xB8)
		{
			tick(2);
			CLV();
		}
		else if (opcode == 0xC9 || opcode == 0xC5 || opcode == 0xD5
				|| opcode == 0xCD || opcode == 0xDD || opcode == 0xD9
				|| opcode == 0xC1 || opcode == 0xD1)
		{
			char operand = 0;
			char zpAddr = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0xC9:
				operand = accessMemory(++pc);
				break;
			case 0xC5:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0xD5:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick();
				break;
			case 0xCD:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			case 0xDD:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + x));
				if ((loc & 0x0F00) != ((loc + x) & 0xF00))
				{
					tick();
				}
				break;
			case 0xD9:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			case 0xC1:
				zpAddr = (char) ((accessMemory(++pc) + x) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0xD1:
				zpAddr = (char) (accessMemory(++pc) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			}
			CMP(operand);
		}
		else if (opcode == 0xE0 || opcode == 0xE4 || opcode == 0xEC)
		{
			char operand = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0xE0:
				operand = accessMemory(++pc);
				break;
			case 0xE4:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0xEC:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			}
			CPX(operand);
		}
		else if (opcode == 0xC0 || opcode == 0xC4 || opcode == 0xCC)
		{
			char operand = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0xC0:
				operand = accessMemory(++pc);
				break;
			case 0xC4:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0xCC:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			}
			CPY(operand);
		}
		else if (opcode == 0xC6 || opcode == 0xD6 || opcode == 0xCE
				|| opcode == 0xDE)
		{
			char loc = 0;
			switch (opcode)
			{
			case 0xC6:
				loc = accessMemory(++pc);
				tick();
				break;
			case 0xD6:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				tick(2);
				break;
			case 0xCE:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				tick();
				break;
			case 0xDE:
				loc = (char) ((accessMemory(++pc) | (accessMemory(++pc) << 8)) + x);
				tick(2);
				break;
			}
			DEC(loc);
		}
		else if (opcode == 0xCA)
		{
			DEX();
			tick(2);
		}
		else if (opcode == 0x88)
		{
			DEY();
			tick(2);
		}
		else if (opcode == 0x49 || opcode == 0x45 || opcode == 0x55
				|| opcode == 0x4D || opcode == 0x5D || opcode == 0x59
				|| opcode == 0x41 || opcode == 0x51)
		{
			char operand = 0;
			char zpAddr = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0x49:
				operand = accessMemory(++pc);
				break;
			case 0x45:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0x55:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick();
				break;
			case 0x4D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			case 0x5D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + x));
				if ((loc & 0x0F00) != ((loc + x) & 0xF00))
				{
					tick();
				}
				break;
			case 0x59:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			case 0x41:
				zpAddr = (char) ((accessMemory(++pc) + x) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0x51:
				zpAddr = (char) (accessMemory(++pc) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			}
			EOR(operand);
		}
		else if (opcode == 0xE6 || opcode == 0xF6 || opcode == 0xEE
				|| opcode == 0xFE)
		{
			char loc = 0;
			switch (opcode)
			{
			case 0xE6:
				loc = accessMemory(++pc);
				tick();
				break;
			case 0xF6:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				tick(2);
				break;
			case 0xEE:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				tick();
				break;
			case 0xFE:
				loc = (char) ((accessMemory(++pc) | (accessMemory(++pc) << 8)) + x);
				tick(2);
				break;
			}
			INC(loc);
		}
		else if (opcode == 0xE8)
		{
			INX();
			tick();
		}
		else if (opcode == 0xC8)
		{
			INY();
			tick();
		}
		else if (opcode == 0x4C || opcode == 0x6C)
		{
			char loc = 0;
			switch (opcode)
			{
			case 0x4C:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				break;
			case 0x6C:
				char addr = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				if ((addr & 0xFF) == 0xFF)
				{
					loc = (char) (accessMemory(addr) | (accessMemory((char) (addr & 0xFF00)) << 8));
				}
				else
				{
					loc = (char) (accessMemory(addr) | (accessMemory((char) (addr + 1)) << 8));
				}
				break;
			}
			JMP(loc);
		}
		else if (opcode == 0x20)
		{
			char loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
			tick();
			JSR(loc);
		}
		else if (opcode == 0xA9 || opcode == 0xA5 || opcode == 0xB5
				|| opcode == 0xAD || opcode == 0xBD || opcode == 0xB9
				|| opcode == 0xA1 || opcode == 0xB1)
		{
			char operand = 0;
			char zpAddr = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0xA9:
				operand = accessMemory(++pc);
				break;
			case 0xA5:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0xB5:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick();
				break;
			case 0xAD:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			case 0xBD:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + x));
				if ((loc & 0x0F00) != ((loc + x) & 0xF00))
				{
					tick();
				}
				break;
			case 0xB9:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			case 0xA1:
				zpAddr = (char) ((accessMemory(++pc) + x) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0xB1:
				zpAddr = (char) (accessMemory(++pc) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			}
			LDA(operand);
		}
		else if (opcode == 0xA2 || opcode == 0xA6 || opcode == 0xB6
				|| opcode == 0xAE || opcode == 0xBE)
		{
			char operand = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0xA2:
				operand = accessMemory(++pc);
				break;
			case 0xA6:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0xB6:
				loc = (char) ((accessMemory(++pc) + y) & 0xFF);
				operand = accessMemory(loc);
				tick();
				break;
			case 0xAE:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			case 0xBE:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			}
			LDX(operand);
		}
		else if (opcode == 0xA0 || opcode == 0xA4 || opcode == 0xB4
				|| opcode == 0xAC || opcode == 0xBC)
		{
			char operand = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0xA0:
				operand = accessMemory(++pc);
				break;
			case 0xA4:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0xB4:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick();
				break;
			case 0xAC:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			case 0xBC:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + x));
				if ((loc & 0x0F00) != ((loc + x) & 0x0F00))
				{
					tick();
				}
				break;
			}
			LDY(operand);
		}
		else if (opcode == 0x4A || opcode == 0x46 || opcode == 0x56
				|| opcode == 0x4E || opcode == 0x5E)
		{
			char operand = 0;
			char loc = 0;
			boolean go = true;
			switch (opcode)
			{
			case 0x4A:
				LSR();
				tick();
				go = false;
				break;
			case 0x46:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				tick();
				break;
			case 0x56:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick(2);
				break;
			case 0x4E:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0x5E:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				loc = (char) (loc + x);
				operand = accessMemory((char) (loc));
				tick(3);
				break;
			}
			if (go)
				LSR(operand, loc);
		}
		else if (opcode == 0xEA)
		{
			tick();
			NOP();
		}
		else if (opcode == 0x09 || opcode == 0x05 || opcode == 0x15
				|| opcode == 0x0D || opcode == 0x1D || opcode == 0x19
				|| opcode == 0x01 || opcode == 0x11)
		{
			char operand = 0;
			char zpAddr = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0x09:
				operand = accessMemory(++pc);
				break;
			case 0x05:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0x15:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick();
				break;
			case 0x0D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			case 0x1D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + x));
				if ((loc & 0x0F00) != ((loc + x) & 0xF00))
				{
					tick();
				}
				break;
			case 0x19:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			case 0x01:
				zpAddr = (char) ((accessMemory(++pc) + x) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0x11:
				zpAddr = (char) (accessMemory(++pc) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			}
			ORA(operand);
		}
		else if (opcode == 0x48)
		{
			PHA();
			tick();
		}
		else if (opcode == 0x08)
		{
			PHP();
			tick();
		}
		else if (opcode == 0x68)
		{
			PLA();
			tick(2);
		}
		else if (opcode == 0x28)
		{
			PLP();
			tick(2);
		}
		else if (opcode == 0x2A || opcode == 0x26 || opcode == 0x36
				|| opcode == 0x2E || opcode == 0x3E)
		{
			char operand = 0;
			char loc = 0;
			boolean go = true;
			switch (opcode)
			{
			case 0x2A:
				ROL();
				tick();
				go = false;
				break;
			case 0x26:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				tick();
				break;
			case 0x36:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick(2);
				break;
			case 0x2E:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0x3E:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				loc = (char) (loc + x);
				operand = accessMemory(loc);
				tick(3);
				break;
			}
			if (go)
				ROL(operand, loc);
		}
		else if (opcode == 0x6A || opcode == 0x66 || opcode == 0x76
				|| opcode == 0x6E || opcode == 0x7E)
		{
			char operand = 0;
			char loc = 0;
			boolean go = true;
			switch (opcode)
			{
			case 0x6A:
				ROR();
				tick();
				go = false;
				break;
			case 0x66:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				tick();
				break;
			case 0x76:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick(2);
				break;
			case 0x6E:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0x7E:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				loc = (char) (loc + x);
				operand = accessMemory(loc);
				tick(3);
				break;
			}
			if (go)
				ROR(operand, loc);
		}
		else if (opcode == 0x40)
		{
			RTI();
			tick(2);
		}
		else if (opcode == 0x60)
		{
			RTS();
			tick(3);
		}
		else if (opcode == 0xE9 || opcode == 0xE5 || opcode == 0xF5
				|| opcode == 0xED || opcode == 0xFD || opcode == 0xF9
				|| opcode == 0xE1 || opcode == 0xF1)
		{
			char operand = 0;
			char zpAddr = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0xE9:
				operand = accessMemory(++pc);
				break;
			case 0xE5:
				loc = accessMemory(++pc);
				operand = accessMemory(loc);
				break;
			case 0xF5:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				operand = accessMemory(loc);
				tick();
				break;
			case 0xED:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				break;
			case 0xFD:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + x));
				if ((loc & 0x0F00) != ((loc + x) & 0xF00))
				{
					tick();
				}
				break;
			case 0xF9:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			case 0xE1:
				zpAddr = (char) ((accessMemory(++pc) + x) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory(loc);
				tick();
				break;
			case 0xF1:
				zpAddr = (char) (accessMemory(++pc) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory((char) (loc + y));
				if ((loc & 0x0F00) != ((loc + y) & 0x0F00))
				{
					tick();
				}
				break;
			}
			SBC(operand);
		}
		else if (opcode == 0x38)
		{
			SEC();
			tick();
		}
		else if (opcode == 0xF8)
		{
			SED();
			tick();
		}
		else if (opcode == 0x78)
		{
			SEI();
			tick();
		}
		else if (opcode == 0x85 || opcode == 0x95 || opcode == 0x8D
				|| opcode == 0x9D || opcode == 0x99 || opcode == 0x81
				|| opcode == 0x91)
		{
			char zpAddr = 0;
			char loc = 0;
			switch (opcode)
			{
			case 0x85:
				loc = accessMemory(++pc);
				break;
			case 0x95:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				tick();
				break;
			case 0x8D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				break;
			case 0x9D:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				loc = (char) (loc + x);
				tick();
				break;
			case 0x99:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				loc = (char) (loc + y);
				tick();
				break;
			case 0x81:
				zpAddr = (char) ((accessMemory(++pc) + x) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				tick();
				break;
			case 0x91:
				zpAddr = (char) (accessMemory(++pc) & 0xFF);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				loc = (char) (loc + y);
				tick();
				break;
			}
			STA(loc);
		}
		else if (opcode == 0x86 || opcode == 0x96 || opcode == 0x8E)
		{
			char loc = 0;
			switch (opcode)
			{
			case 0x86:
				loc = accessMemory(++pc);
				break;
			case 0x96:
				loc = (char) ((accessMemory(++pc) + y) & 0xFF);
				tick();
				break;
			case 0x8E:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				break;
			}
			STX(loc);
		}
		else if (opcode == 0x84 || opcode == 0x94 || opcode == 0x8C)
		{
			char loc = 0;
			switch (opcode)
			{
			case 0x84:
				loc = accessMemory(++pc);
				break;
			case 0x94:
				loc = (char) ((accessMemory(++pc) + x) & 0xFF);
				tick();
				break;
			case 0x8C:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				break;
			}
			STY(loc);
		}
		else if (opcode == 0xAA)
		{
			TAX();
			tick();
		}
		else if (opcode == 0xA8)
		{
			TAY();
			tick();
		}
		else if (opcode == 0xBA)
		{
			TSX();
			tick();
		}
		else if (opcode == 0x8A)
		{
			TXA();
			tick();
		}
		else if (opcode == 0x9A)
		{
			TXS();
			tick();
		}
		else if (opcode == 0x98)
		{
			TYA();
			tick();
		}
		else
		{
			System.out.printf("Invalid opcode at 0x%04x%n", (int)pc);
		}

		pc++;
	}

	public char accessMemory(boolean write, char addr, char b)
	{
		if (reset && write)
			write = false;
		tick();
		if (addr < 0x2000)
		{
			// Internal RAM
			if (!write)
			{
				return (char) (memory[addr] & 0xFF);
			}
			else
			{
				memory[addr] = b;
			}
		}
		else if (addr < 0x4000)
		{
			// PPU registers
		}
		else if (addr < 0x8000)
		{
			// APU registers
		}
		else
		{
			// Cartridge access
			return game.access(write, addr, b);
		}
		return 0;
	}

	public char accessMemory(char address)
	{
		return accessMemory(false, address, (char) 0);
	}

	// Opcodes, as far as the eye can see!
	public void ADC(char operand)
	{
		char result = (char) (a + operand + c);
		c = 0;
		if (result > 0xFF)
			c = 1;
		result &= 0xFF;
		v = 0;
		if (((a ^ result) & (operand ^ result) & 0x80) != 0)
			v = 1;
		a = result;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void AND(char operand)
	{
		a &= operand;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void ASL()
	{
		c = (byte) (a >> 7);
		a = (char) ((a << 1) & 0xFF);
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void ASL(char operand, char loc)
	{
		c = (byte) (operand >> 7);
		operand = (char) ((operand << 1) & 0xFF);
		accessMemory(true, loc, operand);
		z = 0;
		if (operand == 0)
			z = 1;
		n = (byte) (operand >> 7);
	}

	public void BCC(char operand)
	{
		if (c == 0)
		{
			tick();
			pc += operand;
			if (((pc - operand) & 0x0F00) != (pc & 0x0F00))
			{
				tick();
			}
		}
	}

	public void BCS(char operand)
	{
		if (c == 1)
		{
			tick();
			pc += operand;
			if (((pc - operand) & 0x0F00) != (pc & 0x0F00))
			{
				tick();
			}
		}
	}

	public void BEQ(char operand)
	{
		if (z == 1)
		{
			tick();
			pc += operand;
			if (((pc - operand) & 0x0F00) != (pc & 0x0F00))
			{
				tick();
			}
		}
	}

	public void BIT(char operand)
	{
		z = 0;
		if ((a & operand) == 0)
			z = 1;
		v = (byte) ((operand & 0x40) >> 6);
		n = (byte) (operand >> 7);
	}

	public void BMI(char operand)
	{
		if (n == 1)
		{
			tick();
			pc += operand;
			if (((pc - operand) & 0x0F00) != (pc & 0x0F00))
			{
				tick();
			}
		}
	}

	public void BNE(char operand)
	{
		if (z == 0)
		{
			tick();
			pc += operand;
			if (((pc - operand) & 0x0F00) != (pc & 0x0F00))
			{
				tick();
			}
		}
	}

	public void BPL(char operand)
	{
		if (n == 0)
		{
			tick();
			pc += operand;
			if (((pc - operand) & 0x0F00) != (pc & 0x0F00))
			{
				tick();
			}
		}
	}

	public void BRK()
	{
		accessMemory((char) (pc + 1));
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc >> 4));
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc & 0xFF));
		char status = (char) ((n << 7) | (v << 6) | (1 << 5) | (1 << 4)
				| (d << 3) | (i << 2) | (z << 1) | (c));
		accessMemory(true, (char) (0x0100 + sp--), status);
		pc = (char) (accessMemory((char) 0xFFFE) | (accessMemory((char) 0xFFFF) << 8));
		pc--;
		tick();
	}

	public void NMI()
	{
		accessMemory((char) (pc + 1));
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc >> 4));
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc & 0xFF));
		char status = (char) ((n << 7) | (v << 6) | (1 << 5) | (1 << 4)
				| (d << 3) | (i << 2) | (z << 1) | (c));
		accessMemory(true, (char) (0x0100 + sp--), status);
		pc = (char) (accessMemory((char) 0xFFFA) | (accessMemory((char) 0xFFFB) << 8));
		pc--;
		tick();
	}

	public void BVC(char operand)
	{
		if (v == 0)
		{
			tick();
			pc += operand;
			if (((pc - operand) & 0x0F00) != (pc & 0x0F00))
			{
				tick();
			}
		}
	}

	public void BVS(char operand)
	{
		if (v == 1)
		{
			tick();
			pc += operand;
			if (((pc - operand) & 0x0F00) != (pc & 0x0F00))
			{
				tick();
			}
		}
	}

	public void CLC()
	{
		c = 0;
	}

	public void CLD()
	{
		d = 0;
	}

	public void CLI()
	{
		i = 0;
	}

	public void CLV()
	{
		v = 0;
	}

	public void CMP(char operand)
	{
		c = 0;
		if (a >= operand)
			c = 1;
		z = 0;
		if (a == operand)
			z = 1;
		n = (byte) (((a - operand) & 0xFF) >> 7);
	}

	public void CPX(char operand)
	{
		c = 0;
		if (x >= operand)
			c = 1;
		z = 0;
		if (x == operand)
			z = 1;
		n = (byte) (((x - operand) & 0xFF) >> 7);
	}

	public void CPY(char operand)
	{
		c = 0;
		if (y >= operand)
			c = 1;
		z = 0;
		if (y == operand)
			z = 1;
		n = (byte) (((y - operand) & 0xFF) >> 7);
	}

	public void DEC(char loc)
	{
		char cur = accessMemory(loc);
		cur--;
		cur &= 0xFF;
		accessMemory(true, loc, cur);
		z = 0;
		if (cur == 0)
			z = 1;
		n = (byte) (cur >> 7);
	}

	public void DEX()
	{
		x--;
		x &= 0xFF;
		z = 0;
		if (x == 0)
			z = 1;
		n = (byte) (x >> 7);
	}

	public void DEY()
	{
		y--;
		y &= 0xFF;
		z = 0;
		if (y == 0)
			z = 1;
		n = (byte) (y >> 7);
	}

	public void EOR(char operand)
	{
		a ^= operand;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void INC(char loc)
	{
		char cur = accessMemory(loc);
		cur++;
		cur &= 0xFF;
		accessMemory(true, loc, cur);
		z = 0;
		if (cur == 0)
			z = 1;
		n = (byte) (cur >> 7);
	}

	public void INX()
	{
		tick();
		x++;
		x &= 0xFF;
		z = 0;
		if (x == 0)
			z = 1;
		n = (byte) (x >> 7);
	}

	public void INY()
	{
		tick();
		y++;
		y &= 0xFF;
		z = 0;
		if (y == 0)
			z = 1;
		n = (byte) (y >> 7);
	}

	public void JMP(char operand)
	{
		pc = operand;
		pc--;
	}

	public void JSR(char operand)
	{
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc >> 8));
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc & 0xFF));
		pc = operand;
		pc--;
	}

	public void LDA(char operand)
	{
		a = operand;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void LDX(char operand)
	{
		x = operand;
		z = 0;
		if (x == 0)
			z = 1;
		n = (byte) (x >> 7);
	}

	public void LDY(char operand)
	{
		y = operand;
		z = 0;
		if (y == 0)
			z = 1;
		n = (byte) (y >> 7);
	}

	public void LSR()
	{
		c = (byte) (a & 0x01);
		a >>= 1;
		z = 0;
		if (c == 0)
			z = 1;
		n = 0;
	}

	public void LSR(char operand, char loc)
	{
		c = (byte) (operand & 0x01);
		operand >>= 1;
		accessMemory(true, loc, operand);
		z = 0;
		if (operand == 0)
			z = 1;
		n = 0;
	}

	public void NOP()
	{

	}

	public void ORA(char operand)
	{
		a |= operand;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void PHA()
	{
		accessMemory(true, (char) (0x0100 + sp--), a);
	}

	public void PHP()
	{
		char status = (char) ((n << 7) | (v << 6) | (1 << 5) | (1 << 4)
				| (d << 3) | (i << 2) | (z << 1) | (c));
		accessMemory(true, (char) (0x0100 + sp--), status);
	}

	public void PLA()
	{
		a = accessMemory((char) (0x100 + ++sp));
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void PLP()
	{
		char status = accessMemory((char) (0x100 + ++sp));
		c = (byte) (status & 0x01);
		z = (byte) ((status >> 1) & 0x01);
		i = (byte) ((status >> 2) & 0x01);
		d = (byte) ((status >> 3) & 0x01);
		v = (byte) ((status >> 6) & 0x01);
		n = (byte) ((status >> 7) & 0x01);
	}

	public void ROL()
	{
		byte oldC = c;
		c = (byte) (a >> 7);
		a <<= 1;
		a |= oldC;
		a &= 0xFF;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void ROL(char operand, char loc)
	{
		byte oldC = c;
		c = (byte) (operand >> 7);
		operand <<= 1;
		operand |= oldC;
		operand &= 0xFF;
		accessMemory(true, loc, operand);
		z = 0;
		if (operand == 0)
			z = 1;
		n = (byte) (operand >> 7);
	}

	public void ROR()
	{
		byte oldC = c;
		c = (byte) (a & 0x01);
		a >>= 1;
		a |= (char) (oldC << 7);
		a &= 0xFF;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void ROR(char operand, char loc)
	{
		byte oldC = c;
		c = (byte) (operand & 0x01);
		operand >>= 1;
		operand |= (char) (oldC << 7);
		operand &= 0xFF;
		accessMemory(true, loc, operand);
		z = 0;
		if (operand == 0)
			z = 1;
		n = (byte) (operand >> 7);
	}

	public void RTI()
	{
		char status = accessMemory((char) (0x100 + ++sp));
		c = (byte) (status & 0x01);
		z = (byte) ((status >> 1) & 0x01);
		i = (byte) ((status >> 2) & 0x01);
		d = (byte) ((status >> 3) & 0x01);
		v = (byte) ((status >> 6) & 0x01);
		n = (byte) ((status >> 7) & 0x01);
		pc = (char) (accessMemory((char) (0x100 + ++sp)) | (accessMemory((char) (0x100 + ++sp)) << 8));
		pc--;
	}

	public void RTS()
	{
		pc = (char) (accessMemory((char) (0x100 + ++sp)) | (accessMemory((char) (0x100 + ++sp)) << 8));
	}

	public void SBC(char operand)
	{
		char result = (char) (a - operand - (1 - c));
		c = 1;
		if (result > 0xFF)
			c = 0;
		result &= 0xFF;
		v = 0;
		if (((a ^ result) & (operand ^ result) & 0x80) != 0)
			v = 1;
		a = result;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void SEC()
	{
		c = 1;
	}

	public void SED()
	{
		d = 1;
	}

	public void SEI()
	{
		i = 1;
	}

	public void STA(char loc)
	{
		accessMemory(true, loc, a);
	}

	public void STX(char loc)
	{
		accessMemory(true, loc, x);
	}

	public void STY(char loc)
	{
		accessMemory(true, loc, y);
	}

	public void TAX()
	{
		x = a;
		z = 0;
		if (x == 0)
			z = 1;
		n = (byte) (x >> 7);
	}

	public void TAY()
	{
		y = a;
		z = 0;
		if (y == 0)
			z = 1;
		n = (byte) (y >> 7);
	}

	public void TSX()
	{
		x = sp;
		z = 0;
		if (x == 0)
			z = 1;
		n = (byte) (x >> 7);
	}

	public void TXA()
	{
		a = x;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}

	public void TXS()
	{
		sp = x;
	}

	public void TYA()
	{
		a = y;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}
}