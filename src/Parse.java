import com.sun.deploy.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {

    private static HashSet<String> stopWords;
    private HashMap<String, Integer>transperToFormat;
    private List<String> quotes;
    private String[] tokens;
    private String text;
    private String header;
    private Document currDoc;
    private int currIndex;
    private String newTerm;
    private String[] info= new String[2];

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
        tokens= StringUtils.splitString(text," ():?[] ");
        cleanText();
        parseText();
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
            currDoc.add(token);
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
                return true;
            }

            if (firstToken.toLowerCase().equals("between") && isNum(secondToken)
                    && (thirdToken.toLowerCase().equals("and") || thirdToken.toLowerCase().equals("to")) && isNum(fourthToken)) {
                newTerm = firstToken + " " + secondToken + " " + thirdToken + " " + fourthToken;
                return true;
            }
            return false;
        }
        return false;
    }



    private Boolean isNum(String val){
        return (intPattern.matcher(val).find()|| digitPattern.matcher(val).find()||
                doublePattern.matcher(val).find()|| fractionPattern.matcher(val).find());
    }



    private String clearTheToken(String token) {
        String pureToken= token.replaceAll(":","");
        pureToken= token.replaceAll(";","");

        return pureToken;
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
