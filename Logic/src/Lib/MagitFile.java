package Lib;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public  abstract class MagitFile {
    private static final String MAGIT_FOLDER = "\\.magit\\";
    private static final String OBJECTS_FOLDER = "\\.magit\\objects\\";
    public abstract SHA1 MakeSH1();
    public abstract FileType GetMagitFileType();
    protected void AddToRepositoryHelper(Repository repo,String type) throws IOException {
        FileUtils.AppendTextToFile(repo.GetLocation()+MAGIT_FOLDER+type+".txt",this.MakeSH1().getSh1());
        File magitFileZzipFile = FileUtils.CreateTextFile(repo.GetLocation() + OBJECTS_FOLDER + this.MakeSH1() + ".txt", this.toString());
        FileUtils.CreateZipFile(magitFileZzipFile, this.MakeSH1(), repo.GetLocation() + OBJECTS_FOLDER);
        magitFileZzipFile.delete();
    }
    public abstract void AddToRepository(Repository repository) throws IOException;

}
