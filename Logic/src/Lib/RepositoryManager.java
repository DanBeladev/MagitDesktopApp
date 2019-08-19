package Lib;
import Lib.Blob;
import MagitExceptions.*;
import resources.generated.*;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public void InitRepository(String name, String path) throws RepositoryAllreadyExistException, IOException {
//
        if (new File(path + MAGIT_FOLDER).mkdirs()) {
            new File(path + OBJECTS_FOLDER).mkdirs();
            new File(path + BRANCHES_FOLDER).mkdirs();
            FileUtils.CreateTextFile(path + BRANCHES_FOLDER + "HEAD.txt", "");
            FileUtils.CreateTextFile(path + MAGIT_FOLDER + "blobs.txt", "");
            FileUtils.CreateTextFile(path + MAGIT_FOLDER + "folders.txt", "");
            FileUtils.CreateTextFile(path + MAGIT_FOLDER + "commits.txt", "");
            FileUtils.CreateTextFile(path + MAGIT_FOLDER + "repository name.txt", name);
            m_currentRepository = new Repository(name, path);
        } else {
            throw new RepositoryAllreadyExistException("the repository in path: " + path + " allready exist");
        }

    }

    public void BonusInit(String name, String path) throws IOException {

        if (new File(path).mkdirs()){
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
            Branch b=new Branch();
            b.setName("master");
            m_currentRepository.setActiveBranch(b);
            m_currentRepository.getBranchesMap().put("master", m_currentRepository.getActiveBranch());
        } else {
            throw  new FileAlreadyExistsException("The directory: " + path + " already exists");
        }
    }

    public void MakeCommit(String message) throws IOException, ParseException, RepositoryDoesnotExistException, CommitException {
        if (m_currentRepository == null) {
            throw new RepositoryDoesnotExistException("You have to initialize repository before making commit");
        }
        if(!m_currentRepository.getCommitMap().isEmpty() && !HasOpenChanges()){
            throw new CommitException("You have no changes in yours WC compared to your previous commit ");
        }
        Commit newCommit = new Commit(message);
        FileDetails rootFolderDetails = MoveWCtoObjectFolder(m_currentRepository.GetLocation(), 0);

        newCommit.SetDetailsToCommitByGivenRootFolderAndUser(rootFolderDetails, m_User);
        if (m_currentRepository.getCommitMap().isEmpty()) {
            m_currentRepository.getActiveBranch().setCommitSH1(newCommit.MakeSH1());
            FileUtils.WriteToFile(newCommit.MakeSH1().getSh1(),m_currentRepository.GetLocation()+BRANCHES_FOLDER+"master.txt");
            m_currentRepository.getActiveBranch().CreateBranchTextFile(m_currentRepository.GetLocation() + BRANCHES_FOLDER);
        } else {
            newCommit.AddPrevCommit(m_currentRepository.getActiveBranch().getCommitSH1());
            m_currentRepository.getActiveBranch().UpdateSHA1AndBranchFileContent(newCommit.MakeSH1(), m_currentRepository.GetLocation() + BRANCHES_FOLDER + m_currentRepository.getActiveBranch().getName() + ".txt");
        }
        File commitFile = FileUtils.CreateTextFile(m_currentRepository.GetLocation() + OBJECTS_FOLDER + newCommit.MakeSH1() + ".txt", newCommit.toString());
        FileUtils.CreateZipFile(commitFile, newCommit.MakeSH1(), m_currentRepository.GetLocation() + OBJECTS_FOLDER);
        FileUtils.AppendTextToFile(m_currentRepository.GetLocation() + MAGIT_FOLDER + "commits.txt", newCommit.MakeSH1().getSh1());
        commitFile.delete();
        m_currentRepository.getCommitMap().put(newCommit.MakeSH1(), newCommit);

    }

    private void UpdateHeadFileContent(String name) throws IOException {
        FileUtils.WriteToFile(name, m_currentRepository.GetLocation() + BRANCHES_FOLDER + "HEAD.txt");
    }


    public List<List<String>> ShowStatus() throws IOException, ParseException, CommitException {
        if (m_currentRepository.getCommitMap().isEmpty()) {
            throw new CommitException("You have to do at least one commit before Show Status method");
        }
        if (!m_currentRepository.getChangedList().isEmpty()) {
            m_currentRepository.getChangedList().clear();
        }
        if (!m_currentRepository.getAddedList().isEmpty()) {
            m_currentRepository.getAddedList().clear();
        }
        if (!m_currentRepository.getDeletedList().isEmpty()) {
            m_currentRepository.getDeletedList().clear();
        }

        List<List<String>> listOfLists = new ArrayList<>();
        GetAllCurrentCommitsFiles().stream().forEach(v -> m_currentRepository.getDeletedList().add(v));
        MoveWCtoObjectFolder(m_currentRepository.GetLocation(), 1);
        if(!m_currentRepository.getChangedList().isEmpty() || !m_currentRepository.getDeletedList().isEmpty() || !m_currentRepository.getAddedList().isEmpty()){
            m_currentRepository.getChangedList().add(m_currentRepository.GetLocation());
        }
        listOfLists.add(m_currentRepository.getAddedList());
        listOfLists.add(m_currentRepository.getChangedList());
        listOfLists.add(m_currentRepository.getDeletedList());
        return listOfLists;


    }

    public boolean HasOpenChanges() throws IOException, ParseException, CommitException {
        boolean hasChanges = false;
        List<List<String>> openChangesList = ShowStatus();
        for (List<String> list : openChangesList) {
            if (!list.isEmpty()) {
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    private FileDetails MoveWCtoObjectFolder(String path, int ver) throws IOException, ParseException {
        List<FileDetails> subFiles = new ArrayList<>();
        FileDetails fileDetails;
        File file = new File(path);
        File[] filesInFile = file.listFiles();
        for (File f : filesInFile) {
            if (!f.getName().equals(".magit")) {
                if (f.isDirectory()) {
                    fileDetails = MoveWCtoObjectFolder(path + "\\" + f.getName(), ver);
                    subFiles.add(fileDetails);

                } else {
                    String content = FileUtils.ReadContentFromFile(f);
                    SHA1 Sh1 = new SHA1();
                    Sh1.MakeSH1FromContent(content);
                    fileDetails = new FileDetails();
                    if (!m_currentRepository.getBlobsMap().containsKey(Sh1) && ver == 0) {
                        Blob b = new Blob(content);
                        PutInMapNewMagitFileAndAddToObjectFolderAndInitFileDetails(b, f, fileDetails);
                    } else {
                        if (!m_currentRepository.getCommitMap().isEmpty()) {
                            fileDetails = CheckInCommit(path + "\\" + f.getName(), Sh1, ver);
                        }
                        if (fileDetails == null || m_currentRepository.getCommitMap().isEmpty()) {
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
        if (!m_currentRepository.getFoldersMap().containsKey(folderSh1) && ver == 0) {
            PutInMapNewMagitFileAndAddToObjectFolderAndInitFileDetails(folder, file, fileDetails);
        } else {
            if (!m_currentRepository.getCommitMap().isEmpty()) {
                fileDetails = CheckInCommit(path, folderSh1, ver);
            }
            if (fileDetails == null || m_currentRepository.getCommitMap().isEmpty()) {
                fileDetails = new FileDetails();
                fileDetails.SetDetails(file.getName(), folderSh1, FileType.FOLDER, m_User);
            }

        }

        return fileDetails;
    }

    private void PutInMapNewMagitFileAndAddToObjectFolderAndInitFileDetails(MagitFile mf, File file, FileDetails fileDetails) throws IOException {
        File tmpfile;
        if (mf.GetMagitFileType() == FileType.FOLDER) {
            FileUtils.AppendTextToFile(m_currentRepository.GetLocation() + MAGIT_FOLDER + "folders.txt", mf.MakeSH1().getSh1());
            m_currentRepository.getFoldersMap().put(mf.MakeSH1(), (Folder) mf);
            tmpfile = ((Folder) mf).CreateTextFileRepresentFolder(m_currentRepository.GetLocation() +
                    OBJECTS_FOLDER + file.getName() + ".txt");
        } else {
            FileUtils.AppendTextToFile(m_currentRepository.GetLocation() + MAGIT_FOLDER + "blobs.txt", mf.MakeSH1().getSh1());
            m_currentRepository.getBlobsMap().put(mf.MakeSH1(), (Blob) mf);
            tmpfile = new File(m_currentRepository.GetLocation() + OBJECTS_FOLDER + mf.MakeSH1().getSh1() + ".txt");
            FileUtils.WriteToFile((((Blob) mf).getContent()), m_currentRepository.GetLocation() + OBJECTS_FOLDER + mf.MakeSH1().getSh1() + ".txt");
        }
        FileUtils.CreateZipFile(tmpfile, mf.MakeSH1(), m_currentRepository.GetLocation() + OBJECTS_FOLDER);
        tmpfile.delete();
        fileDetails.SetDetails(file.getName(), mf.MakeSH1(), mf.GetMagitFileType(), m_User);
    }

    private FileDetails CheckInCommit(String path, SHA1 sha1, int ver) throws IOException, ParseException {
        String innerPath = m_currentRepository.GetLocation();
        if (!path.equals(innerPath)) {
            innerPath = path.substring(m_currentRepository.GetLocation().length() + 1);
        } else {
            String[] tmp = m_currentRepository.GetLocation().split("\\\\");
            innerPath = tmp[tmp.length - 1];
            if (ver == 1) {
                return null;
            }
        }
        String[] array = innerPath.split("\\\\");
        List<FileDetails> rootFolderDetails = GetRootFolderFileDetailsFromCurrentCommit();
        return CheckInCommitRecursion(array, rootFolderDetails, sha1/*, ending*/, ver, path);
    }

    private FileDetails CheckInCommitRecursion(String[] innerPath, List<FileDetails> folderDetails, SHA1 sh1, int ver, String fullPath) throws IOException, ParseException {
        m_currentRepository.getDeletedList().removeIf(v -> v.equals(fullPath));
        String tmp = innerPath[0];
        folderDetails = folderDetails.stream().filter((value) -> value.getName().equals(tmp)).collect(Collectors.toList());
        if (innerPath.length == 1 || (folderDetails.isEmpty())) {
            if (folderDetails.isEmpty() || !folderDetails.get(0).getSh1().getSh1().equals(sh1.getSh1())) {
                if (ver == 1) {
                    if (folderDetails.isEmpty()) {
                        m_currentRepository.getAddedList().add(fullPath);
                    } else {
                        m_currentRepository.getChangedList().add(fullPath);
                    }
                }
                return null;
            } else {
                return folderDetails.get(0);
            }
        }
        innerPath = Arrays.copyOfRange(innerPath, 1, innerPath.length);
        String content = FileUtils.getContentFromZippedFile(m_currentRepository.GetLocation() + OBJECTS_FOLDER + folderDetails.get(0).getSh1().getSh1() + ".zip");
        folderDetails = FileUtils.ParseFolderTextFileToFileDetailsList(content);
        return CheckInCommitRecursion(innerPath, folderDetails, sh1, ver, fullPath);
    }

    private List<FileDetails> GetRootFolderFileDetailsFromCurrentCommit() throws IOException, ParseException {
        File file = GetCommitOfHeadBranch();
        String content = FileUtils.ReadContentFromFile(file);
        content = content.substring(0, 40);
        String commitFolderContent = FileUtils.getContentFromZippedFile(m_currentRepository.GetLocation() + OBJECTS_FOLDER + content + ".zip");
        String RootFolderContent = FileUtils.getContentFromZippedFile(m_currentRepository.GetLocation() + OBJECTS_FOLDER + commitFolderContent.substring(0, 40) + ".zip");
        return FileUtils.ParseFolderTextFileToFileDetailsList(RootFolderContent);
    }

    public void DeleteBranch(String name) throws HeadBranchDeletedExcption, BranchDoesNotExistException, BranchFileDoesNotExistInFolderException {
        if (m_currentRepository.getActiveBranch().getName().equals(name)) {
            throw new HeadBranchDeletedExcption("The branch: " + name + " is Head branch, you can't delete it");
        } else if (!m_currentRepository.getBranchesMap().containsKey(name)) {
            throw new BranchDoesNotExistException("The branch: " + name + " does not exist");
        } else {
            m_currentRepository.getBranchesMap().remove(name);
            File BranchFile = new File(m_currentRepository.GetLocation() + BRANCHES_FOLDER + name + ".txt");
            if (!BranchFile.delete()) {
                throw new BranchFileDoesNotExistInFolderException("the file:" + name + ".txt did'nt found");
            }
        }
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

    public void CreateNewBranch(String name) throws BranchNameIsAllreadyExistException, IOException, CommitException {
        if (m_currentRepository.getCommitMap().isEmpty()) {
            throw new CommitException("you don't have any commits so you can't make new branch");
        }
        if (m_currentRepository.getBranchesMap().containsKey(name)) {
            throw new BranchNameIsAllreadyExistException("The name: " + name + " is already exist");
        } else {
            File file = GetCommitOfHeadBranch();
            Branch newBranch = new Branch(name, new SHA1(FileUtils.ReadContentFromFile(file)));
            newBranch.CreateBranchTextFile(m_currentRepository.GetLocation() + BRANCHES_FOLDER);
            m_currentRepository.getBranchesMap().put(name, newBranch);
        }
    }

    private File GetCommitOfHeadBranch() throws IOException {
        File file = new File(m_currentRepository.GetLocation() + BRANCHES_FOLDER + "HEAD.txt");
        if (!file.exists()) {
            throw new FileNotFoundException("HEAD file is not exist");
        } else {
            String fileName = FileUtils.ReadContentFromFile(file);
            file = new File(m_currentRepository.GetLocation() + BRANCHES_FOLDER + fileName + ".txt");
        }
        return file;
    }

    public void CheckOut(String name) throws BranchDoesNotExistException, IOException, ParseException, BranchIsAllReadyOnWCException {
        Branch branch;
        if (!m_currentRepository.getBranchesMap().containsKey(name)) {
            throw new BranchDoesNotExistException("The branch: " + name + " does'nt exist");
        } else if (m_currentRepository.getActiveBranch() != null && m_currentRepository.getActiveBranch().getName().equals(name)) {
            throw new BranchIsAllReadyOnWCException("The Branch: " + name + " is already Head branch");
        } else {
            branch = m_currentRepository.getBranchesMap().get(name);
            m_currentRepository.setActiveBranch(branch);
            SHA1 commitSH1 = branch.getCommitSH1();
            SHA1 mainFolderSH1 = m_currentRepository.getCommitMap().get(commitSH1).getMainFolderSH1();
            UpdateHeadFileContent(branch.getName());
            DeleteWC();
            CheckOutRecursion(m_currentRepository.GetLocation(), mainFolderSH1);
        }
    }


    private void DeleteWC() {
        File file = new File(m_currentRepository.GetLocation());
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

    private void CheckOutRecursion(String path, SHA1 mainFolderSH1) throws IOException, ParseException {
        String content = FileUtils.getContentFromZippedFile(m_currentRepository.GetLocation() + OBJECTS_FOLDER + mainFolderSH1.getSh1() + ".zip");
        List<FileDetails> fileDetailsList = FileUtils.ParseFolderTextFileToFileDetailsList(content);
        for (FileDetails fileDetails : fileDetailsList) {
            if (fileDetails.getFileType() == FileType.FILE) {
                FileUtils.UnzipFile(m_currentRepository.GetLocation() + OBJECTS_FOLDER + fileDetails.getSh1() + ".zip", path, fileDetails.getName());
            } else {
                boolean isCreated = new File(path + "\\" + fileDetails.getName()).mkdirs();
                if (isCreated) {
                    CheckOutRecursion(path + "\\" + fileDetails.getName(), fileDetails.getSh1());
                }

            }
        }
    }

    public List<CommitDetails> ShowActiveBranchHistory() throws CommitException {
        IsRepositoryHasAtLeastOneCommit();
        List<CommitDetails> commitDetailsList = new LinkedList<>();
        Branch activeBranch = new Branch(m_currentRepository.getActiveBranch());
        CommitDetails details;
        if (!m_currentRepository.getCommitMap().isEmpty()) {
            while (activeBranch.getCommitSH1() != null) {
                details = new CommitDetails(m_currentRepository.getCommitMap().get(activeBranch.getCommitSH1()));
                commitDetailsList.add(details);
                if (!m_currentRepository.getCommitMap().get(activeBranch.getCommitSH1()).getPrevCommits().isEmpty()) {
                    activeBranch.setCommitSH1(m_currentRepository.getCommitMap().get(activeBranch.getCommitSH1()).getPrevCommits().get(0));
                } else {
                    activeBranch.setCommitSH1(null);
                }
            }
        }
        return commitDetailsList;
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

    public List<BranchDetails> ShowBranches() {
        List<BranchDetails> list = new ArrayList<>();
        m_currentRepository.getBranchesMap().entrySet().stream().forEach((value) -> {
            list.add(new BranchDetails(value.getValue(), m_currentRepository.getCommitMap().get(value.getValue().getCommitSH1()), m_currentRepository.getActiveBranch() == value.getValue()));
        });
        return list;
    }

    public List<FileDetails> ShowAllCommitFiles() throws ParseException {
        Commit commit = m_currentRepository.getCommitMap().get(m_currentRepository.getActiveBranch().getCommitSH1());
        Folder folder = m_currentRepository.getFoldersMap().get(commit.getMainFolderSH1());
        String folderPathName = m_currentRepository.GetLocation();

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
                folder = m_currentRepository.getFoldersMap().get(fd.getSh1());
                showALLCommitFilesRecursion(commitFiles, folder, path);
            }
            fdToReturn.setName(path);
            commitFiles.add(fdToReturn);
        }
    }

    public List<FileDetails> ResetHeadBranch(SHA1 sha1) throws CommitException, ParseException, IOException {
        if (!m_currentRepository.getCommitMap().containsKey(sha1)) {
            throw new CommitException("SHA-1: " + sha1.getSh1() + " doesnt exist");
        } else {
            m_currentRepository.getActiveBranch().setCommitSH1(sha1);
            FileUtils.WriteToFile(sha1.getSh1(),m_currentRepository.GetLocation()+BRANCHES_FOLDER+m_currentRepository.getActiveBranch().getName()+".txt");
            Commit commit= m_currentRepository.getCommitMap().get(m_currentRepository.getActiveBranch().getCommitSH1());
            DeleteWC();
            CheckOutRecursion(m_currentRepository.GetLocation(),commit.getMainFolderSH1());
            return ShowAllCommitFiles();
        }
    }

    public Repository GetCurrentRepository() {
        return m_currentRepository;
    }

    private List<String> GetAllCurrentCommitsFiles() throws IOException, ParseException {
        List<String> filesInCommit = new ArrayList<>();
        List<FileDetails> rootFolderDetails = GetRootFolderFileDetailsFromCurrentCommit();
        String path = m_currentRepository.GetLocation();
        GetAllCurrentCommitsFilesRec(rootFolderDetails, filesInCommit, path);
        return filesInCommit;

    }

    private void GetAllCurrentCommitsFilesRec(List<FileDetails> folderDetails, List<String> FilesInCommit, String path) {
        folderDetails.stream().forEach((val) -> {
            String innerPath = path + "\\" + val.getName();
            FilesInCommit.add(innerPath);
            if (val.getFileType() == FileType.FOLDER) {
                try {
                    String data = FileUtils.getContentFromZippedFile(m_currentRepository.GetLocation() + OBJECTS_FOLDER + val.getSh1() + ".zip");
                    List<FileDetails> innerFolder = FileUtils.ParseFolderTextFileToFileDetailsList(data);
                    GetAllCurrentCommitsFilesRec(innerFolder, FilesInCommit, innerPath);
                } catch (IOException | ParseException e) {
                    e.getMessage();
                }
            }
        });
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

    private void changeMagitRepositoryToRepository(MagitRepository magitRepository) throws RepositoryAllreadyExistException, IOException, ParseException, BranchDoesNotExistException, BranchIsAllReadyOnWCException {
        FileUtils.deleteDirectory(magitRepository.getLocation());
        BonusInit(magitRepository.getName(),magitRepository.getLocation());
        LoadObjectsFolder(magitRepository);

    }

    private void LoadBranches(MagitRepository magitRepository, Map<String, SHA1> commitIDToCommitSha1) throws IOException, BranchDoesNotExistException, ParseException, BranchIsAllReadyOnWCException {
        List<MagitSingleBranch> branchesList = magitRepository.getMagitBranches().getMagitSingleBranch();
        for (MagitSingleBranch msb : branchesList) {
            File branchFile = new File(m_currentRepository.GetLocation() + BRANCHES_FOLDER + msb.getName() + ".txt");
            branchFile.getPath();
            SHA1 sha1 = commitIDToCommitSha1.get(msb.getPointedCommit().getId());
            if(sha1!=null) {
                FileUtils.WriteToFile(sha1.getSh1(), m_currentRepository.GetLocation() + BRANCHES_FOLDER + msb.getName() + ".txt");
                Branch branch = new Branch(msb.getName(), sha1);
                m_currentRepository.getBranchesMap().put(branch.getName(), branch);
            }
        }
        UpdateHeadFileContent(magitRepository.getMagitBranches().getHead());
        m_currentRepository.setActiveBranch(null);
        if(m_currentRepository.getBranchesMap().get(magitRepository.getMagitBranches().getHead()).getCommitSH1()!=null) {
            CheckOut(magitRepository.getMagitBranches().getHead());
        }
        else{
            Branch b=new Branch();
            b.setName("master");
            m_currentRepository.setActiveBranch(b);
            m_currentRepository.getBranchesMap().put("master", m_currentRepository.getActiveBranch());
        }
    }

    private void LoadObjectsFolder(MagitRepository magitRepository) throws IOException, ParseException, BranchDoesNotExistException, BranchIsAllReadyOnWCException {
        List<MagitSingleFolder> folderList = magitRepository.getMagitFolders().getMagitSingleFolder();
        List<MagitSingleFolder> rootFolders = folderList.stream().filter(v -> v.isIsRoot()).collect(Collectors.toList());
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
        if(!m_currentRepository.getFoldersMap().containsKey(f.MakeSH1())) {
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
        sha1.MakeSH1FromContent(magitBlob.getContent());
        if (!m_currentRepository.getBlobsMap().containsKey(sha1)){
            FileUtils.CreateZipFile(magitBlob.getContent(), magitBlob.getName(), m_currentRepository.GetLocation() + OBJECTS_FOLDER);
            b = new Blob(magitBlob.getContent());
            m_currentRepository.getBlobsMap().put(b.MakeSH1(), b);
            FileUtils.AppendTextToFile(m_currentRepository.GetLocation() + MAGIT_FOLDER + "blobs.txt", b.MakeSH1().getSh1());
        } else {
            b = m_currentRepository.getBlobsMap().get(sha1);
        }
        return b;
    }

    public void LoadXML() throws BranchIsAllReadyOnWCException, IOException, BranchDoesNotExistException, ParseException, RepositoryAllreadyExistException {
        changeMagitRepositoryToRepository(m_MagitRepository);
    }

    public MagitRepository GetMagitRepository() {
        return m_MagitRepository;
    }

    public void IsCurrentRepositoryInitialize() throws RepositoryDoesnotExistException {
        if (m_currentRepository == null) {
            throw new RepositoryDoesnotExistException("Error, not initialize \"current repository\"");
        }
    }

    public void IsRepositoryHasAtLeastOneCommit() throws CommitException {
        try {
            IsCurrentRepositoryInitialize();
            if (m_currentRepository.getCommitMap().isEmpty()) {
                throw new CommitException("Error, you don't have any commits in your current repository");
            }
        } catch (RepositoryDoesnotExistException e) {
            throw new CommitException("Error, not initialize repository.");
        }

    }

    public void ExportRepositoryToXML(String path) throws XMLException, RepositoryDoesnotExistException {
        try{
            Path xmlPath = Paths.get(path); 
            
        }catch(InvalidPathException e){
            throw new XMLException("input is invalid path");
        }
       XMLChecker.isXMLFile(path);

       if(m_currentRepository!=null){
           MagitRepository magitRepository = new MagitRepository();
           magitRepository.setLocation(String.valueOf(Paths.get(m_currentRepository.GetLocation())));
           magitRepository.setName(m_currentRepository.getName());
           if(!m_currentRepository.getBranchesMap().isEmpty()){
               exportMagitBranches(magitRepository);
           }
           if(!m_currentRepository.getCommitMap().isEmpty()){
               exportMagitCommits(magitRepository);
           }
          XMLChecker.MagitRepositoryToXML(magitRepository,path);
           
       }else {
           throw new RepositoryDoesnotExistException("Repository wasn't been loaded");
       }
        
    }

    private void exportMagitCommits(MagitRepository i_MagitRepository) {
        Map<SHA1, Commit> commits = m_currentRepository.getCommitMap();

        i_MagitRepository.setMagitCommits(new MagitCommits());
        i_MagitRepository.setMagitFolders(new MagitFolders());
        i_MagitRepository.setMagitBlobs(new MagitBlobs());
        Set<String> sha1TrackerSet = new HashSet<>();

        List<MagitSingleCommit> magitCommits = i_MagitRepository.getMagitCommits().getMagitSingleCommit();

        for(Map.Entry<SHA1, Commit> commitEntry: commits.entrySet()) {
            if(commitEntry.getKey().getSh1().length() == 40) {
                MagitSingleCommit magitCommit = new MagitSingleCommit();
                PrecedingCommits precedingCommits = new PrecedingCommits();
                List<PrecedingCommits.PrecedingCommit> magitPrecedingCommits = precedingCommits.getPrecedingCommit();

                for(SHA1 precedingCommitSha1: commitEntry.getValue().getPrevCommits()) {
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

    private void exportMagitFolders(MagitRepository i_MagitRepository, MagitSingleFolder i_MagitFolder, Set<String> i_Sha1TrackerSet) {
        List<MagitSingleFolder> magitFolders = i_MagitRepository.getMagitFolders().getMagitSingleFolder();
        magitFolders.add(i_MagitFolder);
        i_Sha1TrackerSet.add(i_MagitFolder.getId());

        MagitSingleFolder.Items items = new MagitSingleFolder.Items();
        List<Item> itemsList = items.getItem();
        i_MagitFolder.setItems(items);

        Folder folder = m_currentRepository.getFoldersMap().get(new SHA1(i_MagitFolder.getId()));

        for(FileDetails itemData: folder.getInnerFiles()) {
            Item item = new Item();
            item.setType(itemData.getFileType()==FileType.FILE? "blob" : itemData.getFileType().toString().toLowerCase());
            item.setId(itemData.getSh1().getSh1());
            itemsList.add(item);

            if(!i_Sha1TrackerSet.contains(itemData.getSh1().getSh1())) {
                if (itemData.getFileType().equals(FileType.FOLDER)) {
                    MagitSingleFolder magitSubFolder = new MagitSingleFolder();
                    magitSubFolder.setId(itemData.getSh1().getSh1());
                    magitSubFolder.setIsRoot(false);
                    magitSubFolder.setLastUpdateDate(itemData.getLastUpdated().toString());
                    magitSubFolder.setLastUpdater(itemData.getWhoUpdatedLast().getName());
                    magitSubFolder.setName(itemData.getName());

                    exportMagitFolders(i_MagitRepository, magitSubFolder, i_Sha1TrackerSet);
                }
                else {
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

    private void exportMagitBlob(MagitRepository i_MagitRepository, MagitBlob i_MagitBlob, Set<String> i_Sha1TrackerSet) {
        List<MagitBlob> magitBlobs = i_MagitRepository.getMagitBlobs().getMagitBlob();
        String blobContent = m_currentRepository.getBlobsMap().get(new SHA1(i_MagitBlob.getId())).getContent();
        i_MagitBlob.setContent(blobContent);
        magitBlobs.add(i_MagitBlob);
        i_Sha1TrackerSet.add(i_MagitBlob.getId());
    }

    private void exportMagitBranches(MagitRepository i_MagitRepository) {
        Map<String, Branch> branches = m_currentRepository.getBranchesMap();
        i_MagitRepository.setMagitBranches(new MagitBranches());
        List<MagitSingleBranch> magitBranches = i_MagitRepository.getMagitBranches().getMagitSingleBranch();

        for(Map.Entry<String, Branch> branchEntry: branches.entrySet()) {
            MagitSingleBranch magitBranch = new MagitSingleBranch();
            MagitSingleBranch.PointedCommit pointedCommit = new MagitSingleBranch.PointedCommit();
            try {
                pointedCommit.setId(branchEntry.getValue().getCommitSH1().getSh1());
            }
            catch (NullPointerException e){
                pointedCommit.setId("null");
            }
            magitBranch.setPointedCommit(pointedCommit);
            magitBranch.setTrackingAfter(branchEntry.getValue().GetTrakingAfter());
            magitBranch.setTracking(branchEntry.getValue().IsTracking());
            magitBranch.setName(branchEntry.getValue().getName());
            magitBranch.setIsRemote(branchEntry.getValue().IsRemote());

            if(branchEntry.getValue()==m_currentRepository.getActiveBranch()) {
                i_MagitRepository.getMagitBranches().setHead(magitBranch.getName());
            }

            magitBranches.add(magitBranch);
        }
    }
}

