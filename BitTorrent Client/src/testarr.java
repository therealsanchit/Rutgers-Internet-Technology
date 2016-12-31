/*
 * RUBTClient.java
 * Tested on null.cs.rutgers.edu
 *
 *  Created on: Oct 17, 2016
 *      Authors: Jesse Gatling(jag548) and Sanchit Sharma(sss269)
 */
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class testarr{
	public static void main(String[] args){
		
		ArrayList<byte[]> downarr = new ArrayList<byte[]>();
		downarr = SaveRead.Read("PieceArrayList.sav");
		mkfl("testing1.mp4",downarr);
		System.out.println("Done");
		
	}
	
		public static void mkfl(String filenm, ArrayList<byte[]> filedat){
		int k = 0;
		double kj = 0;
		int percnt = 0;
		double chkpercnt = 0;
		int llength = 0;
		//SaveRead.Save(filedat);
		System.out.println("0% done...");
		for(int i = 0; i < filedat.size()-1; i++){
			llength += filedat.get(i).length;
		}
		System.out.println(llength);
		byte[] sav_dat = new byte[llength];
		for(int i = 0; i < filedat.size()-1; i++){
			//System.out.println(i);
			//System.out.println("ArrayList index: " + i);
			for(int j = 0; j < filedat.get(i).length; j++){
				//System.out.println("byte[] index: " + j);
				sav_dat[k] = filedat.get(i)[j];
				k++;
				kj++;
				chkpercnt = (kj/llength)*100;
				//System.out.println(chkpercnt%100);
				if((int)chkpercnt%100 == 5+percnt){
					percnt+=5;
					System.out.println(percnt + "% done...");
				}
			}
		}
		System.out.println("100% done...");
		System.out.println("Making file...");
		try (FileOutputStream fileWriter = new FileOutputStream(filenm)) {
            fileWriter.write(sav_dat);
            System.out.println("Saving " + filenm + " success");
        } catch (IOException e) {
            System.err.println("Saving " + filenm + " failed");
        }
		return;
		
	}
	
}