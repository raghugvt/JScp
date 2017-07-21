package com.emc.util.scp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.logging.Level;

public abstract class Scp {
	private static final double ONE_SECOND = 1000.0;
	private static final byte[] ACK = { 0 };

	private Session session;

	/**
	 * Constructor for Scp
	 *
	 * @param session
	 *            the ssh session to use
	 * @param verbose
	 *            if true do verbose logging
	 */
	public Scp(Session session) {
		this.session = session;
	}

	/**
	 * Open an ssh channel.
	 *
	 * @param command
	 *            the command to use
	 * @return the channel
	 * @throws JSchException
	 *             on error
	 */
	protected Channel openExecChannel(String command) throws JSchException {
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
		return channel;
	}

	/**
	 * Send an ack.
	 *
	 * @param out
	 *            the output stream to use
	 * @throws IOException
	 *             on error
	 */
	protected void sendAck(OutputStream out) throws IOException {
		out.write(ACK);
		out.flush();
	}

	/**
	 * Reads the response, throws a BuildException if the response indicates an
	 * error.
	 *
	 * @param in
	 *            the input stream to use
	 * @throws IOException
	 *             on I/O error
	 * @throws BuildException
	 *             on other errors
	 */
	protected int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	/**
	 * Carry out the transfer.
	 *
	 * @throws IOException
	 *             on I/O errors
	 * @throws JSchException
	 *             on ssh errors
	 */
	public abstract void transfer() throws IOException, JSchException;
}