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
		for (int i = 0; i < 3; i++)
		{
			ppu.tick();
		}
		apu.tick();
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
			char zpAddr = 0;
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
				tick();
				tick();
			break;
			case 0x0E:
				loc = (char) (accessMemory(++pc) | (accessMemory(++pc) << 8));
				operand = accessMemory(loc);
				tick();
			break;
			case 0x1E:
				loc = (char) ((accessMemory(++pc) | (accessMemory(++pc) << 8)) + x);
				operand = accessMemory(loc);
				tick();
				tick();
			break;
			}
			if (go) ASL(operand, loc);
		}
		else if (opcode == 0x90)
		{
			//BCC
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
}