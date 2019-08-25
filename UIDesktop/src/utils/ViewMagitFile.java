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

    public void setM_Content(String m_Content) {
        this.m_Content = m_Content;
    }

    public String getM_Name() {
        return m_Name;
    }

    public void setM_Name(String m_Name) {
        this.m_Name = m_Name;
    }

    @Override
    public String toString() {
        return m_Name;
    }
}
