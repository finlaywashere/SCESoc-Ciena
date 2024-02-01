package dhcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketServer {
	private static final int PORT = 1234;
	
	private static List<Socket> sockets;
	
	public static void main(String[] args) {
		try {
			sockets = new ArrayList<Socket>();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					super.run();
					for(Socket s : sockets) {
						try {
							PrintWriter out = new PrintWriter(s.getOutputStream(),true);
							out.println("SERVER CLOSED!");
							out.flush();
							out.close();
							s.close();
						}catch(Exception e) {
							// Ignore exception as socket is likely closed
						}
					}
				}
			});
			ServerSocket ss = new ServerSocket(PORT);
			while(!ss.isClosed()) {
				try {
					Socket s = ss.accept();
					sockets.add(s);
					new Thread() {
						public void run() {
							try {
								new Server(s.getInputStream(), s.getOutputStream());
							} catch (IOException e) {
								e.printStackTrace();
							}
						};
					}.start();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			ss.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
