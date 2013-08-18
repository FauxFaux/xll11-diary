package data;

import java.io.Serializable;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import encryption.Crypto;


public class Entry implements Serializable {

	private String date,title, description;
	private String password;
	private String key;
	public Entry(String title, String description, String password, String key) { 
		this.title = title;
		this.description = description;
		this.password = password;
		this.key = key;
		setDate();
	}

	public boolean changePass(String oldPass, String newPass) {
		oldPass = Crypto.md5(oldPass);
		newPass = Crypto.md5(newPass);
		if (oldPass.equals(password)) {
			this.password = newPass;
			return true;
			
		}
		else
			return false;
	}
	
	public String getKey(String pass) { 
		if (pass.equals(password))  
			return key;
		else 
			return "badkey";

	} 
	public String getDate() { return date; }
	public String getTitle() { return title; }
	public String getDesc() { return description; }

	public void setTitle(String title) { this.title = title; }
	public void setDesc(String desc) { this.description = desc; }
	public void setDate() { 
		// Create an instance of SimpleDateFormat used for formatting 
		// the string representation of date (month/day/year)
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		// Get the date today using Calendar object.
		Date today = (Date) Calendar.getInstance().getTime();        
		// Using DateFormat format method we can create a string 
		// representation of a date with the defined format.
		String reportDate = df.format(today);
		date = reportDate;

	}

}
