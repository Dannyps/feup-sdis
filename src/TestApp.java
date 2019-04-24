import java.io.Console;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Shared.RMIRemote;
import Utils.ChunkInfo;
import Utils.ConsoleColours;
import Utils.FileInfo;
import Utils.PrintMessage;
import Shared.PeerState;

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
		PrintMessage.printMessages = true;
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
		int b = stub.backup(args[2], Integer.parseInt(args[3]));
		if (b == 0) {
			PrintMessage.p("Success", "The backup thread has been launched successfully.");
		}
		return true;
	}

	private static boolean restore(RMIRemote stub, String[] args) throws RemoteException {
		checkArgs(args, "file_name");
		int r = stub.restore(args[2]);
		if (r == -1) {
			PrintMessage.p("Error", "The requested file (" + args[2] + ") was not found!", ConsoleColours.RED_BOLD,
					ConsoleColours.RED);
		}
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
			System.exit(-1);
		}
	}

	private static boolean state(RMIRemote stub) throws RemoteException {
		PeerState s = stub.getServiceState();

		// display information regarding local service state

		// display own backed up files
		System.out.println(ConsoleColours.YELLOW_BRIGHT + "=====================================");
		System.out.println("Information about local files backups");
		System.out.println("=====================================" + ConsoleColours.RESET);
		ConcurrentHashMap<String, FileInfo> localBackedUpFiles = s.getLocalBackedUpFiles();
		if (localBackedUpFiles.size() == 0)
			System.out.println("No backed up files");
		for (Map.Entry<String, FileInfo> backedUpFile : localBackedUpFiles.entrySet()) {
			String filename = backedUpFile.getKey();
			FileInfo finfo = backedUpFile.getValue();
			System.out.println(ConsoleColours.BLUE_BOLD_BRIGHT + "FILE" + ConsoleColours.RESET);
			System.out.println(String.format("\tFile name: %s", filename));
			System.out.println(String.format("\tFile Id: %s", finfo.getFileIdHex()));
			System.out.println(String.format("\tDesired replication degree: %d", finfo.getRdegree()));
			System.out.println(String.format("\tNumber of chunks: %d", finfo.getNumberChunks()));
			// show chunks
			for (Map.Entry<Integer, ChunkInfo> chunk : finfo.getChunks().entrySet()) {
				System.out.println(ConsoleColours.RED_BOLD_BRIGHT + "\tCHUNK" + ConsoleColours.RESET);
				System.out.println(String.format("\t\tChunk Id: %s", chunk.getKey()));
				System.out.println(
						String.format("\t\tPerceived Replication Degree: %d", chunk.getValue().getBackupDegree()));
			}
		}

		// display backed up chunks
		System.out.println(ConsoleColours.YELLOW_BRIGHT + "===============================");
		System.out.println("Information about stored chunks");
		System.out.println("===============================" + ConsoleColours.RESET);
		ConcurrentHashMap<String, ConcurrentHashMap<Integer, ChunkInfo>> fileChunks = s.getStoredChunks();
		for (String fileId : fileChunks.keySet()) {
			System.out.println(ConsoleColours.BLUE_BOLD_BRIGHT + "FILE " + fileId + ConsoleColours.RESET);
			boolean hasLocalChunks = false;
			ConcurrentHashMap<Integer, ChunkInfo> chunks = fileChunks.get(fileId);
			for (Map.Entry<Integer, ChunkInfo> chunk : chunks.entrySet()) {
				if (chunk.getValue().isStoredLocally()) {
					hasLocalChunks = true;
					System.out.println(ConsoleColours.RED_BOLD_BRIGHT + "\tCHUNK" + ConsoleColours.RESET);
					System.out.println(String.format("\t\tChunk Id: %s", chunk.getKey()));
					System.out.println(String.format("\t\tChunk Size: %d", chunk.getValue().getChunkSize()));
					System.out.println(
							String.format("\t\tPerceived Replication Degree: %d", chunk.getValue().getBackupDegree()));
				}
			}

			if (!hasLocalChunks)
				System.out.println("\tNo local backed up chunks");
		}

		// display information about peer storage
		System.out.println(ConsoleColours.YELLOW_BRIGHT + "=========================");
		System.out.println("Information about storage");
		System.out.println("=========================" + ConsoleColours.RESET);
		System.out.println(String.format("Storage capacity: %d (KB)", s.getStorageCapacity() / 1000));
		System.out.println(String.format("Used storage: %d (KB)", s.getStorageUsed() / 1000));
		return true;
	}
}