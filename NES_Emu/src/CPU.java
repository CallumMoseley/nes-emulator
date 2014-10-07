public class CPU
{
	public char a, x, y, sp;
	public byte c, z, i, d, v, n;
	public char pc;

	public char[] memory = new char[0x10000];
	
	public PPU ppu;
	public APU apu;
	
	public boolean reset, nmi, irq;
	
	public void initialize()
	{
		reset = true;
		nmi = false;
		irq = false;
		
		a = 0x00;
		x = 0x00;
		y = 0x00;
		
		c = 0x00;
		z = 0x00;
		i = 0x00;
		d = 0x00;
		v = 0x00;
		n = 0x00;
		
		sp = 0xFF;
		pc = 0x00;
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
		if (opcode == 0x69 || opcode == 0x65 || opcode == 0x75 || opcode == 0x6D || opcode == 0x7D || opcode == 0x79 || opcode == 0x61 || opcode == 0x71)
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
				loc = (char) ((accessMemory(++pc) + x) % 256);
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
				zpAddr = (char) ((accessMemory(++pc) + x) % 256);
				loc = (char) (accessMemory(zpAddr) | (accessMemory((char) (zpAddr + 1)) << 8));
				operand = accessMemory(loc);
				tick();
			break;
			case 0x71:
				zpAddr = (char) (accessMemory(++pc) % 256);
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
		else if (opcode == 0x29 || opcode == 0x25 || opcode == 0x35 || opcode == 0x2D || opcode == 0x3D || opcode == 0x39 || opcode == 0x21 || opcode == 0x31)
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
		else if (opcode == 0x0A || opcode == 0x06 || opcode == 0x16 || opcode == 0x0E || opcode == 0x1E)
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
			if (go) ASL(operand, loc);
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
				operand = (char)(accessMemory(++pc) | (accessMemory(++pc) << 8));
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
		
		pc++;
	}

	public char accessMemory(boolean write, char address, char b)
	{
		if (reset && write) write = false;
		tick();
		if (!write)
		{
			return memory[address];
		}
		memory[address] = b;
		return 0;
	}
	
	public char accessMemory(char address)
	{
		return accessMemory(false, address, (char)0);
	}
	
	// Opcodes, as far as the eye can see!
	public void ADC(char operand)
	{
		a += operand + c;
		byte curC = c;
		c = 0;
		if (a > 256) c = 1;
		v = 0;
		if (c != curC) v = 1;
		a %= 256;
		z = 0;
		if (a == 0) z = 1;
		n = (byte) (a >> 7);
	}
	
	public void AND(char operand)
	{
		a &= operand;
		z = 0;
		if (a == 0) z = 1;
		n = (byte) (a >> 7);
	}
	
	public void ASL()
	{
		c = (byte) (a >> 7);
		a = (char) ((a << 1) & 0xFF);
		z = 0;
		if (a == 0) z = 1;
		n = (byte) (a >> 7);
	}
	
	public void ASL(char operand, char loc)
	{
		c = (byte) (operand >> 7);
		operand = (char) ((operand << 1) & 0xFF);
		accessMemory(true, loc, operand);
		z = 0;
		if (operand == 0) z = 1;
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
			pc -= 2;
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
			pc -= 2;
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
			pc -= 2;
		}
	}
	
	public void BIT(char operand)
	{
		z = 0;
		if ((a & operand) == 0) z = 1;
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
			pc -= 2;
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
			pc -= 2;
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
			pc -= 2;
		}
	}
	
	public void BRK()
	{
		accessMemory((char) (pc + 1));
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc >> 4));
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc & 0xFF));
		char status = (char) ((n << 7) | (v << 6) | (1 << 5) | (1 << 4) | (d << 3) | (i << 2) | (z << 1) | (c));
		accessMemory(true, (char) (0x0100 + sp--), status);
		pc = (char) (accessMemory((char) 0xFFFE) | (accessMemory((char) 0xFFFF) << 8));
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
			pc -= 2;
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
			pc -= 2;
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
		if (a >= operand) c = 1;
		z = 0;
		if (a == operand) z = 1;
		n = (byte) (((a - operand) & 0xFF) >> 7);
	}
	
	public void CPX(char operand)
	{
		c = 0;
		if (x >= operand) c = 1;
		z = 0;
		if (x == operand) z = 1;
		n = (byte) (((x - operand) & 0xFF) >> 7);
	}
	
	public void CPY(char operand)
	{
		c = 0;
		if (y >= operand) c = 1;
		z = 0;
		if (y == operand) z = 1;
		n = (byte) (((y - operand) & 0xFF) >> 7);
	}
	
	public void DEC(char loc)
	{
		char cur = accessMemory(loc);
		cur--;
		cur &= 0xFF;
		accessMemory(true, loc, cur);
		z = 0;
		if (cur == 0) z = 1;
		n = (byte) (cur >> 7);
	}
	
	public void DEX()
	{
		tick();
		x--;
		x &= 0xFF;
		z = 0;
		if (x == 0) z = 1;
		n = (byte) (x >> 7);
	}
	
	public void DEY()
	{
		tick();
		y--;
		y &= 0xFF;
		z = 0;
		if (y == 0) z = 1;
		n = (byte) (y >> 7);
	}
	
	public void EOR(char operand)
	{
		a ^= operand;
		z = 0;
		if (a == 0) z = 1;
		n = (byte) (a >> 7);
	}
	
	public void INC(char loc)
	{
		char cur = accessMemory(loc);
		cur++;
		cur &= 0xFF;
		accessMemory(true, loc, cur);
		z = 0;
		if (cur == 0) z = 1;
		n = (byte) (cur >> 7);
	}
	
	public void INX()
	{
		tick();
		x++;
		x &= 0xFF;
		z = 0;
		if (x == 0) z = 1;
		n = (byte) (x >> 7);
	}
	
	public void INY()
	{
		tick();
		y++;
		y &= 0xFF;
		z = 0;
		if (y == 0) z = 1;
		n = (byte) (y >> 7);
	}
	
	public void JMP(char operand)
	{
		pc = operand;
		pc--;
	}
	
	public void JSR(char operand)
	{
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc >> 4));
		accessMemory(true, (char) (0x0100 + sp--), (char) (pc & 0xFF));
		tick();
		pc = operand;
		pc--;
	}
	
	public void LDA(char operand)
	{
		a = operand;
		z = 0;
		if (a == 0) z = 1;
		n = (byte) (a >> 7);
	}
	
	public void LDX(char operand)
	{
		x = operand;
		z = 0;
		if (x == 0) z = 1;
		n = (byte) (x >> 7);
	}
	
	public void LDY(char operand)
	{
		y = operand;
		z = 0;
		if (y == 0) z = 1;
		n = (byte) (y >> 7);
	}
	
	public void LSR()
	{
		c = (byte) (a & 0x01);
		c >>= 1;
		z = 0;
		if (c == 0) z = 1;
		n = 0;
	}
	
	public void LSR(char operand, char loc)
	{
		c = (byte) (operand & 0x01);
		operand >>= 1;
		accessMemory(true, loc, operand);
		z = 0;
		if (operand == 0) z = 1;
		n = 0;
	}
	
	public void NOP()
	{
		tick();
	}
	
	public void ORA(char operand)
	{
		a |= operand;
		z = 0;
		if (a == 0) z = 1;
		n = (byte) (a >> 7);
	}
	
	public void PHA()
	{
		accessMemory(true, (char) (0x0100 + sp--), a);
		tick();
	}
	
	public void PHP()
	{
		char status = (char) ((n << 7) | (v << 6) | (1 << 5) | (1 << 4) | (d << 3) | (i << 2) | (z << 1) | (c));
		accessMemory(true, (char) (0x0100 + sp--), status);
	}
	
	public void PLA()
	{
		a = accessMemory((char) (0x100 + ++sp));
		z = 0;
		if (a == 0) z = 1;
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
		if (a == 0) z = 1;
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
		if (operand == 0) z = 1;
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
		if (a == 0) z = 1;
		n = (byte) (a >> 7);
	}
	
	public void ROR(char operand, char loc)
	{
		byte oldC = c;
		c = (byte) (operand >> 7);
		operand <<= 1;
		operand |= oldC;
		operand &= 0xFF;
		accessMemory(true, loc, operand);
		z = 0;
		if (operand == 0) z = 1;
		n = (byte) (operand >> 7);
	}
}