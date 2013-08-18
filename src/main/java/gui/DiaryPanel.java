package gui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import data.Entry;
import data.EntryDB;




import encryption.Crypto;


public class DiaryPanel extends JPanel {
	//JList related
	JList<Object> list;
	DefaultListModel<Object> model ;
	Vector<Entry> entries;

	//JComponents
	JTextField txtTitle , txtDate;
	JPasswordField txtKey;
	JTextArea txtDescription;
	JPanel objectDisplay, crudPanel, listPanel;
	JButton cmdUpdate,cmdGenerateKey, cmdNew,cmdClear, cmdRemove, cmdUpdateList, cmdChangePass, cmdManualBackup;
	
	//ChangePassword frame components
	JButton cmdChange,cmdCancel;
	JPasswordField passOld,passNew;
	ChangePasswordScreen passFrame;
	JPanel MainChangeFramePanel;

	//Listeners
	ActionListener listener = new Listener();
	SelectListener selectListener = new SelectListener();


	//For solving the problem of the message() function jumping twice @ write()
	boolean flip = false;
	MessageDigest md;

	public DiaryPanel() {
		generateBackup(false);
		this.setLayout(new BorderLayout());
		//Main display panel
		//Button settings
		txtDate = new JTextField("Date will be automatically generated");
		txtDate.setEditable(false);
		txtDate.setPreferredSize(new Dimension(200,20));
		txtTitle = new JTextField("Title");
		txtTitle.setPreferredSize(new Dimension(375,20));

		txtKey = new JPasswordField("Password here...");
		txtKey.setPreferredSize(new Dimension(170, 20));
		txtKey.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				update();
			}});

		//Adding scroller to the text area
		txtDescription = new JTextArea(20,30);
		txtDescription.setLineWrap(true);
		txtDescription.setWrapStyleWord(true);
		JScrollPane textAreaScroller = new JScrollPane(txtDescription);
		textAreaScroller.setAutoscrolls(true);
		textAreaScroller.setPreferredSize(new Dimension(410, 350));
		objectDisplay = new JPanel();
		objectDisplay.add(txtDate);
		objectDisplay.add(txtKey);
		objectDisplay.add(txtTitle);

		objectDisplay.add(textAreaScroller);
		//end of main display panel

		//CRUD Panel
		crudPanel = new JPanel();
		//Buttons
		cmdChangePass = new JButton("Change Password");
		cmdRemove = new JButton("Remove");
		cmdClear = new JButton("Clear");
		cmdNew = new JButton("New");
		cmdUpdateList = new JButton("Update List");
		cmdUpdate = new JButton("Update");
		cmdManualBackup = new JButton("Backup List");
		
		//Attaching listener
		cmdChangePass.addActionListener(listener);
		cmdClear.addActionListener(listener);
		cmdNew.addActionListener(listener);
		cmdRemove.addActionListener(listener);
		cmdUpdate.addActionListener(listener);
		cmdUpdateList.addActionListener(listener);
		cmdManualBackup.addActionListener(listener);
		
		crudPanel.add(cmdChangePass);
		crudPanel.add(cmdRemove);
		crudPanel.add(cmdNew);
		crudPanel.add(cmdClear);
		crudPanel.add(cmdUpdate);
		crudPanel.add(cmdUpdateList);
		crudPanel.add(cmdManualBackup);
		//End of CRUD



		//Generating the list panel, and the list model.
		listPanel = new JPanel();
		list = new JList<>();
		model = new DefaultListModel<>();

		//Gets a Vector that contains all the Entry objects stored at "\db.dat", and adds them to the list.
		entries = EntryDB.getEntries();
		for (Entry v : entries) {
			model.addElement(v.getTitle());
		}

		//Setting the default model to the one we've created
		list.setModel(model);
		//Selection mode to one selection at a time
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		//Setting fixed sizes for the list objects dimension
		list.setFixedCellHeight(20);
		list.setFixedCellWidth(160);

		//Adding a vertical scroll bbar
		JScrollPane listScroller = new JScrollPane(list);
		//Setting fixed size for the list
		listScroller.setPreferredSize(new Dimension(250, 400));
		//Adding the listener
		list.addListSelectionListener(selectListener);
		//Adding the list to the list panel. (via listScroller)
		listPanel.add(listScroller);

		//Adding the panels.
		add(crudPanel,BorderLayout.SOUTH);
		add(objectDisplay,BorderLayout.CENTER);
		add(listPanel, BorderLayout.EAST);



	}

	//Encryption related functions
	//Encrypt -> Takes a string and a key, and returns the string after it's been encrypted with the key
	//If there's a problem with the encryption key, it will return an empty string.

	//Returns txtKey text in MD5 hash.
	public String passInMD5() {
		return Crypto.md5(txtKey.getText());
	}

	public String encrypt(String text, String key) { 
		String returnString = "";
		try {
			returnString = Crypto.encrypt(text, key);
		} catch (Exception e) {
			if (flip==false) {
				message("Problem with the key");
			}
			flip = !flip;
		}
		return returnString;
	}

	//Decrypt -> Takes a string (persumable encrypted), and a key, and tries to decrypt that text with this key.
	//if the key is bad/not the correct key, it will return the encrypted text.
	public String decrypt(String text, String key) {
		String returnString = "";
		try {
			returnString = Crypto.decrypt(text, key);
		} catch (Exception e) {
			return text;

		}
		return returnString;
	}
	//End of encryption methods



	//Message method will pop up a JOption pane message dialog.
	private void message(String message) { 
		JOptionPane.showMessageDialog(null, message);
	}
	//what update does: clears the list, and then reloads the list again with the data from "\db.dat"
	private void update() { 
		try {
			model.removeAllElements();
			entries = EntryDB.getEntries();
			String pass =  passInMD5();
			//To get the key for each entry will need to provide the password of the object in md5 hash 
			for (Entry v : entries) {
				model.addElement(decrypt(v.getTitle(), v.getKey(pass)));
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			message("Problem detected, index out of bounds");
		}
	}
	
	private void changePass(String oldPass, String newPass) {
		int passwordChanged = 0;
		Vector<Entry> passwordList = EntryDB.getEntries();
		
		for (int i = 0 ; i < passwordList.size() ; i++) {
			if(passwordList.get(i).changePass(oldPass, newPass))
				passwordChanged++;
		}
		
		EntryDB.overwriteDB(passwordList);
		message(passwordChanged +" passwords was successfuly changed");
		update();
	}

	//Backup method
	
	private void generateBackup(boolean overwrite) {
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		String date = String.valueOf(day) + "-" + String.valueOf(month) + "-" + String.valueOf(year);

		EntryDB.generateBackup(EntryDB.DB,new File(date+".dat"), overwrite);
	}

	//Action listener for the CRUD Buttons/Generate key
	private class Listener implements ActionListener {


		public void actionPerformed(ActionEvent e) {

			//Componenets of ChangePassFrame
			if (e.getSource() == cmdCancel) {
				passFrame.exit();
			}
			
			if (e.getSource() == cmdChange) { 
				String oldPass = String.valueOf(passOld.getPassword());
				String newPass = String.valueOf(passNew.getPassword());
				changePass(oldPass, newPass);
			}
			
			//diaryPanel componenets
			
			if (e.getSource() == cmdManualBackup) { 
				generateBackup(true);
			}
			if (e.getSource() == cmdChangePass) {
				passFrame = new ChangePasswordScreen();
			}
			if (e.getSource() == cmdNew) {

				//Adds a new entry, then reloads the list.
				//Adds the key to the object itself, and the object will have a field called password
				//that is the txtKey.getTxt() in MD5 format, and whenever you want to retrive the decryption key
				//you'll have to use EntryObject.getKey(password in md5);
				String key = Crypto.generateKey();
				String title = encrypt(txtTitle.getText(),key);
				String desc = encrypt(txtDescription.getText(),key);
				EntryDB.addEntry(new Entry(title, desc, passInMD5(), key));
				update();
			}

			if(e.getSource() == cmdUpdateList) { 
				//Manually reloads the list
				update();
			}

			if (e.getSource() == cmdUpdate) {
				//Edits the selected object on the list, then it takes the vector that holds the objects
				//including the updated object, and writes it to the "\db.dat"
				//Pulls the key for each entry from the entry itself!! only if the password in the Entry file is similar to the password here
				int index = list.getSelectedIndex(); 
				if (index > -1) {
					String key = entries.elementAt(index).getKey(passInMD5()); 
					{ 
						if (!key.equals("badkey")) {
							entries.elementAt(index).setDate();
							entries.elementAt(index).setDesc(encrypt(txtDescription.getText(),key));
							entries.elementAt(index).setTitle(encrypt(txtTitle.getText(),key));
							EntryDB.overwriteDB(entries);
							update();
						}
						else 
							message("You must have the correct key of this entry first in order to update it");
					}
				}
			}

			if (e.getSource() == cmdRemove) {
				//Removes the selected index from the list, then takes the vector, that is now without the removed object,
				//and then rewrites the file "\dat.db" with the objects in the vector
				int index = list.getSelectedIndex();
				if (index != -1) {
					if(!entries.get(index).getKey(passInMD5()).equals("badkey")) {
						entries.remove(list.getSelectedIndex());
						EntryDB.overwriteDB(entries);
						update();
					}
					else
						message("You must use the same password this Entry was created with");

				}
			}
			if(e.getSource() == cmdClear) {
				//Clears the form field
				txtDate.setText("Date will be automatically generated");
				txtTitle.setText("");
				txtDescription.setText("");
			}


		} 
	}

	private class SelectListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			int index = list.getSelectedIndex(); 
			if (index != -1) {
				String key = entries.get(index).getKey(passInMD5());
				txtDate.setText(entries.get(index).getDate());
				txtDescription.setText(decrypt(entries.get(index).getDesc(),key));
				txtTitle.setText(decrypt(entries.get(index).getTitle(),key));
			}
		}
	}
	
	private class ChangePasswordScreen extends JFrame {

		public void exit() {
			dispose();
		}
		
		public ChangePasswordScreen() {
			this.setTitle("Change password");
			this.setSize(400,200);
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setVisible(true);
			this.setResizable(false);
			this.setLocationRelativeTo(DiaryPanel.this);
			
			MainChangeFramePanel = new JPanel();
			
			cmdChange = new JButton("Change");
			cmdCancel = new JButton("Cancel");
			
			cmdChange.addActionListener(listener);
			cmdCancel.addActionListener(listener);
			
			passOld = new JPasswordField();
			passOld.setPreferredSize(new Dimension(320,20));
			JPanel passOldPanel = new JPanel();
			passOldPanel.setBorder(BorderFactory.createTitledBorder("Type your old password"));
			passOldPanel.add(passOld);
			
			passNew = new JPasswordField();
			passNew.setPreferredSize(new Dimension(320,20));
			JPanel passNewPanel = new JPanel();
			passNewPanel.setBorder(BorderFactory.createTitledBorder("Type your new password"));
			passNewPanel.add(passNew);
			
			MainChangeFramePanel .add(passOldPanel);
			MainChangeFramePanel .add(passNewPanel);
			MainChangeFramePanel .add(cmdChange);
			MainChangeFramePanel .add(cmdCancel);
			
			this.add(MainChangeFramePanel );
		}
		
	}
}
