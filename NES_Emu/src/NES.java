import java.io.File;

public class NES
{
	CPU cpu;
	
	public NES()
	{
		cpu = new CPU();
//		cpu.load(new File("AllSuiteA.bin"));
	}
	
	public void startCPU()
	{
		for (;;)
		{
			cpu.op();
		}
	}
}