import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Debugger extends JFrame {

	private NES nes;
	private JPanel debugPanel;

	// Components
	private JTextArea instructions;
	private JPanel rightPane;
	private JPanel controls;

	private JPanel info;
	private JLabel cpuCount;
	private JLabel cpuLastInstruction;
	private JLabel ppuCount;
	private JLabel scanline;
	private JLabel pixel;
	// End components

	public Debugger(NES nes) {
		super("Debugger");
		this.nes = nes;
		nes.attachDebugger(this);
		debugPanel = new JPanel();
		setContentPane(debugPanel);

		debugPanel.setLayout(new BorderLayout());

		instructions = new JTextArea();
		debugPanel.add(instructions, BorderLayout.CENTER);

		rightPane = new JPanel();
		debugPanel.add(rightPane, BorderLayout.EAST);

		controls = new JPanel();
		info = new JPanel();
		rightPane.add(controls, BorderLayout.NORTH);
		rightPane.add(info, BorderLayout.SOUTH);

		cpuCount = new JLabel("CPU Cycles: 0");
		cpuLastInstruction = new JLabel("Last Instruction: NOP");
		ppuCount = new JLabel("PPU Cycles: 0");
		scanline = new JLabel("Scanline: 0");
		pixel = new JLabel("Pixel: 0");

		info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
		info.add(cpuCount);
		info.add(cpuLastInstruction);
		info.add(ppuCount);
		info.add(scanline);
		info.add(pixel);
		
		this.getContentPane().setPreferredSize(new Dimension(1024, 768));
		this.pack();
	}

	public void updateCPU(int count, String lastIns) {
		cpuCount.setText("CPU Cycles: " + count);
		cpuLastInstruction.setText("Last Instruction: " + lastIns);
	}

	public void updatePPU(int count, int scan, int pix) {
		ppuCount.setText("PPU Cycles: " + count);
		scanline.setText("Scanline: " + scan);
		pixel.setText("Pixel: " + pix);
	}
}