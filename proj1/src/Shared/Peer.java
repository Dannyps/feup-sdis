package Shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Listeners.MCListen;
import Listeners.MDBListen;
import Messages.PutChunkMessage;
import Utils.*;
import Workers.BackupWorker;

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
	// Maps fileIds to new map which maps chunk numbers to a set of peer ids who stored the chunk
	private HashMap<String, HashMap<Integer, TreeSet<Integer>>> storedChunks;
	
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
	 * 
	 * @param fileId
	 * @param chunkNo
	 * @param peerId
	 */
	public void chunkStored(String fileId, Integer chunkNo, Integer peerId) {
		// check if there's some reference to the said file
		HashMap<Integer, TreeSet<Integer>> fileChunks = this.storedChunks.get(fileId);
		if(fileChunks == null) {
			fileChunks = new HashMap<Integer, TreeSet<Integer>>();
			this.storedChunks.put(fileId, fileChunks);
		}
			
		
		// check if there's a reference to the said chunk
		TreeSet<Integer> peers = fileChunks.get(chunkNo);
		if(peers == null) {
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

		this.storedChunks = new HashMap<String, HashMap<Integer, TreeSet<Integer>>>();

		// instatiate thread pool
		this.executor = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		
		System.out.println("[INFO] Bound to all three sockets successfully.");
	}

	MulticastSocket bindToMultiCast(AddrPort ap) {
		MulticastSocket s = null;
		try {
			s = new MulticastSocket(ap.getPort());
			s.joinGroup(ap.getInetAddress());
		} catch (SocketException e) {
			System.out.println("[FATAL] Could not enter multicast group " + ap + ": " + e.getMessage());
			System.exit(6);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}

	public String sayHello() {
		return "Hello, world!";
	}

	public int backup(String filename, int replicationDegree) {
		RegularFile f = new RegularFile(filename, replicationDegree);
		ArrayList<Chunk> lst;

		try {
			lst = f.getChunks();
			for (Chunk c : lst) {
				System.err.println("[Created chunk] " + c);
				this.executor.submit(new BackupWorker(c));
			}
		} catch (IOException e) {
			System.err.println("Failed to open " + e.getMessage());
			e.printStackTrace();
		}

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
}
