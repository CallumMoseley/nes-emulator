import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class NESEmu extends JPanel implements KeyListener {
	private NES nes;
	private Debugger debug;

	public NESEmu() {
		nes = new NES();
		nes.startCPU();

		setPreferredSize(new Dimension(256, 240));

		addKeyListener(this);
		setFocusable(true);
	}

	@Override
	public void paintComponent(Graphics g) {
		BufferedImage screen = nes.getPPU().getScreen();
		g.drawImage(screen, 0, 0, null);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F6) {
			debug = new Debugger(nes);
			debug.setVisible(true);
			nes.reset();
			nes.pause();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}