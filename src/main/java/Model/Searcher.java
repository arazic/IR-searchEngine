package Model;

import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;



public class Searcher {
    Ranker ranker;
    Parse parser;
    boolean stemming;
    boolean semantic;
    TreeMap<String, TreeMap<String, Integer>> tremsInDoc ; // docName, <Term-"tf">
    TreeMap<String, Integer> termsDf ; // Term, df-how manyDocs
    TreeMap<String, Integer> allRelevantDocsSize; // docName, size - |d|
    HashMap<String, String> allRelevantDocsEntyies; // docName,Entities
    HashSet<String> legalEntities;
    int totalCurposDoc ;
    double averageDocLength ;
    String postingPath;
    boolean loadEntities;
    private BufferedWriter queriesWriter;

    public Searcher(Parse parser, boolean isStemming,  String postingPath ) {
        ranker = new Ranker();
        this.parser = parser;
        this.stemming=isStemming;
        this.postingPath=postingPath;
        this.legalEntities= new HashSet<>();
        loadEntities= false;
        try {
            queriesWriter=new  BufferedWriter(new FileWriter((postingPath+"/queriesAnswers.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadEntities() {
        try {

        String  s="";
        if(stemming)
            s="With";
        else
            s="No";

        FileReader capitalFile = new FileReader((postingPath) + "/finalPostingCapital"+s+"Stemming.txt");
        BufferedReader capitalReader = new BufferedReader(capitalFile);
        String line= capitalReader.readLine();


        while (line!=null){
            String termName= line.substring(0,line.indexOf("!"));
            if(termName.contains(" "))
                legalEntities.add(termName);
                line=capitalReader.readLine();
        }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleQueryFromData(StringBuilder query)
    {
        String[] queryData =parseQueryFromData(query);
        int queryID=Integer.parseInt(queryData[0]);
        TreeMap<String,Integer> termsQuery = parser.parseQuery(queryData[1],stemming);
        if (semantic) {
            TreeMap<String, Integer> semanticTerms = getWords(termsQuery);
            termsQuery.putAll(semanticTerms);
        }
        infoFromPosting(termsQuery);
        LinkedList<String> rankedDocuments=ranker.rankDocuments();
        writeQueryAnswerToFile(queryID,rankedDocuments);

    }

    public void readQueriesFromData(String pathToQueries,boolean isStem, boolean isSemantic ,String postingPath)
    {
        if(!loadEntities) {
            loadEntities();
            loadEntities= true;
        }

        this.semantic=isSemantic;
        this.stemming=isStem;
        File path= new File(pathToQueries);
        String line;
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            while ((line = br.readLine()) != null)
            {
                if(line.equals("</top>")){
                    sb.append(line).append("\n");
                    handleQueryFromData(sb);
                    sb.setLength(0);
                }
                else
                    sb.append(line).append("\n");
            }
        }
        catch (IOException i)
        {
            i.printStackTrace();
        }
    }

    private String[] parseQueryFromData(StringBuilder stringBuilder)
    {
        String text =stringBuilder.toString();
        String [] tokens=StringUtils.split(text," \n");
        String[] queryData= new String[2];
        String query ="";
        int index=0;
        while(index<tokens.length)
        {
            if(tokens[index].equals("<num>"))
            {
                if(index+2<tokens.length)
                {
                    queryData[0]=tokens[index+2];
                    index=index+3;
                    break;
                }
            }
            index++;
        }
        while(index<tokens.length)
        {
            if(tokens[index].equals("<title>"))
            {
                index++;
                while(index<tokens.length)
                {
                    if(tokens[index].equals("<desc>"))
                    {
                        index++;
                        queryData[1]=query;
                        break;
                    }
                    else
                    {
                        query=query+tokens[index]+" ";
                    }
                    index++;
                }
                break;
            }
            index++;
        }
        return queryData;
    }

    public LinkedList<Pair<String,String>> search( String query, boolean stemming, boolean semantic) {
        this.stemming=stemming;
        this.semantic=semantic;
        if(!loadEntities) {
            loadEntities();
            loadEntities= true;
        }

        TreeMap<String, Integer> termsQuery = parser.parseQuery(query, stemming); //tremName, tf in query
        if (semantic) {
            TreeMap<String, Integer> semanticTerms = getWords(termsQuery);
            termsQuery.putAll(semanticTerms);
        }
        infoFromPosting(termsQuery);
        if(allRelevantDocsSize.isEmpty())
        {
            return null;
        }
        LinkedList<String> rankedDocuments=ranker.rankDocuments();
        double doubleID=(Math.random()*((999-100)+1))+100;
        int id=(int)doubleID;
        writeQueryAnswerToFile(id,rankedDocuments);
        LinkedList<Pair<String,String>> rankedDocumentsAndEntity= fillEntities(rankedDocuments);
        termsQuery.clear();
        return rankedDocumentsAndEntity;
    }

    private LinkedList<Pair<String,String>> fillEntities(LinkedList<String> rankedDocuments) {
        LinkedList<Pair<String,String>> ans=new LinkedList<>();
        for (int i=0; i<rankedDocuments.size();i++){
            String entities= allRelevantDocsEntyies.get(rankedDocuments.get(i));
            ans.add(i,new Pair<>(rankedDocuments.get(i),entities));
        }
        return ans;
    }


    private TreeMap<String, Integer> getWords(TreeMap<String, Integer> termsQuery) {
        TreeMap<String, Integer> result= new TreeMap<> ();
        TreeMap<String, Integer> ans= new TreeMap<> ();

        for (String term : termsQuery.keySet()) {
            String curTerm= term.replace(" ","+");
            String ansJ= StringUtils.substring(getFromJ(curTerm),1,getFromJ(curTerm).length()-1);
            for(String jsomAns: ansJ.split("}")){
                jsomAns=StringUtils.substring(jsomAns,1);
                ans=cleanAns(jsomAns,curTerm);
                result.putAll(ans);
            }
            System.out.println("************************");
        }
        return result;
    }

    private TreeMap<String,Integer> cleanAns(String jsomAns,String curTerm) {
        TreeMap<String,Integer> ans= new TreeMap<>();
        jsomAns= StringUtils.substring(jsomAns,1,jsomAns.length()-1);
        String []parseJson= StringUtils.split(jsomAns,',');
        String []SynonymTerm= StringUtils.split(parseJson[0],":");
        String []SynonymScore= StringUtils.split(parseJson[1],":");
        if(Integer.parseInt(SynonymScore[1])>1500 && !(SynonymTerm[1].equals(curTerm))){
            ans.put(SynonymTerm[1],Integer.parseInt(SynonymScore[1]));
        }
        return ans;
    }


    public String getFromJ(String term){
        URL currentTermUrl;
        HttpURLConnection connection;
        BufferedReader bufferedReader;
        StringBuilder fromJ = new StringBuilder();

        try {
            currentTermUrl = new URL("http://api.datamuse.com/words?ml=" + term);
            connection  = (HttpURLConnection)  currentTermUrl.openConnection();
            connection.setRequestMethod("GET");
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;

            while ((line = bufferedReader.readLine()) != null)
                fromJ.append(line);
            bufferedReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fromJ != null ? fromJ.toString() : null;
    }


    public void writeQueryAnswerToFile(int queryID, List<String> documents)
    {
        try {
            for (String document :documents)
            {
                String ans =queryID+" 0 "+document+" 1  42.38 mt";
                queriesWriter.append(ans);
                queriesWriter.newLine();
            }
            queriesWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void infoFromPosting( TreeMap<String, Integer> termsQuery) {
        try {
            tremsInDoc= new TreeMap<>();
            termsDf= new TreeMap<>();// Term, df-how manyDocs
            allRelevantDocsSize = new TreeMap<>(); // docNam
            allRelevantDocsEntyies = new HashMap<>(); // docNam
            totalCurposDoc=0;
            averageDocLength=0;

            String  s="";
            if(stemming)
                s="With";
            else
                s="No";



            for (String term : termsQuery.keySet()) {
                FileReader numberFile = new FileReader((postingPath) + "/finalPostingNumbers"+s+"Stemming.txt");
                FileReader capitalFile = new FileReader((postingPath) + "/finalPostingCapital"+s+"Stemming.txt");
                FileReader lowerFile1 = new FileReader((postingPath) + "/finalPostingLowert"+s+"StemmingD.txt");
                FileReader lowerFile2 = new FileReader((postingPath) + "/finalPostingLowert"+s+"StemmingP.txt");
                FileReader lowerFile3 = new FileReader((postingPath) + "/finalPostingLowert"+s+"StemmingZ.txt");

                BufferedReader lowerReader1 = new BufferedReader(lowerFile1);
                BufferedReader lowerReader2 = new BufferedReader(lowerFile2);
                BufferedReader lowerReader3 = new BufferedReader(lowerFile3);
                BufferedReader numberReader = new BufferedReader(numberFile);
                BufferedReader capitalReader = new BufferedReader(capitalFile);
                if (term.toUpperCase().equals(term)){
                    String line= capitalReader.readLine();
                    while (line!=null){
                        String termName= line.substring(0,line.indexOf("!"));
                        if(termName.equals(term)){
                            rePostingTerms(line);
                            System.out.println(term);
                            break;
                        }
                        line=capitalReader.readLine();
                    }
                }
                else if (term.charAt(0) > '9' || term.charAt(0) < '0') {
                    if (term.charAt(0) <= 'd') {
                        String line= lowerReader1.readLine();
                        while (line!=null){
                            String termName= line.substring(0,line.indexOf("!"));
                            if(termName.equals(term)){
                                rePostingTerms(line);
                                System.out.println(term);
                                break;
                            }
                            line=lowerReader1.readLine();
                        }
                    } else if (term.charAt(0) <= 'p') {
                        String line= lowerReader2.readLine();
                        while (line!=null){
                            String termName= line.substring(0,line.indexOf("!"));
                            if(termName.equals(term)){
                                rePostingTerms(line);
                                System.out.println(term);
                                break;
                            }
                            line=lowerReader2.readLine();
                        }
                    }
                    else {
                        String line= lowerReader3.readLine();
                        while (line!=null){
                            String termName= line.substring(0,line.indexOf("!"));
                            if(termName.equals(term)){
                                rePostingTerms(line);
                                System.out.println(term);
                                break;
                            }
                            line=lowerReader3.readLine();
                        }
                    }
                } else {

                    String line= numberReader.readLine();
                    while (line!=null){
                        String termName= line.substring(0,line.indexOf("!"));
                        if(termName.equals(term)){
                            rePostingTerms(line);
                            System.out.println(term);
                            break;
                        }
                        line=numberReader.readLine();
                    }
                }
            }
            if(!allRelevantDocsSize.isEmpty())
            {
                rePostingDocs(postingPath);
                ranker.setData(tremsInDoc, termsDf, allRelevantDocsSize, termsQuery);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void rePostingDocs(String postingPath) {
        try {

            String  s="";
            if(stemming) {
                s="With";
            }
            else {
                s="No";
            }

            FileReader Documents = new FileReader((postingPath) + "/postingDocuments"+s+"Stemming.txt");
            BufferedReader bufferedDocs= new BufferedReader(Documents);
            String line= bufferedDocs.readLine();
            while (line!=null) {
                Set set = allRelevantDocsSize.entrySet();
                Iterator iterator = set.iterator();
                Map.Entry entry = (Map.Entry) iterator.next();
                while (entry!=null)
                {
                    String docInPosting= line.substring(0,line.indexOf("!"));
                    if(docInPosting.charAt(0)==' ')
                        docInPosting= docInPosting.substring(1);
                    if(docInPosting.charAt(docInPosting.length()-1)==' ')
                        docInPosting= docInPosting.substring(0, docInPosting.length()-1);

                    if (entry.getKey().equals(docInPosting)){
                        String [] parseDoc=  StringUtils.split(line,"!");
                        allRelevantDocsSize.put(docInPosting, Integer.valueOf(parseDoc[3]));
                        String realEntities= getRealEntities(parseDoc[parseDoc.length-1]);
                        allRelevantDocsEntyies.put(docInPosting, realEntities);
                        //allRelevantDocsEntyies.put(docInPosting, parseDoc[parseDoc.length-1]);
                        if(iterator.hasNext())
                            entry = (Map.Entry) iterator.next();
                        else
                            entry=null;
                    }
                    line=bufferedDocs.readLine();
                    if(line.charAt(0)=='~'){
                        totalCurposDoc= Integer.parseInt(line.substring(1));
                        line=bufferedDocs.readLine();
                        averageDocLength= Integer.parseInt(line.substring(1));
                        line=bufferedDocs.readLine();
                        ranker.setCorpusSize(totalCurposDoc);
                        ranker.setAvergeDocSize(averageDocLength);
                        break;
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getRealEntities(String s) {
        String[] entities= StringUtils.split(s, "|");
        String ans="";
        for (String suspect: entities){
            if(legalEntities.contains(suspect.toUpperCase()))
                ans= ans+"|"+ suspect;
        }
    return ans;
    }

    private void rePostingTerms(String term) {

        String[] miniParse= StringUtils.split(term,"!");
        String termName= miniParse[0];

        termsDf.put(termName, Integer.valueOf(miniParse[2])); //insert df of term

        String[] splitDocs= StringUtils.split(miniParse[1].substring(1,miniParse[1].length()),",");
        for (int i=0; i<splitDocs.length; i++){
            String docName= StringUtils.substring(splitDocs[i],0,splitDocs[i].indexOf(":"));
            if(docName.charAt(0)==' ')
                docName= StringUtils.substring(docName,1);
            if(docName.charAt(docName.length()-1)==' ')
                docName= StringUtils.substring(docName,0,docName.length()-1);
            String tfInDoc=StringUtils.substring(splitDocs[i],splitDocs[i].indexOf(":")+1);
            if(tfInDoc.charAt(tfInDoc.length()-1)==']')
                tfInDoc= StringUtils.substring(tfInDoc,0,tfInDoc.length()-1);
            allRelevantDocsSize.put(docName, 0);
            if(tremsInDoc.containsKey(docName)){
                TreeMap<String,Integer> temp= tremsInDoc.get(docName);
                temp.put(termName, Integer.valueOf(tfInDoc));
                tremsInDoc.put(docName,temp);
            }
            else{
                TreeMap<String,Integer> temp= new TreeMap<>();
                //System.out.println(docName);
                temp.put(termName, Integer.valueOf(tfInDoc));
                tremsInDoc.put(docName,temp);
            }
        }
    }
}
