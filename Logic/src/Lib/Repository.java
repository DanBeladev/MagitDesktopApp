package Lib;
import Lib.Blob;
import MagitExceptions.CommitException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class Repository {
    private static final String MAGIT_FOLDER = "\\.magit\\";
    private static final String OBJECTS_FOLDER = "\\.magit\\objects\\";
    private static final String BRANCHES_FOLDER = "\\.magit\\branches\\";
    private String m_Name;
    private String m_Location;
    private Branch m_ActiveBranch;
    private boolean isCloned;
    private List<String> m_DeletedList;
    private List<String> m_AddedList;
    private List<String> m_ChangedList;
    private Map<SHA1, Commit> m_CommitMap;
    private Map<SHA1, Blob> m_BlobsMap;
    private Map<SHA1, Folder> m_FoldersMap;
    private Map<String, Branch> m_BranchesMap;

    public Repository(String name, String path) {
        m_Name = name;
        m_Location = path;
        InitMaps();
        m_ActiveBranch=null;
        isCloned=false;

    }

    public Repository(String path) {
        m_Location = path;
        InitMaps();
    }

    private void InitMaps() {
        this.m_CommitMap = new HashMap<>();
        this.m_BlobsMap = new HashMap<>();
        this.m_FoldersMap = new HashMap<>();
        this.m_BranchesMap = new HashMap<>();
        this.m_ActiveBranch = new Branch();
        DeltasListReset();
    }

    public void DeltasListReset(){
        this.m_ChangedList =new ArrayList<>();
        this.m_AddedList =new ArrayList<>();
        this.m_DeletedList =new ArrayList<>();
    }

    public List<String> getDeletedList() {
        return m_DeletedList;
    }

    public List<String> getAddedList() {
        return m_AddedList;
    }

    public List<String> getChangedList() {
        return m_ChangedList;
    }

    public void LoadData() throws IOException, ParseException {
        LoadName();
        LoadBlobs();
        LoadFolders();
        LoadCommits();
        LoadBranches();
        LoadActiveBranch();
    }

    private void LoadActiveBranch() throws IOException {
        File file = new File (m_Location  + BRANCHES_FOLDER+"HEAD.txt");
        if(!FileUtils.ReadContentFromFile(file).equals("")) {
            m_ActiveBranch = m_BranchesMap.get(FileUtils.ReadContentFromFile(file));
        }
    }

    private void LoadBranches() throws IOException {
        File file = new File (m_Location + BRANCHES_FOLDER);
        File [] files =file.listFiles();
        assert files != null;
        List<File> RRFolder=Arrays.stream(files).filter(File::isDirectory).collect(Collectors.toList());
        if(!RRFolder.isEmpty()){
            isCloned=true;
            File remoteFolder = RRFolder.get(0);
            File [] filesInRemoteFolder =remoteFolder.listFiles();
            assert filesInRemoteFolder != null;
            for(File f : filesInRemoteFolder){
                RemoteBranch remoteBranch = new RemoteBranch(remoteFolder.getName()+"\\"+f.getName().substring(0,f.getName().length()-4),new SHA1 (FileUtils.ReadContentFromFile(f)));
                m_BranchesMap.put(remoteBranch.getName(),remoteBranch);
            }
        }
        for (File f : files) {
            if(!f.isDirectory()) {
                if (!f.getName().equals("HEAD.txt")) {
                    String branchName = f.getName().substring(0, f.getName().length() - 4);
                    if(!RRFolder.isEmpty()){
                        File remoteFolder = RRFolder.get(0);
                        if(m_BranchesMap.containsKey(remoteFolder.getName()+ "\\" +branchName)){
                            RemoteTrackingBranch rtb =new RemoteTrackingBranch(branchName, new SHA1(FileUtils.ReadContentFromFile(f)),(RemoteBranch)m_BranchesMap.get(remoteFolder.getName()+"\\"+branchName));
                            m_BranchesMap.put(branchName, rtb);
                        }
                        else{
                            Branch branch = new Branch(branchName, new SHA1(FileUtils.ReadContentFromFile(f)));
                            m_BranchesMap.put(branchName, branch);
                        }
                    }
                    else {
                        Branch branch = new Branch(branchName, new SHA1(FileUtils.ReadContentFromFile(f)));
                        m_BranchesMap.put(branchName, branch);
                    }
                }
            }
        }

    }

    private void LoadCommits() throws IOException, ParseException {
        File file = new File(m_Location + MAGIT_FOLDER + "commits.txt");
        List<String> lines = FileUtils.GetLinesFromFile(file);
        for (String line : lines) {
            String content = FileUtils.getContentFromZippedFile(m_Location + OBJECTS_FOLDER + line + ".zip");
            String[] commitDetails = content.split(",");
            Commit commit;
            //todo:: when doing merge to manage the case with some elements in prev commits list
            if(commitDetails.length == 5){
                commit =new Commit(commitDetails[2]);
            }else {
                 commit =new Commit(commitDetails[3]);
            }
            commit.SetMainFolderSH1(new SHA1(commitDetails[0]));
            commit.SetCreateTime(new OurDate(commitDetails[commitDetails.length-1]));
            commit.SetWhoUpdated(new User(commitDetails[commitDetails.length-2]));
            List<SHA1> prevCommits = new ArrayList<>();
            if(!commitDetails[1].equals("null")){
                prevCommits.add(new SHA1(commitDetails[1]));
                if(commitDetails.length==6){
                    prevCommits.add(new SHA1(commitDetails[2].substring(1)));
                }
            }
            commit.SetPrevCommits(prevCommits);
            m_CommitMap.put(commit.MakeSH1(),commit);
        }

    }

    private void LoadFolders() throws IOException, ParseException {
        File file = new File(m_Location + MAGIT_FOLDER + "folders.txt");
        List<String> lines = FileUtils.GetLinesFromFile(file);
        for (String line : lines) {
            String content = FileUtils.getContentFromZippedFile(m_Location + OBJECTS_FOLDER + line + ".zip");
            List<FileDetails> fileDetailsList=FileUtils.ParseFolderTextFileToFileDetailsList(content);
            Folder f =new Folder(fileDetailsList);
            m_FoldersMap.put(f.MakeSH1(),f);
        }
    }

    private void LoadBlobs() throws IOException {
        File file = new File(m_Location + MAGIT_FOLDER + "blobs.txt");
        List<String> lines = FileUtils.GetLinesFromFile(file);
        String content;
        for (String line : lines) {
            content = FileUtils.getContentFromZippedFile(m_Location +  OBJECTS_FOLDER + line + ".zip");
            Blob blob = new Blob(content);
            m_BlobsMap.put(blob.MakeSH1(),blob);
        }
    }

    private void LoadName() throws IOException {
        File file = new File(m_Location + MAGIT_FOLDER + "repository name.txt");
        m_Name = FileUtils.ReadContentFromFile(file);
    }

    public Map<String, Branch> getBranchesMap() {
        return m_BranchesMap;
    }


    public Branch getActiveBranch() {
        return m_ActiveBranch;
    }

    public Map<SHA1, Commit> getCommitMap() {
        return m_CommitMap;
    }

    public Map<SHA1, Blob> getBlobsMap() {
        return m_BlobsMap;
    }

    public Map<SHA1, Folder> getFoldersMap() {
        return m_FoldersMap;
    }

    public String GetLocation() {
        return m_Location;
    }

    public void setActiveBranch(Branch branch) {
        m_ActiveBranch = branch;
    }

    public String getName()
    {
        return m_Name;
    }

    public Commit getCommitFromCommitsMap(SHA1 sha1){
        return m_CommitMap.get(sha1);
    }


    public String GetContentOfBlob(SHA1 sha1Blob){
        return getBlobsMap().get(sha1Blob).getContent();
    }

    public String GetContentOfFolder(SHA1 folderSha1){
        return getFoldersMap().get(folderSha1).toString();
    }

    public Commit getCommitFromMapCommit(SHA1 sha1) {
            return getCommitFromCommitsMap(sha1);
    }

    public SHA1 getCommitSha1ByBranchName(String branchName) {
        return m_BranchesMap.get(branchName).getCommitSH1();
    }


    public void clearDeltaLists() {
        if (!m_ChangedList.isEmpty()) {
            m_ChangedList.clear();
        }
        if (!m_AddedList.isEmpty()) {
            m_AddedList.clear();
        }
        if (!m_DeletedList.isEmpty()) {
            m_DeletedList.clear();
        }
    }

    public boolean isCloned() {
        return isCloned;
    }

    public void setCloned(boolean cloned) {
        isCloned = cloned;
    }
}

