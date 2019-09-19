package Lib;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {


    public static void WriteToFile(String toWrite, String path) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write(toWrite);
        fw.close();
    }

    public static File CreateTextFile(String path, String content) throws IOException {

        File file = new File(path);
        if (file.createNewFile()) {
            WriteToFile(content, path);
            return file;
        }
        return null;

    }

    public static String ReadContentFromFile(File f) throws IOException {
        String content = "";
        String done="";
        try {
            content = new String(Files.readAllBytes(Paths.get(f.getPath())));
            done=content.replace("\r","");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return done;
    }

    public static void CreateZipFile(File file, SHA1 sha1, String location) throws IOException {
        String sourceFile = file.getAbsolutePath();
        FileOutputStream fos = new FileOutputStream(location + sha1 + ".zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.close();
        fis.close();
        fos.close();
    }

    public static void CreateZipFile(String content, String fileName, String location) throws IOException {
        SHA1 sh1 = new SHA1();
        sh1.MakeSH1FromContent(content);
        if (fileName == null) {
            fileName = sh1.getSh1();
        }
        File file = new File(location + fileName + ".txt");
        file.createNewFile();
        WriteToFile(content, location + fileName + ".txt");
        CreateZipFile(file, sh1, location);
        file.delete();
    }

    public static void UnzipFile(String src, String dst, String name) throws IOException {
        File destDir = new File(dst);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(src));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry, name);
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();

    }

    public static void AppendTextToFile(String filePath, String text) throws IOException {
        text = text + "\n";
        Files.write(Paths.get(filePath), text.getBytes(), StandardOpenOption.APPEND);
    }

    public static List<String> GetLinesFromFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry, String name) throws IOException {
        File destFile = new File(destinationDir, name);

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    System.gc();
                    if(!files[i].delete()){
                        System.out.println("problem in deleting "+files[i].getName());
                    }
                }
            }
        }
        return (path.delete());
    }

    public static boolean deleteDirectory(String path) {
        return deleteDirectory(new File(path));
    }


    public static List<FileDetails> ParseFolderTextFileToFileDetailsList(File file) throws IOException, ParseException {
        List<FileDetails> fileDetailsList = new ArrayList<>();
        FileDetails fileDetails;
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String fileDetailsString = bf.readLine();
        while (fileDetailsString != null) {
            fileDetails = FileDetails.ParseFileDetailsString(fileDetailsString);
            fileDetailsList.add(fileDetails);
            fileDetailsString = bf.readLine();
        }

        return fileDetailsList;
    }

    public static void ClearDirectory(String path) {
        File file = new File(path);
        File[] listFiles = file.listFiles();
        for (File f : listFiles) {
            if (f.isDirectory()) {
                FileUtils.deleteDirectory(f);
            } else {
                f.delete();
            }
        }
    }

    public static List<FileDetails> ParseFolderTextFileToFileDetailsList(String content) throws ParseException {
        List<FileDetails> fileDetailsList = new ArrayList<>();
        FileDetails fileDetails;
        String[] stringArray = content.split("\n");
        if (stringArray.length == 1 && stringArray[0].equals("")) {
            return fileDetailsList;
        }
        for (String str : stringArray) {
            fileDetails = FileDetails.ParseFileDetailsString(str);
            fileDetailsList.add(fileDetails);
        }

        return fileDetailsList;
    }

    public static String getContentFromZippedFile(String path) throws IOException {
        ZipFile zipFile = new ZipFile(path);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        String content = "";
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            InputStream stream = zipFile.getInputStream(entry);
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                content = content + line;
                line = br.readLine();
                if (line != null) {
                    //line = "\r\n" + line;
                    line='\n'+line;
                }
            }


        }
        return content;
    }

    public static void createParentsFoldersByPath(String path) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        file.createNewFile();
    }

    public static void CopyDirectory(File source, File target) throws IOException {
        Files.copy(Paths.get(source.getPath()), Paths.get(target.getPath()));
        if (source.isDirectory()) {
            File[] innerFiles = source.listFiles();
            if (innerFiles != null) {
                for (File innerFile : innerFiles) {
                    CopyDirectory(innerFile, new File(target + "\\" + innerFile.getName()));
                }
            }
        }
    }

    public static void createFoldersByPathAndWriteContent(String content, String path) throws IOException {
        createParentsFoldersByPath(path);
        WriteToFile(content,path);
    }


    public static void BuildTestDirectory(String path) throws Exception {
        new File(path + "\\" + "folder1").mkdirs();
        new File(path + "\\" + "folder2").mkdirs();
        new File(path + "\\" + "folder3").mkdirs();
        FileUtils.WriteToFile("file 1 content", path + "\\" + "file1.txt");
        FileUtils.WriteToFile("file 2 content", path + "\\" + "file2.txt");


        new File(path + "\\" + "folder1\\folder1.1").mkdirs();
        FileUtils.WriteToFile("file 1.1 content", path + "\\" + "folder1\\file1.1.txt");
        FileUtils.WriteToFile("file 1.1.1 content", path + "\\" + "folder1\\folder1.1\\file1.1.1.txt");

        new File(path + "\\" + "folder2\\folder2.1").mkdirs();
        new File(path + "\\" + "folder2\\folder2.2").mkdirs();
        FileUtils.WriteToFile("file 2.1 content", path + "\\" + "folder2\\file2.1.txt");
        FileUtils.WriteToFile("file 2.1.1 content", path + "\\" + "folder2\\folder2.1\\file2.1.1.txt");
        FileUtils.WriteToFile("file 2.2.1 content", path + "\\" + "folder2\\folder2.2\\file2.2.1.txt");
        FileUtils.WriteToFile("file 2.2.2 content", path + "\\" + "folder2\\folder2.1\\file2.2.2.txt");

        new File(path + "\\" + "folder3\\folder3.1").mkdirs();
        FileUtils.WriteToFile("file 3.1 content", path + "\\" + "folder3\\file3.1.txt");
        FileUtils.WriteToFile("file 3.1.1 content", path + "\\" + "folder3\\folder3.1\\file3.1.1.txt");
    }

    public static void DoChangesTest(String path) throws IOException {
        deleteDirectory(path + "\\folder3");
        AppendTextToFile(path + "\\file1.txt", " shinuy");
        WriteToFile("hadash", path + "\\folder2\\filehadash.txt");
    }
}
