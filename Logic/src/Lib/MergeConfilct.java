package Lib;

public class MergeConfilct {
    private String path;
    private String theirsContent;
    private String ancestorContent;
    private String ourContent;
    private String resolveContent = null;


    public MergeConfilct(String path, String ourContent,String theirsContent, String ancestorContent){
        this.path=path;
        this.ancestorContent=ancestorContent;
        this.ourContent=ourContent;
        this.theirsContent=theirsContent;
    }

    public void resolveConflict(String content){
        resolveContent = content;
    }
}
