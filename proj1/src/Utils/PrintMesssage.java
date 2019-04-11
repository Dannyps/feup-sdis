package Utils;

import Messages.Message;

/**
 * PrintMesssage
 */
public class PrintMesssage {

    public static boolean printMessages = false;

    public static void p(String action, Message m) {
        PrintMesssage.p(action, m.toString(), ConsoleColours.BLUE_BOLD, ConsoleColours.BLUE);
    }

    public static void p(String action, String m) {
        PrintMesssage.p(action, m, ConsoleColours.BLUE_BOLD, ConsoleColours.BLUE);
    }

    public static void p(String action, String m, String accentColor, String msgColor) {
        if (printMessages)
            System.out.println(accentColor + "[" + action + "] " + msgColor + m + ConsoleColours.RESET);
    }
}