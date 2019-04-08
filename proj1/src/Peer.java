import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

import Messages.Message;
import Messages.PutChunkMessage;
import utils.AddrPort;
import utils.ConsoleColours;
import utils.Chunk;
import utils.RegularFile;

public class Peer implements RMIRemote {
	final static int UDP_MAX_SIZE = 65535; //https://en.wikipedia.org/wiki/User_Datagram_Protocol
	
	private String serviceAP;
	private Registry registry;
	private AddrPort MC;
	private AddrPort MDB;
	private ProtocolVersion protoVer;
	private Integer serverId;
	private AddrPort MDR;
	private MulticastSocket mcSocket;
	private MulticastSocket mdbSocket;
	private MulticastSocket mdrSocket;

	public Peer(Registry registry, AddrPort mC, AddrPort mDB, AddrPort mDR, ProtocolVersion pv, Integer serverId,
			String serviceAP) {
		this.registry = registry;
		this.MC = mC;
		this.MDB = mDB;
		this.MDR = mDR;
		this.protoVer = pv;
		this.serverId = serverId;
		this.serviceAP = serviceAP;

		bindToMC();
		bindToMDB();
		bindToMDR();

		System.out.println("bound to all three sockets");
	}

	private void bindToMC() {
		try {
			this.mcSocket = new MulticastSocket(MC.getPort());
			this.mcSocket.joinGroup(MC.getInetAddress());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void bindToMDB() {
		try {
			this.mdbSocket = new MulticastSocket(MDB.getPort());
			this.mdbSocket.joinGroup(MDB.getInetAddress());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void bindToMDR() {
		try {
			this.mdrSocket = new MulticastSocket(MDR.getPort());
			this.mdrSocket.joinGroup(MDR.getInetAddress());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String sayHello() {
		return "Hello, world!";
	}

	public int backup(String filename, int replicationDegree) {
		RegularFile f = new RegularFile(filename, replicationDegree);
		ArrayList<Chunk> lst;
		
		try {
			lst = f.getChunks();
			for(Chunk c : lst) {
				System.err.println("[Created chunk] " + c);
				PutChunkMessage msg = new PutChunkMessage(this.protoVer.getV(), this.serverId, c);
				byte[] rawMsg = msg.getMessage();
				DatagramPacket dp = new DatagramPacket(rawMsg, rawMsg.length, MDB.getInetSocketAddress());
				this.mdbSocket.send(dp);
				System.err.println("[Sent message] " + msg);
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

				// Listen for packets on MDR
				while(true) {
					byte[] b = new byte[Peer.UDP_MAX_SIZE];
					DatagramPacket dp = new DatagramPacket(b, Peer.UDP_MAX_SIZE);
					obj.mdbSocket.receive(dp);
					// resize the byte array to fit the exact datagram data length
					b = Arrays.copyOf(dp.getData(), dp.getLength());
					// parse the message
					Message msg = Message.parseMessage(b);
					System.err.println("[Received message] " + msg);
				}
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
			System.err.println(ConsoleColours.RED_BOLD_BRIGHT + "[ERROR] Expected 6 arguments, got " + args.length + "!");
			System.exit(-1);
		}
	}
}
