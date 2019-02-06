import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerSet {
	private static Set<ServerInfo> servers = new HashSet<ServerInfo>();
	private static Lock serverLock = new ReentrantLock();
	
	public static Set<ServerInfo> get() {
		serverLock.lock();
		return servers;
	}
	
	public static void release() {
		serverLock.unlock();
	}
}
