/*
 * RUBTClient.java
 * Tested on null.cs.rutgers.edu
 *
 *  Created on: Oct 17, 2016
 *      Authors: Jesse Gatling(jag548) and Sanchit Sharma(sss269)
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;



public class Message {
	
	//public static final byte keepalive_id = {'0','0','0','0'};
	public static final byte choke_id = 0;
	public static final byte unchoke_id = 1;
	public static final byte interested_id = 2;
	public static final byte notinterested_id = 3;
	public static final byte have_id = 4;
	public static final byte piece_id = 7;
	public static final byte bitfield_id = 5;
	public static final byte request_id = 6;
	public static final int keepAlive_length = 0;
	public static final int choke_length = 1, interested_length = 1, unintersted_length = 1, unchoke_length = 1;
	public static final int have_length = 5;
	public static final int request_length = 13;
	public static final int cancel_length = 13;
	public static final int port_length = 3;
	

	final byte id;
	final int length;
	final int index;
	final int rlength;
	final int beg;
	final byte[] bitfield;	
	
	public Message(int length, byte id){
		this.id = id;
		this.length = length;
		this.rlength = -1;
		this.index = -1;
		this.beg = -1;
		this.bitfield = null;
	}
	
	
	public Message(int length, byte id, int index, int beg, int rlength){
		this.id = id;
		if(id == 5) this.length = length + index;
		else this.length = length;
		this.rlength = rlength;
		this.index = index;
		this.beg = beg;
		this.bitfield = null;
	}
	
	public Message(int length, byte id, int index, int beg, int rlength, byte[] bitfield){
		this.id = id;
		if(id == 5) this.length = length + index;
		else this.length = length;
		this.rlength = rlength;
		this.index = index;
		this.beg = beg;
		this.bitfield = bitfield;
	}
	
}
