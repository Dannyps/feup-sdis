import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Testing application 
 * 
 * Commands must be of the following format:
 * 
 * java TestApp peer_rmi_id sub_protocol [operand_1] [operand_2]
 * 
 *
 */
public class TestApp {

    public static void main(String[] args) {

	String peer_rmi_id = args[1];
	try {
	    /* null host is the same as localhost */
	    Registry registry = LocateRegistry.getRegistry(null);
	    /* */
	    RMIRemote stub = (RMIRemote) registry.lookup(peer_rmi_id);
	} catch (Exception e) {
	    System.err.println("Client exception: " + e.toString());
	    e.printStackTrace();
	}
    }
}
