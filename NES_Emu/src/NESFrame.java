import javax.swing.JFrame;

public class NESFrame extends JFrame
{
	private NESEmu nes;
	
	public NESFrame()
	{
		super("NES Emulator");
		nes = new NESEmu();
		setContentPane(nes);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public static void main(String[] args)
	{
		NESFrame frame = new NESFrame();
		frame.pack();
		frame.setVisible(true);
	}
}