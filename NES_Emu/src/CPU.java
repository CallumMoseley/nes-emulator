public class CPU
{
	private PPU ppu;
	private APU apu;
	
	private int a, x, y, s, c, z, i, d, v, n, pc;
	
	private int[] memory;
	
	boolean irq, nmi, reset;
	
	public CPU()
	{
		a = 0;
		x = 0;
		y = 0;
		s = 0xFD;
		
		c = 0;
		z = 0;
		i = 0;
		d = 0;
		v = 0;
		n = 0;
		
		pc = 0;
		
		memory = new int[0x10000];
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
	
	private int accessMemory(boolean write, int addr, int d)
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
	
	private void ADC(int operand)
	{
		int sum = accessMemory(operand) + a + c;
		c = 0;
		if (sum > 0xFF)
			c = 1;
		sum &= 0xFF;
		v = 0;
		if (((a ^ sum) & (accessMemory(operand) ^ sum) & 0x80) != 0)
			v = 1;
		a = sum;
		n = a >> 7;
		z = 0;
		if (a == 0)
			z = 1;
	}
	
	private void AND(int operand)
	{
		a &= accessMemory(operand);
		z = 0;
		if (a == 0)
			z = 1;
		n = a >> 7;
	}
	
	private void ASL()
	{
		c = a >> 7;
		a <<= 1;
		a &= 0xFF;
		z = 0;
		if (a == 0)
			z = 1;
		n = a >> 7;
	}
	
	private void ASL(int operand)
	{
		c = accessMemory(operand) >> 7;
		accessMemory(true, operand, accessMemory(operand) << 1);
		z = 0;
		if (accessMemory(operand) == 0)
			z = 1;
		n = accessMemory(operand) >> 7;
	}
	
	private void BCC(int operand)
	{
		if (c == 0)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand - 128;
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
			pc += operand - 128;
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
			pc += operand - 128;
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
		v = (accessMemory(operand) & 0x80) >> 6;
		n = accessMemory(operand) >> 7;
	}
	
	private void BMI(int operand)
	{
		if (n == 1)
		{
			tick();
			int page = pc & 0xFF00;
			pc += operand - 128;
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
			pc += operand - 128;
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
			pc += operand - 128;
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
			pc += operand - 128;
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
			pc += operand - 128;
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
		if (a == accessMemory(operand))
		{
			z = 1;
		}
		c = 0;
		if (a >= accessMemory(operand))
		{
			c = 1;
		}
		n = 0;
		if (((a - accessMemory(operand)) & 0xFF) >> 7 == 1)
		{
			n = 1;
		}
	}
	
	private void CPX(int operand)
	{
		z = 0;
		if (x == accessMemory(operand))
		{
			z = 1;
		}
		c = 0;
		if (x >= accessMemory(operand))
		{
			c = 1;
		}
		n = 0;
		if (((x - accessMemory(operand)) & 0xFF) >> 7 == 1)
		{
			n = 1;
		}
	}
	
	private void CPY(int operand)
	{
		z = 0;
		if (y == accessMemory(operand))
		{
			z = 1;
		}
		c = 0;
		if (y >= accessMemory(operand))
		{
			c = 1;
		}
		n = 0;
		if (((y - accessMemory(operand)) & 0xFF) >> 7 == 1)
		{
			n = 1;
		}
	}
	
	private void DEC(int operand)
	{
		int t = accessMemory(operand) - 1;
		accessMemory(true, operand, t);
		z = 0;
		if (t == 0)
		{
			z = 1;
		}
		n = t >> 7;
	}
	
	private void DEX()
	{
		x -= 1;
		x &= 0xFF;
		z = 0;
		if (x == 0)
		{
			z = 1;
		}
		n = x >> 7;
	}
	
	private void DEY()
	{
		y -= 1;
		y &= 0xFF;
		z = 0;
		if (y == 0)
		{
			z = 1;
		}
		n = y >> 7;
	}
	
	private void EOR(int operand)
	{
		a ^= accessMemory(operand);
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = a >> 7;
	}
	
	private void INC(int operand)
	{
		accessMemory(true, operand, accessMemory(operand) + 1);
	}
	
	private void INX()
	{
		x++;
	}
	
	private void INY()
	{
		y++;
	}
	
	private void JMP(int operand)
	{
		pc = (accessMemory(operand) << 8) | accessMemory(operand + 1);
	}
	
	private void JSR(int operand)
	{
		accessMemory(true, s--, pc & 0xFF);
		accessMemory(true, s--, pc >> 8);
		
		pc = operand;
	}
	
	private void LDA(int operand)
	{
		a = operand;
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = a >> 1;
	}
	
	private void LDX(int operand)
	{
		x = operand;
		z = 0;
		if (x == 0)
		{
			z = 0;
		}
		n = x >> 7;
	}
	
	private void LDY(int operand)
	{
		y = operand;
		z = 0;
		if (y == 0)
		{
			z = 0;
		}
		n = y >> 7;
	}

	private void LSR()
	{
		c = a & 0x01;
		a >>= 1;
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = 0;
	}
	
	private void LSR(int operand)
	{
		c = accessMemory(operand) & 0x01;
		accessMemory(true, operand, accessMemory(operand) >> 1);
		z = 0;
		if (accessMemory(operand) == 0)
		{
			z = 1;
		}
		n = 0;
	}
	
	private void NOP()
	{
		
	}
	
	private void ORA(int operand)
	{
		a |= accessMemory(operand);
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = a >> 7;
	}
	
	private void PHA()
	{
		accessMemory(true, s--, a);
	}
	
	private void PHP()
	{
		accessMemory(true, s--, (n << 7) | (v << 6) | (d << 3) | (i << 2) | (z << 1) | c);
	}
	
	private void PLA()
	{
		a = accessMemory(++s);
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = a >> 7;
	}
	
	private void PLP()
	{
		int r = accessMemory(++s);
		n = (r >> 7) & 0x01;
		v = (r >> 6) & 0x01;
		d = (r >> 3) & 0x01;
		i = (r >> 2) & 0x01;
		z = (r >> 1) & 0x01;
		c = (r) & 0x01;
	}
	
	private void ROL()
	{
		c = a >> 7;
		a <<= 1;
		a &= 0xFF;
		a |= c;
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = a >> 7;
	}
	
	private void ROL(int operand)
	{
		c = accessMemory(operand) >> 7;
		accessMemory(true, operand, ((accessMemory(operand) << 1) | c) & 0xFF);
		z = 0;
		if (accessMemory(operand) == 0)
		{
			z = 1;
		}
		n = accessMemory(operand) >> 7;
	}
	
	private void ROR()
	{
		c = a & 0x01;
		a >>= 1;
		a |= (c << 7);
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = a >> 7;
	}
	
	private void ROR(int operand)
	{
		c = accessMemory(operand) & 0x01;
		accessMemory(true, operand, (accessMemory(operand) >> 1) | (c << 7));
		z = 0;
		if (accessMemory(operand) == 0)
		{
			z = 1;
		}
		n = accessMemory(operand) >> 7;
	}
	
	private void RTI()
	{
		PLP();
		pc = (accessMemory(s++) << 8) | accessMemory(s++);
	}
	
	private void RTS()
	{
		pc = (accessMemory(s++) << 8) | accessMemory(s++);
	}
	
	public void SBC(int operand)
	{
		int result = a - accessMemory(operand) - (1 - c);
		c = 1;
		if (result < 0)
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
	
	public void STA(int operand)
	{
		accessMemory(true, operand, a);
	}
	
	public void STX(int operand)
	{
		accessMemory(true, operand, x);
	}
	
	public void STY(int operand)
	{
		accessMemory(true, operand, y);
	}
	
	public void TAX()
	{
		x = a;
		z = 0;
		if (x == 0)
		{
			z = 1;
		}
		n = x >> 7;
	}
	
	public void TAY()
	{
		y = a;
		z = 0;
		if (y == 0)
		{
			z = 1;
		}
		n = y >> 7;
	}
	
	public void TSX()
	{
		x = s;
		z = 0;
		if (x == 0)
		{
			z = 1;
		}
		n = x >> 7;
	}
	
	public void TXA()
	{
		a = x;
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = a >> 7;
	}
	
	public void TXS()
	{
		s = x;
	}
	
	public void TYA()
	{
		a = y;
		z = 0;
		if (a == 0)
		{
			z = 1;
		}
		n = a >> 7;
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
		new Opcode() { public void execute(byte operand) { NOP(); } },		  // B
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