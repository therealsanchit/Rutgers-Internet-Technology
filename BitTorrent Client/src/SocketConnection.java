/*
 * RUBTClient.java
 * Tested on null.cs.rutgers.edu
 *
 *  Created on: Oct 17, 2016
 *      Authors: Jesse Gatling(jag548) and Sanchit Sharma(sss269)
 */
import java.net.ServerSocket;

import GivenTools.Bencoder2;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

public class SocketConnection {
	
	String ip;
	Socket socket;
	DataOutputStream out;
	DataInputStream in;
	
	protected byte peerReturnByte[];
	protected byte peerReserved[];
	protected byte peerInfohash[];
	protected byte peerPeerid[];
	
	public SocketConnection(String ip){
		this.ip = ip;
	}
	
	String address;
	int port;
	
	void initalize(){
	String delims[] = ip.split(":");
	address = delims[0];
	port = Integer.parseInt(delims[1]);
	
	try {
		System.out.println("Connectng to : " + ip);
		socket = new Socket(address,port);
		out =  new DataOutputStream(socket.getOutputStream());
		in = new DataInputStream(socket.getInputStream());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	}
	
	int execute(byte[] info, byte[] peer){
		try {
			byte[] protocol = "BitTorrent protocol".getBytes();
			out.writeByte(19);
			out.write(protocol);
			out.write(new byte[8]);
			out.write(info);
			out.write(peer);
			
			int peerLengthId = in.readByte();
			
			if(peerLengthId != 19){
				System.out.println("Not a proper bittorrent protocol");
				socket.close();
				return -1;
				}
			
			peerReturnByte = new byte[peerLengthId];
			in.readFully(peerReturnByte); //"BitTorrent protocol"
			if(!bytesCompare(peerReturnByte,protocol)){
				System.out.println("Wrong protocol");
				socket.close();
				return -1;
			}
			
			peerReserved = new byte[8];
			in.readFully(peerReserved);
			

			
			peerInfohash = new byte[20];
			in.readFully(peerInfohash);
			
			peerPeerid = new byte[20];
			in.readFully(peerPeerid);
			
			if(!bytesCompare(peerInfohash, info)){
				socket.close();
				System.out.println("Wrong hash");
				return -1;
			}
		/**	if(!bytesCompare(peerPeerid, peer)){
			socket.close();                         //dont need this forsure
			System.out.println("Wrong peer");
			return -1;
			}
		**/	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 1;
		}
	
	
	 protected boolean bytesCompare(byte[] a, byte[] b) {
	        if (a.length != b.length) {
	            return false;
	        }
	        for (int i = 0; i < a.length; i++) {
	            if (a[i] != b[i]) {
	                return false;
	            }
	        }
	        return true;
	}
	 
		
		public String openFileToString(byte[] _bytes)
	{
	    String file_string = "";

	    for(int i = 0; i < _bytes.length; i++)
	    {
	        file_string += (char)_bytes[i];
	    }

	    return file_string;    
	}
}
