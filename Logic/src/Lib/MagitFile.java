package Lib;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public  abstract class MagitFile {

    public abstract SHA1 MakeSH1();
    public abstract FileType GetMagitFileType();


}
