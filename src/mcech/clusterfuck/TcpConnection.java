package mcech.clusterfuck;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import mcech.clusterfuck.types.File;
import mcech.log.Logger;
import mcech.util.Base64;

public class TcpConnection implements Closeable {
	public interface Listener {
		public void onRequestID(TcpConnection connection);
		public void onRequestList(TcpConnection connection);
		public void onRequestData(TcpConnection connection, String file, long pos, int len);
		public void onReceiveID(TcpConnection connection, String id);
		public void onReceiveList(TcpConnection connection, Set<File> files);
		public void onReceiveData(String file, long pos, byte[] data);
		public void onFatalError(Exception error);
	}
	
	public TcpConnection(InetSocketAddress address, Listener listener) throws IOException {
		this(new Socket(address.getAddress(), address.getPort()), listener);
	}
	
	public void requestID() throws IOException {
		writeLine("REQUEST ID");
		flush();
	}
	
	public void requestList() throws IOException {
		writeLine("REQUEST LIST");
		flush();
	}
	
	public void requestData(String file, long pos, int len) throws IOException {
		writeLine("REQUEST DATA " + file + " " + pos + " " + len);
		flush();
	}
	
	public void sendID(String id) throws IOException {
		writeLine("ID " + id);
		flush();
	}
	
	public void sendList(Set<File> files) throws IOException {
		writeLine("BEGIN LIST");
		for (File file : files) {
			writeLine("FILE " + file.name + " " + file.size);
		}
		writeLine("END LIST");
		flush();
		
	}
	
	public void sendData(String file, long pos, byte[] data) throws IOException {
		writeLine("DATA " + file + " " + pos + " " + Base64.encode(data));
		flush();
	}
	
	@Override
	public void close() {
		stopped_ = true;
		try {
			socket_.close();
		}
		catch (Exception e) {
			Logger.logWarning(e);
		}
	}
	
	TcpConnection(Socket socket, Listener listener) throws IOException {
		socket_ = socket;
		in_ = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out_ = new OutputStreamWriter(socket.getOutputStream());
		Thread worker = new Thread(() -> {
			try {
				Logger.logDebug("TCP connection to " + socket.getInetAddress().getHostAddress() + " established");
				while (!stopped_) {
					String line = in_.readLine();
					while (!stopped_) {
						if (line.startsWith("REQUEST ID")) {
							listener.onRequestID(this);
						}
						else if (line.startsWith("REQUEST LIST")) {
							listener.onRequestList(this);
						}
						else if (line.startsWith("REQUEST DATA")) {
							try {
								String[] lineArray = line.split(" ");
								String file = lineArray[2];
								long pos = Long.parseLong(lineArray[3]);
								int len = Integer.parseInt(lineArray[4]);
								listener.onRequestData(this, file, pos, len);
							}
							catch (IndexOutOfBoundsException | NumberFormatException e) {
								String remote = socket.getInetAddress().getHostAddress();
								Logger.logWarning("Received invalid line from " + remote + ": " + line);
							}
						}
						else if (line.startsWith("ID")) {
							try {
								String[] lineArray = line.split(" ");
								String id = lineArray[1];
								listener.onReceiveID(this, id);
							}
							catch (IndexOutOfBoundsException e) {
								String remote = socket.getInetAddress().getHostAddress();
								Logger.logWarning("Received invalid line from " + remote + ": " + line);
							}
						}
						else if (line.startsWith("BEGIN LIST")) {
							Set<File> files = new HashSet<>();
							line = in_.readLine();
							while (!line.startsWith("END LIST")) {
								try {
									String[] lineArray = line.split(" ");
									String name = lineArray[1];
									long size = Long.parseLong(lineArray[2]);
									files.add(new File(name, size));
								}
								catch (IndexOutOfBoundsException | NumberFormatException e) {
									String remote = socket.getInetAddress().getHostAddress();
									Logger.logWarning("Received invalid line from " + remote + ": " + line);
								}
								line = in_.readLine();
							}
							listener.onReceiveList(this, files);
						}
						else if (line.startsWith("DATA")) {
							try {
								String[] lineArray = line.split(" ");
								String file = lineArray[1];
								long pos = Long.parseLong(lineArray[2]);
								byte[] data = Base64.decode(lineArray[3]);
								listener.onReceiveData(file, pos, data);
							}
							catch (IndexOutOfBoundsException | NumberFormatException e) {
								String remote = socket.getInetAddress().getHostAddress();
								Logger.logWarning("Received invalid line from " + remote + ": " + line);
							}
						}
						line = in_.readLine();
					}
				}
			}
			catch (Exception err) {
				if (!stopped_) {
					listener.onFatalError(err);
				}
			}
			finally {
				String remote = socket.getInetAddress().getHostAddress();
				Logger.logDebug("TCP connection to " + remote + " closed");
			}
		});
		worker.start();
	}
	
	private void writeLine(String line) throws IOException {
		out_.write(line);
		out_.write('\n');
	}
	
	private void flush() throws IOException {
		out_.flush();
	}
	
	private final Socket socket_;
	private final BufferedReader in_;
	private final Writer out_;
	private volatile boolean stopped_ = false;
}
