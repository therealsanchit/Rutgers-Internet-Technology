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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.GregorianCalendar;
import java.util.Random;

import GivenTools.*;

public class MessageManager {
	SocketConnection socketConnection;
	static byte[] rawBytes;
	static int hash_index = 0;
	ArrayList<byte[]> byteList = new ArrayList<>();
	int reqIndex = 0;
	int reqBeg = 0;
	int reqLen = 24;
	int totalFileBytes;
	int numPieces;
	static int pieceSize;
	int downloadedPieces = 0;
	int downloadPieceSize = 0;
	static int i = 0;
	int reqIndx = 0;
	int k = 0;
	FileOutputStream fileWriter;

	public MessageManager(SocketConnection sc, int totalFileBytes, int pieceSize) {
		this.socketConnection = sc;
		this.totalFileBytes = totalFileBytes;
		this.pieceSize = pieceSize;
		this.numPieces = (totalFileBytes / pieceSize);
	}

	public static Message rdMsg(DataInputStream in) throws IOException {

		String rec = "Recieved: ";
		int length = in.readInt();
		int index;
		int rlength;
		int beg;
		byte[] bitfield;

		if (length == 0) {
			System.out.println(rec + "Keep Alive");
			return new Message(length, (byte) 0);
		}
		byte id = in.readByte();

		switch (id) {

		case (Message.choke_id):
			System.out.println(rec + "Choke");
			return new Message(length, id);

		case (Message.unchoke_id):
			System.out.println(rec + "Unchoke");
			return new Message(length, id);

		case (Message.interested_id):
			System.out.println(rec + "Interested");
			return new Message(length, id);

		case (Message.notinterested_id):
			System.out.println(rec + "Not Interested");
			return new Message(length, id);

		case (Message.have_id):
			index = in.readInt();
			System.out.println(rec + "Have");
			return new Message(length, id, index, -1, -1, null);

		case (Message.bitfield_id):
			bitfield = new byte[length - 1];
			in.readFully(bitfield);
			index = (int) bitfield.length;
			System.out.println(rec + "Bitfield");
			return new Message(length, id, index, -1, -1, bitfield);

		case (Message.request_id):
			index = in.readInt();
			beg = in.readInt();
			rlength = in.readInt();
			System.out.println(rec + "Request");
			return new Message(length, id, index, beg, rlength, null);

		case(Message.piece_id):
			rlength = length-9;
			rawBytes = new byte[rlength]; //piece block
			index = in.readInt();
			beg = in.readInt();
			in.readFully(rawBytes);
			System.out.println(rec + "Piece");
			return new Message(length,id,index,beg,rlength,rawBytes);
		}
		System.out.println("NULL");
		return null;
	}

	public static String sndMsg(Message msg, DataOutputStream out) throws IOException {
		String sent = "Sent: ";
		switch (msg.id) {

		case (Message.choke_id):
			out.writeInt(msg.length);
			out.write(msg.id);
			return sent + "Choke";

		case (Message.unchoke_id):
			out.writeInt(msg.length);
			out.write(msg.id);
			return sent + "Unchoke";

		case (Message.interested_id):
			out.writeInt(Message.interested_length);
			out.write(msg.id);
			return sent + "Interested";

		case (Message.notinterested_id):
			out.writeInt(msg.length);
			out.write(msg.id);
			return sent + "Not Interested";

		case (Message.have_id):
			out.writeInt(msg.length);
			out.write(msg.id);
			out.writeInt(msg.index);
			return sent + "Have";

		case (Message.bitfield_id):
			out.writeInt(msg.length);
			out.write(msg.id);
			out.write(msg.bitfield);
			return sent + "Bitfield";

		case (Message.request_id):
			out.writeInt(msg.length);
			out.write(msg.id);
			out.writeInt(msg.index);
			out.writeInt(msg.beg);
			out.writeInt(msg.rlength);
			return sent + "Request";
		}
		return "Ivalid Configuration";

	}

	public boolean startMessaging(String filenm,ByteBuffer[] SHA1HASH,int add,int threadnum) throws IOException {
		boolean beg = true;
		boolean writechck = true;
		i = threadnum;
		Message interested = new Message(Message.interested_length, Message.interested_id);
		System.out.println(sndMsg(interested, socketConnection.out));

		Message reply = rdMsg(socketConnection.in);

		while (reply.id != Message.unchoke_id)
			reply = rdMsg(socketConnection.in);

		while (totalFileBytes != 0) {
			if(reqBeg == pieceSize && writechck){
				i+=add;
				reqBeg = 0;
			}
			if(writechck){
				request();
			}

			reply = rdMsg(socketConnection.in);
			if (reply == null || reply.id != Message.piece_id)
				reply = choke(reply);

			if (reply.id == Message.piece_id && writechck) {
				piece(reply);
				mkfl(filenm,beg);
				RUBTClient.minpack++;
			}
			//System.out.println("minpack: " + RUBTClient.minpack);
			//System.out.println("i: " + i);
			/*
			else{
				i--;
			}*/
			beg = false;
			if(RUBTClient.minpack > i)writechck = true;
			else writechck = false;
		}
/*		
	System.out.println("Combining pieces...");
	ArrayList<byte[]> fileBytes = combinePieces();*/
	return true;
	
	
	}
		public static void mkfl(String filenm, boolean first){
			if(first){
				
				try (FileOutputStream fileWriter = new FileOutputStream(filenm,false)) {
					fileWriter.write(rawBytes);
					System.out.println("Saving " + filenm + " success");
				} catch (IOException e) {
					System.err.println("Saving " + filenm + " failed");
				}
				
			}
			else{
				try (FileOutputStream fileWriter = new FileOutputStream(filenm,true)) {
					fileWriter.write(rawBytes);
					System.out.println("Saving " + filenm + " success");
				} catch (IOException e) {
					System.err.println("Saving " + filenm + " failed");
				}
			}
		return;
		
	}
	
	private boolean checkHash(ByteBuffer torrINF, byte[] piece){
		
		byte[] piece_sha1;

        try {
            MessageDigest enc = MessageDigest.getInstance("SHA-1");
			enc.update(piece);
			piece_sha1 = enc.digest(piece);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Incorrect Algorithm");
            return false;
        }
		
		if(Arrays.equals(piece_sha1, torrINF.array())) return true;
		return false;
		
	}
	private ArrayList<byte[]> combinePieces(){
		
		ArrayList<byte[]> cP = new ArrayList<>();
		
		for(int i = 0;i<byteList.size()-1;i=i+2){
			if(byteList.get(i+1) == null){
				cP.add(byteList.get(i));
				return cP;
			}
			byte[] combined = new byte[byteList.get(i).length + byteList.get(i+1).length];
			System.arraycopy(byteList.get(i), 0, combined, 0, byteList.get(i).length);
			System.arraycopy(byteList.get(i+1), 0, combined, byteList.get(i).length, byteList.get(i+1).length);
			cP.add(combined);
		}
		
		return cP;
	}

	private void piece(Message reply) throws IOException {

		System.out.println(reply.rlength);
		reqBeg += reply.rlength;
		totalFileBytes -= reply.rlength;
		byteList.add(rawBytes);		
		System.out.println(totalFileBytes);
		System.out.println(i);

	}

	private Message choke(Message reply) throws IOException {

		while (reply == null || reply.id == Message.choke_id || reply.id == Message.interested_id
				|| reply.length == 0) {
			reply = rdMsg(socketConnection.in);
		}

		if (reply.id == Message.unchoke_id)
			return new Message(Message.unchoke_length, Message.unchoke_id);

		if (reply.id == Message.piece_id) {
				return new Message(reply.rlength + 9, Message.piece_id, reply.index, reply.beg, reply.rlength);
		}

		return reply;
	}

	private void request() throws IOException {
		//System.out.println("totalFileBytes: " + totalFileBytes + "pieceSize: " + pieceSize + " ");
		if (totalFileBytes < pieceSize) {
			pieceSize = totalFileBytes;
			
			/*
			if(pieceSize > 16384){
				pieceSize = pieceSize/2;
			}*/
			
			
			Message request = new Message(Message.request_length, Message.request_id, i, reqBeg, totalFileBytes);
			System.out.println("totalFileBytes: " + totalFileBytes + "pieceSize: " + pieceSize + " ");
			System.out.println(sndMsg(request, socketConnection.out));
			System.out.println("DONE!");
			return;
		}else{
			Message request = new Message(Message.request_length, Message.request_id, i, reqBeg, 16384);
			System.out.println(sndMsg(request, socketConnection.out));
		}

		return;
	}
}
