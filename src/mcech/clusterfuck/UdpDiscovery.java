package mcech.clusterfuck;

import static mcech.net.BroadcastSocket.BROADCAST_ADDRESS;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import mcech.log.Logger;
import mcech.net.BroadcastSocket;;

public class UdpDiscovery implements Closeable {
	public static interface Listener {
		public void onDiscovery(SocketAddress address);
		public void onFatalError(Exception error);
	}
	
	public UdpDiscovery(int port, Listener listener) throws IOException {
		socket_ = new BroadcastSocket(port);
		Thread worker = new Thread(() -> {
			try {
				Logger.logDebug("UDP discovery started");
				int delay = MIN_DELAY;
				while (!stopped_) {
					long endTime = System.currentTimeMillis() + delay;
					
					DatagramPacket packet = new DatagramPacket(message_, message_.length, BROADCAST_ADDRESS, port);
					socket_.send(packet);
					
					int remaining = (int)(endTime - System.currentTimeMillis());
					while(!stopped_ && remaining > 0) {
						try {
							socket_.setTimeout(remaining);
							packet = socket_.receive();
							SocketAddress address = packet.getSocketAddress();
							if (Arrays.equals(message_, packet.getData())) {
								listener.onDiscovery(address);
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
					listener.onFatalError(err);
				}
			}
			finally {
				Logger.logDebug("UDP discovery stopped");
			}
		});
		worker.start();
	}
	
	@Override
	public void close() {
		stopped_ = true;
		socket_.close();
	}
	
	private static final int MIN_DELAY = 1000;       //  1 second
	private static final int MAX_DELAY = 60 * 1000;  // 60 seconds
	private static final byte[] message_ = ("CLUSTERFUCK").getBytes();
	
	private final BroadcastSocket socket_;
	private volatile boolean stopped_ = false;
}
