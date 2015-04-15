public class CPU
{
	private PPU ppu;
	private APU apu;
	
	private int a, x, y, s;
	private int c, z, i, d, v, n;
	private int pc;
	
	private int[] memory;
	
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
		
		memory = new int[0x10000];
	}
	
	public void op()
	{
		if (reset)
		{
			reset = false;
			s -= 3;
			i = 1;
			pc = memory[0xFFFE] | (memory[0xFFFF] << 8);
		}
		int opcode = memory[pc++];
		tick(cycles[opcode]);
		int operand = 0;
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
	
	private void ADC(int operand)
	{
		int sum = memory[operand] + a + c;
		c = 0;
		if (sum > 0xFF)
			c = 1;
		sum &= 0xFF;
		v = 0;
		if (((a ^ sum) & (operand ^ result) & 0x80) != 0)
			v = 1;
		a = sum;
		n = a >> 7;
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
		c = memory[operand] >> 7;
		memory[operand] <<= 1;
		memory[operand] &= 0xFF;
		z = 0;
		if (memory[operand] == 0)
			z = 1;
		n = memory[operand] >> 7;
	}
	
	private void BCC(int operand)
	{
		if (c == 0)
		{
			tick();
			if (operand > 127)
			{
				operand -= 256;
			}
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
			if (operand > 127)
			{
				operand -= 256;
			}
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
			if (operand > 127)
			{
				operand -= 256;
			}
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
		int r = a & memory[operand];
		z = 0;
		if (r == 0)
			z = 1;
		v = (memory[operand] & 0x80) >> 6;
		n = memory[operand] >> 7;
	}
	
	private void BMI(int operand)
	{
		if (n == 1)
		{
			tick();
			if (operand > 127)
			{
				operand -= 256;
			}
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
			if (operand > 127)
			{
				operand -= 256;
			}
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
			if (operand > 127)
			{
				operand -= 256;
			}
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
		pc = (memory[0xFFFF] << 8) & memory[0xFFFE];
	}
	
	private void BVC(int operand)
	{
		if (v == 0)
		{
			tick();
			if (operand > 127)
			{
				operand -= 256;
			}
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
			if (operand > 127)
			{
				operand -= 256;
			}
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
		
	}
	
	private void NOP()
	{
		
	}
	
	interface Opcode
	{
		public void execute(int operand);
	}
	
	private Opcode[] opcodes = new Opcode[]
	{
		// 0
		new Opcode() { public void execute(int operand) { BRK(); } },		 // 0
		new Opcode() { public void execute(int operand) { ORA(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 4
		new Opcode() { public void execute(int operand) { ORA(operand); } }, // 5
		new Opcode() { public void execute(int operand) { ASL(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { PHP(); } },		 // 8
		new Opcode() { public void execute(int operand) { ORA(operand); } }, // 9
		new Opcode() { public void execute(int operand) { ASL(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { NOP(); } },		 // C
		new Opcode() { public void execute(int operand) { ORA(operand); } }, // D
		new Opcode() { public void execute(int operand) { ASL(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// 1
		new Opcode() { public void execute(int operand) { BPL(operand); } }, // 0
		new Opcode() { public void execute(int operand) { ORA(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 4
		new Opcode() { public void execute(int operand) { ORA(operand); } }, // 5
		new Opcode() { public void execute(int operand) { ASL(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { CLC(); } },		 // 8
		new Opcode() { public void execute(int operand) { ORA(operand); } }, // 9
		new Opcode() { public void execute(int operand) { NOP(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { NOP(); } },		 // C
		new Opcode() { public void execute(int operand) { ORA(operand); } }, // D
		new Opcode() { public void execute(int operand) { ASL(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// 2
		new Opcode() { public void execute(int operand) { JSR(operand); } }, // 0
		new Opcode() { public void execute(int operand) { AND(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { BIT(operand); } }, // 4
		new Opcode() { public void execute(int operand) { AND(operand); } }, // 5
		new Opcode() { public void execute(int operand) { ROL(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { PLP(); } },		 // 8
		new Opcode() { public void execute(int operand) { AND(operand); } }, // 9
		new Opcode() { public void execute(int operand) { ROL(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { BIT(operand); } }, // C
		new Opcode() { public void execute(int operand) { AND(operand); } }, // D
		new Opcode() { public void execute(int operand) { ROL(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// 3
		new Opcode() { public void execute(int operand) { BMI(operand); } }, // 0
		new Opcode() { public void execute(int operand) { AND(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 4
		new Opcode() { public void execute(int operand) { AND(operand); } }, // 5
		new Opcode() { public void execute(int operand) { ROL(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { SEC(); } },		 // 8
		new Opcode() { public void execute(int operand) { AND(operand); } }, // 9
		new Opcode() { public void execute(int operand) { NOP(); } },		 // A
		new Opcode() { public void execute(int operand) { SOP(); } },		 // B
		new Opcode() { public void execute(int operand) { NOP(); } },		 // C
		new Opcode() { public void execute(int operand) { AND(operand); } }, // D
		new Opcode() { public void execute(int operand) { ROL(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// 4
		new Opcode() { public void execute(int operand) { RTI(); } },		 // 0
		new Opcode() { public void execute(int operand) { EOR(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 4
		new Opcode() { public void execute(int operand) { EOR(operand); } }, // 5
		new Opcode() { public void execute(int operand) { LSR(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { PHA(); } },		 // 8
		new Opcode() { public void execute(int operand) { EOR(operand); } }, // 9
		new Opcode() { public void execute(int operand) { LSR(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { JMP(operand); } }, // C
		new Opcode() { public void execute(int operand) { ADC(operand); } }, // D
		new Opcode() { public void execute(int operand) { ROR(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// 5
		new Opcode() { public void execute(int operand) { BVC(operand); } }, // 0
		new Opcode() { public void execute(int operand) { EOR(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 4
		new Opcode() { public void execute(int operand) { EOR(operand); } }, // 5
		new Opcode() { public void execute(int operand) { LSR(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { CLI(); } },		 // 8
		new Opcode() { public void execute(int operand) { EOR(operand); } }, // 9
		new Opcode() { public void execute(int operand) { NOP(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { NOP(); } },		 // C
		new Opcode() { public void execute(int operand) { EOR(operand); } }, // D
		new Opcode() { public void execute(int operand) { LSR(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// 6
		new Opcode() { public void execute(int operand) { RTS(); } },		 // 0
		new Opcode() { public void execute(int operand) { ADC(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 4
		new Opcode() { public void execute(int operand) { ADC(operand); } }, // 5
		new Opcode() { public void execute(int operand) { ROR(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { PLA(); } },		 // 8
		new Opcode() { public void execute(int operand) { ADC(operand); } }, // 9
		new Opcode() { public void execute(int operand) { ROR(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { JMP(operand); } }, // C
		new Opcode() { public void execute(int operand) { ADC(operand); } }, // D
		new Opcode() { public void execute(int operand) { ROR(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// 7
		new Opcode() { public void execute(int operand) { BVS(operand); } }, // 0
		new Opcode() { public void execute(int operand) { ADC(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 4
		new Opcode() { public void execute(int operand) { ADC(operand); } }, // 5
		new Opcode() { public void execute(int operand) { ROR(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { SEI(); } },		 // 8
		new Opcode() { public void execute(int operand) { ADC(operand); } }, // 9
		new Opcode() { public void execute(int operand) { NOP(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { NOP(); } },		 // C
		new Opcode() { public void execute(int operand) { ADC(operand); } }, // D
		new Opcode() { public void execute(int operand) { ROR(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// 8
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 0
		new Opcode() { public void execute(int operand) { STA(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { STY(operand); } }, // 4
		new Opcode() { public void execute(int operand) { STA(operand); } }, // 5
		new Opcode() { public void execute(int operand) { STX(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { DEY(); } },		 // 8
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 9
		new Opcode() { public void execute(int operand) { TXA(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { STY(operand); } }, // C
		new Opcode() { public void execute(int operand) { STA(operand); } }, // D
		new Opcode() { public void execute(int operand) { STX(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F

		// 9
		new Opcode() { public void execute(int operand) { BCC(operand); } }, // 0
		new Opcode() { public void execute(int operand) { STA(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { STY(operand); } }, // 4
		new Opcode() { public void execute(int operand) { STA(operand); } }, // 5
		new Opcode() { public void execute(int operand) { STX(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { TYA(); } },		 // 8
		new Opcode() { public void execute(int operand) { STA(operand); } }, // 9
		new Opcode() { public void execute(int operand) { TXS(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { NOP(); } },		 // C
		new Opcode() { public void execute(int operand) { STA(operand); } }, // D
		new Opcode() { public void execute(int operand) { NOP(); } },		 // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// A
		new Opcode() { public void execute(int operand) { LDY(operand); } }, // 0
		new Opcode() { public void execute(int operand) { LDA(operand); } }, // 1
		new Opcode() { public void execute(int operand) { LDX(operand); } }, // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { LDY(operand); } }, // 4
		new Opcode() { public void execute(int operand) { LDA(operand); } }, // 5
		new Opcode() { public void execute(int operand) { LDX(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { TAY(); } },		 // 8
		new Opcode() { public void execute(int operand) { LDA(operand); } }, // 9
		new Opcode() { public void execute(int operand) { TAX(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { LDY(operand); } }, // C
		new Opcode() { public void execute(int operand) { LDA(operand); } }, // D
		new Opcode() { public void execute(int operand) { LDX(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// B
		new Opcode() { public void execute(int operand) { BCS(operand); } }, // 0
		new Opcode() { public void execute(int operand) { LDA(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { LDY(operand); } }, // 4
		new Opcode() { public void execute(int operand) { LDA(operand); } }, // 5
		new Opcode() { public void execute(int operand) { LDX(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { CLV(); } },		 // 8
		new Opcode() { public void execute(int operand) { LDA(operand); } }, // 9
		new Opcode() { public void execute(int operand) { TSX(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { LDY(operand); } }, // C
		new Opcode() { public void execute(int operand) { LDA(operand); } }, // D
		new Opcode() { public void execute(int operand) { LDX(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// C
		new Opcode() { public void execute(int operand) { CPY(operand); } }, // 0
		new Opcode() { public void execute(int operand) { CMP(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } }, // 2
		new Opcode() { public void execute(int operand) { NOP(); } }, // 3
		new Opcode() { public void execute(int operand) { CPY(operand); } }, // 4
		new Opcode() { public void execute(int operand) { CMP(operand); } }, // 5
		new Opcode() { public void execute(int operand) { DEC(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } }, // 7
		new Opcode() { public void execute(int operand) { INY(); } }, // 8
		new Opcode() { public void execute(int operand) { CMP(operand); } }, // 9
		new Opcode() { public void execute(int operand) { DEX(); } }, // A
		new Opcode() { public void execute(int operand) { NOP(); } }, // B
		new Opcode() { public void execute(int operand) { CPY(operand); } }, // C
		new Opcode() { public void execute(int operand) { CMP(operand); } }, // D
		new Opcode() { public void execute(int operand) { DEC(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } }, // F
		
		// D
		new Opcode() { public void execute(int operand) { BNE(operand); } }, // 0
		new Opcode() { public void execute(int operand) { CMP(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 2
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 3
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 4
		new Opcode() { public void execute(int operand) { CMP(operand); } }, // 5
		new Opcode() { public void execute(int operand) { DEC(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } },		 // 7
		new Opcode() { public void execute(int operand) { CLD(); } },		 // 8
		new Opcode() { public void execute(int operand) { CMP(operand); } }, // 9
		new Opcode() { public void execute(int operand) { NOP(); } },		 // A
		new Opcode() { public void execute(int operand) { NOP(); } },		 // B
		new Opcode() { public void execute(int operand) { NOP(); } },		 // C
		new Opcode() { public void execute(int operand) { CMP(operand); } }, // D
		new Opcode() { public void execute(int operand) { DEC(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } },		 // F
		
		// E
		new Opcode() { public void execute(int operand) { CPX(operand); } }, // 0
		new Opcode() { public void execute(int operand) { SBC(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } }, // 2
		new Opcode() { public void execute(int operand) { NOP(); } }, // 3
		new Opcode() { public void execute(int operand) { CPX(operand); } }, // 4
		new Opcode() { public void execute(int operand) { SBC(operand); } }, // 5
		new Opcode() { public void execute(int operand) { INC(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } }, // 7
		new Opcode() { public void execute(int operand) { INX(); } }, // 8
		new Opcode() { public void execute(int operand) { SBC(operand); } }, // 9
		new Opcode() { public void execute(int operand) { NOP(); } }, // A
		new Opcode() { public void execute(int operand) { NOP(); } }, // B
		new Opcode() { public void execute(int operand) { CPX(operand); } }, // C
		new Opcode() { public void execute(int operand) { SBC(operand); } }, // D
		new Opcode() { public void execute(int operand) { INC(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } }, // F
		
		// F
		new Opcode() { public void execute(int operand) { BEQ(operand); } }, // 0
		new Opcode() { public void execute(int operand) { SBC(operand); } }, // 1
		new Opcode() { public void execute(int operand) { NOP(); } }, // 2
		new Opcode() { public void execute(int operand) { NOP(); } }, // 3
		new Opcode() { public void execute(int operand) { NOP(); } }, // 4
		new Opcode() { public void execute(int operand) { SBC(operand); } }, // 5
		new Opcode() { public void execute(int operand) { INC(operand); } }, // 6
		new Opcode() { public void execute(int operand) { NOP(); } }, // 7
		new Opcode() { public void execute(int operand) { SED(); } }, // 8
		new Opcode() { public void execute(int operand) { SBC(operand); } }, // 9
		new Opcode() { public void execute(int operand) { NOP(); } }, // A
		new Opcode() { public void execute(int operand) { NOP(); } }, // B
		new Opcode() { public void execute(int operand) { NOP(); } }, // C
		new Opcode() { public void execute(int operand) { SBC(operand); } }, // D
		new Opcode() { public void execute(int operand) { INC(operand); } }, // E
		new Opcode() { public void execute(int operand) { NOP(); } }, // F
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