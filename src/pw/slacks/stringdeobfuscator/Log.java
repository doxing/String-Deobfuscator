package pw.slacks.stringdeobfuscator;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/* Created on 5/19/2015 @ 10:06 PM */
public final class Log {
    private static PrintWriter writer;

    static {
        try {
            writer = new PrintWriter(new FileWriter("output.log", true), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a line of text to the log file.
     * @param s = The string to be written to the log file.
     */
    public static void write(String s){
        writer.println(s);
    }

    /**
     * Logs to the console if verbose.
     * @param s = The string to be logged.
     */
    public static void log(String s) {
        if(StringDeobfuscator.verbose)
            System.out.println(s);
    }
}