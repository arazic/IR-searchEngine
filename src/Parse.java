import com.sun.deploy.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {

    private static HashSet<String> stopWords;
    private HashMap<String, Integer>transperToFormat;
    private HashMap<String,Integer> allDocTerms;
    private TreeSet<Term> terms;
    private List<String> quotes;
    private String[] tokens;
    private String text;
    private String header;
    private Document currDoc;
    private int currIndex;
    private String newTerm;

    private Pattern priceLength4= Pattern.compile("(million|billion|trillion)"+ " "+"U.S."+ " "+"dollars");
    private Pattern intPattern= Pattern.compile("^[0-9]*$");
    private Pattern digitPattern= Pattern.compile("\\d");
    private Pattern doublePattern= Pattern.compile("^[0-9]*$"+"."+"^[0-9]*$");
    private Pattern fractionPattern= Pattern.compile("^[0-9]*$"+" "+"^[0-9]*$"+"/"+"^[0-9]*$");

    public Parse()
    {
        transperToFormat= new HashMap<>();
        quotes = new LinkedList<>();
        transperToFormat.put("million",1);
        transperToFormat.put("billion",1000);
        transperToFormat.put("trillion",1000000);
    }

    private void mergeTerms()
    {
        TreeMap<String,Integer> mapTerms = new TreeMap<>(allDocTerms);
        Iterator <String> it = mapTerms.keySet().iterator();
        Iterator <Term> termIterator = terms.iterator();
        while (it.hasNext())
        {
            Term term =(Term) termIterator;
            String termString = it.toString();
            if(term.compareTo(termString)==0)
            {
                term.setFreq(term.getFreq()+mapTerms.get(termString));
                if(termIterator.hasNext())
                {
                    termIterator.next();
                    it.next();
                }

            }
        }

    }
    
    

    public void createDocument(StringBuilder content)
    {
        currDoc = new Document();
        text = content.toString();
        text = text.replaceAll("\n"," ");
        if(text ==null || text.isEmpty())
        {
            return;
        }
        getDocName();
        getHeader();
        getText();
        // find qoutes in text
        findQuotes();
        // clean header and insert to text

        // split text by delimters
        tokens= StringUtils.splitString(text," ():?[] "); // to add ""
        cleanText();
        parseText();
        updateDoc();
    }

    private void updateDoc() {
        currDoc.addTermsToDoc(allDocTerms);
    }


    private void cleanText()
    {
        String token="";
        for(int i=currIndex; i<tokens.length;i++)
        {
            token=tokens[i];
            if(token.charAt(0)=='.'|| token.charAt(0)==',')
            {
                token=token.substring(1);
            }
            if(token.isEmpty()==false &&(token.charAt(token.length()-1)=='.' ||token.charAt(token.length()-1)==','))
            {
                token=token.substring(0,token.length()-1);
            }
            tokens[i]=token;
        }
    }

    private void findQuotes()
    {
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(text);
        while (m.find())
        {
            quotes.add(m.group(1));
        }
    }



    public void parseText()
    {
        while (currIndex<tokens.length)
        {
            if(matchToFour())
            {
                continue;
            }
            else if(matchToThree())
            {
                continue;
            }
            else if(matchToTwo())
            {
                continue;
            }
            else if(oneTerm())
            {
                continue;
            }

        }
    }


    public void createStopWords( List<String> stringList) // create stop words hash
    {
        for (String word:stringList)
        {
            stopWords.add(word);
        }
    }


    private boolean oneTerm()
    {
        String token=tokens[currIndex];
        if(!stopWords.contains(token.toLowerCase()))
        {
            makeTerm(token);
            return true;
        }
        return false;
    }

    private boolean matchToTwo() {
        return false;
    }

    private boolean matchToThree() {
        return false;
    }

    private boolean matchToFour()
    {
        if (currIndex + 3 <= tokens.length) { // can check terms in size 4
            String firstToken = tokens[currIndex];
            String secondToken = tokens[currIndex + 1];
            String thirdToken = tokens[currIndex + 2];
            String fourthToken = tokens[currIndex + 3];

            Matcher matcherPrice4 = priceLength4.matcher(" " + secondToken.toLowerCase() + " " + thirdToken + " " + fourthToken);
            if (isNum(firstToken) && matcherPrice4.find()) {
                int num = Integer.valueOf(firstToken) * transperToFormat.get(secondToken);
                newTerm = Integer.toString(num) + " M Dollars";
                currIndex=currIndex+4;
                makeTerm(newTerm);
                return true;
            }

            if (firstToken.toLowerCase().equals("between") && isNum(secondToken)
                    && (thirdToken.toLowerCase().equals("and") || thirdToken.toLowerCase().equals("to")) && isNum(fourthToken)) {
                newTerm = firstToken + " " + secondToken + " " + thirdToken + " " + fourthToken;
                currIndex=currIndex+4;
                makeTerm(newTerm);
                return true;
            }
            return false;
        }
        return false;
    }

    private void makeTerm(String newTerm) {
        if(allDocTerms.containsKey(newTerm))
             allDocTerms.put(newTerm,allDocTerms.get(newTerm)+1);
        else
            allDocTerms.put(newTerm,1);
    }


    private Boolean isNum(String val){
        return (intPattern.matcher(val).find()|| digitPattern.matcher(val).find()||
                doublePattern.matcher(val).find()|| fractionPattern.matcher(val).find());
    }


    private String[] extractOneWordExpression(int place, String kind) {
        String []info=new String[2];
        place++;
        String phrase="";
        while(!tokens[place].equals(kind)) {
            phrase = phrase + tokens[place];
            place++;}
        info[0]= Integer.toString(place);
        info[1]=phrase;
        return info;
    }


    private void getText()
    {
        Pattern p = Pattern.compile("<TEXT>(.*)</TEXT>");
        Matcher m = p.matcher(text);
        while (m.find()) {
            text =m.group(1);
        }
    }

    private void getHeader()
    {
        Pattern p = Pattern.compile("<HEADER>(.*)</HEADER>");
        Matcher m = p.matcher(text);
        while (m.find()) {
            header =m.group(1);
        }

    }

    private void getDocName()
    {
        Pattern p = Pattern.compile("<DOCNO>(.*)</DOCNO>");
        Matcher m = p.matcher(text);
        String docName="";
        while (m.find()) {
            docName =m.group(1);
        }
        currDoc.setDocName(docName);
    }

}
