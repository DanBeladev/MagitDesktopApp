package Lib;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Branch {

    private  String m_Name;
    private  SHA1 m_CommitSH1;
    //private boolean m_IsRemote = false;
    //private boolean m_IsTracking = false;
    //private String m_TrakingAfter = "";


    public Branch(String name, SHA1 commitSH1) {
        this.m_Name = name;
        this.m_CommitSH1 = commitSH1;
    }

    public Branch (){

    }

    public Branch(Branch branch){
        m_Name = branch.getName();
        m_CommitSH1 = new SHA1(branch.getCommitSH1().getSh1());
    }

    public SHA1 getCommitSH1() {
        return m_CommitSH1;
    }

    public void setCommitSH1(SHA1 m_CommitSH1) {
        this.m_CommitSH1 = m_CommitSH1;
    }

    public void setName(String name){
        this.m_Name = name;
    }

    public void CreateBranchTextFile(String path) throws IOException {
       File file = new File(path + m_Name + ".txt");
       if(file.createNewFile())
       {
           WriteBranchSHA1ToFile(path + m_Name + ".txt");
       }
    }

    private void UpdateBranchWithNewSHA1(SHA1 newSh1)
    {
        m_CommitSH1 = newSh1;
    }

    private void WriteBranchSHA1ToFile(String path) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write(m_CommitSH1.getSh1());
        fw.close();
    }

    public void UpdateSHA1AndBranchFileContent(SHA1 sh1,String path) throws IOException {
        UpdateBranchWithNewSHA1(sh1);

        WriteBranchSHA1ToFile(path);
    }

    public String getName() {
        return m_Name;
    }

   /* public boolean IsTracking() {
        return m_IsTracking;
    }

    public void SetIsTracking(boolean i_IsTracking) {
        m_IsTracking = i_IsTracking;
    }

    public String GetTrakingAfter() {
        return m_TrakingAfter;
    }

    public void SetTrakingAfter(String i_TrakingAfter) {
        m_TrakingAfter = i_TrakingAfter;
    }

    public boolean IsRemote() { return m_IsRemote; }

    public void setIsRemote(boolean i_IsRemote) {
        m_IsRemote = i_IsRemote;
    }*/

}
