package Model;

import com.medallia.word2vec.Searcher.Match;
import com.medallia.word2vec.Searcher.UnknownWordException;
import com.medallia.word2vec.Word2VecModel;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

/*import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;*/

/**
 * It is her job to perform the queries.
 * The class will receive a query or queries
 * , the class will analyze the input according to the text analysis
 * done on the documents and return the the documents most relevant
 * to the query are graded (using the detailed Ranker class)
 */

public class Searcher {
    Ranker ranker;
    Parse parser;
    boolean stemming;
    boolean semantic;
    TreeMap<String, TreeMap<String, Integer>> tremsInDoc ; // docName, <Term-"tf">
    TreeMap<String, Integer> allRelevantDocsSize; // docName, size - |d|
    HashMap<String, String> allRelevantDocsEntities; // docName,Entities
    TreeMap<String, Integer> termsDf ; // Term, df-how manyDocs
    HashSet<String> legalEntities;
    int totalCurposDoc ;
    double averageDocLength ;
    String postingPath;
    boolean loadEntities;
    private BufferedWriter queriesWriter;
    private com.medallia.word2vec.Searcher searcher;

    public Searcher(Parse parser, boolean isStemming,  String postingPath) {
        ranker = new Ranker();
        this.parser = parser;
        this.stemming=isStemming;
        this.postingPath=postingPath;
        this.legalEntities= new HashSet<>();
        loadEntities= false;
        loadSemanticsModel();
    }

    /**
     * This function loads a model that helps us extract
     *  the synonyms into the words of the question.
     *  This is to better retrieve!
     */
    public void loadSemanticsModel()
    {
        try {
//            System.out.println(Paths.get(".").toAbsolutePath());
           File file = new File("./word2vec.c.output.model.txt");// for Jar
//            File file = new File(this.getClass().getClassLoader().getResource("/word2vec.c.output.model.txt").getFile()); //for intellij
            Word2VecModel model =Word2VecModel.fromTextFile(file);
            com.medallia.word2vec.Searcher searcher =model.forSearch();
            this.searcher=searcher;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *The function uses the word2vec model to find
     * words related to the original words of the query
     * @param queryTerms
     * @return Other words that have a connection to the terms in the query
     * TreeMap<String,Integer> - <term, freq in the query>
     */
    private TreeMap<String,Integer> getSemanticsWord(TreeMap<String,Integer> queryTerms)
    {
        TreeMap<String,Integer> semanticsTerms=new TreeMap<>();
        for (String term:queryTerms.keySet())
        {
            try {
                if(searcher.contains(term))
                {
                    List<Match> maches =searcher.getMatches(term,2);
                    for ( com.medallia.word2vec.Searcher.Match match:maches)
                    {
                        String semanticTerm=match.match();
                        semanticsTerms.put(semanticTerm,queryTerms.get(term));
                    }
                }
            } catch (UnknownWordException e) {
                e.printStackTrace();
            }
        }
        return semanticsTerms;
    }


    /**
     * The function loads from the posting file the found entities
     */
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

    /**
     * The function creates a data structure that contains the query number and list of its answers.
     * The list of answers is built from a couple which is a document and entities.
     * @param pathToQueries - The path in which the query or queries are saved
     * @param isStem - if the user ask for Stemming
     * @param isSemantic -- if the user ask for Semantic
     * @return Data structure described above
     */
    public HashMap<Integer, LinkedList<Pair<String,String>>> readQueriesFromData(String pathToQueries,boolean isStem, boolean isSemantic)
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
        HashMap<Integer, LinkedList<Pair<String,String>>> queryDocsEntity= new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            while ((line = br.readLine()) != null)
            {
                if(line.equals("</top>")){
                    sb.append(line).append("\n");
                    handleQueryFromData(sb, queryDocsEntity);
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
        return queryDocsEntity;
    }

    /**
     * The function gets a data structure that contains the query number and list of its answers.
     * The list of answers is built from a couple which is a document and entities.
     * @param query the current query
     * @param queryDocsEntity data structure
     */
    private void handleQueryFromData(StringBuilder query, HashMap<Integer, LinkedList<Pair<String, String>>> queryDocsEntity) {
        String[] queryData = parseQueryFromData(query);
        int queryID = Integer.parseInt(queryData[0]);
        TreeMap<String, Integer> termsQuery = parser.parseQuery(queryData[1], stemming);
        TreeMap<String, Integer> titleQuery = parser.parseQuery(queryData[2], stemming);
        for (String title : titleQuery.keySet()) {
            if (termsQuery.containsKey(title)) {
                double wight = termsQuery.get(title) * titleQuery.get(title) * 1.3;
                int intWight = (int) wight;
                termsQuery.replace(title, intWight);
            }
        }
        if (semantic) {
            TreeMap<String, Integer> semanticTerms = getSemanticsWord(termsQuery);
            if (stemming) {
                semanticTerms = parser.parseStemming(semanticTerms);
            }
            for (String semantic : semanticTerms.keySet()) {
                if (termsQuery.containsKey(semantic) == false) {
                    termsQuery.put(semantic, semanticTerms.get(semantic));
                }
            }
        }
        else if(!semantic && stemming){
            termsQuery = parser.parseStemming(termsQuery);
        }
        infoFromPosting(termsQuery);
        LinkedList<String> rankedDocuments=ranker.rankDocuments();
        writeQueryAnswerToFile(queryID,rankedDocuments);
        LinkedList<Pair<String,String>> rankedDocumentsAndEntity= fillEntities(rankedDocuments);
        termsQuery.clear();
        queryDocsEntity.put(queryID,rankedDocumentsAndEntity);

    }


    /**
     * the method parse the query to title and description
     * @param query
     * @return array:
     *         queryData[1]=description;
     *         queryData[2]=title;
     */
    private String[] parseQueryFromData(StringBuilder query)
    {
        String text =query.toString();
        String [] tokens=StringUtils.split(text," \n");
        String[] queryData= new String[3];
        String title="";
        String description ="";
        int index=0;
        while(index<tokens.length)
        {
            if(tokens[index].equals("<num>"))
            {
                if(index+2<tokens.length)
                {
                    queryData[0]=tokens[index+2];
                    index=index+4;
                    break;
                }
            }
            index++;
        }
        //get title
        while(index<tokens.length)
        {
            if(tokens[index].equals("<desc>"))
            {
                index=index+2;
                break;
            }
            description=description+tokens[index]+" ";
            title=title+tokens[index]+" ";
            index++;
        }
        //get description
        while (index<tokens.length)
        {
            if(tokens[index].equals("<narr>"))
            {
                index=index+2;
                break;
            }
            description=description+tokens[index]+" ";
            index++;
        }
        queryData[1]=description;
        queryData[2]=title;
        return queryData;
    }

    /**
     * The main method of this class, gets query,
     * @param query
     * @param stemming- A variable that says whether it asks for stemming
     * @param semantic- A variable that says whether it asks for semantic
     * @return list of answers is built from a couple which is a document and entities.
     */
    public LinkedList<Pair<String,String>> search( String query, boolean stemming, boolean semantic) {
        this.stemming=stemming;
        this.semantic=semantic;
        if(!loadEntities) {
            loadEntities();
            loadEntities= true;
        }

        TreeMap<String, Integer> termsQuery = parser.parseQuery(query, stemming); //tremName, tf in query
        if (semantic)
        {
            TreeMap<String, Integer> semanticTerms = getSemanticsWord(termsQuery);
            if(stemming) {
                semanticTerms = parser.parseStemming(semanticTerms);
            }
            for (String semanticTerm:semanticTerms.keySet())
            {
                if(termsQuery.containsKey(semanticTerm)==false)
                {
                    termsQuery.put(semanticTerm,semanticTerms.get(semanticTerm));
                }
            }
        }
        else if(!semantic && stemming){
                termsQuery = parser.parseStemming(termsQuery);
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

    /**
     * fill the entities of the doc from the posting file
     * @param rankedDocuments
     * @return
     */
    private LinkedList<Pair<String,String>> fillEntities(LinkedList<String> rankedDocuments) {
        LinkedList<Pair<String,String>> ans=new LinkedList<>();
        for (int i=0; i<rankedDocuments.size();i++){
            String entities= allRelevantDocsEntities.get(rankedDocuments.get(i));
            ans.add(i,new Pair<>(rankedDocuments.get(i),entities));
        }
        return ans;
    }


    /**
     * create file that save all the engine result
     * @param queryID
     * @param documents
     */
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

    /**
     * Read from all the posting files the relevant information,
     * fill the data structure:
     *     1. tremsInDoc
     *     2. termsDf
     *     3. allRelevantDocsSize
     *     4. allRelevantDocsEntities
     * @param termsQuery - accept all the queries term
     */
    public void infoFromPosting(TreeMap<String,Integer> termsQuery)
    {
        tremsInDoc= new TreeMap<>();
        termsDf= new TreeMap<>();// Term, df-how manyDocs
        allRelevantDocsSize = new TreeMap<>(); // docNam - size
        allRelevantDocsEntities = new HashMap<>(); // docNam - entities
        totalCurposDoc=0;
        averageDocLength=0;
        TreeMap<String,String> allDictionatryTerms= new TreeMap<>();
        TreeMap<String,Integer> tempTermsQuery = new TreeMap<>();
        TreeSet<String> termsToRemove=new TreeSet<>();
        for (String term:termsQuery.keySet())
        {
            boolean flag=false;
            if(Indexer.checkTerm(term))
            {
                allDictionatryTerms.put(term,Indexer.getTermPosition(term));
                flag=true;
            }
            else
            {
                String lowerCase=term.toLowerCase();
                if(!term.equals(lowerCase))
                {
                    if(Indexer.checkTerm(lowerCase))
                    {
                        allDictionatryTerms.put(lowerCase,Indexer.getTermPosition(lowerCase));
                        tempTermsQuery.put(lowerCase,termsQuery.get(term));
                        flag=true;
                    }
                }
            }
            if(!flag)
            {
                termsToRemove.add(term);
            }
        }
        if(termsToRemove.isEmpty()==false)
        {
            for (String term:termsToRemove)
            {
                termsQuery.remove(term);
      //          System.out.println(term+ " deleted");
            }
        }
        if(tempTermsQuery.isEmpty()==false)
        {
            for (String term:tempTermsQuery.keySet())
            {
                if(termsQuery.containsKey(term.toUpperCase()))
                {
                    termsQuery.remove(term.toUpperCase());
                }
                termsQuery.put(term,tempTermsQuery.get(term));
            }
        }
        TreeMap<String,Integer> capitalTerms = new TreeMap<>();
        TreeMap<String,Integer> numberTerms = new TreeMap<>();
        TreeMap<String,Integer> dLowerTerms = new TreeMap<>();
        TreeMap<String,Integer> pLowerTerms = new TreeMap<>();
        TreeMap<String,Integer>  zLowerTerms = new TreeMap<>();
        for (String term:allDictionatryTerms.keySet())
        {
            String data = allDictionatryTerms.get(term);
            String[] splitData =data.split("!");
            int pointer=-1;
            int documentFrequency=-1;
            if(splitData.length>2)
            {
                documentFrequency=Integer.parseInt(splitData[0]);
                pointer=Integer.parseInt(splitData[splitData.length-1]);
                termsDf.put(term,documentFrequency);
            }
            else
            {
                continue;
            }
            if(term.charAt(0)<='9' && term.charAt(0)>='0')
            {
                numberTerms.put(term,pointer);
            }
            else if(term.charAt(0)<='Z' && term.charAt(0)>='A')
            {
                capitalTerms.put(term,pointer);
            }
            else if(term.charAt(0)<='d')
            {
                dLowerTerms.put(term,pointer);
            }
            else if(term.charAt(0)<='p')
            {
                pLowerTerms.put(term,pointer);
            }
            else
            {
                zLowerTerms.put(term,pointer);
            }
        }
        String  s="";
        if(stemming)
            s="With";
        else
            s="No";
        try {
            FileReader numberFile = new FileReader((postingPath) + "/finalPostingNumbers"+s+"Stemming.txt");
            FileReader capitalFile = new FileReader((postingPath) + "/finalPostingCapital"+s+"Stemming.txt");
            FileReader lowerFile1 = new FileReader((postingPath) + "/finalPostingLower"+s+"StemmingD.txt");
            FileReader lowerFile2 = new FileReader((postingPath) + "/finalPostingLower"+s+"StemmingP.txt");
            FileReader lowerFile3 = new FileReader((postingPath) + "/finalPostingLower"+s+"StemmingZ.txt");
            BufferedReader lowerReader1 = new BufferedReader(lowerFile1);
            BufferedReader lowerReader2 = new BufferedReader(lowerFile2);
            BufferedReader lowerReader3 = new BufferedReader(lowerFile3);
            BufferedReader numberReader = new BufferedReader(numberFile);
            BufferedReader capitalReader = new BufferedReader(capitalFile);
            getPostingData(numberTerms,numberReader);
            getPostingData(capitalTerms,capitalReader);
            getPostingData(dLowerTerms,lowerReader1);
            getPostingData(pLowerTerms,lowerReader2);
            getPostingData(zLowerTerms,lowerReader3);
            if(!allRelevantDocsSize.isEmpty())
            {
                rePostingDocs(postingPath);
                ranker.setData(tremsInDoc, termsDf, allRelevantDocsSize, termsQuery);
            }
        }
        catch (Exception e)
        {

        }

    }

    /**
     *  read till the wanted pointer
     * @param termsPointer
     * @param postingReader
     */
    private void getPostingData(TreeMap<String,Integer> termsPointer,BufferedReader postingReader)
    {
        if(termsPointer.isEmpty())
        {
            return;
        }
        try {
            int counter = 0;
            for (Map.Entry entry : termsPointer.entrySet()) {
                int pointer = (int) entry.getValue();
                String line = null;
                line = postingReader.readLine();
                while (line != null) {
                    if (counter == pointer-1) {
                        rePostingTerms(line);
                        counter++;
                  //      System.out.println(entry.getKey() +" == "+ line);
                        break;
                    }
                    counter++;
                    line = postingReader.readLine();
                }
            }
        }
        catch (Exception e)
        {

        }
    }

    /**
     * Read the doc details from the posting file
     * @param postingPath
     */
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
                        allRelevantDocsEntities.put(docInPosting, realEntities);
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
                        ranker.setAverageDocSize(averageDocLength);
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

    /**
     * parse the term info from the posting doc
     * @param term
     */
    private void rePostingTerms(String term)
    {
        String[] miniParse= StringUtils.split(term,"!");
        String termName= miniParse[0];
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
                temp.put(termName, Integer.valueOf(tfInDoc));
                tremsInDoc.put(docName,temp);
            }
        }
    }


    /**
     * write the result to path
     * @param queriesResultPath
     */
    public void setQueriesResultPath(String queriesResultPath) {
        try {
            queriesWriter=new  BufferedWriter(new FileWriter((queriesResultPath+"/queriesAnswers.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//Brings synonyms on the web API
    /*private TreeMap<String, Integer> getWords(TreeMap<String, Integer> termsQuery) {
        TreeMap<String, Integer> result= new TreeMap<> ();
        TreeMap<String, Integer> ans= new TreeMap<> ();

        for (String term : termsQuery.keySet()) {
            String curTerm= term.replace(" ","+");
           String ansJ= StringUtils.substring(getFromJ(curTerm),1,getFromJ(curTerm).length()-1);
          for(String jsomAns: ansJ.split("}")){

               jsomAns=StringUtils.substring(jsomAns,1);
              ans=cleanAns(jsomAns,curTerm);

              result.putAll(ans);
               break;
         }
     }
     return result;
    }*/

   /* public String getFromJ(String term){
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
*/
/*
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
*/

}
