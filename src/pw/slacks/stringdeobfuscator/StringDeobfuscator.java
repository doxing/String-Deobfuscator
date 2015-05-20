package pw.slacks.stringdeobfuscator;

import java.io.File;
import java.io.IOException;
import java.util.List;

/* Created on 5/18/2015 @ 12:39 AM */
public class StringDeobfuscator {

    public static boolean verbose;

    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Invalid usage!\nUSAGE: java -jar $this.jar <file/directory> [verbose]");
            System.exit(0);
        }

        verbose = args.length == 2 && Boolean.parseBoolean(args[1]);

        Deobfuscator deobfuscator = new Deobfuscator();

        String fileName = args[0];
        File file = new File(fileName);
        if(!file.isDirectory())
            deobfuscateClassStrings(deobfuscator, file);
        else
            deobfuscateRecursively(deobfuscator, file);
    }

    public static void deobfuscateRecursively(Deobfuscator deobfuscator, File file){
        for(File f : file.listFiles()){
            System.out.println(f.getPath());
            if(f.isDirectory())
                deobfuscateRecursively(deobfuscator, f);
            else if(f.getName().endsWith(".class"))
                deobfuscateClassStrings(new Deobfuscator(), f);
        }
    }

    private static void deobfuscateClassStrings(Deobfuscator de, File f){
        de.parseFile(f);
        List<Object> deobfuscatedStrings = de.deobfuscateString();
        if(deobfuscatedStrings != null) {
            Log.write("=============================\nClass: " + f.getPath() + "\n=============================");
            for (Object s1 : deobfuscatedStrings) {
                System.out.println(s1.toString());
                Log.write(s1.toString());
            }
        }
    }

}
