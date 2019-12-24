package Model;//package PACKAGE_NAME;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Observable;
import java.util.TreeMap;


/**
 * Model class is part of mvs design pattern- this charge on the logic part of the engine
 */
public class Model extends Observable
{
    public EngineManager engineManager;
    public TreeMap<String, String> terms;
    public int totalDocNum;
    public String totalTime;

    public void startEngine(String corpusPath,String postingPath,boolean isStemming){
        long millis=System.currentTimeMillis();
        java.util.Date date=new java.util.Date(millis);
       // System.out.println("start "+ date);
        Instant start = java.time.Instant.now();

        this.engineManager= new EngineManager(corpusPath, postingPath,isStemming);
        if(!engineManager.existCorpusPath()){
            engineManager.setCorpusPath(corpusPath);
        }
        engineManager.startEngine();


         millis=System.currentTimeMillis();
        java.util.Date date2=new java.util.Date(millis);
       // System.out.println("end "+ date2);

        Instant end = java.time.Instant.now();
        Duration between = java.time.Duration.between(start, end);
        long mili= between.toMillis()/10;

        totalTime=between.toMinutes()+" minutes, "+
                between.getSeconds()+" seconds and "+ mili +" millis";

        totalDocNum= engineManager.totalDocNum();
        terms=engineManager.getTermsDic();

        setChanged();
        notifyObservers("finishEngine");

    }

    public TreeMap<String,String> loadDictionary(String postingPath, boolean isStemming) {

        if(engineManager==null){
            this.engineManager= new EngineManager(postingPath,isStemming);
            return engineManager.getIndexer();
        }
        else {
            engineManager.setIsStemming(isStemming);
            return engineManager.getIndexer();
        }
    }

    public void showDictionary() {

          this.terms=engineManager.getTermsDic();
          setChanged();
          notifyObservers("showDictionary");
    }


    public TreeMap<String, String> getTerms() {
        return terms;
    }

    public void reset() {
        if(engineManager!=null){
             engineManager.reset();
            this.engineManager= null;
            setChanged();
            notifyObservers("resetSucceed");
        }

    }
}
