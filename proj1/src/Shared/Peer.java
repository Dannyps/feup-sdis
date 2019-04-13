package Shared;

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Listeners.MCListen;
import Listeners.MDBListen;
import Listeners.MDRListen;
import Messages.GetChunkMessage;
import Messages.Message;
import Messages.MessageType;
import Utils.*;
import Workers.BackupWorker;
import Workers.ChunkReceiverWatcher;
import Workers.RestoreWorker;

public class Peer implements RMIRemote {
	private String serviceAP;
	private Registry registry;
	private AddrPort MC;
	private AddrPort MDB;
	private AddrPort MDR;
	private ProtocolVersion protoVer;
	private Integer serverId;
	private MulticastSocket mcSocket;
	private MulticastSocket mdbSocket;
	private MulticastSocket mdrSocket;
	private ConcurrentHashMap<String, FileInfo> myBackedUpFiles;
	// Maps fileIds to new map which maps chunk numbers to a set of peer ids who
	// stored the chunk
	private HashMap<String, HashMap<Integer, TreeSet<Integer>>> storedChunks;

	// the lastest chunk headers received (from recovery)
	private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>> receivedChunkInfo;

	// the lastest chunk bodies received (from recovery)
	private ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>> receivedChunkData;

	// static variable single_instance of type Singleton
	private static Peer single_instance = null;

	ExecutorService executor;

	// static method to create instance of Singleton class
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

	/**
	 * Returns a list of peers identifiers which stored the pair (fileId, ChunkNo)
	 * 
	 * @param fileId
	 * @param ChunkNo
	 * @return
	 */
	public TreeSet<Integer> getPeersContainChunk(String fileId, Integer ChunkNo) {
		// get all stored chunks for the file
		HashMap<Integer, TreeSet<Integer>> chunks = this.storedChunks.get(fileId);
		if (chunks == null)
			return null;
		// get list of peers who stored the specified chunk
		return chunks.get(ChunkNo);
	}

	/**
	 * 
	 * @param fileId
	 * @param chunkNo
	 * @param peerId
	 */
	public void chunkStored(String fileId, Integer chunkNo, Integer peerId) {
		// check if there's some reference to the said file
		HashMap<Integer, TreeSet<Integer>> fileChunks = this.storedChunks.get(fileId);
		if (fileChunks == null) {
			fileChunks = new HashMap<Integer, TreeSet<Integer>>();
			this.storedChunks.put(fileId, fileChunks);
		}

		// check if there's a reference to the said chunk
		TreeSet<Integer> peers = fileChunks.get(chunkNo);
		if (peers == null) {
			peers = new TreeSet<Integer>();
			fileChunks.put(chunkNo, peers);
		}

		// store the peer id in the set of peers who stored this tuple (fileid, chunkno)
		peers.add(peerId);
	}

	private Peer(Registry registry, AddrPort mC, AddrPort mDB, AddrPort mDR, ProtocolVersion pv, Integer serverId,
			String serviceAP) {
		this.registry = registry;
		this.MC = mC;
		this.MDB = mDB;
		this.MDR = mDR;
		this.protoVer = pv;
		this.serverId = serverId;
		this.serviceAP = serviceAP;

		this.mcSocket = bindToMultiCast(MC);
		this.mdbSocket = bindToMultiCast(MDB);
		this.mdrSocket = bindToMultiCast(MDR);

		this.myBackedUpFiles = new ConcurrentHashMap<String, FileInfo>();

		this.storedChunks = new HashMap<String, HashMap<Integer, TreeSet<Integer>>>();

		this.receivedChunkInfo = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>>();
		this.receivedChunkData = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>>();

		// instatiate thread pool
		this.executor = new ThreadPoolExecutor(8, 8, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		System.out.println("[INFO] Bound to all three sockets successfully.");
		PrintMessage.printMessages = true;
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

	MulticastSocket bindToMultiCast(AddrPort ap) {
		MulticastSocket s = null;
		try {
			s = new MulticastSocket(ap.getPort());
			s.joinGroup(ap.getInetAddress());
		} catch (SocketException e) {
			System.out.println("[FATAL] Could not enter multicast group " + ap + ": " + e.getMessage());
			System.exit(6);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(6);
		}
		return s;
	}

	public int backup(String filename, int replicationDegree) {
		RegularFile f = new RegularFile(filename, replicationDegree);
		ArrayList<Chunk> lst;

		try {
			this.getMyBackedUpFiles().put(filename, new FileInfo(filename, f.getFileId(), f.getReplicationDegree()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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
			FileInfo fi = this.myBackedUpFiles.get(filename);
			byte[] fileId = fi.getFileId();

			ArrayList<Chunk> chunkList = new ArrayList<Chunk>();

			/**
			 * Keep track of all threads launched in order to restore the chunks. When all
			 * of them have ended successfully, we can reconstitute the file.
			 */
			// TODO no longer needed
			ArrayList<Future<Integer>> taskList = new ArrayList<Future<Integer>>();

			for (int cno = 0; cno < fi.getChunks().size(); cno++) {
				GetChunkMessage msg = new GetChunkMessage(this.protoVer.getV(), this.serverId, fileId, cno);
				PrintMessage.p("Created GETCHUNK", msg.toString(), ConsoleColours.GREEN_BOLD, ConsoleColours.GREEN);
				this.executor.submit(new RestoreWorker(msg, cno, chunkList));
			}

			ChunkReceiverWatcher watcher = new ChunkReceiverWatcher(fi);
			Thread t = new Thread(watcher);
			t.start();
		} catch (Exception e) {
			return -1;
		}

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
		try {
			AddrPort MC = new AddrPort(args[3]);
			AddrPort MDB = new AddrPort(args[4]);
			AddrPort MDR = new AddrPort(args[5]);
			ProtocolVersion protocolVersion = new ProtocolVersion(args[0]);
			Integer serverId = Integer.parseInt(args[1]);
			String serviceAP = args[2];

			try {
				Registry registry = LocateRegistry.getRegistry();
				Peer obj = new Peer(registry, MC, MDB, MDR, protocolVersion, serverId, serviceAP);
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

	public ConcurrentHashMap<String, FileInfo> getMyBackedUpFiles() {
		return myBackedUpFiles;
	}
}
