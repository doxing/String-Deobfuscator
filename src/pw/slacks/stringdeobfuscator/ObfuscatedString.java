package pw.slacks.stringdeobfuscator;


import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringEscapeUtils;

/* Created on 5/17/2015 @ 11:38 PM */
@ToString
public class ObfuscatedString {

    @Getter private final int progessId, nextId, id;
    @Getter private final String encryptedString, fieldName;

    public ObfuscatedString(int id, int nextId, int progessId, String encryptedString){
        this(id, nextId, progessId, encryptedString, "");
    }

    public ObfuscatedString(int id, int nextId, int progessId, String encryptedString, String fieldName){
        this.progessId = progessId;
        this.nextId = nextId;
        this.id = id;
        this.encryptedString = StringEscapeUtils.unescapeJava(encryptedString);
        this.fieldName = fieldName;
    }
}
