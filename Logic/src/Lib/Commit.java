package Lib;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit extends MagitFile implements CommitRepresentative {
    private SHA1 m_MainFolderSH1;
    private List<SHA1> m_PrevCommits;
    private String m_Message;
    private User m_WhoUpdated;
    private OurDate m_CreateTime;

    public List<SHA1> getPrevCommits() {
        return m_PrevCommits;
    }

    public Commit(String message) {
        super();
        m_Message = message;
        m_PrevCommits=new ArrayList<>(2);
    }

    public SHA1 getMainFolderSH1() {
        return m_MainFolderSH1;
    }

    @Override
    public SHA1 MakeSH1() {
        SHA1 sh1 = new SHA1();
        sh1.MakeSH1FromContent( m_MainFolderSH1 + "," +
                m_Message + "," +
                m_WhoUpdated + "," +
                m_CreateTime);
        return sh1;
    }

    @Override
    public FileType GetMagitFileType() {
        return FileType.COMMIT;
    }

    @Override
    public void AddToRepository(Repository repository) throws IOException {
        AddToRepositoryHelper(repository,"commits");
        repository.getCommitMap().put(this.MakeSH1(),this);
    }

    @Override
    public String toString() {
        String prevCommitsString;
        if(!m_PrevCommits.isEmpty()) {
            prevCommitsString = m_PrevCommits.toString().substring(1, m_PrevCommits.toString().length() - 1);
        }
        else{
            prevCommitsString="null";
        }
        return
                m_MainFolderSH1 + "," +
                        prevCommitsString+","+
                        m_Message + "," +
                        m_WhoUpdated + "," +
                        m_CreateTime;

    }



    public void SetMainFolderSH1(SHA1 m_MainFolderSH1) {
        this.m_MainFolderSH1 = m_MainFolderSH1;
    }

    public void SetWhoUpdated(User m_WhoUpdated) {
        this.m_WhoUpdated = m_WhoUpdated;
    }

    public void SetCreateTime(OurDate m_CreateTime) {
        this.m_CreateTime = m_CreateTime;
    }

    public void SetPrevCommits(List<SHA1> m_PrevCommits) {
        this.m_PrevCommits = m_PrevCommits;
    }

    public void AddPrevCommit(SHA1 prevCommitSh1) {
        if (m_PrevCommits == null) {
            m_PrevCommits = new ArrayList<>();
        }
        m_PrevCommits.add(prevCommitSh1);
    }

    public User getWhoUpdated() {
        return m_WhoUpdated;
    }

    public OurDate getCreateTime() {
        return m_CreateTime;
    }
    public String getMessage() {
        return m_Message;
    }

    public void SetDetailsToCommitByGivenRootFolderAndUser(FileDetails fd, User user){
        SetWhoUpdated(user);
        SetCreateTime(fd.getLastUpdated());
        SetMainFolderSH1(fd.getSh1());
    }

    @Override
    public String getSha1() {
        return MakeSH1().getSh1();
    }

    @Override
    public String getFirstPrecedingSha1() {
        if(m_PrevCommits.size()==0){
            return "";
        }
        return m_PrevCommits.get(0).getSh1();
    }

    @Override
    public String getSecondPrecedingSha1() {
        if(m_PrevCommits.size()<=1){
            return "";
        }
        return m_PrevCommits.get(1).getSh1();
    }
}
