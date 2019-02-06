import java.util.Arrays;

public class ServerInfo {
	private static final int INFO_LENGTH = 48;
	private String name;
	private byte[] IP;
	private int port;
	private int count;
	private int limit;
	
	public ServerInfo(String name, byte[] IP, int port, int count, int limit) {
		this.name = name;
		this.IP = IP;
		this.port = port;
		this.count = count;
		this.limit = limit;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[INFO_LENGTH];
		for (int i = 0; i < IP.length; i++) {
			bytes[i] = IP[i];
		}
		byte[] portBytes = Utility.intToBytes(port);
		for (int i = 0; i < 4; i++) {
			bytes[IP.length + i] = portBytes[i];
		}
		byte[] countBytes = Utility.intToBytes(count);
		for (int i = 0; i < 4; i++) {
			bytes[IP.length + 4 + i] = countBytes[i];
		}
		byte[] limitBytes = Utility.intToBytes(limit);
		for (int i = 0; i < 4; i++) {
			bytes[IP.length + 8 + i] = limitBytes[i];
		}
		byte[] nameBytes = name.getBytes();
		for (int i = 0; i < nameBytes.length; i++) {
			bytes[IP.length + 12 + i] = nameBytes[i];
		}
		return bytes;
	}
	
	@Override
	public int hashCode() {
		int result = 0;
		for (int i = 0; i < name.length(); i++) {
			result += (name.charAt(i) * 37);
			result = result % (Integer.MAX_VALUE / 2);
		}
		for (int i = 0; i < IP.length; i++) {
			result += ((IP[i]) * 37);
			result = result % (Integer.MAX_VALUE / 2);
		}
		result += port;
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ServerInfo)) {
			return false;
		}
		ServerInfo other = (ServerInfo) o;
		
		if (!this.name.equals(other.name)) {
			return false;
		}
		if (!Arrays.equals(this.IP, other.IP)) {
			return false;
		}
		if (this.port != other.port) {
			return false;
		}
		return true;
	}
}
