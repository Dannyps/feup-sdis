import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements RMIRemote {

	public Peer(Registry registry) {
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

		Integer id=-1;
		
		try {
			Registry registry = LocateRegistry.getRegistry();
			Peer obj = new Peer(registry);
			RMIRemote stub = (RMIRemote) UnicastRemoteObject.exportObject(obj, 0);

			id = (int) (System.currentTimeMillis() % 1000000);

			// Bind the remote object's stub in the registry
			registry.rebind(id.toString(), stub);

			System.err.println("Peer ready on "+ id.toString());
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
		new Peer(null);
	}
}
