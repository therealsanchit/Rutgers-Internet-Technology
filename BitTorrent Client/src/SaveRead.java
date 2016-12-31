/*
 * RUBTClient.java
 * Tested on null.cs.rutgers.edu
 *
 *  Created on: Oct 17, 2016
 *      Authors: Jesse Gatling(jag548) and Sanchit Sharma(sss269)
 */
import java.io.*;
import java.util.ArrayList;

public class SaveRead{
	
	public static void Save(ArrayList<byte[]> dwnld){
		try{  // Catch errors in I/O if necessary.
			// Open a file to write to, named SavedObj.sav.
			FileOutputStream saveFile=new FileOutputStream("PieceArrayList.sav");

			// Create an ObjectOutputStream to put objects into save file.
			ObjectOutputStream save = new ObjectOutputStream(saveFile);

			// Now we do the save.
			save.writeObject(dwnld);

			// Close the file.
			save.close(); // This also closes saveFile.
		}
		catch(Exception exc){
		exc.printStackTrace(); // If there was an error, print the info.
		}
		
	}
	public static ArrayList<byte[]> Read(String Down){
		ArrayList<byte[]> piecearr = new ArrayList<byte[]>();
		try{
			FileInputStream donwload_file = new FileInputStream(Down);
			ObjectInputStream donwload_data = new ObjectInputStream(donwload_file);
			piecearr = (ArrayList) donwload_data.readObject();
			donwload_data.close();
			
		}
		catch(Exception exc){
			exc.printStackTrace(); // If there was an error, print the info.
		}
		return piecearr;
		
	}
}