package Lib;

import jdk.nashorn.internal.objects.annotations.Constructor;

import java.io.IOException;

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

    @Override
    public void AddToRepository(Repository repository) throws IOException {
        AddToRepositoryHelper(repository,"blobs");
        repository.getBlobsMap().put(this.MakeSH1(),this);
    }

    public String getContent() {
        return m_Content;
    }
    @Override
    public  String toString(){
        return getContent();
    }

}
