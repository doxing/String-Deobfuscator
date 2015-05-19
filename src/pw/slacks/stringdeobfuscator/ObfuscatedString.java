package pw.slacks.stringdeobfuscator;


import lombok.Getter;
import lombok.ToString;

/* Created on 5/17/2015 @ 11:38 PM */
@ToString
public class ObfuscatedString {

    @Getter private final int progessId, nextId, id;
    @Getter private final String obfuscatedString, fieldName;

    public ObfuscatedString(int id, int nextId, int progessId, String obfuscatedString){
        this(id, nextId, progessId, obfuscatedString, "");
    }

    public ObfuscatedString(int id, int nextId, int progessId, String obfuscatedString, String fieldName){
        this.progessId = progessId;
        this.nextId = nextId;
        this.id = id;
        this.obfuscatedString = obfuscatedString;
        this.fieldName = fieldName;
    }
}
