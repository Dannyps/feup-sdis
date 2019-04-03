import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements RMIRemote {

	public Peer(Registry registry, AddrPort mC, AddrPort mDB, AddrPort mDR, ProtocolVersion pv, Integer serverId,
			String serviceAP) {
	}

	public String sayHello() {
		return "Hello, world!";
	}

    public int backup(String filename, int replicationDegree) {
		return 0;
	}

    public int restore(String filename) {
		return 0;
	}

    public int delete(String filename) {
		return 0;
	}

    public int reclaim(int a) {
		return 0;
	}

    public String getState() {
		return "my state";
	}

	public static void main(String args[]) {
		parseArgs(args);
		try{
			AddrPort MC = new AddrPort(args[0]);
			AddrPort MDB = new AddrPort(args[1]);
			AddrPort MDR = new AddrPort(args[2]);
			ProtocolVersion protocolVersion = new ProtocolVersion(args[3]);
			Integer serverId = Integer.parseInt(args[4]);
			String serviceAP = args[5];
	
			try {
				Registry registry = LocateRegistry.getRegistry();
				Peer obj = new Peer(registry, MC, MDB, MDR, protocolVersion, serverId, serviceAP);
				RMIRemote stub = (RMIRemote) UnicastRemoteObject.exportObject(obj, 0);
	
				// Bind the remote object's stub in the registry
				registry.rebind(serviceAP, stub);
	
				System.err.println("Peer ready on " + serviceAP);
			} catch (Exception e) {
				System.err.println("Server exception: " + e.toString());
				e.printStackTrace();
			}
			new Peer(null, MDR, MDR, MDR, protocolVersion, serverId, serviceAP);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-2);
		}
	}

	/**
	 * Loads arguments from the args list. Terminates execution on bad args.
	 * @param args
	 */
	private static void parseArgs(String[] args) {
		if (args.length != 6) {
			System.err.println(ConsoleColours.RED_BOLD_BRIGHT + "[ERROR] Expected 6 arguments, got " + args.length + "!");
			System.exit(-1);
		}

		for (String var : args) {
			System.out.println(var);
		}
	}
}
