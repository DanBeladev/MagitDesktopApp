package models;

import java.util.ArrayList;
import java.util.List;

public class CssManeger {
    private List<String> cssFiles;
    private int currentIndex;

    public CssManeger(){
        cssFiles=new ArrayList<>();
        currentIndex=0;
    }
    public void addCssSheet(String path){
        cssFiles.add(path);
    }
    public void nextCss(){
        currentIndex=(currentIndex+1)%(cssFiles.size());
    }

    public String getCurrentCss() {
        return cssFiles.get(currentIndex);
    }
}
