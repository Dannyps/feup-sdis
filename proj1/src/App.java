
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Client Interface
 * 
 * Communicates via RMI with the specified peer. Is ran to perform one single
 * operation. Arguments must be passed when calling running the program. No
 * input will be read from the standard input. Commands must be of the following
 * format:<br/>
 * 
 * <p>
 * <strong>java App peer_rmi_id sub_protocol [operand_1] [operand_2]</strong>
 * </p>
 * 
 * Exit codes:<br/>
 * -1: wrong arguments<br/>
 * -2: peer_rmi_id invalid.<br/>
 *
 */
public class App {

	private App() {
	}

	/**
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		int nargs = args.length;
		if (nargs < 2) {
			System.err.println(ConsoleColours.YELLOW + "[ERROR] Expected at least 2 arguments, got " + nargs + ".");
			System.exit(-1);
		}

		String peer_rmi_id = args[0];
		String command = args[1];
		try {
			Registry registry = LocateRegistry.getRegistry(null);
			RMIRemote stub = (RMIRemote) registry.lookup(peer_rmi_id);

			command = command.toUpperCase();
			switch (command) {
			case "BACKUP":
				backup(stub, args);
				break;
			case "RESTORE":
				restore(stub, args);
				break;
			case "DELETE":
				delete(stub, args);
				break;
			case "RECLAIM":
				reclaim(stub, args);
				break;
			case "STATE":
				state(stub);
				break;

			default:
				System.err.println(ConsoleColours.YELLOW + "[ERROR] The specified sub_protocol (" + command
						+ ") could not be recognized!");
				break;
			}
		} catch (NotBoundException e) {
			System.err.println(
					ConsoleColours.RED_BOLD + "[FATAL] The specified RMI ID (" + peer_rmi_id + ") does not exist!");
			System.exit(-2);
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private static void backup(RMIRemote stub, String[] args) {
		int nargs = args.length;
		if (nargs != 4) {
			System.err.println(ConsoleColours.RED_BOLD + "[FATAL] Expected 2 arguments, got " + (nargs - 2) + "!");
			System.err.println(ConsoleColours.RED_BOLD + "BACKUP requires two arguments: "
					+ ConsoleColours.BLUE_BACKGROUND_BRIGHT + "file_name" + ConsoleColours.RESET
					+ ConsoleColours.RED_BOLD + " and " + ConsoleColours.BLUE_BACKGROUND_BRIGHT + "replication_degree"
					+ ConsoleColours.RESET + ConsoleColours.RED_BOLD + ".");
			System.exit(-1);
		}

	}

	private static void restore(RMIRemote stub, String[] args) {
		checkArgs(args, "file_name");

	}

	private static void delete(RMIRemote stub, String[] args) {
		checkArgs(args, "file_name");

	}

	private static void reclaim(RMIRemote stub, String[] args) {
		checkArgs(args, "max_disk_space");

	}

	/**
	 * kills the process on fail
	 * @param args
	 * @param required
	 */
	private static void checkArgs(String[] args, String required) {
		int nargs = args.length;
		if (nargs != 3) {
			System.err.println(ConsoleColours.RED_BOLD + "[FATAL] Expected 1 argument, got " + (nargs - 2) + "!");
			System.err.println(ConsoleColours.RED_BOLD + "DELETE requires one argument: "
					+ ConsoleColours.BLUE_BACKGROUND_BRIGHT + required + ConsoleColours.RESET
					+ ConsoleColours.RED_BOLD + ".");
			System.exit(-1);
		}
	}

	private static void state(RMIRemote stub) {
		// TODO Auto-generated method stub

	}
}