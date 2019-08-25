package utils;

public class ViewMagitFile {

    private String m_Content;
    private  String m_Name;

    public ViewMagitFile(String content, String name){
        m_Content=content;
        m_Name =name;
    }

    public String getM_Content() {
        return m_Content;
    }

    @Override
    public String toString() {
        return m_Name;
    }
}
