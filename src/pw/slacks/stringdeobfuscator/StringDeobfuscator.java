package pw.slacks.stringdeobfuscator;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/* Created on 5/18/2015 @ 12:39 AM */
public class StringDeobfuscator {

    private static boolean VERBOSE;
    private static PrintWriter pw;

    private List<ObfuscatedString> obfuscatedStringList;
    private String startString = null, className;
    private int arraySize, addedKeys;
    private short startId = 0;
    private byte[] keyList = new byte[5];
    private BytecodeViewer bytecodeViewer;

    public StringDeobfuscator(){
        this.obfuscatedStringList = new ArrayList<>();
        this.bytecodeViewer = new BytecodeViewer();
    }

    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Invalid usage!\nUSAGE: java -jar $this.jar <file/directory> [verbose]");
            System.exit(0);
        }

        VERBOSE = args.length == 2 && Boolean.parseBoolean(args[1]);

        pw = new PrintWriter(new FileWriter("output.log", true), true);

        StringDeobfuscator stringDeobfuscator = new StringDeobfuscator();

        String fileName = args[0];
        File file = new File(fileName);
        if(!file.isDirectory())
            deobfuscateClassStrings(stringDeobfuscator, file);
        else
            deobfuscateRecursively(stringDeobfuscator, file);
    }

    public static void deobfuscateRecursively(StringDeobfuscator stringDeobfuscator, File file){
        for(File f : file.listFiles()){
            System.out.println(f.getPath());
            if(f.isDirectory())
                deobfuscateRecursively(stringDeobfuscator, f);
            else if(f.getName().endsWith(".class"))
                deobfuscateClassStrings(new StringDeobfuscator(), f);
        }
    }

    private static void deobfuscateClassStrings(StringDeobfuscator sd, File f){
        sd.parseFile(f);
        List<Object> deobfuscatedStrings = sd.deobfuscateString();
        if(deobfuscatedStrings != null) {
            writeToFile("=============================\nClass: " + f.getPath() + "\n=============================");
            for (Object s1 : deobfuscatedStrings) {
                System.out.println(s1.toString());
                writeToFile(s1.toString());
            }
        }
    }

    public static void writeToFile(String s){
        pw.println(s);
    }

    private void log(String s){
        if(VERBOSE)
            System.out.println(s);
    }

    public void parseFile(File file) {
        try {
            this.className = file.getName();
            this.addedKeys = 0;
            this.arraySize = 0;
            this.obfuscatedStringList.clear();

            String byteCode = this.bytecodeViewer.scanClass(new FileInputStream(file));

            String[] lines = byteCode.split("\n");

            for (int lineNum = 0; lineNum < lines.length; lineNum++) {
                String line = lines[lineNum];
                if (lines.length > lineNum + 1 && lineNum - 1 != -1) {
                    String prevLine = lines[lineNum - 1];
                    String nextLine = lines[lineNum + 1];
                    parseLines(line, prevLine, nextLine);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    private String parseLdc(String line){
        return StringEscapeUtils.unescapeJava(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
    }

    private void parseLines(String line, String prevLine, String nextLine){
        if (prevLine.contains("BIPUSH") && arraySize == 0) {
            this.arraySize = Integer.parseInt(prevLine.split("PUSH ")[1]);
            log("Set array size: " + this.arraySize);
        }
        if (startString == null) {
            if (line.contains("LDC \"") && nextLine.contains("PUSH")) {
                this.startString = parseLdc(line);
                this.startId = Short.parseShort(nextLine.split("PUSH ")[1]);
                log("Set start string: " + this.startString);
                log("Set start id: " + this.startId);
            }
        }

        if (prevLine.contains("PUTSTATIC") && line.contains("LDC \"") && nextLine.contains("PUSH")) {
            String string = parseLdc(line);
            int nextId = Integer.parseInt(nextLine.split("PUSH ")[1]);
            addObfuscatedString(string, nextId - 1, nextId, -999);
        }

        if (line.contains("LDC \"") && (prevLine.contains("ICONST") || prevLine.contains("PUSH"))) {
            try {
                String string = parseLdc(line);
                int nextId;
                if (nextLine.contains("ICONST")) {
                    nextId = Integer.parseInt(nextLine.split("_")[1]);
                } else if (nextLine.contains("PUSH")){
                    nextId = Integer.parseInt(nextLine.split("PUSH ")[1]);
                } else{
                    return;
                }
                addObfuscatedString(string, nextId - 1, nextId, nextId + 1);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                log("IndexOutOfBounds String: " + line);
            }
        }

        if (line.contains("FRAME FULL") && (nextLine.contains("PUSH") || nextLine.contains("ICONST")) && addedKeys<5) {
            int key;
            if (nextLine.contains("PUSH"))
                key = Integer.parseInt(nextLine.split("PUSH ")[1]);
            else
                key = Integer.parseInt(nextLine.split("_")[1]);
            keyList[addedKeys++] = (byte) key;
        }
    }

    private void addObfuscatedString(String s, int id, int nextId, int progressId){
        ObfuscatedString obfuscatedString = new ObfuscatedString(id, nextId, progressId, s);
        this.obfuscatedStringList.add(obfuscatedString);
        log("Added obfuscated string: " + obfuscatedString.toString());
    }

    /**
     * Deobfuscates the obfuscated class's strings
     * The method is copied and modified from an obfuscated class
     * @return List of the deobfuscated strings
     */
    public List<Object> deobfuscateString() throws NullPointerException {
        boolean looped = false;
        System.out.println("Starting string deobfuscation on: " + className);

        List<Object> deobfuscatedList = new ArrayList<>();

        String nextString = startString;

        if(arraySize < 0) {
            log("Array size is less than zero, setting it");
            this.arraySize = obfuscatedStringList.size();
        }

        String[] var10000 = new String[arraySize];
        String[] stringArray = var10000;

        int progressId = 0, nextId = startId;

        int bailout = 0;
        while((++bailout < 20000)) {
            char[] nextChars1;
            label163:
            {
                if(nextString == null) {
                    log("nextString is null");
                    return null;
                }
                char[] nextChars0 = nextString.toCharArray();
                int keyId = 0;
                nextChars1 = nextChars0;
                int nextChars1Length = nextChars0.length;
                if (nextChars0.length > 1) {
                    nextChars1 = nextChars0;
                    nextChars1Length = nextChars0.length;
                    if (nextChars0.length <= keyId) {
                        break label163;
                    }
                }

                do {
                    char[] nextChars2 = nextChars1;
                    int keyId1 = keyId;

                    while (true) {
                        if(nextChars2.length == 0) {
                            log("nextChars2's length is zero");
                            break;
                        }
                        char singleChar = nextChars2[keyId1];

                        byte key = keyList[keyId%5];
                        nextChars2[keyId1] = (char) (singleChar ^ key);
                        ++keyId;

                        if (nextChars1Length != 0) {
                            break;
                        }

                        keyId1 = nextChars1Length;
                        nextChars2 = nextChars1;
                    }
                } while (nextChars1Length > keyId);
            }

            String newString = (new String(nextChars1)).intern();

            if(nextId == startId && looped){
                return deobfuscatedList;
            }

            if(obfuscatedStringList.size()==0) {
                log("obfuscatedStringList size is zero");
                return null;
            }

            boolean notFound = true;
            for(ObfuscatedString s : obfuscatedStringList){
                if(s.getId() == nextId && s.getProgessId() != -999){
                    notFound = false;
                    if(progressId >= stringArray.length) {
                        log("progressId is larger than stringArray's length");
                        return null;
                    }
                    stringArray[progressId] = newString;
                    deobfuscatedList.add(newString);
                    stringArray = var10000;
                    progressId = s.getProgessId();
                    nextString = s.getObfuscatedString();
                    nextId = (short)s.getNextId();
                    break;
                }
                else if(s.getId() == nextId){
                    notFound = false;
                    nextString = s.getObfuscatedString();
                    nextId = (short)s.getNextId();
                    break;
                }
            }
            looped = true;
            if(notFound)
                nextId = -2;
        }
        return deobfuscatedList;
    }

}
