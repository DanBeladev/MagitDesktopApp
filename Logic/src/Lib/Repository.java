package Lib;

import Lib.Blob;
import MagitExceptions.*;
import javafx.scene.control.CheckBox;
import puk.team.course.magit.ancestor.finder.AncestorFinder;

import java.io.File;
import java.io.FileNotFoundException;
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
    private String RRLocation;
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
        m_ActiveBranch = null;
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

    public void DeltasListReset() {
        this.m_ChangedList = new ArrayList<>();
        this.m_AddedList = new ArrayList<>();
        this.m_DeletedList = new ArrayList<>();
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
        LoadRRLocation();
    }

    private void LoadRRLocation() throws IOException {
        File file = new File(m_Location + MAGIT_FOLDER + "RRLocation.txt");
        if (file.exists()) {
            RRLocation = FileUtils.ReadContentFromFile(file);
        }
    }

    private void LoadActiveBranch() throws IOException {
        File file = new File(m_Location + BRANCHES_FOLDER + "HEAD.txt");
        if (!FileUtils.ReadContentFromFile(file).equals("")) {
            m_ActiveBranch = m_BranchesMap.get(FileUtils.ReadContentFromFile(file));
        }
    }

    private void LoadBranches() throws IOException {
        File file = new File(m_Location + BRANCHES_FOLDER);
        File[] files = file.listFiles();
        assert files != null;
        List<File> RRFolder = Arrays.stream(files).filter(File::isDirectory).collect(Collectors.toList());
        if (!RRFolder.isEmpty()) {
            File remoteFolder = RRFolder.get(0);
            File[] filesInRemoteFolder = remoteFolder.listFiles();
            assert filesInRemoteFolder != null;
            for (File f : filesInRemoteFolder) {
                RemoteBranch remoteBranch = new RemoteBranch(remoteFolder.getName() + "\\" + f.getName().substring(0, f.getName().length() - 4), new SHA1(FileUtils.ReadContentFromFile(f)));
                m_BranchesMap.put(remoteBranch.getName(), remoteBranch);
            }
        }
        for (File f : files) {
            if (!f.isDirectory()) {
                if (!f.getName().equals("HEAD.txt")) {
                    String branchName = f.getName().substring(0, f.getName().length() - 4);
                    if (!RRFolder.isEmpty()) {
                        File remoteFolder = RRFolder.get(0);
                        if (m_BranchesMap.containsKey(remoteFolder.getName() + "\\" + branchName)) {
                            RemoteTrackingBranch rtb = new RemoteTrackingBranch(branchName, new SHA1(FileUtils.ReadContentFromFile(f)), (RemoteBranch) m_BranchesMap.get(remoteFolder.getName() + "\\" + branchName));
                            m_BranchesMap.put(branchName, rtb);
                        } else {
                            Branch branch = new Branch(branchName, new SHA1(FileUtils.ReadContentFromFile(f)));
                            m_BranchesMap.put(branchName, branch);
                        }
                    } else {
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
            if (commitDetails.length == 5) {
                commit = new Commit(commitDetails[2]);
            } else {
                commit = new Commit(commitDetails[3]);
            }
            commit.SetMainFolderSH1(new SHA1(commitDetails[0]));
            commit.SetCreateTime(new OurDate(commitDetails[commitDetails.length - 1]));
            commit.SetWhoUpdated(new User(commitDetails[commitDetails.length - 2]));
            List<SHA1> prevCommits = new ArrayList<>();
            if (!commitDetails[1].equals("null")) {
                prevCommits.add(new SHA1(commitDetails[1]));
                if (commitDetails.length == 6) {
                    prevCommits.add(new SHA1(commitDetails[2].substring(1)));
                }
            }
            commit.SetPrevCommits(prevCommits);
            m_CommitMap.put(commit.MakeSH1(), commit);
        }

    }

    private void LoadFolders() throws IOException, ParseException {
        File file = new File(m_Location + MAGIT_FOLDER + "folders.txt");
        List<String> lines = FileUtils.GetLinesFromFile(file);
        for (String line : lines) {
            String content = FileUtils.getContentFromZippedFile(m_Location + OBJECTS_FOLDER + line + ".zip");
            List<FileDetails> fileDetailsList = FileUtils.ParseFolderTextFileToFileDetailsList(content);
            Folder f = new Folder(fileDetailsList);
            m_FoldersMap.put(f.MakeSH1(), f);
        }
    }

    private void LoadBlobs() throws IOException {
        File file = new File(m_Location + MAGIT_FOLDER + "blobs.txt");
        List<String> lines = FileUtils.GetLinesFromFile(file);
        String content;
        for (String line : lines) {
            content = FileUtils.getContentFromZippedFile(m_Location + OBJECTS_FOLDER + line + ".zip");
            Blob blob = new Blob(content);
            m_BlobsMap.put(blob.MakeSH1(), blob);
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

    public String getName() {
        return m_Name;
    }

    public Commit getCommitFromCommitsMap(SHA1 sha1) {
        return m_CommitMap.get(sha1);
    }


    public String GetContentOfBlob(SHA1 sha1Blob) {
        return getBlobsMap().get(sha1Blob).getContent();
    }

    public String GetContentOfFolder(SHA1 folderSha1) {
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

    public void setRRLocation(String location) {
        RRLocation = location;
    }

    public String getRRLocation() {
        return RRLocation;
    }

    public void DeleteWC() {
        File file = new File(GetLocation());
        File[] fileList = file.listFiles();
        for (File f : fileList) {
            if (!f.getName().equals(".magit")) {
                if (f.isDirectory()) {
                    FileUtils.deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    public boolean HasOpenChanges(User m_User) throws IOException, ParseException, CommitException {
        boolean hasChanges = false;
        List<List<String>> openChangesList = ShowStatus(m_User);
        for (List<String> list : openChangesList) {
            if (!list.isEmpty()) {
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    public List<List<String>> ShowStatus(User m_User) throws IOException, ParseException, CommitException {
        if (getCommitMap().isEmpty()) {
            throw new CommitException("You have to do at least one commit before Show Status method");
        }
        clearDeltaLists();


        List<List<String>> listOfLists = new ArrayList<>();
        GetAllCurrentCommitsFiles().forEach(v -> getDeletedList().add(v));
        MoveWCtoObjectFolder(GetLocation(), 1, m_User);
        if (!getChangedList().isEmpty() || !getDeletedList().isEmpty() || !getAddedList().isEmpty()) {
            getChangedList().add(GetLocation());
        }
        listOfLists.add(getAddedList());
        listOfLists.add(getChangedList());
        listOfLists.add(getDeletedList());
        return listOfLists;

    }

    private List<String> GetAllCurrentCommitsFiles() throws IOException, ParseException {
        List<String> filesInCommit = new ArrayList<>();
        List<FileDetails> rootFolderDetails = GetRootFolderFileDetailsFromCurrentCommit();
        String path = GetLocation();
        GetAllCurrentCommitsFilesRec(rootFolderDetails, filesInCommit, path);
        return filesInCommit;

    }

    private List<FileDetails> GetRootFolderFileDetailsFromCurrentCommit() throws IOException, ParseException {
        File file = GetCommitOfHeadBranch();
        String content = FileUtils.ReadContentFromFile(file);
        content = content.substring(0, 40);
        String commitFolderContent = FileUtils.getContentFromZippedFile(GetLocation() + OBJECTS_FOLDER + content + ".zip");
        String RootFolderContent = FileUtils.getContentFromZippedFile(GetLocation() + OBJECTS_FOLDER + commitFolderContent.substring(0, 40) + ".zip");
        return FileUtils.ParseFolderTextFileToFileDetailsList(RootFolderContent);
    }

    private File GetCommitOfHeadBranch() throws IOException {
        File file = new File(GetLocation() + BRANCHES_FOLDER + "HEAD.txt");
        if (!file.exists()) {
            throw new FileNotFoundException("HEAD file is not exist");
        } else {
            String fileName = FileUtils.ReadContentFromFile(file);
            file = new File(GetLocation() + BRANCHES_FOLDER + fileName + ".txt");
        }
        return file;
    }

    private void GetAllCurrentCommitsFilesRec(List<FileDetails> folderDetails, List<String> FilesInCommit, String path) {
        folderDetails.forEach((val) -> {
            String innerPath = path + "\\" + val.getName();
            FilesInCommit.add(innerPath);
            if (val.getFileType() == FileType.FOLDER) {
                try {
                    String data = FileUtils.getContentFromZippedFile(GetLocation() + OBJECTS_FOLDER + val.getSh1() + ".zip");
                    List<FileDetails> innerFolder = FileUtils.ParseFolderTextFileToFileDetailsList(data);
                    GetAllCurrentCommitsFilesRec(innerFolder, FilesInCommit, innerPath);
                } catch (IOException | ParseException e) {
                    e.getMessage();
                }
            }
        });
    }

    private FileDetails MoveWCtoObjectFolder(String path, int ver, User m_User) throws IOException, ParseException {
        List<FileDetails> subFiles = new ArrayList<>();
        FileDetails fileDetails;
        File file = new File(path);
        File[] filesInFile = file.listFiles();
        for (File f : filesInFile) {
            if (!f.getName().equals(".magit")) {
                if (f.isDirectory()) {
                    fileDetails = MoveWCtoObjectFolder(path + "\\" + f.getName(), ver, m_User);
                    subFiles.add(fileDetails);

                } else {
                    String content = FileUtils.ReadContentFromFile(f);
                    SHA1 Sh1 = new SHA1();
                    Sh1.MakeSH1FromContent(content);
                    fileDetails = new FileDetails();
                    if (!getBlobsMap().containsKey(Sh1) && ver == 0) {
                        Blob b = new Blob(content);
                        PutInMapNewMagitFileAndAddToObjectFolderAndInitFileDetails(b, f, fileDetails, m_User);
                    } else {
                        if (!getCommitMap().isEmpty()) {
                            fileDetails = CheckInCommit(path + "\\" + f.getName(), Sh1, ver);
                        }
                        if (fileDetails == null || getCommitMap().isEmpty()) {
                            fileDetails = new FileDetails();
                            fileDetails.SetDetails(f.getName(), Sh1, FileType.FILE, m_User);
                        }
                    }
                    subFiles.add(fileDetails);
                }
            }
        }

        fileDetails = new FileDetails();
        Folder folder = new Folder(subFiles);
        SHA1 folderSh1 = folder.MakeSH1();
        if (!getFoldersMap().containsKey(folderSh1) && ver == 0) {
            PutInMapNewMagitFileAndAddToObjectFolderAndInitFileDetails(folder, file, fileDetails, m_User);
        } else {
            if (!getCommitMap().isEmpty()) {
                fileDetails = CheckInCommit(path, folderSh1, ver);
            }
            if (fileDetails == null || getCommitMap().isEmpty()) {
                fileDetails = new FileDetails();
                fileDetails.SetDetails(file.getName(), folderSh1, FileType.FOLDER, m_User);
            }

        }

        return fileDetails;
    }

    private void PutInMapNewMagitFileAndAddToObjectFolderAndInitFileDetails(MagitFile mf, File file, FileDetails fileDetails, User m_User) throws IOException {
        File tmpfile;
        if (mf.GetMagitFileType() == FileType.FOLDER) {
            FileUtils.AppendTextToFile(GetLocation() + MAGIT_FOLDER + "folders.txt", mf.MakeSH1().getSh1());
            getFoldersMap().put(mf.MakeSH1(), (Folder) mf);
            tmpfile = ((Folder) mf).CreateTextFileRepresentFolder(GetLocation() +
                    OBJECTS_FOLDER + file.getName() + ".txt");
        } else {
            FileUtils.AppendTextToFile(GetLocation() + MAGIT_FOLDER + "blobs.txt", mf.MakeSH1().getSh1());
            getBlobsMap().put(mf.MakeSH1(), (Blob) mf);
            tmpfile = new File(GetLocation() + OBJECTS_FOLDER + mf.MakeSH1().getSh1() + ".txt");
            FileUtils.WriteToFile((((Blob) mf).getContent()), GetLocation() + OBJECTS_FOLDER + mf.MakeSH1().getSh1() + ".txt");
        }
        FileUtils.CreateZipFile(tmpfile, mf.MakeSH1(), GetLocation() + OBJECTS_FOLDER);
        tmpfile.delete();
        fileDetails.SetDetails(file.getName(), mf.MakeSH1(), mf.GetMagitFileType(), m_User);
    }

    public void MakeCommit(String message, Commit anotherPrevCommit, User m_User) throws IOException, ParseException, RepositoryDoesnotExistException, CommitException {
        if (this == null) {
            throw new RepositoryDoesnotExistException("You have to initialize repository before making commit");
        }
        if (!getCommitMap().isEmpty() && !HasOpenChanges(m_User)) {
            throw new CommitException("You have no changes in yours WC compared to your previous commit ");
        }
        Commit newCommit = new Commit(message);
        FileDetails rootFolderDetails = MoveWCtoObjectFolder(GetLocation(), 0, m_User);

        newCommit.SetDetailsToCommitByGivenRootFolderAndUser(rootFolderDetails, m_User);
        if (getCommitMap().isEmpty()) {
            getActiveBranch().setCommitSH1(newCommit.MakeSH1());
            FileUtils.WriteToFile(newCommit.MakeSH1().getSh1(), GetLocation() + BRANCHES_FOLDER + "master.txt");
            getActiveBranch().CreateBranchTextFile(GetLocation() + BRANCHES_FOLDER);
        } else {
            newCommit.AddPrevCommit(getActiveBranch().getCommitSH1());
            if (anotherPrevCommit != null) {
                newCommit.AddPrevCommit(anotherPrevCommit.MakeSH1());
            }
            getActiveBranch().UpdateSHA1AndBranchFileContent(newCommit.MakeSH1(), GetLocation() + BRANCHES_FOLDER + getActiveBranch().getName() + ".txt");
        }
        File commitFile = FileUtils.CreateTextFile(GetLocation() + OBJECTS_FOLDER + newCommit.MakeSH1() + ".txt", newCommit.toString());
        FileUtils.CreateZipFile(commitFile, newCommit.MakeSH1(), GetLocation() + OBJECTS_FOLDER);
        FileUtils.AppendTextToFile(GetLocation() + MAGIT_FOLDER + "commits.txt", newCommit.MakeSH1().getSh1());
        commitFile.delete();
        getCommitMap().put(newCommit.MakeSH1(), newCommit);

    }

    private FileDetails CheckInCommit(String path, SHA1 sha1, int ver) throws IOException, ParseException {
        String innerPath = GetLocation();
        if (!path.equals(innerPath)) {
            innerPath = path.substring(GetLocation().length() + 1);
        } else {
            String[] tmp = GetLocation().split("\\\\");
            innerPath = tmp[tmp.length - 1];
            if (ver == 1) {
                return null;
            }
        }
        String[] array = innerPath.split("\\\\");
        List<FileDetails> rootFolderDetails = GetRootFolderFileDetailsFromCurrentCommit();
        return CheckInCommitRecursion(array, rootFolderDetails, sha1, ver, path);
    }

    private FileDetails CheckInCommitRecursion(String[] innerPath, List<FileDetails> folderDetails, SHA1 sh1, int ver, String fullPath) throws IOException, ParseException {
        getDeletedList().removeIf(v -> v.equals(fullPath));
        String tmp = innerPath[0];
        folderDetails = folderDetails.stream().filter((value) -> value.getName().equals(tmp)).collect(Collectors.toList());
        if (innerPath.length == 1 || (folderDetails.isEmpty())) {
            if (folderDetails.isEmpty() || !folderDetails.get(0).getSh1().getSh1().equals(sh1.getSh1())) {
                if (ver == 1) {
                    if (folderDetails.isEmpty()) {
                        getAddedList().add(fullPath);
                    } else {
                        getChangedList().add(fullPath);
                    }
                }
                return null;
            } else {
                return folderDetails.get(0);
            }
        }
        innerPath = Arrays.copyOfRange(innerPath, 1, innerPath.length);
        String content = FileUtils.getContentFromZippedFile(GetLocation() + OBJECTS_FOLDER + folderDetails.get(0).getSh1().getSh1() + ".zip");
        folderDetails = FileUtils.ParseFolderTextFileToFileDetailsList(content);
        return CheckInCommitRecursion(innerPath, folderDetails, sh1, ver, fullPath);
    }

    public void Push(User user) throws CommitException, RepositoryDoesntTrackAfterOtherRepositoryException, IOException, ParseException, RemoteTrackingBranchException, OpenChangesException {
        IsRepositoryHasAtLeastOneCommit();

        if (getRRLocation() == null) {
            throw new RepositoryDoesntTrackAfterOtherRepositoryException("Current function available only on cloned repositories ");
        }
        else if (!new File(getRRLocation()).exists()){
            throw new FileNotFoundException("Failed to find Remote Repository in address: " + getRRLocation()+". maybe you didnt load it yet ?");
        }

        Branch activeBranch = getActiveBranch();
        if (activeBranch instanceof RemoteTrackingBranch) {
            Repository rrRepo = new Repository(getRRLocation());
            rrRepo.LoadData();
            if (rrRepo.HasOpenChanges(user)) {
                throw new OpenChangesException("you can't push to RR because its with open changes");
            }
            if (!getBranchesMap().get(rrRepo.getName() + "\\" + activeBranch.getName()).getCommitSH1().equals(rrRepo.getBranchesMap().get(activeBranch.getName()).getCommitSH1())) {
                throw new RemoteTrackingBranchException("Remote tracking branch pointed to different commit compare to this branch in Remote repository");
            }
            Commit commitPointedByHead = getCommitFromMapCommit(getActiveBranch().getCommitSH1());
            addAllBranchData(commitPointedByHead, this, rrRepo);
            Branch activeInRR = rrRepo.getActiveBranch();
            if (activeInRR == rrRepo.getBranchesMap().get(activeBranch.getName())) {
                activeInRR.setCommitSH1(activeBranch.getCommitSH1());
                FileUtils.WriteToFile(activeBranch.getCommitSH1().getSh1(), rrRepo.GetLocation() + BRANCHES_FOLDER + activeInRR.getName() + ".txt");
                Commit commit = rrRepo.getCommitMap().get(rrRepo.getActiveBranch().getCommitSH1());
                rrRepo.DeleteWC();
                CheckOutRecursion(rrRepo.GetLocation(), commit.getMainFolderSH1());
            } else {
                Branch remote = rrRepo.getBranchesMap().get(activeBranch.getName());
                remote.UpdateSHA1AndBranchFileContent(activeBranch.getCommitSH1(), rrRepo.GetLocation() + BRANCHES_FOLDER + activeBranch.getName() + ".txt");
            }
            getBranchesMap().get(rrRepo.getName() + "\\" + activeBranch.getName()).UpdateSHA1AndBranchFileContent(activeBranch.getCommitSH1(), GetLocation() + BRANCHES_FOLDER + rrRepo.getName() + "\\" + activeBranch.getName() + ".txt");
        }

    }

    public void IsRepositoryHasAtLeastOneCommit() throws CommitException {
        if (getCommitMap().isEmpty()) {
            throw new CommitException("Error: you don't have any commits in your current repository");
        }
    }

    private void addAllBranchData(Commit commit, Repository dataSupplierRepo, Repository gettingDataRepo) throws IOException {
        if (!gettingDataRepo.getCommitMap().containsKey(commit)) {
            Folder mainFolder = dataSupplierRepo.getFoldersMap().get(commit.getMainFolderSH1());
            AddCommitData(mainFolder, dataSupplierRepo, gettingDataRepo);
            commit.AddToRepository(gettingDataRepo);
            List<SHA1> prevCommitsSha1 = commit.getPrevCommits();
            for (SHA1 prevCommitSha1 : prevCommitsSha1) {
                Commit prevCommit = dataSupplierRepo.getCommitMap().get(prevCommitSha1);
                if (!gettingDataRepo.getCommitMap().containsKey(prevCommit)) {
                    addAllBranchData(prevCommit, dataSupplierRepo, gettingDataRepo);
                }
            }

        }
    }

    private void AddCommitData(Folder folder, Repository dataSupplierRepo, Repository gettingDataRepo) throws IOException {
        folder.AddToRepository(gettingDataRepo);
        List<FileDetails> innerFiles = folder.getInnerFiles();
        for (int i = 0; i < innerFiles.size(); i++) {
            if (innerFiles.get(i).getFileType() == FileType.FILE) {
                if (!gettingDataRepo.getBlobsMap().containsKey(innerFiles.get(i).getSh1())) {
                    dataSupplierRepo.getBlobsMap().get(innerFiles.get(i).getSh1()).AddToRepository(gettingDataRepo);
                }
            } else {
                if (!gettingDataRepo.getFoldersMap().containsKey(innerFiles.get(i).getSh1())) {
                    Folder innerFolder = dataSupplierRepo.getFoldersMap().get(innerFiles.get(i).getSh1());
                    AddCommitData(innerFolder, dataSupplierRepo, gettingDataRepo);
                }
            }
        }
    }

    public void CheckOut(String name, User user) throws BranchDoesNotExistException, IOException, ParseException, BranchIsAllReadyOnWCException, CheckoutToRemoteBranchException {
        Branch branch;
        if (!getBranchesMap().containsKey(name)) {
            throw new BranchDoesNotExistException("The branch: " + name + " does'nt exist");
        } else if (getActiveBranch() != null && getActiveBranch().getName().equals(name)) {
            throw new BranchIsAllReadyOnWCException("The Branch: " + name + " is already Head branch");
        } else {
            branch = getBranchesMap().get(name);
            if (branch instanceof RemoteBranch) {
                throw new CheckoutToRemoteBranchException("not available to do checkout to remote branch");
            }
            setActiveBranch(branch);
            SHA1 commitSH1 = branch.getCommitSH1();
            SHA1 mainFolderSH1 = getCommitMap().get(commitSH1).getMainFolderSH1();
            UpdateHeadFileContent(branch.getName());
            DeleteWC();
            CheckOutRecursion(GetLocation(), mainFolderSH1);
        }
    }

    private void CheckOutRecursion(String path, SHA1 mainFolderSH1) throws IOException, ParseException {
        String content = FileUtils.getContentFromZippedFile(GetLocation() + OBJECTS_FOLDER + mainFolderSH1.getSh1() + ".zip");
        List<FileDetails> fileDetailsList = FileUtils.ParseFolderTextFileToFileDetailsList(content);
        for (FileDetails fileDetails : fileDetailsList) {
            if (fileDetails.getFileType() == FileType.FILE) {
                FileUtils.UnzipFile(GetLocation() + OBJECTS_FOLDER + fileDetails.getSh1() + ".zip", path, fileDetails.getName());
            } else {
                boolean isCreated = new File(path + "\\" + fileDetails.getName()).mkdirs();
                if (isCreated) {
                    CheckOutRecursion(path + "\\" + fileDetails.getName(), fileDetails.getSh1());
                }

            }
        }
    }

    public void UpdateHeadFileContent(String name) throws IOException {
        FileUtils.WriteToFile(name, GetLocation() + BRANCHES_FOLDER + "HEAD.txt");
    }

    public List<MergeConfilct> MergeHeadBranchWithOtherBranch(String branchName, User user) throws ParseException, IOException, OpenChangesException, BranchDoesNotExistException, FFException, CommitException {
        Branch their = m_BranchesMap.get(branchName);
        if (their == null) {
            throw new BranchDoesNotExistException("The given branch doesn't exist");
        }
        if (HasOpenChanges(user)) {
            throw new OpenChangesException("You have open changes on your working copy");
        }
        if (their == getActiveBranch()) {
            throw new BranchDoesNotExistException("Impossible merge HEAD branch with itself, You have to choose another branch");
        }
        AncestorFinder ancestorFinder = new AncestorFinder((v) -> getCommitFromCommitsMap(new SHA1(v)));
        String activeBranchCommitSH1 = getActiveBranch().getCommitSH1().getSh1();
        String theirBranchCommitSH1 = their.getCommitSH1().getSh1();
        String ancestorSha1 = ancestorFinder.traceAncestor(activeBranchCommitSH1, theirBranchCommitSH1);
        if (ancestorSha1.equals(activeBranchCommitSH1) || ancestorSha1.equals(theirBranchCommitSH1)) {
            handleFastForwardMerge(ancestorSha1, getActiveBranch(), their, user);
        }
        List<FileDetails> ourFileDetails = ShowAllCommitFiles(getActiveBranch().getCommitSH1());
        List<FileDetails> theirFileDetails = ShowAllCommitFiles(their.getCommitSH1());
        List<FileDetails> ancestorFileDetails = ShowAllCommitFiles(new SHA1(ancestorSha1));
        Map<String, FileStatusCompareAncestor> ourFilesCompareAncestor = new HashMap<>();
        Map<String, FileStatusCompareAncestor> theirFilesCompareAncestor = new HashMap<>();

        for (FileDetails file : ancestorFileDetails) {
            if (file.getFileType() == FileType.FILE) {
                ClassifyFilesForSons(file, ourFileDetails, ourFilesCompareAncestor);
                ClassifyFilesForSons(file, theirFileDetails, theirFilesCompareAncestor);
            }
        }
        handleWithAddedFile(ourFileDetails, ourFilesCompareAncestor);
        handleWithAddedFile(theirFileDetails, theirFilesCompareAncestor);
        return MergeTwoSons(ancestorFileDetails, ourFilesCompareAncestor, theirFilesCompareAncestor, ourFileDetails, theirFileDetails);
    }

    private void handleFastForwardMerge(String ancestorSha1, Branch activeBranch, Branch theirBranch, User user) throws IOException, ParseException, FFException, CommitException, OpenChangesException {
        if (ancestorSha1.equals(activeBranch.getCommitSH1().getSh1())) {
            ResetHeadBranch(theirBranch.getCommitSH1(), user);
        }
        throw new FFException("Fast Forward Merge");

    }

    private void handleWithAddedFile(List<FileDetails> fileDetailsList, Map<String, FileStatusCompareAncestor> compareToAncestor) {
        for (FileDetails fd : fileDetailsList) {
            if (!compareToAncestor.containsKey(fd.getName()) && fd.getFileType() == FileType.FILE) {
                compareToAncestor.put(fd.getName(), FileStatusCompareAncestor.ADDED);
            }
        }
    }

    private void ClassifyFilesForSons(FileDetails file, List<FileDetails> sonFilesDetails, Map<String, FileStatusCompareAncestor> sonFilesMap) {
        List<FileDetails> givenFileInSon = sonFilesDetails.stream().filter(v -> v.getName().equals(file.getName())).collect(Collectors.toList());
        if (givenFileInSon.size() == 0) {
            sonFilesMap.put(file.getName(), FileStatusCompareAncestor.DELETED);
        } else {
            if (givenFileInSon.get(0).getSh1().equals(file.getSh1())) {
                sonFilesMap.put(file.getName(), FileStatusCompareAncestor.SAME);
            } else {
                sonFilesMap.put(file.getName(), FileStatusCompareAncestor.CHANGED);
            }
        }
    }

    private List<MergeConfilct> MergeTwoSons(List<FileDetails> ancestorFilesList, Map<String, FileStatusCompareAncestor> ourClassifiedFiles, Map<String, FileStatusCompareAncestor> theirsClassifiedFiles, List<FileDetails> ourFileDetails, List<FileDetails> theirsFileDetails) throws IOException {
        List<FileDetails> mergeList = new ArrayList<>();
        List<MergeConfilct> conflictedList = new ArrayList<>();
        MergeConfilct confilct;
        for (FileDetails fd : ancestorFilesList) {
            List<FileDetails> currentFileInOurCommit = ourFileDetails.stream().filter(v -> v.getName().equals(fd.getName())).collect(Collectors.toList());
            List<FileDetails> currentFileInTheirsCommit = theirsFileDetails.stream().filter(v -> v.getName().equals(fd.getName())).collect(Collectors.toList());
            if (fd.getFileType() == FileType.FILE) {
                if (ourClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.SAME && theirsClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.SAME) {
                    mergeList.add(fd);
                } else if (theirsClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.SAME && ourClassifiedFiles.get(fd.getName()) != FileStatusCompareAncestor.SAME) {
                    if (ourClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.CHANGED) {
                        mergeList.add(currentFileInOurCommit.get(0));
                    }
                } else if (theirsClassifiedFiles.get(fd.getName()) != FileStatusCompareAncestor.SAME && ourClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.SAME) {
                    if (theirsClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.CHANGED) {
                        mergeList.add(currentFileInTheirsCommit.get(0));
                    }
                } else {
                    if (!(theirsClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.DELETED && ourClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.DELETED)) {
                        if (theirsClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.CHANGED && ourClassifiedFiles.get(fd.getName()) == FileStatusCompareAncestor.CHANGED &&
                                currentFileInOurCommit.get(0).getSh1().equals(currentFileInTheirsCommit.get(0).getSh1())) {
                            mergeList.add(currentFileInOurCommit.get(0));
                        } else {
                            String ourFDContent = "";
                            String theirFDContent = "";
                            if (!currentFileInOurCommit.isEmpty())
                                ourFDContent = GetContentOfBlob(currentFileInOurCommit.get(0).getSh1());
                            if (!currentFileInTheirsCommit.isEmpty()) {
                                theirFDContent = GetContentOfBlob(currentFileInTheirsCommit.get(0).getSh1());
                            }
                            confilct = new MergeConfilct(fd.getName(), ourFDContent, theirFDContent, GetContentOfBlob(fd.getSh1()));
                            conflictedList.add(confilct);
                        }
                    }
                }
            }
        }
        addSonAddedFiles(ourClassifiedFiles,theirsClassifiedFiles,ourFileDetails,theirsFileDetails, mergeList,conflictedList);
        //addSonAddedFiles(theirsClassifiedFiles,ourClassifiedFiles, theirsFileDetails, mergeList,conflictedList);
        spanWCByMergeList(mergeList);
        return conflictedList;
    }

    private void addSonAddedFiles(Map<String, FileStatusCompareAncestor> ourMap,Map<String, FileStatusCompareAncestor> thriesMap, List<FileDetails> ourFilesList,List<FileDetails> theirsFilesList, List<FileDetails> mergeList,List<MergeConfilct> confilctList) {
        for (FileDetails fd : ourFilesList) {
            if (ourMap.get(fd.getName()) == FileStatusCompareAncestor.ADDED) {
                FileStatusCompareAncestor fsOfSon2=thriesMap.get(fd.getName());
                List<FileDetails> thisFileInsSon2=theirsFilesList.stream().filter(v->v.getName().equals(fd.getName())).collect(Collectors.toList());
                if(fsOfSon2 == FileStatusCompareAncestor.ADDED && thisFileInsSon2.get(0).getSh1()!=fd.getSh1()){
                    MergeConfilct mc=new MergeConfilct(fd.getName(),GetContentOfBlob(fd.getSh1()),GetContentOfBlob(thisFileInsSon2.get(0).getSh1()),"");
                    confilctList.add(mc);
                }
                else {
                    mergeList.add(fd);
                }
                theirsFilesList.removeIf(v->v.getName().equals(fd.getName()));
            }
        }
        for (FileDetails fd : theirsFilesList) {
            if (thriesMap.get(fd.getName()) == FileStatusCompareAncestor.ADDED) {
                FileStatusCompareAncestor fsOfSon2=ourMap.get(fd.getName());
                List<FileDetails> thisFileInsSon2=ourFilesList.stream().filter(v->v.getName().equals(fd.getName())).collect(Collectors.toList());
                if(fsOfSon2 == FileStatusCompareAncestor.ADDED && thisFileInsSon2.get(0).getSh1()!=fd.getSh1()){
                    MergeConfilct mc=new MergeConfilct(fd.getName(),GetContentOfBlob(thisFileInsSon2.get(0).getSh1()),GetContentOfBlob(fd.getSh1()),"");
                    confilctList.add(mc);
                }
                else {
                    mergeList.add(fd);
                }
            }
        }
    }

    public List<List<String>> compareTwoCommits(Commit current, Commit parent) throws CommitException, ParseException {
        clearDeltaLists();
        if (current == null || parent == null) {
            throw new CommitException("Error: commit without parent to compare");
        }
        List<FileDetails> currentCommitDetails = ShowAllCommitFiles(current.MakeSH1());
        List<FileDetails> otherCommitDetails = ShowAllCommitFiles(parent.MakeSH1());

        for (FileDetails fileDetails : currentCommitDetails) {
            List<FileDetails> sameFiles = otherCommitDetails.stream().filter(v -> v.getName().equals(fileDetails.getName())).collect(Collectors.toList());
            if (sameFiles.isEmpty()) {
                getAddedList().add(fileDetails.getName());
            } else {
                if (!sameFiles.get(0).getSh1().equals(fileDetails.getSh1())) {
                    getChangedList().add(fileDetails.getName());
                }
                otherCommitDetails.remove(sameFiles.get(0));
            }
        }

        for (FileDetails fileDetailsInOtherList : otherCommitDetails) {
            getDeletedList().add(fileDetailsInOtherList.getName());
        }
        List<List<String>> deltas = new ArrayList<>();
        deltas.add(getAddedList());
        deltas.add(getChangedList());
        deltas.add(getDeletedList());
        return deltas;
    }

    private void spanWCByMergeList(List<FileDetails> mergeList) throws IOException {
        DeleteWC();
        for (FileDetails fd : mergeList) {
            String content = FileUtils.getContentFromZippedFile(GetLocation() + OBJECTS_FOLDER + fd.getSh1().getSh1() + ".zip");
            FileUtils.createFoldersByPathAndWriteContent(content, fd.getName());

        }
    }

    public void spanWCsolvedConflictList(List<MergeConfilct> confilctList) throws IOException {
        for (MergeConfilct confilct : confilctList) {
            if (confilct.getResolveContent() != null) {
                FileUtils.createFoldersByPathAndWriteContent(confilct.getResolveContent(), confilct.getPath());
            }
        }
    }

    private List<FileDetails> ShowAllCommitFiles(SHA1 commitSha1) throws ParseException {
        Commit commit = getCommitMap().get(commitSha1);
        Folder folder = getFoldersMap().get(commit.getMainFolderSH1());
        String folderPathName = GetLocation();

        List<FileDetails> commitFiles = new ArrayList<>();
        showALLCommitFilesRecursion(commitFiles, folder, folderPathName);

        commitFiles.add(new FileDetails(folderPathName, commit.getMainFolderSH1(), FileType.FOLDER, new User(commit.getWhoUpdated().getName()), new OurDate(commit.getCreateTime())));
        return commitFiles;
    }

    private void showALLCommitFilesRecursion(List<FileDetails> commitFiles, Folder folder, String fullPathName) {
        for (FileDetails fd : folder.getInnerFiles()) {
            FileDetails fdToReturn = new FileDetails(fd);
            String path = fullPathName + "\\" + fd.getName();
            if (fd.getFileType() == FileType.FOLDER) {
                folder = getFoldersMap().get(fd.getSh1());
                showALLCommitFilesRecursion(commitFiles, folder, path);
            }
            fdToReturn.setName(path);
            commitFiles.add(fdToReturn);
        }
    }

    public void ResetHeadBranch(SHA1 sha1, User user) throws CommitException, ParseException, IOException, OpenChangesException {
        if (HasOpenChanges(user)) {
            throw new OpenChangesException("you have open changes");
        }
        if (!getCommitMap().containsKey(sha1)) {
            throw new CommitException("SHA-1: " + sha1.getSh1() + " doesnt exist");
        } else if (getActiveBranch().getCommitSH1().equals(sha1)) {
            throw new CommitException("SHA-1: " + sha1.getSh1() + " already pointed by Head branch");
        } else {
            getActiveBranch().setCommitSH1(sha1);
            FileUtils.WriteToFile(sha1.getSh1(), GetLocation() + BRANCHES_FOLDER + getActiveBranch().getName() + ".txt");
            Commit commit = getCommitMap().get(getActiveBranch().getCommitSH1());
            DeleteWC();
            CheckOutRecursion(GetLocation(), commit.getMainFolderSH1());
        }
    }

    public void Pull(User user) throws RepositoryDoesnotExistException, RepositoryDoesntTrackAfterOtherRepositoryException, ParseException, CommitException, IOException, OpenChangesException, BranchDoesNotExistException {
        if (getRRLocation() == null) {
            throw new RepositoryDoesntTrackAfterOtherRepositoryException("Current function available only on cloned repositories ");
        }
        if (HasOpenChanges(user)) {
            throw new OpenChangesException("Can't apply this function with open Changes");
        }
        else if (!new File(getRRLocation()).exists()){
            throw new FileNotFoundException("Failed to find Remote Repository in address: " + getRRLocation()+". maybe you didnt load it yet ?");
        }
        Branch activeBranch = getActiveBranch();
        if (activeBranch instanceof RemoteTrackingBranch) {
            if (!activeBranch.getCommitSH1().equals(((RemoteTrackingBranch) activeBranch).getFollowAfter().getCommitSH1())) {
                throw new OpenChangesException("Impossible to do pull with Local changes on RTB without push");
            }
            Repository rrRepo = new Repository(getRRLocation());
            rrRepo.LoadData();
            Branch branchInRR = rrRepo.getBranchesMap().get(activeBranch.getName());
            Commit commit = rrRepo.getCommitMap().get(branchInRR.getCommitSH1());
            addAllBranchData(commit, rrRepo, this);
            ResetHeadBranch(branchInRR.getCommitSH1(), user);
            Branch RB = getBranchesMap().get(rrRepo.getName() + "\\" + activeBranch.getName());
            RB.UpdateSHA1AndBranchFileContent(branchInRR.getCommitSH1(), GetLocation() + BRANCHES_FOLDER + rrRepo.getName() + "\\" + branchInRR.getName() + ".txt");
        } else {
            throw new BranchDoesNotExistException("Pull is possible only for Remote Tracking branches");
        }
    }

    public void DeleteBranch(String name) throws HeadBranchDeletedExcption, BranchDoesNotExistException, BranchFileDoesNotExistInFolderException {
        if (getActiveBranch().getName().equals(name)) {
            throw new HeadBranchDeletedExcption("The branch: " + name + " is Head branch, you can't delete it");
        } else if (!getBranchesMap().containsKey(name)) {
            throw new BranchDoesNotExistException("The branch: " + name + " does not exist");
        } else {
            getBranchesMap().remove(name);
            File BranchFile = new File(GetLocation() + BRANCHES_FOLDER + name + ".txt");
            if (!BranchFile.delete()) {
                throw new BranchFileDoesNotExistInFolderException("the file:" + name + ".txt did'nt found");
            }
        }
    }

    public void CreateNewBranch(String name, SHA1 commitSH1ToPoint) throws BranchNameIsAllreadyExistException, IOException, CommitException, RepositoryDoesnotExistException {
        if (getCommitMap().isEmpty()) {
            throw new CommitException("you don't have any commits so you can't make new branch");
        }
        if (getBranchesMap().containsKey(name)) {
            throw new BranchNameIsAllreadyExistException("The name: " + name + " is already exist");
        }else if(name == null || name.isEmpty()){
            throw  new BranchNameIsAllreadyExistException("Branch name have to be contains at least one character");
        }
        else {

            Branch newBranch = new Branch(name, commitSH1ToPoint/*new SHA1(FileUtils.ReadContentFromFile(file))*/);
            newBranch.CreateBranchTextFile(GetLocation() + BRANCHES_FOLDER);
            getBranchesMap().put(name, newBranch);
        }
    }

    public void CreateNewRemoteTrackingBranch(String name, RemoteBranch remoteBranch) throws RepositoryDoesnotExistException, CommitException, BranchNameIsAllreadyExistException, IOException {
        if (getCommitMap().isEmpty()) {
            throw new CommitException("you don't have any commits so you can't make new branch");
        }
        if (getBranchesMap().containsKey(name)) {
            throw new BranchNameIsAllreadyExistException("The name: " + name + " is already exist");
        } else {
            Branch remoteTrackingBranch = new RemoteTrackingBranch(name, remoteBranch.getCommitSH1(), remoteBranch);
            remoteTrackingBranch.CreateBranchTextFile(GetLocation() + BRANCHES_FOLDER);
            getBranchesMap().put(name, remoteTrackingBranch);
        }
    }

    public List<CommitDetails> ShowActiveBranchHistory() throws CommitException {
        IsRepositoryHasAtLeastOneCommit();
        List<CommitDetails> commitDetailsList = new LinkedList<>();
        Branch activeBranch = new Branch(getActiveBranch());
        CommitDetails details;
        if (!getCommitMap().isEmpty()) {
            while (activeBranch.getCommitSH1() != null) {
                details = new CommitDetails(getCommitMap().get(activeBranch.getCommitSH1()));
                commitDetailsList.add(details);
                if (!getCommitMap().get(activeBranch.getCommitSH1()).getPrevCommits().isEmpty()) {
                    activeBranch.setCommitSH1(getCommitMap().get(activeBranch.getCommitSH1()).getPrevCommits().get(0));
                } else {
                    activeBranch.setCommitSH1(null);
                }
            }
        }
        return commitDetailsList;
    }

    public List<BranchDetails> ShowBranches() {
        List<BranchDetails> list = new ArrayList<>();
        getBranchesMap().entrySet().stream().forEach((value) -> {
            list.add(new BranchDetails(value.getValue(), getCommitMap().get(value.getValue().getCommitSH1()), getActiveBranch() == value.getValue()));
        });
        return list;
    }

    public void FetchRRNewData() throws RepositoryDoesnotExistException, RepositoryDoesntTrackAfterOtherRepositoryException, IOException, ParseException {
        if (getRRLocation() == null) {
            throw new RepositoryDoesntTrackAfterOtherRepositoryException("Current function available only on cloned repositories ");
        }
        else if (!new File(getRRLocation()).exists()){
            throw new FileNotFoundException("Failed to find Remote Repository in address: " + getRRLocation()+". maybe you didnt load it yet ?");
        }
        else {
            Repository rrRepository = new Repository(getRRLocation());
            rrRepository.LoadData();
            LoadFromRRMagitFiles(rrRepository.getBlobsMap(), getBlobsMap(), GetLocation() + MAGIT_FOLDER + "blobs.txt");
            LoadFromRRMagitFiles(rrRepository.getCommitMap(), getCommitMap(), GetLocation() + MAGIT_FOLDER + "commits.txt");
            LoadFromRRMagitFiles(rrRepository.getFoldersMap(), getFoldersMap(), GetLocation() + MAGIT_FOLDER + "folders.txt");

            List<Branch> RRBranches = new ArrayList<>(rrRepository.getBranchesMap().values());
            for (Branch branch : RRBranches) {
                if (!getBranchesMap().containsKey(rrRepository.getName() + "\\" + branch.getName())) {
                    RemoteBranch rb = new RemoteBranch(rrRepository.getName() + "\\" + branch.getName(), branch.getCommitSH1());
                    rb.CreateBranchTextFile(GetLocation() + BRANCHES_FOLDER);
                    getBranchesMap().put(rb.getName(), rb);
                } else {
                    Branch b = getBranchesMap().get(rrRepository.getName() + "\\" + branch.getName());
                    b.UpdateSHA1AndBranchFileContent(branch.getCommitSH1(), GetLocation() + BRANCHES_FOLDER + rrRepository.getName() + "\\" + branch.getName() + ".txt");
                }
            }
        }
    }

    private <T extends MagitFile> void LoadFromRRMagitFiles(Map<SHA1, T> mapInRR, Map<SHA1, T> mapInLR, String pathFileToWrite) throws IOException {
        List<T> list = new ArrayList<>(mapInRR.values());
        for (T magitFile : list) {
            if (!mapInLR.containsKey(magitFile.MakeSH1())) {
                mapInLR.put(magitFile.MakeSH1(), magitFile);
                FileUtils.AppendTextToFile(pathFileToWrite, magitFile.MakeSH1().getSh1());
                File magitFileZzipFile = FileUtils.CreateTextFile(GetLocation() + OBJECTS_FOLDER + magitFile.MakeSH1() + ".txt", magitFile.toString());
                FileUtils.CreateZipFile(magitFileZzipFile, magitFile.MakeSH1(), GetLocation() + OBJECTS_FOLDER);
                magitFileZzipFile.delete();
            }
        }
    }

    public String getMainFolderName() {
        String path = GetLocation();
        String[] parts = path.split("\\\\");
        return parts[parts.length - 1];
    }

    public List<SHA1> getCurrentRepositoryAllCommitsSHA1() {
        List<SHA1> commitSha1List = new ArrayList<>(getCommitMap().keySet());
        commitSha1List.sort(Comparator.comparing(v -> getCommitMap().get(v).getCreateTime().getDate()));
        Collections.reverse(commitSha1List);
        return commitSha1List;
    }

    public void pushLocalBranchToRemoteBranch(Branch branchToPush) throws RepositoryDoesntTrackAfterOtherRepositoryException, IOException, ParseException, BranchNameIsAllreadyExistException, CommitException, RepositoryDoesnotExistException, HeadBranchDeletedExcption, BranchDoesNotExistException, BranchFileDoesNotExistInFolderException {
        if (getRRLocation() == null) {
            throw new RepositoryDoesntTrackAfterOtherRepositoryException("Current function available only on cloned repositories ");
        }
        else if(branchToPush instanceof RemoteBranch || branchToPush instanceof RemoteTrackingBranch) {
            throw new BranchDoesNotExistException("Current function available only on local branches");
        }
        else
        {
            Repository RR = new Repository(getRRLocation());
            RR.LoadData();
            Commit firstCommitOnBranch = getCommitFromMapCommit(branchToPush.getCommitSH1());
            addAllBranchData(firstCommitOnBranch,this,RR);
            getBranchesMap().remove(branchToPush.getName());
            File BranchFile = new File(GetLocation() + BRANCHES_FOLDER + branchToPush.getName() + ".txt");
            if(!BranchFile.delete()){
                throw new IOException("Something went wrong by trying remove branch text file");
            }
            RR.CreateNewBranch(branchToPush.getName(),firstCommitOnBranch.MakeSH1());
            RemoteBranch remoteBranch = createRemoteBranch(branchToPush.getName(),branchToPush.getCommitSH1(),RR.getName());
            CreateNewRemoteTrackingBranch(branchToPush.getName(),remoteBranch);
           // setActiveBranch(m_BranchesMap.get(branchToPush.getName()));
        }
    }

    private RemoteBranch createRemoteBranch(String BranchNameInRR,SHA1 commitSha1,String RRname) throws IOException {
        RemoteBranch RB = new RemoteBranch(RRname+"\\"+BranchNameInRR,commitSha1);
        RB.CreateBranchTextFile(GetLocation()+ BRANCHES_FOLDER);
        m_BranchesMap.put(RB.getName(),RB);
        return RB;
    }


  /*  public void ResetHeadBranch(SHA1 sha1) throws CommitException, ParseException, IOException {
        if (!getCommitMap().containsKey(sha1)) {
            throw new CommitException("SHA-1: " + sha1.getSh1() + " doesnt exist");
        } else if (getActiveBranch().getCommitSH1().equals(sha1)) {
            throw new CommitException("SHA-1: " + sha1.getSh1() + " already pointed by Head branch");
        } else {
            getActiveBranch().setCommitSH1(sha1);
            FileUtils.WriteToFile(sha1.getSh1(), GetLocation() + BRANCHES_FOLDER + getActiveBranch().getName() + ".txt");
            Commit commit = getCommitMap().get(getActiveBranch().getCommitSH1());
            DeleteWC(GetLocation());
            CheckOutRecursion(GetLocation(), commit.getMainFolderSH1());
        }
    }*/

}


