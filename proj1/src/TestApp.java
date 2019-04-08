import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import utils.ConsoleColours;

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
public class TestApp {

	private static Thread shutDownThread = new Thread() {
		public void run() {
			// clear the console on shutdown.
			System.out.println(ConsoleColours.RESET);
		}
	};

	private TestApp() {
	}

	/**
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(shutDownThread);
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
		} catch (ConnectException e) {
			System.err.println(ConsoleColours.RED_BOLD
					+ "[FATAL] The TestApp could not connect to the rmiregistry service:\n" + e.getMessage());
			System.err.println(ConsoleColours.GREEN_BOLD_BRIGHT + "[TIP] " + ConsoleColours.YELLOW_BOLD
					+ "Perhaps the rmiregistry service is not running.");
			System.exit(-2);
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private static boolean backup(RMIRemote stub, String[] args) throws NumberFormatException, RemoteException {
		int nargs = args.length;
		if (nargs != 4) {
			System.err.println(ConsoleColours.RED_BOLD + "[FATAL] Expected 2 arguments, got " + (nargs - 2) + "!");
			System.err.println(ConsoleColours.RED_BOLD + "BACKUP requires two arguments: "
					+ ConsoleColours.BLUE_BACKGROUND_BRIGHT + "file_name" + ConsoleColours.RESET
					+ ConsoleColours.RED_BOLD + " and " + ConsoleColours.BLUE_BACKGROUND_BRIGHT + "replication_degree"
					+ ConsoleColours.RESET + ConsoleColours.RED_BOLD + ".");
			System.exit(-1);
		}
		stub.backup(args[2], Integer.parseInt(args[3]));
		return true;
	}

	private static boolean restore(RMIRemote stub, String[] args) throws RemoteException {
		checkArgs(args, "file_name");
		stub.restore(args[2]);
		return true;
	}

	private static boolean delete(RMIRemote stub, String[] args) throws RemoteException {
		checkArgs(args, "file_name");
		stub.delete(args[2]);
		return true;
	}

	private static boolean reclaim(RMIRemote stub, String[] args) throws NumberFormatException, RemoteException {
		checkArgs(args, "max_disk_space");
		stub.reclaim(Integer.parseInt(args[2]));
		return true;
	}

	/**
	 * kills the process on fail
	 * 
	 * @param args
	 * @param required
	 */
	private static void checkArgs(String[] args, String required) {
		int nargs = args.length;
		if (nargs != 3) {
			System.err.println(ConsoleColours.RED_BOLD + "[FATAL] Expected 1 argument, got " + (nargs - 2) + "!");
			System.err.println(
					ConsoleColours.RED_BOLD + "DELETE requires one argument: " + ConsoleColours.BLUE_BACKGROUND_BRIGHT
							+ required + ConsoleColours.RESET + ConsoleColours.RED_BOLD + ".");
			System.exit(-1);
		}
	}

	private static boolean state(RMIRemote stub) throws RemoteException {
		String s = stub.getState();

		System.out.println(s);
		return true;
	}
}