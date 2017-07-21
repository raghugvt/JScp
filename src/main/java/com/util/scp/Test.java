package com.emc.util.scp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Test {

	public static void main(String[] args) throws Exception {
		JSch jsch = new JSch();
		String user = "root";
		String remoteHost = "17.217.87.145";
		int portNumber = 22;
		Session session = jsch.getSession(user, remoteHost, portNumber);
		session.setPassword("Password123!");
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no"); // Disabling checking strict key checking
		session.setConfig(config);
		session.connect();
		
		// Test to Copy file from remote host to local host
		String localFile = "C:\\\\Users\\\\jir2\\\\Desktop\\\\Inbox\\\\Scratchpad\\\\DWH\\\\idea.txt";
		String remotePath = "/tmp/idea.txt";
		
		ScpFrom scpFrom = new ScpFrom(session, remotePath, localFile);
		scpFrom.transfer();

		// Test to copy file from local host to remote host
		String localFile2 = "C:\\\\Users\\\\jir2\\\\Desktop\\\\Inbox\\\\Scratchpad\\\\DWH\\\\crontab.txt";
		String remotePath2 = "/tmp/crontab.txt";
		ScpTo scpTo = new ScpTo(session, localFile2, remotePath2);
		scpFrom.transfer();
		System.out.println("Transfer Complete!!");
		
	}

}
