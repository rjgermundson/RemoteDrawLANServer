import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IntermediateServer {
	private static final int PACKET_SIZE = 256;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java IntermediateServer <ip> <port>");
		}
		InetAddress address = null;
		int port = -1;
		try {
			address = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e) {
			System.out.println("Invalid IP: " + args[0]);
			System.exit(1);
		}
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println("Invalid port: " + args[1]);
			System.exit(1);
		}
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(port, address);
		} catch (SocketException e) {
			System.out.println("Failed to open datagram socket.\nIP: " + address + "\nPort: " + port);
		}
		
		BlockingQueue<DatagramPacket> registerQueue = new LinkedBlockingQueue<>();
		BlockingQueue<DatagramPacket> clientQueue = new LinkedBlockingQueue<>();
		RegisterServerThread registerThread = new RegisterServerThread(registerQueue);
		UpdateClientThread updateThread = null;
		try {
			updateThread = new UpdateClientThread(clientQueue);
		} catch (SocketException e) {
			System.out.println("Failed to open datagram socket.\nIP: " + address + "\nPort: " + port);
			System.exit(1);
		}
		registerThread.start();
		updateThread.start();
		while (true) {
			DatagramPacket received = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
			try {
				socket.receive(received);
			} catch (IOException e) {
				System.out.println("Failed to received packet from: " + received.getAddress());
			}
			if ((received.getData()[0] & 256) == 256) {
				// Server registry
				try {
					registerQueue.put(received);
				} catch (InterruptedException e) {
					System.out.println("Failed to put packet from: " + received.getAddress());
				}
			} else {
				clientQueue.add(received);
			}
			if (socket.isClosed()) {
				break;
			}
		}
		socket.close();
	}
	
	
}
