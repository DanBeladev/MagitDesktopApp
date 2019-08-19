package Lib;
import java.text.ParseException;

public class FileDetails implements Comparable<FileDetails> {
    private String m_Name;
    private SHA1 m_Sh1;
    private FileType m_FileType;
    private User m_WhoUpdatedLast;
    private OurDate m_LastUpdated;

    public FileDetails(String name, SHA1 sh1, FileType type, User user, OurDate createTime) {
        m_Name =name;
        m_Sh1 = sh1;
        m_FileType =type;
        m_WhoUpdatedLast = user;
        m_LastUpdated = createTime;
    }

    public FileDetails(){}

    public FileDetails(FileDetails other) {
        m_Name =other.m_Name;
        m_Sh1 = other.m_Sh1;
        m_FileType =other.m_FileType;
        m_WhoUpdatedLast = other.m_WhoUpdatedLast;
        m_LastUpdated = other.m_LastUpdated;
    }

    public static FileDetails ParseFileDetailsString(String fileDetailsString) throws ParseException {
        FileDetails fileDetails = new FileDetails();
        String[] partsOfString = fileDetailsString.split("\\,");
        fileDetails.setName(partsOfString[0]);
        fileDetails.setSh1(new SHA1(partsOfString[1]));
        switch (partsOfString[2]) {
            case "FILE":
                fileDetails.setFileType(FileType.FILE);
                break;
            case "FOLDER":
                fileDetails.setFileType(FileType.FOLDER);
                break;
        }
        fileDetails.setWhoUpdatedLast(new User(partsOfString[3]));
        fileDetails.setLastUpdated(new OurDate(partsOfString[4]));
        return fileDetails;
    }

    public String getName() {
        return m_Name;
    }


    public FileType getFileType() {
        return m_FileType;
    }

    public void setName(String m_Name) {
        this.m_Name = m_Name;
    }

    public void setSh1(SHA1 m_Sh1) {
        this.m_Sh1 = m_Sh1;
    }

    public void setFileType(FileType m_FileType) {
        this.m_FileType = m_FileType;
    }

    public void setWhoUpdatedLast(User m_WhoUpdatedLast) {
        this.m_WhoUpdatedLast = m_WhoUpdatedLast;
    }

    public void setLastUpdated(OurDate m_LastUpdated) {
        this.m_LastUpdated = m_LastUpdated;
    }

    public void SetDetails(String name, SHA1 sh1, FileType file, User user) {
        m_Name = name;
        m_FileType = file;
        m_Sh1 = sh1;
        m_WhoUpdatedLast = user;
        m_LastUpdated = new OurDate();
    }

    @Override
    public String toString() {
        return
                m_Name + "," +
                        m_Sh1 + "," +
                        m_FileType + ","
                        + m_WhoUpdatedLast + "," +
                        m_LastUpdated + '\n';
    }

    public SHA1 getSh1() {
        return m_Sh1;
    }

    public OurDate getLastUpdated() {
        return m_LastUpdated;
    }

    public User getWhoUpdatedLast(){return  m_WhoUpdatedLast;}

    @Override
    public int compareTo(FileDetails o) {
        return m_Name.compareTo(o.getName());
    }
}
