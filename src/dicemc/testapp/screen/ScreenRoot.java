package dicemc.testapp.screen;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class ScreenRoot implements IScreen{
	private JFrame frame;
	private JButton printButton;
	
	public static void openGUI() {
		//GnCLibConsole.currentScreen =  new ScreenRoot();
	}
	
	public ScreenRoot() {
		frame = new JFrame("GnC Test Console");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		init();
		frame.setBounds(new Rectangle(0, 0, 1000, 1000));
		frame.setVisible(true);
	}

	@Override
	public void init() {
		printButton = new JButton("Print");
		printButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(printButton)) System.out.println("Print Button Pressed");
			}
		});
		printButton.setBounds(0, 0, 75, 20);
		printButton.setVisible(true);
		frame.add(printButton);
	}
}
