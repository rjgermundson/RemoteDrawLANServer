import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

public class RegisterServerThread extends Thread {
	BlockingQueue<DatagramPacket> queue;
	
	public RegisterServerThread(BlockingQueue<DatagramPacket> queue) {
		this.queue = queue;
	}
	
	@Override
	public void run() {
		while (true) {
			if (!queue.isEmpty()) {
				DatagramPacket packet = queue.remove();
				if (isValid(packet)) {
					System.out.println("Received server register from " + packet.getAddress());
					ServerInfo info = parse(packet);
					ServerSet.get().remove(info);
					ServerSet.release();
					ServerSet.get().add(info);
					ServerSet.release();
				} else {
					System.out.println("Invalid packet from: " + packet.getAddress());
				}
			}
		}
	}
	
	private boolean isValid(DatagramPacket packet) {
		byte[] data = packet.getData();
		for (int i = 56; i < data.length; i++ ) {
			if (data[i] != (byte) i) {
				return false;
			}
		}
		return true;
	}
	
	private ServerInfo parse(DatagramPacket packet) {
		byte[] data = packet.getData();
		boolean kill = (data[0] & 1) == 1;
		System.out.println("kill: " + kill);
		byte[] IP = new byte[4];
		for (int i = 0; i < IP.length; i++) {
			IP[i] = data[8 + i];
		}
		byte[] portBytes = new byte[4];
		for (int i = 0; i < 4; i++) {
			portBytes[i] = data[IP.length + 8 + i];
		}
		int port = Utility.bytesToInt(portBytes);
		byte[] countBytes = new byte[4];
		for (int i = 0; i < 4; i++) {
			countBytes[i] = data[IP.length + 12 + i];
		}
		int count = Utility.bytesToInt(countBytes);
		byte[] limitBytes = new byte[4];
		for (int i = 0; i < 4; i++) {
			limitBytes[i] = data[IP.length + 16 + i];
		}
		int limit = Utility.bytesToInt(limitBytes);
		String name = new String(data, 24, 32);
		return new ServerInfo(name, IP, port, count, limit, kill);
	}
}
