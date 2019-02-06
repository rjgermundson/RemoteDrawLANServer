import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class UpdateClientThread extends Thread {
	private static final int PACKET_SIZE = 256;
	private static final int INFO_SIZE = 48;
	private static final int PACKET_HEADER = 8;

	private BlockingQueue<DatagramPacket> queue;
	private DatagramSocket socket;
	
	public UpdateClientThread(BlockingQueue<DatagramPacket> queue) throws SocketException {
		this.queue = queue;
		this.socket = new DatagramSocket();
	}
	
	@Override
	public void run() {
		while (true) {
			if (!queue.isEmpty()) {
				DatagramPacket packet = queue.remove();
				if (isValid(packet)) {
					System.out.println("Received update request from " + packet.getAddress());
					try {
						updateClient(packet);
					} catch (IOException e) {
						System.out.println("Failed to update client at: " + packet.getAddress());
					}
				} else {
					System.out.println("Invalid packet from " + packet.getAddress());
				}
			}
		}
	}

	private void updateClient(DatagramPacket packet) throws IOException {
		byte[] hostAddr = packet.getAddress().getAddress();
		if (hostAddr.length != 4) {
			// Client needs IPv4
			return;
		}
		List<DatagramPacket> responses = getResponses(packet.getAddress(), packet.getPort());
		for (DatagramPacket p : responses) {
			socket.send(p);
		}
	}
	
	private List<DatagramPacket> getResponses(InetAddress client, int port) {
		LinkedList<DatagramPacket> responses = new LinkedList<DatagramPacket>();
		Set<ServerInfo> servers = ServerSet.get();
		Iterator<ServerInfo> iter = servers.iterator();
		while (iter.hasNext()) {
			byte[] responseBytes = new byte[PACKET_SIZE];
			int i = 0;
			for (; i < (PACKET_SIZE - PACKET_HEADER) / INFO_SIZE && iter.hasNext(); i++) {
				ServerInfo currInfo = iter.next();
				byte[] serverBytes = currInfo.getBytes();
				System.arraycopy(serverBytes, 0, responseBytes, PACKET_HEADER + (i * serverBytes.length), serverBytes.length);
			}
			byte[] count = Utility.intToBytes(i);
			System.arraycopy(count, 0, responseBytes, count.length, count.length);
			DatagramPacket response = new DatagramPacket(responseBytes, PACKET_SIZE, client,port);
			responses.add(response);
		}
		ServerSet.release();
		return responses;
	}
	
  private static boolean isValid(DatagramPacket packet) {
  	byte[] data = packet.getData();
  	for (int i = 0; i < data.length; i++) {
  		if ((byte) data[i] != (byte) i) {
  			return false;
  		}
  	}
  	return true;
  }
}
