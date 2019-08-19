package Lib;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class SHA1 {
    private String m_Sh1;


    public SHA1(String Sh1) {
        this.m_Sh1 = Sh1;
    }

    public SHA1(){

    }

    public String getSh1() {
        return m_Sh1;
    }

    public void MakeSH1FromContent(String str) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA1");
            digest.update(str.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m_Sh1= bytesToHex(digest.digest());
    }

    public static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return m_Sh1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SHA1 sha1 = (SHA1) o;
        return Objects.equals(m_Sh1, sha1.m_Sh1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_Sh1);
    }
}
