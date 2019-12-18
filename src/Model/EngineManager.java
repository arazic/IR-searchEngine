package Model;


public class EngineManager {

    private String corpusPath;
    private String postingPath;
    private boolean stemming;
    private Parse parse;
    private Posting posting;
    private ReadFile readFile;

    public EngineManager(String corpusPath, String postingPath, boolean stemming){
        this.corpusPath=corpusPath;
        this.postingPath=postingPath;
        this.stemming=stemming;
        this.posting= new Posting(postingPath);
        this.parse= new Parse(posting);
        this.readFile= new ReadFile(corpusPath, parse);
    }

    public EngineManager(String postingPath, boolean stemming){
        this.corpusPath=null;
        this.postingPath=postingPath;
        this.stemming=stemming;
        this.posting= new Posting(postingPath);
        this.parse= new Parse(posting);
        this.readFile= new ReadFile(corpusPath, parse);
        Indexer.initIndexer(postingPath);

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

    public static void setIndexer() {
        Indexer.loadData();
    }
}

