package mcech.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class BroadcastSocket implements Closeable {
	public BroadcastSocket() throws IOException {
		socket_ = new DatagramSocket();
		socket_.setBroadcast(true);
	}
	
	public BroadcastSocket(int port) throws IOException {
		socket_ = new DatagramSocket(port);
		socket_.setBroadcast(true);
	}
	
	public int getLocalPort() {
		return socket_.getLocalPort();
	}
	
	public int getTimeout() {
		return timeout_;
	}
	
	public void setTimeout(int timeout) {
		if (timeout < 0) {
			throw new IllegalArgumentException();
		}
		timeout_ = timeout;
	}
	
	public void send(DatagramPacket packet) throws IOException {
		socket_.send(packet);
	}
	
	public DatagramPacket receive() throws IOException {
		byte[] buffer = new byte[Character.MAX_VALUE];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		if (timeout_ > 0) {
			long endTime = System.currentTimeMillis() + timeout_;
			while (true) {
				int remaining = (int)(endTime - System.currentTimeMillis());
				if (remaining <= 0) {
					throw new SocketTimeoutException();
				}
				socket_.setSoTimeout(remaining);
				try {
					packet.setLength(Character.MAX_VALUE);
					socket_.receive(packet);
				}
				catch (SocketTimeoutException ignore) {}
				if (!isLocalAddress(packet.getAddress())) {
					byte[] result = Arrays.copyOf(packet.getData(), packet.getLength());
					return new DatagramPacket(result, result.length, packet.getSocketAddress());
				}
			}
		}
		else {
			while (true) {
				socket_.setSoTimeout(0);
				try {
					packet.setLength(Character.MAX_VALUE);
					socket_.receive(packet);
				}
				catch (SocketTimeoutException ignore) {}
				if (!isLocalAddress(packet.getAddress())) {
					return packet;
				}
			}
		}
	}
	
	@Override
	public void close() {
		socket_.close();
	}
	
	public static final InetAddress BROADCAST_ADDRESS;
	static {
		try {
			BROADCAST_ADDRESS = InetAddress.getByAddress("255.255.255.255", new byte[]{(byte)255, (byte)255, (byte)255, (byte)255});
		}
		catch (UnknownHostException never) {
			throw new RuntimeException(never);
		}
	}
	
	private boolean isLocalAddress(InetAddress address) throws SocketException {
		return NetworkInterface.networkInterfaces().flatMap(netIf -> netIf.inetAddresses()).anyMatch(addr -> addr.equals(address));
	}
	
	private DatagramSocket socket_;
	private int timeout_;
}
