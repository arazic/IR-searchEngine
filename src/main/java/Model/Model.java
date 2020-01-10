package Model;//package PACKAGE_NAME;

import javafx.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
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
    public LinkedList<Pair<String,String>> currTop50;

    public void startEngine(String corpusPath,String postingPath,boolean isStemming){
        long millis=System.currentTimeMillis();
        java.util.Date date=new java.util.Date(millis);
       // System.out.println("start "+ date);
        Instant start = java.time.Instant.now();

        this.engineManager= new EngineManager(corpusPath, postingPath,isStemming);
        if(!engineManager.existCorpusPath()){
            engineManager.setCorpusPath(corpusPath);
        }
        engineManager.setIsStemming(isStemming);
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

    public void runQuery(String currentQuery, boolean isStemming, boolean isSemantic, String queriesResultPath) {
        if(engineManager==null){
            setChanged();
            notifyObservers("notPreparedToSearch");
        }
        else{
            engineManager.setQueriesResultPath(queriesResultPath);
            currTop50 = engineManager.search(currentQuery, isStemming, isSemantic);
            setChanged();
            notifyObservers("answerSearch");
        }
    }

    public void runQueriesFile(String pathQuery, boolean isStemming, boolean isSemantic, String queriesResultPath) {
        if(engineManager==null){
            setChanged();
            notifyObservers("notPreparedToSearch");
        }
        else{
            engineManager.setQueriesResultPath(queriesResultPath);
            if(engineManager.searchQueriesFile(pathQuery,isStemming,isSemantic)){
                setChanged();
                notifyObservers("finishAndCreateAnswerDoc");
            }

        }
    }
}
