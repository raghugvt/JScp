package com.emc.util.scp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.logging.Level;

public class ScpFrom extends Scp {

	private static final int BUFFER_SIZE = 1024;

	private String localFile;
	private String remotePath;

	/**
	 * Constructor for bringing file from remote to local host
	 *
	 * @param session the scp session to use
	 * @param aRemotePath the remote path
	 * @param aLocalFile the local file
	 * 
	 */
	public ScpFrom(Session session, String aRemotePath, String aLocalFile) {
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
		FileOutputStream fos = null;
		try {

			String rfile = remotePath;
			String lfile = localFile;

			String prefix = null;
			if (new File(lfile).isDirectory()) {
				prefix = lfile + File.separator;
			}

			// exec 'scp -f rfile' remotely
			String command = "scp -f " + rfile;
			// Channel channel=session.openChannel("exec");
			// ((ChannelExec)channel).setCommand(command);
			Channel channel = openExecChannel(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] buf = new byte[1024];

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			while (true) {
				int c = checkAck(in);
				if (c != 'C') {
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize = 0L;
				while (true) {
					if (in.read(buf, 0, 1) < 0) {
						// error
						break;
					}
					if (buf[0] == ' ')
						break;
					filesize = filesize * 10L + (long) (buf[0] - '0');
				}

				String file = null;
				for (int i = 0;; i++) {
					in.read(buf, i, 1);
					if (buf[i] == (byte) 0x0a) {
						file = new String(buf, 0, i);
						break;
					}
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();

				// read a content of lfile
				fos = new FileOutputStream(prefix == null ? lfile : prefix + file);
				int foo;
				while (true) {
					if (buf.length < filesize)
						foo = buf.length;
					else
						foo = (int) filesize;
					foo = in.read(buf, 0, foo);
					if (foo < 0) {
						// error
						break;
					}
					fos.write(buf, 0, foo);
					filesize -= foo;
					if (filesize == 0L)
						break;
				}
				fos.close();
				fos = null;

				if (checkAck(in) != 0) {
					System.exit(0);
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
			}

		} catch (Exception e) {
			System.out.println(e);
			try {
				if (fos != null)
					fos.close();
			} catch (Exception ee) {
			}
		}

	}
}
