package Lib;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Folder extends MagitFile {

    private List<FileDetails> m_InnerFiles;

    public List<FileDetails> getInnerFiles() {
        return m_InnerFiles;
    }

    public Folder(List<FileDetails> innerFiles) {
        this.m_InnerFiles = innerFiles;
    }

    public File CreateTextFileRepresentFolder(String path) throws IOException {
        File f = new File(path);
        if (f.createNewFile()) {
            FileWriter fw = new FileWriter(path);
            fw.write(toString());
            fw.close();
        }
        return f;
    }


    @Override
    public SHA1 MakeSH1() {
        SHA1 sh1 = new SHA1();
        sh1.MakeSH1FromContent(this.toString());
        return sh1;
    }

    @Override
    public void AddToRepository(Repository repository) throws IOException {
        AddToRepositoryHelper(repository,"folders");
        repository.getFoldersMap().put(this.MakeSH1(),this);
    }

    @Override
    public FileType GetMagitFileType() {
        return FileType.FOLDER;
    }

    @Override
    public String toString() {
        Collections.sort(m_InnerFiles);
        String str = "";
        for(FileDetails fileDetails : m_InnerFiles) {
            str = str + fileDetails.toString();
        }
        return  str;
    }
}


