/*
 * RUBTClient.java
 * Tested on null.cs.rutgers.edu
 *
 *  Created on: Oct 17, 2016
 *      Authors: Jesse Gatling(jag548) and Sanchit Sharma(sss269)
 */
public class PeerDictionary {

	
	String info;
	
	public void setString(String string){
		this.info = string;
		
	}
	//port ans ip ans peerid ans
	public String getIp(){
		
		String ip = null;
		
		String delims[] = info.split("\n");
		
		String peerId = delims[6];		 //peer id
		String ipId = delims[4];
		String portId = delims[2];
		
		
		String peerDelims[] = peerId.split(":");
		String ipDelims[] = ipId.split(":");
		String portDelims[] = portId.split(":");
		
		
				
		if(peerDelims[1].contains("-RU")){
			
			ip = ipDelims[1] + ":"+ portDelims[1];
			ip = ip.replaceAll("\\s", "");
			return ip;
		
		}
		System.out.println("null");
		return ip;
	}
	
}
