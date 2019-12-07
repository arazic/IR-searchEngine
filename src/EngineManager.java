
public class EngineManager {

    private String corpusPath;
    private String postingPath;
    private boolean stemming;
    private Parse parse;
    private Posting posting;
    private ReadFile readFile;

    public static void main(String[] args) {
        System.out.println("We are Google!");
        EngineManager engineManager= new EngineManager("C:/Users/user/engine/Xcorpus", "C:/Users/user/engine/posting",false);
        //EngineManager engineManager= new EngineManager("C:/Users/gal/Desktop/FB396001", "C:/Users/user/posting",false);
        engineManager.startEngine();
        System.out.println("chen is my queen!");
    }

    public EngineManager(String corpusPath, String postingPath, boolean stemming){
        this.corpusPath=corpusPath;
        this.postingPath=postingPath;
        this.stemming=stemming;
        this.posting= new Posting(postingPath);
        this.parse= new Parse(posting);
        this.readFile= new ReadFile("C:/Users/user/engine/Xcorpus", parse);
    }

    private void startEngine() {
        Indexer.initIndexer();
        readFile.createDocuments();

    }

}

