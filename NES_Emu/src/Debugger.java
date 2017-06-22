import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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
	private JButton step;
	
	private JPanel info;
	private JLabel cpuCount;
	private JLabel cpuLastInstruction;
	private JLabel ppuCount;
	private JLabel scanline;
	private JLabel pixel;
	// End components

	public Debugger(NES nes) {
		super("Debugger");
		debugPanel = new JPanel();
		setContentPane(debugPanel);

		debugPanel.setLayout(new BorderLayout());

		instructions = new JTextArea();
		debugPanel.add(instructions, BorderLayout.CENTER);

		rightPane = new JPanel();
		debugPanel.add(rightPane, BorderLayout.EAST);

		controls = new JPanel();
		info = new JPanel();
		
		rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
		rightPane.add(controls);
		
		step = new JButton("Step");
		step.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Debugger.this.nes.step();
			}
		});
		
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		controls.add(step);
		
		rightPane.add(info);

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

		this.nes = nes;
		nes.attachDebugger(this);
		
		this.getContentPane().setPreferredSize(new Dimension(1024, 768));
		this.pack();
	}

	public void updateCPU(long count, String lastIns) {
		cpuCount.setText("CPU Cycles: " + count);
		cpuLastInstruction.setText("Last Instruction: " + lastIns);
	}

	public void updatePPU(int count, int scan, int pix) {
		ppuCount.setText("PPU Cycles: " + count);
		scanline.setText("Scanline: " + scan);
		pixel.setText("Pixel: " + pix);
	}
}