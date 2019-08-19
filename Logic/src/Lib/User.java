package Lib;
public class User {
    private String m_Name;

    public User(String name) {
        this.m_Name = name;
    }

    public String getName() {
        return m_Name;
    }

    @Override
    public String toString() {
        return m_Name;
    }
}
