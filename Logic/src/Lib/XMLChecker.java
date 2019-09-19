package Lib;
import MagitExceptions.XMLException;
import resources.generated.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


public class XMLChecker {

    public final static String JAXB_XML_PACKAGE_NAME = "resources.generated";


    public static List<String> CheckXML(MagitRepository magitRepository/*, List<String> errors*/) throws JAXBException, FileNotFoundException, XMLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<String> errors = new ArrayList<>();
        List<MagitBlob> blobs = magitRepository.getMagitBlobs().getMagitBlob();
        List<MagitSingleFolder> folders = magitRepository.getMagitFolders().getMagitSingleFolder();
        List<MagitSingleCommit> commits = magitRepository.getMagitCommits().getMagitSingleCommit();
        List<MagitSingleBranch> branches = magitRepository.getMagitBranches().getMagitSingleBranch();
        MagitBranches magitBranchesObjects = magitRepository.getMagitBranches();
        errors.add(checkDuplicateID(blobs));
        errors.add(checkDuplicateID(folders));
        errors.add(checkDuplicateID(commits));
        errors.add(isBlobExistInFolder(folders, blobs));
        errors.add(isFolderExistInFolder(folders));
        errors.add(isFolderNotPointsToItself(folders));
        errors.add(isRootFolderExistAndMarkedAsRootFolder(commits, folders));
        errors.add(isBranchPointToExistCommit(branches, commits));
        errors.add(isHeadBranchExist(branches,magitBranchesObjects));
        errors.add(isAllRTBsTrackingAfterRB(branches));
        errors.removeIf(v->v.equals(""));
        return errors;
    }

    public static void isXMLFile(String xmlPath) throws XMLException {
        if (!xmlPath.endsWith(".xml")) {
            throw new XMLException("the File: " + xmlPath + " is not XML type file");
        }
    }

    public static void isFileExist(String xmlPath) throws FileNotFoundException {
        File file = new File(xmlPath);
        if (!file.exists()) {
            throw new FileNotFoundException("The file: " + xmlPath + " does not exist");
        }
    }


    private static String isHeadBranchExist(List<MagitSingleBranch> branches,MagitBranches magitBranchesObject) {
        String result ="";

        long count =branches.stream().filter(v->v.getName().equals(magitBranchesObject.getHead())).count();
        if(count == 0){
            result = result.concat("Head branch: " +magitBranchesObject.getHead() + " not exist\n");
        }
        if(count > 1)
        {
            result = result.concat("branch: " +magitBranchesObject.getHead() + " exist more than one time\n");
        }
        return  result;
    }

    private static String isBranchPointToExistCommit(List<MagitSingleBranch> branches, List<MagitSingleCommit> commits) {
        String result = "";
        for (MagitSingleBranch branch : branches) {
            if(!branch.getPointedCommit().getId().equals("")) {
                List<MagitSingleCommit> commitFilteredList = commits.stream().filter(v -> branch.getPointedCommit().getId().equals(v.getId())).collect(Collectors.toList());
                if (commitFilteredList.isEmpty()) {
                    result = result.concat("branch: " + branch.getName() + " points to commit: " + branch.getPointedCommit().getId() + " that not exist\n");
                }
            }
        }

        return result;
    }

    private static String isRootFolderExistAndMarkedAsRootFolder(List<MagitSingleCommit> commits, List<MagitSingleFolder> folders) {
        String result = "";
        for (MagitSingleCommit commit : commits) {
            List<MagitSingleFolder> filterdFolder = folders.stream().filter(v -> commit.getRootFolder().getId().equals(v.getId())).collect(Collectors.toList());
            if (filterdFolder.isEmpty()) {
                result = result.concat("commit: " + commit.getId() + " points to root folder that not exist\n");
            } else {
                if (!filterdFolder.get(0).isIsRoot())
                    result = result.concat("commit: " + commit.getId() + " points to folder: " + commit.getRootFolder().getId() + " that not mark as root folder\n");
            }
        }
        return result;
    }

    private static <T> String checkDuplicateID(List<T> list) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String result = "";
        if (list.isEmpty()) {
            return result;
        }

        Class clazz = list.get(0).getClass();
        Method m = clazz.getDeclaredMethod("getId", null);

        if (m != null) {
            for (int i = 0; i < list.size(); i++)
                for (int j = i; j < list.size(); j++) {
                    if (list.get(i) != list.get(j) && m.invoke(list.get(i)).equals(m.invoke(list.get(j)))) {
                        result = result.concat("in " + clazz.getSimpleName() + ": " + m.invoke(list.get(i)).toString() + " is duplicated\n");
                    }
                }
        } else {
            throw new NoSuchMethodException("class does not contain getId method!");
        }
        return result;
    }

    private static String isBlobExistInFolder(List<MagitSingleFolder> folders, List<MagitBlob> blobs) {
        String result = "";
        for (MagitSingleFolder folder : folders) {
            for (Item item : folder.getItems().getItem()) {
                if (item.getType().equals("blob")) {
                    long count = blobs.stream().filter(v -> v.getId().equals(item.getId())).count();
                    if (count == 0) {
                        result = result.concat("folder: " + folder.getId() + " points to blob with id: " + item.getId() + " that not exist\n");
                    }
                }
            }
        }
        return result;

    }

    private static String isFolderExistInFolder(List<MagitSingleFolder> folders) {
        String result = "";
        for (MagitSingleFolder folder : folders) {
            for (Item item : folder.getItems().getItem()) {
                if (item.getType().equals("folder")) {
                    long count = folders.stream().filter(v -> v.getId().equals(item.getId())).count();
                    if (count == 0) {
                        result = result.concat("folder: " + folder.getId() + " points to folder with id: " + item.getId() + " that not exist\n");
                    }
                    if (count > 1) {
                        result = result.concat("folder with id: " + item.getId() + " is duplicated\n");
                    }
                }
            }
        }
        return result;

    }

    private static String isFolderNotPointsToItself(List<MagitSingleFolder> folders) {
        String result = "";
        for (MagitSingleFolder folder : folders) {
            long count = folder.getItems().getItem().stream().filter(v -> folder.getId().equals(v.getId()) && v.getType().equals("folder")).count();
            if (count != 0) {
                result = result.concat("folder: " + folder.getId() + " contains itself\n");
            }
        }
        return result;
    }

    public static MagitRepository deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (MagitRepository) u.unmarshal(in);
    }

    public static String isAllRTBsTrackingAfterRB(List<MagitSingleBranch> listOfBranches){
        String result = "";
        for(MagitSingleBranch msb:listOfBranches){
            if(msb.isTracking()){
                List<MagitSingleBranch> msbRemotedList=listOfBranches.stream().filter(v->v.getName().equals(msb.getTrackingAfter())).collect(Collectors.toList());
                if(!msbRemotedList.isEmpty()) {
                    MagitSingleBranch msbRemoted = msbRemotedList.get(0);
                    if (!msbRemoted.isIsRemote()){
                        result=result.concat("Branch: "+msb.getName()+" track after: "+msbRemoted.getName()+", but "+msbRemoted.getName()+" is not remote branch");
                    }
                }
                else{
                    result=result.concat("Branch: "+msb.getName()+" is RTB but has not any branch to track after it");
                }
            }
        }
        return result;
    }


    public static void MagitRepositoryToXML(MagitRepository magitRepository, String xmlPath) {
        try {
            File file = new File(xmlPath);
            JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_XML_PACKAGE_NAME);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(magitRepository, file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}