package Lib;

public class RemoteTrackingBranch extends Branch {
    private RemoteBranch followAfter;

    public RemoteTrackingBranch(String name, SHA1 commitSH1, RemoteBranch remoteBranch) {
        super(name, commitSH1);
        followAfter = remoteBranch;
    }

    public RemoteBranch getFollowAfter() {
        return followAfter;
    }
}
