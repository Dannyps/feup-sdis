package Utils;

import Messages.Message;

/**
 * PrintMesssage
 */
public class PrintMesssage {

    public static boolean printMessages = false;

    public static void p(String action, Message m) {
        if (printMessages)
            System.out.println(
                    ConsoleColours.BLUE_BOLD + "[" + action + "] " + ConsoleColours.BLUE + m + ConsoleColours.RESET);
    }
}