package Model;


import java.util.TreeMap;

public class EngineManager {

    private String corpusPath;
    private String postingPath;
    private boolean stemming;
    private Parse parse;
    private Posting posting;
    private ReadFile readFile;

    public EngineManager(String corpusPath, String postingPath, boolean isStemming){
        this.corpusPath=corpusPath;
        this.postingPath=postingPath;
        this.stemming=isStemming;
        this.posting= new Posting(postingPath, isStemming);
        this.parse= new Parse(posting, isStemming);
        this.readFile= new ReadFile(corpusPath, parse);
        Indexer.initIndexer(postingPath,isStemming);

    }

    public EngineManager(String postingPath, boolean isStemming){
        this.corpusPath=null;
        this.postingPath=postingPath;
        this.stemming=isStemming;
        this.posting= new Posting(postingPath, isStemming);
        this.parse= new Parse(posting, isStemming);
        this.readFile= new ReadFile(corpusPath, parse);
        Indexer.initIndexer(postingPath,isStemming);

    }

    public void startEngine() {
        readFile.createDocuments();  //create also tmpPosting files
        posting.margeToMainPostingFile();
    }


    public boolean existCorpusPath() {
        if(this.corpusPath==null)
            return false;
        return true;
    }

    public void setCorpusPath(String corpusPath) {
        this.corpusPath=corpusPath;
    }

    public static boolean setIndexer() {
        return Indexer.loadData();
    }

    public void setIsStemming(boolean isStemming) {
        posting.setIsStemming(isStemming);
        parse.setIsStemming(isStemming);
        Indexer.setIsStemming(isStemming);

    }

    public static TreeMap<String, String> getTermsDic(){
        return Indexer.getTermsDic();
    }

    public void reset() {
        posting.reset();
    }
}

