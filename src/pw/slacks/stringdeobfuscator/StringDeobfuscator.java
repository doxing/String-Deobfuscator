package pw.slacks.stringdeobfuscator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/* Created on 5/18/2015 @ 12:39 AM */
public class StringDeobfuscator {

    public List<ObfuscatedString> obfuscatedStringList;
    private String startString = null, className;
    private int arraySize;
    private short startId = 0;
    private byte[] keyList = new byte[5];
    private int addedKeys;
    public static boolean VERBOSE;
    private static PrintWriter pw;

    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Invalid usage!\nUSAGE: java -jar $this.jar <class file> [verbose]");
            System.exit(0);
        }

        VERBOSE = args.length == 2 && Boolean.parseBoolean(args[1]);

        try {
            pw = new PrintWriter(new FileWriter("output.log", true), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringDeobfuscator stringDeobfuscator = new StringDeobfuscator();
        stringDeobfuscator.parseFile(args[0]);
        List<Object> decryptedStrings = stringDeobfuscator.decryptString();
        writeToFile("=============================\nClass: " + args[0] + "\n=============================");
        for (Object s1 : decryptedStrings) {
            System.out.println(s1.toString());
            writeToFile(s1.toString());
        }
    }

    public static void writeToFile(String s){
        pw.println(s);
    }


    public StringDeobfuscator(){
        obfuscatedStringList = new ArrayList<>();
    }

    private void log(String s){
        if(VERBOSE)
            System.out.println(s);
    }

    public void parseFile(String fileName) {
        try {
            this.className = fileName;
            addedKeys = 0;
            arraySize = 0;
            obfuscatedStringList.clear();
            String byteCode = new ClassBytecodeViewer().scanClass(new FileInputStream(fileName));

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

    private void parseLines(String line, String prevLine, String nextLine){
        if (prevLine.contains("BIPUSH") && arraySize == 0) {
            arraySize = Integer.parseInt(prevLine.split("PUSH ")[1]);
            log("Set array size: " + arraySize);
        }
        if (startString == null) {
            if (line.contains(" LDC ")) {
                startString = line.split("\"")[1];
                startId = Short.parseShort(nextLine.split("PUSH ")[1]);
                log("Set start string: " + startString);
                log("Set start id: " + startId);
            }
        }

        if (prevLine.contains("PUTSTATIC") && line.contains("LDC")) {
            String string = line.split("\"")[1];
            int nextId = Integer.parseInt(nextLine.split("PUSH ")[1]);
            addObfuscatedString(string, nextId - 1, nextId, -999);
        }

        if (line.contains("LDC") && (prevLine.contains("ICONST") || prevLine.contains("PUSH"))) {
            try {
                String string = line.split("\"")[1];
                int nextId;
                if (nextLine.contains("ICONST")) {
                    nextId = Integer.parseInt(nextLine.split("_")[1]);
                } else {
                    nextId = Integer.parseInt(nextLine.split("PUSH ")[1]);
                }
                addObfuscatedString(string, nextId - 1, nextId, nextId + 1);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                log("IndexOutOfBounds String: " + line);
            }
        }

        if (line.contains("FRAME FULL") && (nextLine.contains("PUSH") || nextLine.contains("ICONST"))) {
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
        log("Added obfuscated string: " + obfuscatedString.toString());
        obfuscatedStringList.add(obfuscatedString);
    }

    /**
     * Decrypts the obfuscated class's strings
     * The method is copied and modified from an obfuscated class
     * @return List of the deobfuscated strings
     */
    public List<Object> decryptString(){
        System.out.println("Starting decryption on: " + className);
        List<Object> decryptedList = new ArrayList<>();
        String[] var10000 = new String[arraySize];
        String[] stringArray = var10000;
        byte progressId = 0;
        String nextString = startString;
        short nextId = startId;

        while(true) {
            char[] nextChars1;
            label163:
            {
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
                        char singleChar = nextChars2[keyId1];
                        byte key;
                        switch (keyId % 5) {
                            case 0:
                                key = keyList[0];
                                break;
                            case 1:
                                key = keyList[1];
                                break;
                            case 2:
                                key = keyList[2];
                                break;
                            case 3:
                                key = keyList[3];
                                break;
                            default:
                                key = keyList[4];
                        }

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

            if(nextId == startId-1){
                return decryptedList;
            }

            boolean notFound = true;
            for(ObfuscatedString s : obfuscatedStringList){
                if(s.getId() == nextId && s.getProgessId() != -999){
                    notFound = false;
                    stringArray[progressId] = newString;
                    decryptedList.add(newString);
                    stringArray = var10000;
                    progressId = (byte)s.getProgessId();
                    nextString = s.getEncryptedString();
                    nextId = (short)s.getNextId();
                    break;
                }
                else if(s.getId() == nextId){
                    notFound = false;
                    nextString = s.getEncryptedString();
                    nextId = (short)s.getNextId();
                    break;
                }
            }
            if(notFound){
                nextId = -2;
            }
        }
    }

}
