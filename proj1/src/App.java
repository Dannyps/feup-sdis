
/*
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 * 
 * -Redistributions of source code must retain the above copyright  
 *  notice, this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright 
 *  notice, this list of conditions and the following disclaimer in 
 *  the documentation and/or other materials provided with the 
 *  distribution.
 *  
 * Neither the name of Oracle or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *  
 * You acknowledge that Software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility.
 */

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
			System.out.println("Expected at least 2 arguments, got " + nargs + ".");
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