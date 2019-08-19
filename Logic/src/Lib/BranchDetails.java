package Lib;
public class BranchDetails {
    private String m_Name;

    private SHA1 m_CommitSh1;
    private String m_Message;
    private boolean m_IsHeadBranch;

    public SHA1 getCommitSh1() {
        return m_CommitSh1;
    }

    public BranchDetails(Branch branch, Commit commit, boolean IsHead) {
        m_Name = branch.getName();
        m_CommitSh1 = new SHA1(branch.getCommitSH1().getSh1());
        m_Message = commit.getMessage();
        m_IsHeadBranch = IsHead;

    }

    @Override
    public String toString() {
        String head = m_IsHeadBranch ? "--->HEAD" : "";
        return "\n====================================\n" +
                "Name: " + m_Name + head + System.lineSeparator() +
                "Commit SH1: " + m_CommitSh1 + System.lineSeparator() +
                "Message: " + m_Message + System.lineSeparator() +
                "\n====================================\n";
    }

    public String getName() {
        return m_Name;
    }

    public String getMessage() {
        return m_Message;
    }
}
