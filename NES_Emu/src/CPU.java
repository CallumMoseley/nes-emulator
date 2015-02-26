public class CPU
{
	private final float CLOCK_SPEED = 1.79f; // MHz

	private char a, x, y, sp;
	private byte c, z, i, d, v, n;
	private char pc;

	private char[] memory = new char[0x10000];

	private GamePak game;
	private PPU ppu;
	private APU apu;

	private boolean reset, nmi, irq;

	private long lastClock;
	private long cycleCount;
	private long instructionCount;

	public CPU()
	{
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

		lastClock = System.nanoTime();
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
		cycleCount++;
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				ppu.tick();
			}
			// apu.tick();
		}
		
//		try
//		{
//			long nanoDelay = (long) (1000000000 / (CLOCK_SPEED * 1000) - (System.nanoTime() - lastClock));
//			long milliDelay = nanoDelay / 1000000000;
//			nanoDelay %= 1000000000;
//			Thread.sleep(Math.max(0, milliDelay), Math.max(0, (int)nanoDelay));
//			lastClock = System.nanoTime();
//		}
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
	}
	
	public void op()
	{
		char opcode = accessMemory(pc);
		if (reset)
		{
			reset = false;
			cycleCount = 0;
			instructionCount = 0;
			pc = (char) (accessMemory((char) 0xFFFC) | (accessMemory((char) 0xFFFD) << 8));
			pc--;
			i = 1;
			d = (byte) ((int) (Math.random() * 2));
			tick(5);
		}
		else if (nmi)
		{
			nmi = false;
			NMI();
		}
		else if ((irq && i != 1) || opcode == 0x00)
		{
			BRK();
		}
		else if (opcode == 0x20)
		{
			JSR((char) (accessMemory(++pc) | (accessMemory(++pc) << 8)));
		}
		else if (opcode == 0x40)
		{
			RTI();
		}
		else if (opcode == 0x60)
		{
			RTS();
		}
		else if (opcode == 0x08)
		{
			PHP();
		}
		else if (opcode == 0x28)
		{
			PLP();
		}
		else if (opcode == 0x48)
		{
			PHA();
		}
		else if (opcode == 0x68)
		{
			PLA();
		}
		else if (opcode == 0x88)
		{
			DEY();
		}
		else if (opcode == 0xA8)
		{
			TAY();
		}
		else if (opcode == 0xC8)
		{
			INY();
		}
		else if (opcode == 0xE8)
		{
			INX();
		}
		else if (opcode == 0x18)
		{
			CLC();
		}
		else if (opcode == 0x38)
		{
			SEC();
		}
		else if (opcode == 0x58)
		{
			CLI();
		}
		else if (opcode == 0x78)
		{
			SEI();
		}
		else if (opcode == 0x98)
		{
			TYA();
		}
		else if (opcode == 0xB8)
		{
			CLV();
		}
		else if (opcode == 0xD8)
		{
			CLD();
		}
		else if (opcode == 0xF8)
		{
			SED();
		}
		else if (opcode == 0x8A)
		{
			TXA();
		}
		else if (opcode == 0x9A)
		{
			TXS();
		}
		else if (opcode == 0xAA)
		{
			TAX();
		}
		else if (opcode == 0xBA)
		{
			TSX();
		}
		else if (opcode == 0xCA)
		{
			DEX();
		}
		else if (opcode == 0xEA)
		{
			NOP();
		}
		else
		{
			char instructionSubset = (char) (opcode & 0x03);
			char instruction = (char) ((opcode >> 5) & 0x07);
			if (instructionSubset == 0b01)
			{
				int addressMode = (opcode >> 2) & 0x07;
				char addr = 0;
				if (addressMode == 0b000 || addressMode == 0b101)
				{
					addr = (char) ((accessMemory(++pc) + x) & 0xFF);
				}
				else if (addressMode == 0b001)
				{
					addr = accessMemory(++pc);
				}
				else if (addressMode == 0b010)
				{
					addr = (char) (++pc);
				}
				else if (addressMode == 0b011)
				{
					addr = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				}
				else if (addressMode == 0b100)
				{
					addr = (char) ((accessMemory(++pc) + y) & 0xFF);
				}
				else if (addressMode == 0b110)
				{
					addr = (char) ((accessMemory(++pc) | (accessMemory(++pc) << 8)) + y);
				}
				else if (addressMode == 0b111)
				{
					addr = (char) ((accessMemory(++pc) | (accessMemory(++pc) << 8)) + x);
				}
				
				switch (instruction)
				{
				case 0x000:
					ORA(addr);
					break;
				case 0x001:
					AND(addr);
					break;
				case 0x010:
					EOR(addr);
					break;
				case 0x011:
					ADC(addr);
					break;
				case 0x100:
					STA(addr);
					break;
				case 0x101:
					LDA(addr);
					break;
				case 0x110:
					CMP(addr);
					break;
				case 0x111:
					SBC(addr);
					break;
				}
			}
			else if (instructionSubset == 0b10)
			{
				
			}
			else if (instructionSubset == 0b00)
			{
				
			}
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
			return ppu.accessRegisters(write, addr, b);
		}
		else if (addr < 0x8000)
		{
			// APU registers and joypads
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
		byte o = (byte)operand;
		if (c == 0)
		{
			tick();
			if (((pc + o) & 0xFF00) != (pc & 0xFF00))
			{
				tick();
			}
			pc = (char) (pc + o);
		}
	}

	public void BCS(char operand)
	{
		byte o = (byte)operand;
		if (c == 1)
		{
			tick();
			if (((pc + o) & 0xFF00) != (pc & 0xFF00))
			{
				tick();
			}
			pc = (char) (pc + o);
		}
	}

	public void BEQ(char operand)
	{
		byte o = (byte)operand;
		if (z == 1)
		{
			tick();
			if (((pc + o) & 0xFF00) != (pc & 0xFF00))
			{
				tick();
			}
			pc = (char) (pc + o);
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
		byte o = (byte)operand;
		if (n == 1)
		{
			tick();
			if (((pc + o) & 0xFF00) != (pc & 0xFF00))
			{
				tick();
			}
			pc = (char) (pc + o);
		}
	}

	public void BNE(char operand)
	{
		byte o = (byte)operand;
		if (z == 0)
		{
			tick();
			if (((pc + o) & 0xFF00) != (pc & 0xFF00))
			{
				tick();
			}
			pc = (char) (pc + o);
		}
	}

	public void BPL(char operand)
	{
		byte o = (byte)operand;
		if (n == 0)
		{
			tick();
			if (((pc + o) & 0xFF00) != (pc & 0xFF00))
			{
				tick();
			}
			pc = (char) (pc + o);
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
		byte o = (byte)operand;
		if (v == 0)
		{
			tick();
			if (((pc + o) & 0xFF00) != (pc & 0xFF00))
			{
				tick();
			}
			pc = (char) (pc + o);
		}
	}

	public void BVS(char operand)
	{
		byte o = (byte)operand;
		if (v == 1)
		{
			tick();
			if (((pc + o) & 0xFF00) != (pc & 0xFF00))
			{
				tick();
			}
			pc = (char) (pc + o);
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
	
	// Debugging stuff
	
	//						   0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F
	final String[] opcodeNames = {
							  "BRK","ORA","...","...","...","ORA","ASL","...","PHP","ORA","ASL","...","...","ORA","ASL","...", // 0
							  "BPL","ORA","...","...","...","ORA","ASL","...","CLC","ORA","...","...","...","ORA","ASL","...", // 1
							  "JSR","AND","...","...","BIT","AND","ROL","...","PLP","AND","ROL","...","BIT","AND","ROL","...", // 2
							  "BMI","AND","...","...","...","AND","ROL","...","SEC","AND","...","...","...","AND","ROL","...", // 3
							  "RTI","EOR","...","...","...","EOR","LSR","...","PHA","EOR","LSR","...","JMP","EOR","LSR","...", // 4
							  "BVC","EOR","...","...","...","EOR","LSR","...","CLI","EOR","...","...","...","EOR","LSR","...", // 5
							  "RTS","ADC","...","...","...","ADC","ROR","...","PLA","ADC","ROR","...","JMP","ADC","ROR","...", // 6
							  "BVS","ADC","...","...","...","ADC","ROR","...","SEI","ADC","...","...","...","ADC","ROR","...", // 7
							  "...","STA","...","...","STY","STA","STX","...","DEY","STA","TSX","...","STY","STA","STX","...", // 8
							  "BCC","STA","...","...","STY","STA","STX","...","TYA","STA","TXS","...","...","STA","...","...", // 9
							  "LDY","LDA","LDX","...","LDY","LDA","LDX","...","TAY","LDA","TAX","...","LDY","LDA","LDX","...", // A
							  "BCS","LDA","...","...","LDY","LDA","LDX","...","CLV","LDA","...","...","LDY","LDA","LDX","...", // B
							  "CPY","CMP","...","...","CPY","CMP","DEC","...","INY","CMP","DEX","...","CPY","CMP","DEC","...", // C
							  "BNE","CMP","...","...","...","CMP","DEC","...","CLD","CMP","...","...","...","CMP","DEC","...", // D
							  "CPX","SBC","...","...","CPX","SBC","INC","...","INX","SBC","NOP","...","CPX","SBC","INC","...", // E
							  "BEQ","SBC","...","...","...","SBC","INC","...","SED","SBC","...","...","...","SBC","INC","..."};// F
}