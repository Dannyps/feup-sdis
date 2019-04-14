package Shared;

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Listeners.MCListen;
import Listeners.MDBListen;
import Listeners.MDRListen;
import Messages.GetChunkMessage;
import Utils.*;
import Workers.BackupWorker;
import Workers.ChunkReceiverWatcher;
import Workers.DeleteSenderWorker;
import Workers.FileSystemWorker;
import Workers.RestoreWorker;

public class Peer implements RMIRemote {
	/** The address and port for Control Multicast Channel */
	private AddrPort MC;
	/** The address and port for Data Backup Multicast Channel */
	private AddrPort MDB;
	/** The address and port for Data Recovery Multicast Channel */
	private AddrPort MDR;
	/** The socket for Control channel */
	private MulticastSocket mcSocket;
	/** The socket for Data Backup channel */
	private MulticastSocket mdbSocket;
	/** The socket for Data Recovery channel */
	private MulticastSocket mdrSocket;
	/** The protocol version to be used. Without enhancements, should be 1.0 */
	private ProtocolVersion protoVer;
	/** Numeric identifier for the peer. unique among all peers on the LAN */
	private Integer serverId;
	/**
	 * Keeps track of the lastest chunk headers received through MDR channel. It
	 * maps file identifiers in hexadecimal format to a new table. The later maps
	 * chunk numbers to a timestamp (a system time instance of when did the CHUNK
	 * message arrived on this peer). This data structure is only used for the
	 * Restore protocol. The main purpose of this data structure is to avoid the
	 * Initiator Peer being flooded of Chunk messages for the same chunk. Therefore,
	 * before any peer sends the chunk, it consults this table to know if other
	 * Peers sent the chunk already
	 */
	private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>> receivedChunkInfo;
	/**
	 * Similar to {@link receivedChunkInfo}, but instead of recording timestamps, it
	 * registers the actual chunk raw data. This data structure is populated as
	 * CHUNK messages arrive, but only when the Initiator Peer is restoring a file.
	 * When this table has all chunks of data for the file being restored, some
	 * worker will construct the file on the local filesystem
	 */
	private ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>> receivedChunkData;

	private PeerState state;

	/** Reference to the singleton peer */
	private static Peer single_instance = null;
	/** Manager for the runnables of this Peer (ThreadPool) */
	ExecutorService executor;

	/**
	 * Construct the singleton Peer
	 * 
	 * @param mC       The address:port for Control multicast channel
	 * @param mDB      The address:port for Data Backup multicast channel
	 * @param mDR      The address:port for Data Recovery multicast channel
	 * @param pv       The protocol version to be used
	 * @param serverId This peer unique identifier for the LAN
	 */
	private Peer(AddrPort mC, AddrPort mDB, AddrPort mDR, ProtocolVersion pv, Integer serverId) {
		// initialize fields
		this.MC = mC;
		this.MDB = mDB;
		this.MDR = mDR;
		this.protoVer = pv;
		this.serverId = serverId;

		// Joins the multicast channels for control and data backup/recovery
		this.mcSocket = bindToMultiCast(MC);
		this.mdbSocket = bindToMultiCast(MDB);
		this.mdrSocket = bindToMultiCast(MDR);

		this.receivedChunkInfo = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>>();
		this.receivedChunkData = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>>();

		// load persistent data
		this.state = FileSystemWorker.loadPeerState(serverId);

		// instatiate thread pool
		this.executor = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		System.out.println("[INFO] Bound to all three sockets successfully.");
		PrintMessage.printMessages = true;
	}

	/**
	 * Joins the multicast group in the specified address:port
	 * 
	 * @param ap The address:port of the channel that this peer must join
	 * @return The multicast socket to get and send messages from/to the joined
	 *         multicast group
	 */
	MulticastSocket bindToMultiCast(AddrPort ap) {
		MulticastSocket s = null;
		try {
			s = new MulticastSocket(ap.getPort());
			s.joinGroup(ap.getInetAddress());
		} catch (SocketException e) {
			System.out.println("[FATAL] Could not enter multicast group " + ap + ": " + e.getMessage());
			System.exit(6);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(6);
		}
		return s;
	}

	public int backup(String filename, int replicationDegree) {
		RegularFile f = new RegularFile(filename, replicationDegree);
		ArrayList<Chunk> lst;

		this.state.addLocalFileBackup(f, replicationDegree);
		try {
			lst = f.getChunks();
			int i = 0;
			for (Chunk c : lst) {
				PrintMessage.p("Created chunk", c.toString(), ConsoleColours.GREEN_BOLD, ConsoleColours.GREEN);
				this.executor.submit(new BackupWorker(filename, c, i++));
			}
		} catch (IOException e) {
			System.err.println("Failed to open " + e.getMessage());
			e.printStackTrace();
		}

		return 0;
	}

	public int restore(String filename) {
		try {
			FileInfo fi = this.state.getLocalBackedUpFileInfo(filename);
			// check if the file was backed up. If not, report it and abort
			if (fi == null) {
				PrintMessage.p("Restore failed",
						String.format("Can't restore file %s because it was never backed up", filename),
						ConsoleColours.RED_BOLD, ConsoleColours.RED);
				return -1;
			}
			// get the file identifier
			byte[] fileId = fi.getFileId();
			// request all chunks of this file
			for (int cno = 0; cno < fi.getNumberChunks(); cno++) {
				GetChunkMessage msg = new GetChunkMessage(this.protoVer.getV(), this.serverId, fileId, cno);
				PrintMessage.p("Created GETCHUNK", msg.toString(), ConsoleColours.GREEN_BOLD, ConsoleColours.GREEN);
				this.executor.submit(new RestoreWorker(msg, cno));
			}

			// Launch a watcher that tracks wether all chunks were received or not
			// It may also re-try to request some of the missing chunks
			ChunkReceiverWatcher watcher = new ChunkReceiverWatcher(fi);
			Thread t = new Thread(watcher);
			t.start();
		} catch (Exception e) {
			return -1;
		}

		return 0;
	}

	public int delete(String filename) {
		if (this.state.isLocalFileByFileName(filename)) {

			// this file was backed up
			this.executor.submit(new DeleteSenderWorker(this.state.getLocalBackedUpFileInfo(filename)));
			return 0;
		} else {
			PrintMessage.p("DELETE FILE",
					String.format("Requested file to be deleted, %s, was never backed up", filename),
					ConsoleColours.RED_BOLD, ConsoleColours.RED);
			return -1;
		}
	}

	/**
	 * Disk space in KB
	 */
	public int reclaim(int maximumDiskSpace) {
		// convert to bytes
		maximumDiskSpace *= 1000;

		if (this.state.getStorageCapacity() <= maximumDiskSpace) {
			// optimistic case where space grows
			this.state.setStorageCapacity(maximumDiskSpace);
		} else {
			// launch worker to handle this

		}

		return 0;
	}

	public PeerState getServiceState() {
		return this.state;
	}

	public static void main(String args[]) {
		parseArgs(args);
		try {
			AddrPort MC = new AddrPort(args[3]);
			AddrPort MDB = new AddrPort(args[4]);
			AddrPort MDR = new AddrPort(args[5]);
			ProtocolVersion protocolVersion = new ProtocolVersion(args[0]);
			Integer serverId = Integer.parseInt(args[1]);
			String serviceAP = args[2];

			try {
				Registry registry = LocateRegistry.getRegistry();
				Peer obj = new Peer(MC, MDB, MDR, protocolVersion, serverId);
				single_instance = obj;
				RMIRemote stub = (RMIRemote) UnicastRemoteObject.exportObject(obj, 0);

				// Bind the remote object's stub in the registry
				registry.rebind(serviceAP, stub);

				System.err.println("Peer ready on " + serviceAP);

				// launch backup multicast channel listener
				MCListen mcRunnable = new MCListen(obj.mcSocket);
				Thread mcThread = new Thread(mcRunnable);
				mcThread.start();

				// launch backup multicast channel listener
				MDBListen mdbRunnable = new MDBListen(obj.mdbSocket);
				Thread mdbThread = new Thread(mdbRunnable);
				mdbThread.start();

				// launch backup multicast channel listener
				MDRListen mdrRunnable = new MDRListen(obj.mdrSocket);
				Thread mdrThread = new Thread(mdrRunnable);
				mdrThread.start();

				// launch thread responsible to update persistent data periodically
				FileSystemWorker fsWorker = new FileSystemWorker();
				Thread fsThread = new Thread(fsWorker);
				fsThread.start();

			} catch (Exception e) {
				System.err.println("Server exception: " + e.toString());
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-2);
		}
	}

	/**
	 * Loads arguments from the args list. Terminates execution on bad args.
	 * 
	 * @param args
	 */
	private static void parseArgs(String[] args) {
		if (args.length != 6) {
			System.err
					.println(ConsoleColours.RED_BOLD_BRIGHT + "[ERROR] Expected 6 arguments, got " + args.length + "!");
			System.exit(-1);
		}
	}

	// #region Getters & Setters
	/**
	 * 
	 * @return
	 */
	public static Peer getInstance() {
		return single_instance; /* an unninitialized peer should not be instantiated. */
	}

	/**
	 * @return the multicast channel address
	 */
	public AddrPort getAddrMC() {
		return MC;
	}

	/**
	 * @return the multicast channel address
	 */
	public AddrPort getAddrMDB() {
		return MDB;
	}

	/**
	 * @return the multicast channel address
	 */
	public AddrPort getAddrMDR() {
		return MDR;
	}

	/**
	 * @return the mdrSocket
	 */
	public MulticastSocket getMdbSocket() {
		return mdbSocket;
	}

	/**
	 * @return the mdrSocket
	 */
	public MulticastSocket getMdrSocket() {
		return mdrSocket;
	}

	/**
	 * @return the mdrSocket
	 */
	public MulticastSocket getMcSocket() {
		return mcSocket;
	}

	/**
	 * @return the peer identification
	 */
	public Integer getPeerId() {
		return this.serverId;
	}

	/**
	 * 
	 */
	public String getProtocolVersion() {
		return this.protoVer.getV();
	}

	public PeerState getState() {
		return this.state;
	}

	/**
	 * @return the receivedChunkInfo
	 */
	public ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>> getReceivedChunkInfo() {
		return receivedChunkInfo;
	}

	/**
	 * @return the receivedChunkData
	 */
	public ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>> getReceivedChunkData() {
		return receivedChunkData;
	}

	// #endregion
}
