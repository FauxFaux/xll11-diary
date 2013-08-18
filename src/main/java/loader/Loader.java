package loader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import gui.DiaryPanel;

public class Loader {
	public static void main(String[] args) {

		
		JFrame frame = new JFrame("Encrypted Diary");
		JPanel panel = new DiaryPanel();
		frame.add(panel);
		
		frame.setSize(700,500);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


	}
}

