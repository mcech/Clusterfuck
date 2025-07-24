package mcech.clusterfuck;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import mcech.log.Logger;

public class Discovery implements Closeable {
	public static interface Listener {
		public void onDiscovery(InetSocketAddress address);
		public void onError(Exception error);
	}
	
	public Discovery(int port, Listener listener) throws IOException {
		socket_ = new DatagramSocket(port);
		socket_.setBroadcast(true);
		byte[] buffer = new byte[Character.MAX_VALUE];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		Thread worker = new Thread(() -> {
			try {
				Logger.logInfo("UDP discovery started on port " + port);
				int delay = MIN_DELAY;
				while (!stopped_) {
					packet.setData(message_);
					packet.setAddress(BROADCAST_ADDRESS);
					packet.setPort(port);
					socket_.send(packet);
					
					long endTime = System.currentTimeMillis() + delay;
					int remaining = (int)(endTime - System.currentTimeMillis());
					while(!stopped_ && remaining > 0) {
						try {
							packet.setData(buffer);
							socket_.setSoTimeout(remaining);
							socket_.receive(packet);
							InetSocketAddress address = (InetSocketAddress) packet.getSocketAddress();
							if (!isLocalAddress(packet.getAddress()) || packet.getPort() != socket_.getLocalPort()) {
								if (Arrays.equals(message_, packet.getData())) {
									listener.onDiscovery(address);
								}
							}
						}
						catch (SocketTimeoutException ignore) {}
						remaining = (int)(endTime - System.currentTimeMillis());
					}
					
					delay = Math.min(2 * delay, MAX_DELAY);
				}
			}
			catch (Exception err) {
				if (!stopped_) {
					listener.onError(err);
				}
			}
			finally {
				Logger.logInfo("UDP discovery stopped");
			}
		});
		worker.start();
	}
	
	@Override
	public void close() {
		stopped_ = true;
		socket_.close();
	}
	
	private boolean isLocalAddress(InetAddress address) throws SocketException {
		return NetworkInterface.networkInterfaces().flatMap(netIf -> netIf.inetAddresses()).anyMatch(addr -> addr.equals(address));
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
	private static final int MIN_DELAY = 1000;       //  1 second
	private static final int MAX_DELAY = 60 * 1000;  // 60 seconds
	private static final byte[] message_ = ("CLUSTERFUCK").getBytes();
	
	private final DatagramSocket socket_;
	private volatile boolean stopped_ = false;
}
