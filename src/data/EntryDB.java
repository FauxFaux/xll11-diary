package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.swing.JOptionPane;

public class EntryDB {
	public final static File DB = new File("db.dat");

	public static void generateBackup(File original, File backupName, boolean overwrite) {
		if (DB.exists()) {
			String backupDirName = "Backup";
			backupName = new File(backupDirName +"\\"+backupName);
			File theDir = new File(backupDirName);
			// if the directory does not exist, create it
			if (!theDir.exists() && (theDir.mkdir() == false)) {
				JOptionPane.showMessageDialog(null, "Problem creating the backup folder");
			}

			if (!backupName.exists() || overwrite == true) { // Code taken from: http://www.mkyong.com/java/how-to-copy-file-in-java/

				InputStream inStream = null;
				OutputStream outStream = null;
				try{
					inStream = new FileInputStream(original);
					outStream = new FileOutputStream(backupName);

					byte[] buffer = new byte[1024];

					int length;
					//copy the file content in bytes 
					while ((length = inStream.read(buffer)) > 0){
						outStream.write(buffer, 0, length);
					}

					inStream.close();
					outStream.close();
				}catch(IOException e){
					JOptionPane.showMessageDialog(null, "Error generating backup file");
				}
			}
		}



	}
	public static void addEntry(Entry en) {

		try { //Creating of the stream, if there is nothing in the file, the first object will be written with a header
			//If there is something else exists in the file, the following objects will be appended to the file without a header, making the stream valid (cure's the StreamCorruptException)
			ObjectOutputStream objOut = null;

			if(DB.length() <= 10) 
				objOut = new ObjectOutputStream(new FileOutputStream(DB));

			else //Overriding the Header writing component of ObjectOutputStream
				objOut = new ObjectOutputStream(new FileOutputStream(DB, true)) {
				@Override 
				protected void writeStreamHeader() throws IOException { reset(); } 
			};

			objOut.writeObject((Entry) en); //The actual writing to the file, based on the conditions presented before, this writer will write the object to a file with or without a header
			objOut.close();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null, "IO Exception on EntryDB");
		};


	} //End of addExpense

	public static Vector getEntries() {
		
		Vector<Entry> returnVec = new Vector<Entry>();
		
		try {
			ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(DB));
			Entry ent = (Entry) objIn.readObject(); 
			System.out.println("hey");

			while (ent != null) {
				
				returnVec.add(ent);
				ent = (Entry) objIn.readObject();
			} 
		}catch (Exception e ) { 
		}
		return returnVec;
	}

	public static void overwriteDB(Vector<Entry> v) { 
		FileOutputStream writer;
		try {
			writer = new FileOutputStream(DB);
			writer.write((new String()).getBytes());
			writer.close();
			for(Entry e : v)
				addEntry(e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

