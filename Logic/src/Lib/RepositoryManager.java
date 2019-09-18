package Lib;

import MagitExceptions.*;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import resources.generated.*;
import sun.reflect.generics.visitor.Visitor;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.rmi.Remote;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class RepositoryManager {
    private static final String MAGIT_FOLDER = "\\.magit\\";
    private static final String OBJECTS_FOLDER = "\\.magit\\objects\\";
    private static final String BRANCHES_FOLDER = "\\.magit\\branches\\";
    private MagitRepository m_MagitRepository;
    private User m_User;
    private Repository m_currentRepository;


    public RepositoryManager() {
        this.m_User = new User("Administrator");
        m_MagitRepository = null;
    }

    public void CloneRepository(String localRepoLocation, String remoteRepoLocation, String name) throws RepositoryDoesnotExistException, IOException, RepositorySameToCurrentRepositoryException, ParseException {
        File rootFolderInRR = new File(remoteRepoLocation);
        File rootFolderInLR = new File(localRepoLocation);
        if (!rootFolderInRR.exists() || !rootFolderInRR.isDirectory() || !new File(remoteRepoLocation + "\\.magit").exists()) {
            throw new RepositoryDoesnotExistException("the given remote repository doesn't exist");
        }
        try {
            FileUtils.CopyDirectory(rootFolderInRR, rootFolderInLR);
        } catch (FileAlreadyExistsException e) {
            throw new FileAlreadyExistsException("file: " + e.getMessage() + " already exist");
        }
        File nameOfRRfILE = new File(rootFolderInRR.getPath() + MAGIT_FOLDER + "\\repository name.txt");
        String RRName = FileUtils.ReadContentFromFile(nameOfRRfILE);
        moveBranchesToInnerDirectory(RRName, rootFolderInLR);
        CreateHeadRTB(localRepoLocation, RRName);
        FileUtils.WriteToFile(name, rootFolderInLR.getPath() + MAGIT_FOLDER + "\\repository name.txt");
        ChangeRepository(localRepoLocation);
        m_currentRepository.setRRLocation(remoteRepoLocation);
        FileUtils.CreateTextFile(m_currentRepository.GetLocation() + MAGIT_FOLDER + "RRLocation.txt", remoteRepoLocation);
    }

    private void CreateHeadRTB(String LRLocation, String RRName) throws IOException {
        String activeBranchName = FileUtils.ReadContentFromFile(new File(LRLocation + BRANCHES_FOLDER + "HEAD.txt"));
        String content = FileUtils.ReadContentFromFile(new File(LRLocation + BRANCHES_FOLDER + RRName + "\\" + activeBranchName + ".txt"));
        FileUtils.CreateTextFile(LRLocation + BRANCHES_FOLDER + activeBranchName + ".txt", content);
    }

    private void moveBranchesToInnerDirectory(String rrName, File rootFolderInLR) throws IOException {
        File newDirectoryForBranches = new File(rootFolderInLR + BRANCHES_FOLDER + "\\" + rrName);
        if (newDirectoryForBranches.mkdir()) {
            File[] branches = new File(rootFolderInLR + BRANCHES_FOLDER).listFiles();
            if (branches != null) {
                for (File file : branches) {
                    if (!file.isDirectory()) {
                        if (!file.getName().equals("HEAD.txt")) {
                            Files.copy(Paths.get(file.getPath()), Paths.get(rootFolderInLR + BRANCHES_FOLDER + "\\" + rrName + "\\" + file.getName()));
                            if (!file.delete()) {
                                throw new IOException("error in clone progress");
                            }
                        }
                    }
                }
            }
        }
    }

    public void BonusInit(String name, String path, boolean isLoadedFromXML) throws IOException {

        if (new File(path).mkdirs()) {
            new File(path + MAGIT_FOLDER).mkdirs();
            new File(path + OBJECTS_FOLDER).mkdirs();
            new File(path + BRANCHES_FOLDER).mkdirs();
            FileUtils.CreateTextFile(path + BRANCHES_FOLDER + "HEAD.txt", "master");
            FileUtils.CreateTextFile(path + BRANCHES_FOLDER + "master.txt", "");
            FileUtils.CreateTextFile(path + MAGIT_FOLDER + "blobs.txt", "");
            FileUtils.CreateTextFile(path + MAGIT_FOLDER + "folders.txt", "");
            FileUtils.CreateTextFile(path + MAGIT_FOLDER + "commits.txt", "");
            FileUtils.CreateTextFile(path + MAGIT_FOLDER + "repository name.txt", name);
            m_currentRepository = new Repository(name, path);
            if (!isLoadedFromXML) {
                Branch b = new Branch();
                b.setName("master");
                m_currentRepository.setActiveBranch(b);
                m_currentRepository.getBranchesMap().put("master", m_currentRepository.getActiveBranch());
            }
        } else {
            throw new FileAlreadyExistsException("The directory: " + path + " already exists");
        }
    }

    public void MakeCommit(String message, Commit anotherPrevCommit) throws IOException, ParseException, RepositoryDoesnotExistException, CommitException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.MakeCommit(message, anotherPrevCommit, m_User);
    }


    public void DeleteBranch(String name) throws HeadBranchDeletedExcption, BranchDoesNotExistException, BranchFileDoesNotExistInFolderException, RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.DeleteBranch(name);
    }

    public void ChangeRepository(String path) throws RepositoryDoesnotExistException, IOException, ParseException, RepositorySameToCurrentRepositoryException {
        File file = new File(path + "\\.magit");
        if (!file.exists()) {
            throw new RepositoryDoesnotExistException("in path: " + path + "you dont have any repository");
        } else if (m_currentRepository != null && m_currentRepository.GetLocation().equals(path)) {
            throw new RepositorySameToCurrentRepositoryException("You are working on " + path + " already");
        } else {
            m_currentRepository = new Repository(path);
            m_currentRepository.LoadData();
        }
    }

    public void CreateNewBranch(String name, SHA1 commitSH1ToPoint) throws BranchNameIsAllreadyExistException, IOException, CommitException, RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.CreateNewBranch(name, commitSH1ToPoint);
    }

    public void CreateNewRemoteTrackingBranch(String name, RemoteBranch remoteBranch) throws RepositoryDoesnotExistException, CommitException, BranchNameIsAllreadyExistException, IOException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.CreateNewRemoteTrackingBranch(name, remoteBranch);
    }


    public void CheckOut(String name) throws BranchDoesNotExistException, IOException, ParseException, BranchIsAllReadyOnWCException, CheckoutToRemoteBranchException, RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.CheckOut(name,m_User);
    }

    public List<List<String>> ShowStatus() throws ParseException, CommitException, IOException, RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        return m_currentRepository.ShowStatus(m_User);
    }
    public List<CommitDetails> ShowActiveBranchHistory() throws CommitException, RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        return m_currentRepository.ShowActiveBranchHistory();
    }



    public User GetUser() {
        return m_User;
    }

    public void ChangeUser(User user) {
        SetUser(user);
    }

    private void SetUser(User user) {
        m_User = user;
    }

    public List<BranchDetails> ShowBranches() throws RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        return m_currentRepository.ShowBranches();
    }

    public void ResetHeadBranch(SHA1 sha1) throws CommitException, ParseException, IOException, RepositoryDoesnotExistException, OpenChangesException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.ResetHeadBranch(sha1,m_User);
    }

    public boolean HasOpenChanges() throws ParseException, CommitException, IOException {
        return m_currentRepository.HasOpenChanges(m_User);
    }

    public Repository GetCurrentRepository() {
        return m_currentRepository;
    }

    public List<String> CheckXml(String path) throws NoSuchMethodException, XMLException, IllegalAccessException, JAXBException, IOException, InvocationTargetException {
        XMLChecker.isXMLFile(path);
        XMLChecker.isFileExist(path);
        InputStream inputStream = new FileInputStream(path);
        m_MagitRepository = XMLChecker.deserializeFrom(inputStream);
        return XMLChecker.CheckXML(m_MagitRepository);

    }

    public boolean IsRepositoryExist(String path) {
        File f = new File(path + MAGIT_FOLDER);
        return f.exists();
    }

    private void changeMagitRepositoryToRepository(MagitRepository magitRepository) throws RepositoryAllreadyExistException, IOException, ParseException, BranchDoesNotExistException, BranchIsAllReadyOnWCException, CheckoutToRemoteBranchException, CommitException, OpenChangesException {
        FileUtils.deleteDirectory(magitRepository.getLocation());
        BonusInit(magitRepository.getName(), magitRepository.getLocation(), true);
        if (magitRepository.getMagitRemoteReference() != null && magitRepository.getMagitRemoteReference().getName() != null) {
            String rrLocation = magitRepository.getMagitRemoteReference().getLocation();
            FileUtils.CreateTextFile(m_currentRepository.GetLocation() + MAGIT_FOLDER + "RRLocation.txt", rrLocation);
            m_currentRepository.setRRLocation(rrLocation);

        }
        LoadObjectsFolder(magitRepository);

    }

    private void LoadBranches(MagitRepository magitRepository, Map<String, SHA1> commitIDToCommitSha1) throws IOException, BranchDoesNotExistException, ParseException, BranchIsAllReadyOnWCException, CheckoutToRemoteBranchException, CommitException, OpenChangesException {
        List<MagitSingleBranch> branchesList = magitRepository.getMagitBranches().getMagitSingleBranch();
        branchesList.stream().filter(v -> v.isIsRemote()).forEach(v -> {
            SHA1 sha1 = commitIDToCommitSha1.get(v.getPointedCommit().getId());
            RemoteBranch rb = new RemoteBranch(v.getName(), sha1);
            try {
                FileUtils.createFoldersByPathAndWriteContent(sha1.getSh1(), m_currentRepository.GetLocation() + BRANCHES_FOLDER + v.getName() + ".txt");
                m_currentRepository.getBranchesMap().put(v.getName(), rb);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        for (MagitSingleBranch msb : branchesList) {
            SHA1 sha1 = commitIDToCommitSha1.get(msb.getPointedCommit().getId());
            if (!m_currentRepository.getBranchesMap().containsKey(msb.getName())) {
                if (sha1 != null) {
                    Branch branch;
                    FileUtils.WriteToFile(sha1.getSh1(), m_currentRepository.GetLocation() + BRANCHES_FOLDER + msb.getName() + ".txt");
                    if (msb.isTracking()) {
                        branch = new RemoteTrackingBranch(msb.getName(), sha1, (RemoteBranch) m_currentRepository.getBranchesMap().get(msb.getTrackingAfter()));
                    } else {
                        branch = new Branch(msb.getName(), sha1);
                    }
                    m_currentRepository.getBranchesMap().put(branch.getName(), branch);
                }
            }
        }
        m_currentRepository.UpdateHeadFileContent(magitRepository.getMagitBranches().getHead());
        m_currentRepository.setActiveBranch(null);
        if (m_currentRepository.getBranchesMap().get(magitRepository.getMagitBranches().getHead()).getCommitSH1() != null) {
            m_currentRepository.CheckOut(magitRepository.getMagitBranches().getHead(),m_User);
        } else {
            Branch b = new Branch();
            b.setName("master");
            m_currentRepository.setActiveBranch(b);
            m_currentRepository.getBranchesMap().put("master", m_currentRepository.getActiveBranch());
        }
    }

    private void LoadObjectsFolder(MagitRepository magitRepository) throws IOException, ParseException, BranchDoesNotExistException, BranchIsAllReadyOnWCException, CheckoutToRemoteBranchException, CommitException, OpenChangesException {
        List<MagitSingleFolder> folderList = magitRepository.getMagitFolders().getMagitSingleFolder();
        List<MagitSingleFolder> rootFolders = folderList.stream().filter(MagitSingleFolder::isIsRoot).collect(Collectors.toList());
        Map<String, FileDetails> folderIDToFileDetails = new HashMap<>();
        Map<String, SHA1> commitIDToCommitSha1 = new HashMap<>();

        for (MagitSingleFolder msf : rootFolders) {
            FileDetails fd = CreateFileDetailsToMagitSingleFolderAndMagitBlob(msf, magitRepository);
            folderIDToFileDetails.put(msf.getId(), fd);
        }
        for (MagitSingleCommit msc : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            Commit commit = new Commit(msc.getMessage());
            commit.SetDetailsToCommitByGivenRootFolderAndUser(folderIDToFileDetails.get(msc.getRootFolder().getId()), new User(msc.getAuthor()));
            commit.SetCreateTime(new OurDate(msc.getDateOfCreation()));
            m_currentRepository.getCommitMap().put(commit.MakeSH1(), commit);
            FileUtils.AppendTextToFile(m_currentRepository.GetLocation() + MAGIT_FOLDER + "commits.txt", commit.MakeSH1().getSh1());
            commitIDToCommitSha1.put(msc.getId(), commit.MakeSH1());
        }
        for (MagitSingleCommit msc : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            if (msc.getPrecedingCommits() != null) {
                List<SHA1> parents = new ArrayList<>();
                for (PrecedingCommits.PrecedingCommit p : msc.getPrecedingCommits().getPrecedingCommit()) {
                    SHA1 father = commitIDToCommitSha1.get(p.getId());
                    parents.add(father);
                }

                SHA1 s = commitIDToCommitSha1.get(msc.getId());
                Commit son = m_currentRepository.getCommitMap().get(s);
                son.SetPrevCommits(parents);
            }
        }
        for (SHA1 sha1 : m_currentRepository.getCommitMap().keySet()) {
            Commit com = m_currentRepository.getCommitMap().get((sha1));
            File commitFile = FileUtils.CreateTextFile(m_currentRepository.GetLocation() + OBJECTS_FOLDER + com.MakeSH1() + ".txt", com.toString());
            FileUtils.CreateZipFile(commitFile, com.MakeSH1(), m_currentRepository.GetLocation() + OBJECTS_FOLDER);
            commitFile.delete();
        }
        LoadBranches(magitRepository, commitIDToCommitSha1);

    }


    private FileDetails CreateFileDetailsToMagitSingleFolderAndMagitBlob(MagitSingleFolder folder, MagitRepository magitRepository) throws IOException, ParseException {
        FileDetails fd;
        List<FileDetails> subFiles = new ArrayList<>();
        for (Item item : folder.getItems().getItem()) {
            if (item.getType().equals("blob")) {
                List<MagitBlob> blob = magitRepository.getMagitBlobs().getMagitBlob().stream().filter(v -> v.getId().equals(item.getId())).limit(1).collect(Collectors.toList());
                Blob b = LoadBlob(blob.get(0));
                fd = new FileDetails(blob.get(0).getName(), b.MakeSH1(), FileType.FILE, new User(blob.get(0).getLastUpdater()), new OurDate(blob.get(0).getLastUpdateDate()));
                subFiles.add(fd);
            } else {
                List<MagitSingleFolder> innerFolder = magitRepository.getMagitFolders().getMagitSingleFolder().stream().filter(v -> v.getId().equals(item.getId())).limit(1).collect(Collectors.toList());
                fd = CreateFileDetailsToMagitSingleFolderAndMagitBlob(innerFolder.get(0), magitRepository);
                subFiles.add(fd);
            }
        }
        Folder f = new Folder(subFiles);
        if (!m_currentRepository.getFoldersMap().containsKey(f.MakeSH1())) {
            m_currentRepository.getFoldersMap().put(f.MakeSH1(), f);
            FileUtils.CreateZipFile(f.toString(), folder.getName(), m_currentRepository.GetLocation() + OBJECTS_FOLDER);
            FileUtils.AppendTextToFile(m_currentRepository.GetLocation() + MAGIT_FOLDER + "folders.txt", f.MakeSH1().getSh1());
        }
        fd = new FileDetails(folder.getName(), f.MakeSH1(), FileType.FOLDER, new User(folder.getLastUpdater()), new OurDate(folder.getLastUpdateDate()));
        return fd;
    }


    private Blob LoadBlob(MagitBlob magitBlob) throws IOException {
        SHA1 sha1 = new SHA1();
        Blob b;
        String cont = magitBlob.getContent().replace("\r", "");
        sha1.MakeSH1FromContent(cont);
        if (!m_currentRepository.getBlobsMap().containsKey(sha1)) {
            FileUtils.CreateZipFile(cont, magitBlob.getName(), m_currentRepository.GetLocation() + OBJECTS_FOLDER);
            b = new Blob(cont);
            m_currentRepository.getBlobsMap().put(b.MakeSH1(), b);
            FileUtils.AppendTextToFile(m_currentRepository.GetLocation() + MAGIT_FOLDER + "blobs.txt", b.MakeSH1().getSh1());
        } else {
            b = m_currentRepository.getBlobsMap().get(sha1);
        }
        return b;
    }

    public void LoadXML() throws BranchIsAllReadyOnWCException, IOException, BranchDoesNotExistException, ParseException, RepositoryAllreadyExistException, CheckoutToRemoteBranchException, CommitException, OpenChangesException {
        changeMagitRepositoryToRepository(m_MagitRepository);
    }

    public List<SHA1> getCurrentRepositoryAllCommitsSHA1() throws RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        return m_currentRepository.getCurrentRepositoryAllCommitsSHA1();
    }

    public MagitRepository GetMagitRepository() {
        return m_MagitRepository;
    }

    public void IsCurrentRepositoryInitialize() throws RepositoryDoesnotExistException {
        if (m_currentRepository == null) {
            throw new RepositoryDoesnotExistException("Error: not initialize \"current repository\"");
        }
    }

    public void IsRepositoryHasAtLeastOneCommit() throws CommitException, RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.IsRepositoryHasAtLeastOneCommit();

    }


    public String getMainFolderName() throws RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        return m_currentRepository.getMainFolderName();
    }

    public List<MergeConfilct> MergeHeadBranchWithOtherBranch(String their) throws ParseException, IOException, OpenChangesException, BranchDoesNotExistException, FFException, CommitException, RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        return m_currentRepository.MergeHeadBranchWithOtherBranch(their,m_User);
    }


    public List<List<String>> compareTwoCommits(Commit current, Commit parent) throws CommitException, ParseException, RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        return m_currentRepository.compareTwoCommits(current, parent);
    }

    public void spanWCsolvedConflictList(List<MergeConfilct> confilctList) throws IOException, RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.spanWCsolvedConflictList(confilctList);
    }

    public void FetchRRNewData() throws RepositoryDoesnotExistException, RepositoryDoesntTrackAfterOtherRepositoryException, IOException, ParseException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.FetchRRNewData();
    }

    public void Pull() throws RepositoryDoesnotExistException, RepositoryDoesntTrackAfterOtherRepositoryException, ParseException, CommitException, IOException, OpenChangesException, BranchDoesNotExistException {
        IsCurrentRepositoryInitialize();
        m_currentRepository.Pull(m_User);
    }

    public void Push() throws CommitException, RepositoryDoesntTrackAfterOtherRepositoryException, IOException, ParseException, RemoteTrackingBranchException, RepositoryDoesnotExistException, OpenChangesException {
        IsRepositoryHasAtLeastOneCommit();
        m_currentRepository.Push(m_User);
    }
    public String GetCurrentRepositoryName() throws RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        return m_currentRepository.getName();
    }

    public String GetRRLocation() throws RepositoryDoesnotExistException {
        IsCurrentRepositoryInitialize();
        return m_currentRepository.getRRLocation();
    }
    public void pushLocalBranchToRemoteBranch(Branch b) throws IOException, CommitException, BranchNameIsAllreadyExistException, BranchFileDoesNotExistInFolderException, RepositoryDoesnotExistException, RepositoryDoesntTrackAfterOtherRepositoryException, ParseException, BranchDoesNotExistException, HeadBranchDeletedExcption {
        m_currentRepository.pushLocalBranchToRemoteBranch(b);
    }
}

/*  public void ExportRepositoryToXML(String path) throws XMLException, RepositoryDoesnotExistException {
        try {
            Path xmlPath = Paths.get(path);

        } catch (InvalidPathException e) {
            throw new XMLException("input is invalid path");
        }
        XMLChecker.isXMLFile(path);

        if (m_currentRepository != null) {
            MagitRepository magitRepository = new MagitRepository();
            magitRepository.setLocation(String.valueOf(Paths.get(m_currentRepository.GetLocation())));
            magitRepository.setName(m_currentRepository.getName());
            if (!m_currentRepository.getBranchesMap().isEmpty()) {
                exportMagitBranches(magitRepository);
            }
            if (!m_currentRepository.getCommitMap().isEmpty()) {
                exportMagitCommits(magitRepository);
            }
            XMLChecker.MagitRepositoryToXML(magitRepository, path);

        } else {
            throw new RepositoryDoesnotExistException("Repository wasn't been loaded");
        }

    }*/

/*private void exportMagitBranches(MagitRepository i_MagitRepository) {
        Map<String, Branch> branches = m_currentRepository.getBranchesMap();
        i_MagitRepository.setMagitBranches(new MagitBranches());
        List<MagitSingleBranch> magitBranches = i_MagitRepository.getMagitBranches().getMagitSingleBranch();

        for (Map.Entry<String, Branch> branchEntry : branches.entrySet()) {
            MagitSingleBranch magitBranch = new MagitSingleBranch();
            MagitSingleBranch.PointedCommit pointedCommit = new MagitSingleBranch.PointedCommit();
            try {
                pointedCommit.setId(branchEntry.getValue().getCommitSH1().getSh1());
            } catch (NullPointerException e) {
                pointedCommit.setId("null");
            }
            magitBranch.setPointedCommit(pointedCommit);
            magitBranch.setTrackingAfter(branchEntry.getValue().GetTrakingAfter());
            magitBranch.setTracking(branchEntry.getValue().IsTracking());
            magitBranch.setName(branchEntry.getValue().getName());
            magitBranch.setIsRemote(branchEntry.getValue().IsRemote());

            if (branchEntry.getValue() == m_currentRepository.getActiveBranch()) {
                i_MagitRepository.getMagitBranches().setHead(magitBranch.getName());
            }

            magitBranches.add(magitBranch);
        }
    }*/


/*   private void exportMagitCommits(MagitRepository i_MagitRepository) {
        Map<SHA1, Commit> commits = m_currentRepository.getCommitMap();

        i_MagitRepository.setMagitCommits(new MagitCommits());
        i_MagitRepository.setMagitFolders(new MagitFolders());
        i_MagitRepository.setMagitBlobs(new MagitBlobs());
        Set<String> sha1TrackerSet = new HashSet<>();

        List<MagitSingleCommit> magitCommits = i_MagitRepository.getMagitCommits().getMagitSingleCommit();

        for (Map.Entry<SHA1, Commit> commitEntry : commits.entrySet()) {
            if (commitEntry.getKey().getSh1().length() == 40) {
                MagitSingleCommit magitCommit = new MagitSingleCommit();
                PrecedingCommits precedingCommits = new PrecedingCommits();
                List<PrecedingCommits.PrecedingCommit> magitPrecedingCommits = precedingCommits.getPrecedingCommit();

                for (SHA1 precedingCommitSha1 : commitEntry.getValue().getPrevCommits()) {
                    PrecedingCommits.PrecedingCommit magitPrecedingCommit = new PrecedingCommits.PrecedingCommit();
                    magitPrecedingCommit.setId(precedingCommitSha1.getSh1());
                    magitPrecedingCommits.add(magitPrecedingCommit);
                }

                RootFolder magitRootFolder = new RootFolder();
                magitRootFolder.setId(commitEntry.getValue().getMainFolderSH1().getSh1());
                magitCommit.setRootFolder(magitRootFolder);

                magitCommit.setPrecedingCommits(precedingCommits);
                magitCommit.setMessage(commitEntry.getValue().getMessage());
                magitCommit.setId(commitEntry.getKey().getSh1());
                magitCommit.setDateOfCreation(commitEntry.getValue().getWhoUpdated().getName());
                magitCommit.setAuthor(commitEntry.getValue().getCreateTime().toString());

                magitCommits.add(magitCommit);


                MagitSingleFolder magitFolder = new MagitSingleFolder();
                magitFolder.setName(null);
                magitFolder.setLastUpdater(commitEntry.getValue().getWhoUpdated().getName());
                magitFolder.setLastUpdateDate(commitEntry.getValue().getCreateTime().toString());
                magitFolder.setIsRoot(true);
                magitFolder.setId(commitEntry.getValue().getMainFolderSH1().getSh1());

                exportMagitFolders(i_MagitRepository, magitFolder, sha1TrackerSet);
            }
        }
    }
*/
 /*   private void exportMagitFolders(MagitRepository i_MagitRepository, MagitSingleFolder i_MagitFolder, Set<String> i_Sha1TrackerSet) {
        List<MagitSingleFolder> magitFolders = i_MagitRepository.getMagitFolders().getMagitSingleFolder();
        magitFolders.add(i_MagitFolder);
        i_Sha1TrackerSet.add(i_MagitFolder.getId());

        MagitSingleFolder.Items items = new MagitSingleFolder.Items();
        List<Item> itemsList = items.getItem();
        i_MagitFolder.setItems(items);

        Folder folder = m_currentRepository.getFoldersMap().get(new SHA1(i_MagitFolder.getId()));

        for (FileDetails itemData : folder.getInnerFiles()) {
            Item item = new Item();
            item.setType(itemData.getFileType() == FileType.FILE ? "blob" : itemData.getFileType().toString().toLowerCase());
            item.setId(itemData.getSh1().getSh1());
            itemsList.add(item);

            if (!i_Sha1TrackerSet.contains(itemData.getSh1().getSh1())) {
                if (itemData.getFileType().equals(FileType.FOLDER)) {
                    MagitSingleFolder magitSubFolder = new MagitSingleFolder();
                    magitSubFolder.setId(itemData.getSh1().getSh1());
                    magitSubFolder.setIsRoot(false);
                    magitSubFolder.setLastUpdateDate(itemData.getLastUpdated().toString());
                    magitSubFolder.setLastUpdater(itemData.getWhoUpdatedLast().getName());
                    magitSubFolder.setName(itemData.getName());

                    exportMagitFolders(i_MagitRepository, magitSubFolder, i_Sha1TrackerSet);
                } else {
                    MagitBlob magitBlob = new MagitBlob();
                    magitBlob.setName(itemData.getName());
                    magitBlob.setLastUpdater(itemData.getWhoUpdatedLast().getName());
                    magitBlob.setLastUpdateDate(itemData.getLastUpdated().toString());
                    magitBlob.setId(itemData.getSh1().getSh1());

                    exportMagitBlob(i_MagitRepository, magitBlob, i_Sha1TrackerSet);
                }
            }
        }
    }
*/
 /*   private void exportMagitBlob(MagitRepository i_MagitRepository, MagitBlob i_MagitBlob, Set<String> i_Sha1TrackerSet) {
        List<MagitBlob> magitBlobs = i_MagitRepository.getMagitBlobs().getMagitBlob();
        String blobContent = m_currentRepository.getBlobsMap().get(new SHA1(i_MagitBlob.getId())).getContent();
        i_MagitBlob.setContent(blobContent);
        magitBlobs.add(i_MagitBlob);
        i_Sha1TrackerSet.add(i_MagitBlob.getId());
    }
    */