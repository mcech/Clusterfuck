package mcech.net;

import static mcech.net.BroadcastSocket.BROADCAST_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.Test;

public class BroadcastSocketTest {
	@Test
	public void blockOwnMessageWithSamePort() throws IOException {
		try (BroadcastSocket sock = new BroadcastSocket()) {
			sock.setTimeout(500);
			byte[] msg = "CLUSTERFUCK".getBytes();
			DatagramPacket packet = new DatagramPacket(msg, msg.length, BROADCAST_ADDRESS, sock.getLocalPort());
			sock.send(packet);
			assertThrows(SocketTimeoutException.class, () -> sock.receive());
		}
	}
	
	@Test
	public void permitOwnMessageWithDistinctPorts() throws IOException {
		try (BroadcastSocket sender = new BroadcastSocket(); BroadcastSocket receiver = new BroadcastSocket()) {
			receiver.setTimeout(500);
			byte[] msg = "CLUSTERFUCK".getBytes();
			DatagramPacket sent = new DatagramPacket(msg, msg.length, BROADCAST_ADDRESS, receiver.getLocalPort());
			sender.send(sent);
			DatagramPacket received = receiver.receive();
			assertArrayEquals(msg, received.getData());
		}
	}
	
	@Test
	public void testTimeout() throws IOException {
		try (BroadcastSocket sock = new BroadcastSocket()) {
			sock.setTimeout(500);
			assertThrows(SocketTimeoutException.class, () -> sock.receive());
		}
	}
	
	@Test
	public void testNoTimeout() throws Exception {
		BroadcastSocket sock = new BroadcastSocket();
		sock.setTimeout(0);
		Thread worker = new Thread(() -> {
			try {Thread.sleep(500);} catch (InterruptedException ignore) {}
			sock.close();
		});
		worker.start();
		assertThrows(SocketException.class, () -> sock.receive());
	}
}
