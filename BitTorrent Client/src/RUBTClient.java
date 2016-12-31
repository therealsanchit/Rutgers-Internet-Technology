/*
 * RUBTClient.java
 * Tested on null.cs.rutgers.edu
 *
 *  Created on: Nov 02, 2016
 *      Authors: Jesse Gatling(jag548) and Sanchit Sharma(sss269)
 */
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Random;

import GivenTools.*;
@SuppressWarnings("static-access")
public class RUBTClient {

static ToolKit toolKit = new ToolKit();
public static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5',
'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
'F' };
public static boolean[] usedBestIP;
public static byte[] peerid;
public static ArrayList<String> list;
public static byte[] infohash;
public static int left;
public static int pieceLen;
public static TorrentInfo torrentInfo;
public static String sav_name;
public static SocketConnection socketConnection;
public static boolean lockA = false;
public static int minpack = 0;

public static void main(String args[])throws BencodingException, IOException, MalformedURLException {
	String torr_name = args[0];
	sav_name = args[1];
	byte[] sav_data;
	//int loop = Integer.parseInt(args[1]);
	File file = new File(torr_name);
	Path path = Paths.get(torr_name);
	byte[] data = Files.readAllBytes(path);
	torrentInfo = new TorrentInfo(data);
	Bencoder2 benCoder = new Bencoder2();
	ByteBuffer vals = benCoder.getInfoBytes(data);
	Object decodedData = benCoder.decode(data);
	// toolKit.print(decodedData);
	URL announce = torrentInfo.announce_url;
	infohash = torrentInfo.info_hash.array();
	int uploaded = 0;
	int downloaded = 0;
	left = torrentInfo.file_length;
	int port = announce.getPort();
	String host = announce.getHost();
	pieceLen = torrentInfo.piece_length;
	byte[] intr = new byte[5];
	intr[0] = 0;
	intr[1] = 0;
	intr[2] = 0;
	intr[3] = 1;
	intr[4] = 2;
	//byte[] req = {0,0,0,1,3,6,0,9};
	//int intr = 00012;
	
	peerid = genPeerId();
	String newURL = announce.toString();
	newURL += "?info_hash=" + toHexString(infohash) + "&peer_id=" +
	toHexString(peerid) + "&port=" + port
	+ "&uploaded=" + uploaded + "&downloaded=" + downloaded + "&left=" + left;

	URL FURL = new URL(newURL);
	byte[] resp = SendRecieve(FURL);
	Object decodedResp = benCoder.decode(resp);
	
	list = new ArrayList<>();
	list = genPeerConnections(decodedResp);
	usedBestIP = new boolean[list.size()];
	for(int i = 0; i < list.size(); i++) usedBestIP[i] = false;
      RunnableT R1 = new RunnableT("Thread-1",0);
      R1.start();
      RunnableT R2 = new RunnableT( "Thread-2",1);
      R2.start();/*
	  RunnableT R3 = new RunnableT( "Thread-3");
      R3.start();
	  RunnableT R4 = new RunnableT( "Thread-4");
      R4.start();
	  RunnableT R5 = new RunnableT( "Thread-5");
      R5.start();*/
   } 

static class RunnableT implements Runnable{
	private Thread t;
	private String threadName;
	private int threadNum;
   
   RunnableT(String name, int num) {
      threadName = name;
	  threadNum = num;
	  
      System.out.println("Creating " +  threadName );
   }
	
	public void run() {
		MessageManager messageManager;
		while(true){
		System.out.println("Run Loop: " +  threadName );
		if(!lockA){
			lockA = true;
		System.out.println(Byt2Str(peerid));
		try{
			socketConnection = new SocketConnection(getBestIp(list));
		}catch(Exception e){}
		socketConnection.initalize();
		int check = socketConnection.execute(infohash, peerid);
		messageManager = new
		MessageManager(socketConnection,left,pieceLen);
		lockA = false;
		break;
		}
		}
		try{
			System.out.println(messageManager.startMessaging(sav_name,torrentInfo.piece_hashes,2,threadNum));
		}catch(IOException f){}
}
public void start () {
      System.out.println("Starting " +  threadName );
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}
private static String getBestIp(ArrayList<String> al) throws Exception{
int index = 0;
long rtt = 0;
int best = 0;
for (Iterator iterator = al.iterator(); iterator.hasNext();) {
String string = (String) iterator.next();
System.out.println(string);
String delims[] = string.split(":");
String address = delims[0];
int port = Integer.parseInt(delims[1]);
InetAddress inet = InetAddress.getByName(address);
long finish = 0;
long start = new GregorianCalendar().getTimeInMillis();
if(inet.isReachable(5000)){
finish = new GregorianCalendar().getTimeInMillis();
if(index == 0){
rtt = (finish-start);
best = 0;
}else{
if(rtt<(finish-start)) continue;
else if(!usedBestIP[index]){
rtt = (finish-start);
best = index;
}
}
}else{
System.out.println("Address not reachable");
}
index++;
}
usedBestIP[best] = true;
return al.get(best);
}

public static byte[] SendRecieve(URL announce) {
try {
HttpURLConnection httpConnection = (HttpURLConnection)
announce.openConnection();
DataInputStream dataInputStream = new
DataInputStream(httpConnection.getInputStream());

int dataSize = httpConnection.getContentLength();
byte[] retArray = new byte[dataSize];

dataInputStream.readFully(retArray);
dataInputStream.close();

return retArray;
} catch (IOException e) {
System.out.println("Error: \nCannot connect to tracker");
return null;
}
}

public static String toHexString(byte[] bytes) {
if (bytes == null) {
return null;
}

if (bytes.length == 0) {
return "";
}

StringBuilder sb = new StringBuilder(bytes.length * 3);

for (byte b : bytes) {
byte hi = (byte) ((b >> 4) & 0x0f);
byte lo = (byte) (b & 0x0f);

sb.append('%').append(HEX_CHARS[hi]).append(HEX_CHARS[lo]);
}
return sb.toString();
}

protected static byte[] genPeerId() {
Random rand = new Random(System.currentTimeMillis());
byte[] peerId = new byte[20];

peerId[0] = 'G';
peerId[1] = 'P';
peerId[2] = '0';
peerId[3] = '2';

for (int i = 4; i < 20; ++i) {
peerId[i] = (byte) ('A' + rand.nextInt(26));
}
return peerId;

}

@SuppressWarnings("static-access")
private static ArrayList<String> genPeerConnections(Object decodedResp) {

ArrayList<String> al = new ArrayList<>();

ByteArrayOutputStream baos = new ByteArrayOutputStream();
PrintStream ps = new PrintStream(baos);

PrintStream old = System.out;

System.setOut(ps);

toolKit.print(decodedResp);

System.out.flush();
System.setOut(old);

String output = baos.toString();

int ind = output.indexOf("List");
int end = output.length();

String get = output.substring(ind, end);

String delims[] = get.split("Dictionary");

PeerDictionary pD = new PeerDictionary();

for (int i = 1; i < delims.length; i++) {
pD.setString(delims[i]);
String d = pD.getIp();
if (d != null)
al.add(d);
}

return al;
}
public static String Byt2Str(byte[] _bytes)
{
    String file_string = "";

    for(int i = 0; i < _bytes.length; i++)
    {
        file_string += (char)_bytes[i];
    }

    return file_string;
}
}
