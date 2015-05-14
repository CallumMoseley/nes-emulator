public class CPU
{
	private PPU ppu;
	private APU apu;
	
	private byte a, x, y, s;
	private byte c, z, i, d, v, n;
	private int pc;
	
	private byte[] memory;
	
	boolean irq, nmi, reset;
	
	public CPU()
	{
		a = 0;
		x = 0;
		y = 0;
		s = 0;
		
		c = 0;
		z = 0;
		i = 0;
		d = 0;
		v = 0;
		n = 0;
		
		pc = 0;
		
		memory = new byte[0x10000];
	}
	
	public void op()
	{
		if (reset)
		{
			reset = false;
			s -= 3;
			i = 1;
			pc = accessMemory(0xFFFE) | (accessMemory(0xFFFF) << 8);
		}
		byte opcode = (byte) accessMemory(pc++);
		tick(cycles[opcode]);
		
		byte operand = 0;
		opcodes[opcode].execute(operand);
	}

	private void tick(int cycles)
	{
		for (int i = 0; i < cycles; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				ppu.tick();
			}
			apu.tick();
		}
	}
	
	private void tick()
	{
		tick(1);
	}
	
	private int accessMemory(boolean write, int addr, byte d)
	{
		if (write)
		{
			memory[addr] = d;
			return 0;
		}
		else
		{
			return accessMemory(addr);
		}
	}
	
	private int accessMemory(int addr)
	{
		return accessMemory(addr);
	}
	
	private void ADC(byte operand)
	{
		byte sum = (byte) (accessMemory(operand) + a + c);
		c = 0;
		if (sum > 0xFF)
			c = 1;
		sum &= 0xFF;
		v = 0;
		if (((a ^ sum) & (operand ^ sum) & 0x80) != 0)
			v = 1;
		a = sum;
		n = (byte) (a >> 7);
		z = 0;
		if (a == 0)
			z = 1;
	}
	
	private void AND(int operand)
	{
		a &= operand;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}
	
	private void ASL()
	{
		c = (byte) (a >> 7);
		a <<= 1;
		a &= 0xFF;
		z = 0;
		if (a == 0)
			z = 1;
		n = (byte) (a >> 7);
	}
	
	private void ASL(int operand)
	{
		c = (byte) (accessMemory(operand) >> 7);
		accessMemory(true, operand, (byte) (accessMemory(operand) << 1));
		z = 0;
		if (accessMemory(operand) == 0)
			z = 1;
		n = (byte) (accessMemory(operand) >> 7);
	}
	
	private void BCC(int operand)
	{
		if (c == 0)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand;
			if ((pc & 0xFF00) != page)
			{
				tick();
			}
		}
	}
	
	private void BCS(int operand)
	{
		if (c == 1)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand;
			if ((pc & 0xFF00) != page)
			{
				tick();
			}
		}
	}
	
	private void BEQ(int operand)
	{
		if (z == 1)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand;
			if ((pc & 0xFF00) != page)
			{
				tick();
			}
		}
	}
	
	private void BIT(int operand)
	{
		int r = a & accessMemory(operand);
		z = 0;
		if (r == 0)
			z = 1;
		v = (byte) ((accessMemory(operand) & 0x80) >> 6);
		n = (byte) (accessMemory(operand) >> 7);
	}
	
	private void BMI(int operand)
	{
		if (n == 1)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand;
			if ((pc & 0xFF00) != page)
			{
				tick();
			}
		}
	}
	
	private void BNE(int operand)
	{
		if (z == 0)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand;
			if ((pc & 0xFF00) != page)
			{
				tick();
			}
		}
	}
	
	private void BPL(int operand)
	{
		if (n == 0)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand;
			if ((pc & 0xFF00) != page)
			{
				tick();
			}
		}
	}
	
	private void BRK()
	{
		pc = (accessMemory(0xFFFF) << 8) & accessMemory(0xFFFE);
	}
	
	private void BVC(int operand)
	{
		if (v == 0)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand;
			if ((pc & 0xFF00) != page)
			{
				tick();
			}
		}
	}
	
	private void BVS(int operand)
	{
		if (v == 1)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand;
			if ((pc & 0xFF00) != page)
			{
				tick();
			}
		}
	}
	
	private void CLC()
	{
		c = 0;
	}
	
	private void CLD()
	{
		d = 0;
	}
	
	private void CLI()
	{
		i = 0;
	}
	
	private void CLV()
	{
		v = 0;
	}
	
	private void CMP(int operand)
	{
		z = 0;
		if (a == operand)
		{
			z = 1;
		}
		c = 0;
		if (a >= operand)
		{
			c = 1;
		}
		n = 0;
		if (((a - operand) & 0xFF) >> 7 == 1)
		{
			n = 1;
		}
	}
	
	private void CPX(int operand)
	{
		z = 0;
		if (x == operand)
		{
			z = 1;
		}
		c = 0;
		if (x >= operand)
		{
			c = 1;
		}
		n = 0;
		if (((x - operand) & 0xFF) >> 7 == 1)
		{
			n = 1;
		}
	}
	
	private void CPY(int operand)
	{
		z = 0;
		if (y == operand)
		{
			z = 1;
		}
		c = 0;
		if (y >= operand)
		{
			c = 1;
		}
		n = 0;
		if (((y - operand) & 0xFF) >> 7 == 1)
		{
			n = 1;
		}
	}
	
	private void DEC(int operand)
	{
		byte t = (byte) (accessMemory(operand) - 1);
		accessMemory(true, operand, t);
		z = 0;
		if (t == 0)
		{
			z = 1;
		}
		n = (byte) (t >> 7);
	}
	
	private void DEX(int operand)
	{
		byte t = (byte) (x - 1);
		accessMemory(true, operand, t);
		z = 0;
		if (t == 0)
		{
			z = 1;
		}
		n = (byte) (t >> 7);
	}
	
	private void DEY(int operand)
	{
		byte t = (byte) (y - 1);
		accessMemory(true, operand, t);
		z = 0;
		if (t == 0)
		{
			z = 1;
		}
		n = (byte) (t >> 7);
	}
	
	private void EOR(int operand)
	{
		a ^= operand;
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = (byte) (a >> 7);
	}
	
	private void NOP()
	{
		
	}
	
	interface Opcode
	{
		public void execute(byte operand);
	}
	
	private Opcode[] opcodes = new Opcode[]
	{
		// 0
		new Opcode() { public void execute(byte operand) { BRK(); } },		  // 0
		new Opcode() { public void execute(byte operand) { ORA(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 4
		new Opcode() { public void execute(byte operand) { ORA(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { ASL(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { PHP(); } },		  // 8
		new Opcode() { public void execute(byte operand) { ORA(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { ASL(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // C
		new Opcode() { public void execute(byte operand) { ORA(operand); } }, // D
		new Opcode() { public void execute(byte operand) { ASL(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// 1
		new Opcode() { public void execute(byte operand) { BPL(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { ORA(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 4
		new Opcode() { public void execute(byte operand) { ORA(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { ASL(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { CLC(); } },		  // 8
		new Opcode() { public void execute(byte operand) { ORA(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // C
		new Opcode() { public void execute(byte operand) { ORA(operand); } }, // D
		new Opcode() { public void execute(byte operand) { ASL(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// 2
		new Opcode() { public void execute(byte operand) { JSR(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { AND(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { BIT(operand); } }, // 4
		new Opcode() { public void execute(byte operand) { AND(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { ROL(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { PLP(); } },		  // 8
		new Opcode() { public void execute(byte operand) { AND(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { ROL(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { BIT(operand); } }, // C
		new Opcode() { public void execute(byte operand) { AND(operand); } }, // D
		new Opcode() { public void execute(byte operand) { ROL(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// 3
		new Opcode() { public void execute(byte operand) { BMI(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { AND(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 4
		new Opcode() { public void execute(byte operand) { AND(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { ROL(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { SEC(); } },		  // 8
		new Opcode() { public void execute(byte operand) { AND(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // A
		new Opcode() { public void execute(byte operand) { SOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // C
		new Opcode() { public void execute(byte operand) { AND(operand); } }, // D
		new Opcode() { public void execute(byte operand) { ROL(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// 4
		new Opcode() { public void execute(byte operand) { RTI(); } },		  // 0
		new Opcode() { public void execute(byte operand) { EOR(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 4
		new Opcode() { public void execute(byte operand) { EOR(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { LSR(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { PHA(); } },		  // 8
		new Opcode() { public void execute(byte operand) { EOR(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { LSR(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { JMP(operand); } }, // C
		new Opcode() { public void execute(byte operand) { ADC(operand); } }, // D
		new Opcode() { public void execute(byte operand) { ROR(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// 5
		new Opcode() { public void execute(byte operand) { BVC(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { EOR(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 4
		new Opcode() { public void execute(byte operand) { EOR(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { LSR(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { CLI(); } },		  // 8
		new Opcode() { public void execute(byte operand) { EOR(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // C
		new Opcode() { public void execute(byte operand) { EOR(operand); } }, // D
		new Opcode() { public void execute(byte operand) { LSR(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// 6
		new Opcode() { public void execute(byte operand) { RTS(); } },		  // 0
		new Opcode() { public void execute(byte operand) { ADC(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 4
		new Opcode() { public void execute(byte operand) { ADC(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { ROR(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { PLA(); } },		  // 8
		new Opcode() { public void execute(byte operand) { ADC(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { ROR(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { JMP(operand); } }, // C
		new Opcode() { public void execute(byte operand) { ADC(operand); } }, // D
		new Opcode() { public void execute(byte operand) { ROR(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// 7
		new Opcode() { public void execute(byte operand) { BVS(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { ADC(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 4
		new Opcode() { public void execute(byte operand) { ADC(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { ROR(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { SEI(); } },		  // 8
		new Opcode() { public void execute(byte operand) { ADC(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // C
		new Opcode() { public void execute(byte operand) { ADC(operand); } }, // D
		new Opcode() { public void execute(byte operand) { ROR(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// 8
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 0
		new Opcode() { public void execute(byte operand) { STA(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { STY(operand); } }, // 4
		new Opcode() { public void execute(byte operand) { STA(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { STX(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { DEY(); } },		  // 8
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 9
		new Opcode() { public void execute(byte operand) { TXA(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { STY(operand); } }, // C
		new Opcode() { public void execute(byte operand) { STA(operand); } }, // D
		new Opcode() { public void execute(byte operand) { STX(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F

		// 9
		new Opcode() { public void execute(byte operand) { BCC(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { STA(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { STY(operand); } }, // 4
		new Opcode() { public void execute(byte operand) { STA(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { STX(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { TYA(); } },		  // 8
		new Opcode() { public void execute(byte operand) { STA(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { TXS(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // C
		new Opcode() { public void execute(byte operand) { STA(operand); } }, // D
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// A
		new Opcode() { public void execute(byte operand) { LDY(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { LDA(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { LDX(operand); } }, // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { LDY(operand); } }, // 4
		new Opcode() { public void execute(byte operand) { LDA(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { LDX(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { TAY(); } },		  // 8
		new Opcode() { public void execute(byte operand) { LDA(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { TAX(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { LDY(operand); } }, // C
		new Opcode() { public void execute(byte operand) { LDA(operand); } }, // D
		new Opcode() { public void execute(byte operand) { LDX(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// B
		new Opcode() { public void execute(byte operand) { BCS(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { LDA(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { LDY(operand); } }, // 4
		new Opcode() { public void execute(byte operand) { LDA(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { LDX(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { CLV(); } },		  // 8
		new Opcode() { public void execute(byte operand) { LDA(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { TSX(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { LDY(operand); } }, // C
		new Opcode() { public void execute(byte operand) { LDA(operand); } }, // D
		new Opcode() { public void execute(byte operand) { LDX(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// C
		new Opcode() { public void execute(byte operand) { CPY(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { CMP(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { CPY(operand); } }, // 4
		new Opcode() { public void execute(byte operand) { CMP(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { DEC(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { INY(); } },		  // 8
		new Opcode() { public void execute(byte operand) { CMP(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { DEX(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { CPY(operand); } }, // C
		new Opcode() { public void execute(byte operand) { CMP(operand); } }, // D
		new Opcode() { public void execute(byte operand) { DEC(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// D
		new Opcode() { public void execute(byte operand) { BNE(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { CMP(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 4
		new Opcode() { public void execute(byte operand) { CMP(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { DEC(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { CLD(); } },		  // 8
		new Opcode() { public void execute(byte operand) { CMP(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // C
		new Opcode() { public void execute(byte operand) { CMP(operand); } }, // D
		new Opcode() { public void execute(byte operand) { DEC(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// E
		new Opcode() { public void execute(byte operand) { CPX(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { SBC(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { CPX(operand); } }, // 4
		new Opcode() { public void execute(byte operand) { SBC(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { INC(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { INX(); } },		  // 8
		new Opcode() { public void execute(byte operand) { SBC(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { CPX(operand); } }, // C
		new Opcode() { public void execute(byte operand) { SBC(operand); } }, // D
		new Opcode() { public void execute(byte operand) { INC(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // F
		
		// F
		new Opcode() { public void execute(byte operand) { BEQ(operand); } }, // 0
		new Opcode() { public void execute(byte operand) { SBC(operand); } }, // 1
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 2
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 3
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 4
		new Opcode() { public void execute(byte operand) { SBC(operand); } }, // 5
		new Opcode() { public void execute(byte operand) { INC(operand); } }, // 6
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // 7
		new Opcode() { public void execute(byte operand) { SED(); } },		  // 8
		new Opcode() { public void execute(byte operand) { SBC(operand); } }, // 9
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // A
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // C
		new Opcode() { public void execute(byte operand) { SBC(operand); } }, // D
		new Opcode() { public void execute(byte operand) { INC(operand); } }, // E
		new Opcode() { public void execute(byte operand) { NOP(); } }		  // F
	};

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
							  "...","STA","...","...","STY","STA","STX","...","DEY","...","TSX","...","STY","STA","STX","...", // 8
							  "BCC","STA","...","...","STY","STA","STX","...","TYA","STA","TXS","...","...","STA","...","...", // 9
							  "LDY","LDA","LDX","...","LDY","LDA","LDX","...","TAY","LDA","TAX","...","LDY","LDA","LDX","...", // A
							  "BCS","LDA","...","...","LDY","LDA","LDX","...","CLV","LDA","...","...","LDY","LDA","LDX","...", // B
							  "CPY","CMP","...","...","CPY","CMP","DEC","...","INY","CMP","DEX","...","CPY","CMP","DEC","...", // C
							  "BNE","CMP","...","...","...","CMP","DEC","...","CLD","CMP","...","...","...","CMP","DEC","...", // D
							  "CPX","SBC","...","...","CPX","SBC","INC","...","INX","SBC","NOP","...","CPX","SBC","INC","...", // E
							  "BEQ","SBC","...","...","...","SBC","INC","...","SED","SBC","...","...","...","SBC","INC","..."};// F
	
	//                  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F
	final int[] cycles = {
						7, 6, 0, 0, 0, 3, 5, 0, 3, 2, 2, 0, 0, 4, 6, 0, // 0
						2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0, // 1
						6, 6, 0, 0, 3, 3, 5, 0, 4, 2, 2, 0, 4, 4, 6, 0, // 2
						2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0, // 3
						6, 6, 0, 0, 0, 3, 5, 0, 3, 2, 2, 0, 3, 4, 6, 0, // 4
						2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0, // 5
						6, 6, 0, 0, 0, 3, 5, 0, 4, 2, 2, 0, 5, 4, 6, 0, // 6
						2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0, // 7
						0, 6, 0, 0, 3, 3, 3, 0, 2, 0, 2, 0, 4, 4, 4, 0, // 8
						2, 6, 0, 0, 4, 4, 4, 0, 2, 5, 2, 0, 0, 5, 0, 0, // 9
						2, 6, 2, 0, 3, 3, 3, 0, 2, 2, 2, 0, 4, 4, 4, 0, // A
						2, 5, 0, 0, 4, 4, 4, 0, 2, 4, 2, 0, 4, 4, 4, 0, // B
						2, 6, 0, 0, 3, 3, 5, 0, 2, 2, 2, 0, 4, 4, 6, 0, // C
						2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0, // D
						2, 6, 0, 0, 4, 3, 5, 0, 2, 2, 2, 0, 4, 4, 6, 0, // E
						2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0};// F
}