package mcech.clusterfuck;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import mcech.clusterfuck.TcpConnection.Listener;
import mcech.log.Logger;

public class TcpServer implements Closeable {
	public TcpServer(int port, Listener listener) throws IOException {
		server_ = new ServerSocket(port);
		Thread worker = new Thread(() -> {
			try {
				Logger.logDebug("TCP server started");
				while (!stopped_) {
					Socket socket = server_.accept();
					TcpConnection connection = new TcpConnection(socket, listener);
					listener.onRequestID(connection);
				}
			}
			catch (Exception err) {
				if (!stopped_) {
					listener.onFatalError(err);
				}
			}
			finally {
				Logger.logDebug("TCP server stopped");
			}
		});
		worker.start();
	}
	
	@Override
	public void close() {
		stopped_ = true;
		try {
			server_.close();
		}
		catch (Exception e) {
			Logger.logWarning(e);
		}
	}
	
	private final ServerSocket server_;
	private volatile boolean stopped_ = false;
}
