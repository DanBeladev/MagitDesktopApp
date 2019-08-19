package Lib;
public class CommitDetails {
    private SHA1 m_CommitSHA1;
    private String m_Message;
    private  User m_WhoUpdated;
    private OurDate m_WhenUpdated;

    public CommitDetails (Commit commit){
    m_CommitSHA1 = commit.MakeSH1();
    m_WhoUpdated = commit.getWhoUpdated();
    m_Message = commit.getMessage();
    m_WhenUpdated = commit.getCreateTime();
    }

    public void setM_CommitSHA1(SHA1 m_CommitSHA1) {
        this.m_CommitSHA1 = m_CommitSHA1;
    }

    public void setM_Message(String m_Message) {
        this.m_Message = m_Message;
    }

    public void setM_WhoUpdated(User m_WhoUpdated) {
        this.m_WhoUpdated = m_WhoUpdated;
    }

    public void setM_WhenUpdated(OurDate m_WhenUpdated) {
        this.m_WhenUpdated = m_WhenUpdated;
    }

    @Override
    public String toString() {
        return
                "\n==================================================\n" +
                        "Commit SH1: " + m_CommitSHA1 +
                "\nMessage: " + m_Message +
                "\nWho updated: " + m_WhoUpdated.getName() +
                "\nWhen updated: " + m_WhenUpdated +
        "\n==================================================\n";

    }
}
