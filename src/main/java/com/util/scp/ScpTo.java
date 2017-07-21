package com.emc.util.scp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.logging.Level;

public class ScpTo extends Scp {

	private static final int BUFFER_SIZE = 1024;

	private String localFile;
	private String remotePath;

	/**
	 * Constructor for a copying file from local to remote host
	 *
	 * @param session the scp session to use
	 * @param aLocalFile the local file
	 * @param aRemotePath the remote path
	 * @param verbose if true do verbose logging
	 */
	public ScpTo(Session session, String aLocalFile, String aRemotePath) {
		super(session);
		this.localFile = aLocalFile;
		this.remotePath = aRemotePath;
	}

	/**
	 * Carry out the transfer.
	 *
	 * @throws IOException on i/o errors
	 * @throws JSchException on errors detected by scp
	 */
	public void transfer() throws IOException, JSchException {
		doSingleTransfer(localFile);
	}

	private void doSingleTransfer(String fLocalFile) throws IOException, JSchException {
		String cmd = "scp -t " + remotePath;
		Channel channel = openExecChannel(cmd);
		try {
			
			File file = new File(fLocalFile);

			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();


			if (checkAck(in) != 0) {
				System.exit(0);
			}
			sendFileToRemote(file, in, out);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
		}
	}

	private void sendFileToRemote(File localFile, InputStream in, OutputStream out) throws IOException {
		// send "C0644 filesize filename", where filename should not include '/'
		long filesize = localFile.length();
		String command = "C0644 " + filesize + " ";
		command += localFile.getName();
		command += "\n";

		out.write(command.getBytes());
		out.flush();

		if (checkAck(in) != 0) {
			System.exit(0);
		}

		// send a content of lfile
		FileInputStream fis = new FileInputStream(localFile);
		byte[] buf = new byte[BUFFER_SIZE];
		long startTime = System.currentTimeMillis();
		long totalLength = 0;

		try {

			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0) {
					break;
				}
				out.write(buf, 0, len);
				totalLength += len;
			}
			out.flush();
			sendAck(out);
			if (checkAck(in) != 0) {
				System.exit(0);
			}
		} finally {
			fis.close();
		}
	}
}
