public class NES_Emu {
	
	public static void main(String[] args) {
		CPU cpu = new CPU();
		
		for (;;)
		{
			cpu.op();
		}
	}
}