package Lib;

public class Blob extends MagitFile {
   private String m_Content;

    public Blob(String content) {
        super();
        m_Content = content;
    }

    public void SetContent(String m_Content) {
        this.m_Content = m_Content;
    }

    @Override
    public SHA1 MakeSH1() {
        SHA1 sha1=new SHA1();
        sha1.MakeSH1FromContent(m_Content);
        return sha1;
    }

    @Override
    public FileType GetMagitFileType() {
        return FileType.FILE;
    }
    public String getContent() {
        return m_Content;
    }

}
