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
			char add = 0;
			char zpAdd = 0;
			switch (opcode)
			{
			case 0x69:
				add = accessMemory(++pc);
			break;
			case 0x65:
				add = accessMemory(accessMemory(++pc));
			break;
			case 0x75:
				add = accessMemory((char) ((accessMemory(++pc) + x) % 256));
				tick();
			break;
			case 0x6D:
				add = accessMemory((char) (accessMemory(++pc) | (accessMemory(++pc) << 8)));
			break;
			case 0x7D:
				add = accessMemory((char) ((accessMemory(++pc) | (accessMemory(++pc) << 8)) + x));
				// TODO tick if page crossed
			break;
			case 0x79:
				add = accessMemory((char) ((accessMemory(++pc) | (accessMemory(++pc) << 8)) + y));
				// TODO tick if page crossed
			break;
			case 0x61:
				zpAdd = (char) ((accessMemory(++pc) + x) % 256);
				add = accessMemory((char) (accessMemory(zpAdd) | (accessMemory((char) (zpAdd + 1)) << 8)));
				tick();
			break;
			case 0x71:
				zpAdd = (char) (accessMemory(++pc) % 256);
				add = accessMemory((char) ((accessMemory(zpAdd) | (accessMemory((char) (zpAdd + 1)) << 8)) + y));
				// TODO tick if page crossed
			break;
			}
			byte curC = c;
			a += add;
			c = 0;
			if (a > 255)
			{
				c = 1;
			}
			a %= 256;
			z = 0;
			if (a == 0x00)
			{
				z = 1;
			}
			v = 0;
			if (curC != c)
			{
				v = 1;
			}
			n = 0;
			if (a >> 7 == 1)
			{
				n = 1;
			}
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
}
