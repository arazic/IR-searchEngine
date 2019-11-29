import com.sun.deploy.util.StringUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {

    private static HashSet<String> stopWords = new HashSet<>();
    private static HashMap<String,String> monthMap = new HashMap<>();
    private static HashMap<String,String> unitMap = new HashMap<>();
    private static TreeSet<Term> terms = new TreeSet<>();

    private HashMap<String, Integer>transperToFormat;
    private HashMap<String,Integer> allDocTerms;
    private List<String> quotes;
    private HashMap<String,Integer> entity;

    private String[] tokens;
    private String text;
    private Document currDoc;

    private String header;
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
        allDocTerms = new HashMap<>();
        quotes = new LinkedList<>();
        entity=new HashMap<>();
        loadMonthMap();
        loadUnits();
        transperToFormat.put("million",1);
        transperToFormat.put("billion",1000);
        transperToFormat.put("trillion",1000000);
    }

    private void loadMonthMap()
    {
        monthMap.put("january","01");
        monthMap.put("jan","01");
        monthMap.put("february","02");
        monthMap.put("feb","02");
        monthMap.put("march","03");
        monthMap.put("mar","03");
        monthMap.put("april","04");
        monthMap.put("apr","04");
        monthMap.put("may","05");
        monthMap.put("june","06");
        monthMap.put("jun","06");
        monthMap.put("july","07");
        monthMap.put("jul","07");
        monthMap.put("august","08");
        monthMap.put("aug","08");
        monthMap.put("septmber","09");
        monthMap.put("sep","09");
        monthMap.put("october","10");
        monthMap.put("oct","10");
        monthMap.put("novmber","11");
        monthMap.put("nov","11");
        monthMap.put("december","12");
        monthMap.put("dec","12");
    }

    private void loadUnits()
    {
        unitMap.put("million","M");
        unitMap.put("billion","B");
        unitMap.put("$","Dollars");
        unitMap.put("m","M");
        unitMap.put("b","B");

    }

    public Term createTerm(String sTerm, int frequency)
    {
        if(sTerm==null ||sTerm==" "|| frequency<1)
        {
            System.out.println("can't create a term ");
            return null;
        }
        else
        {
            Term term = new Term(sTerm,frequency,currDoc.getDocName());
            return term;
        }

    }


    private void mergeTerms()
    {
        TreeMap<String,Integer> mapTerms = new TreeMap<>(allDocTerms);
        Set<Term> tempTerms= new TreeSet<>();
        Iterator <String> it = mapTerms.keySet().iterator();
        Iterator <Term> termIterator = terms.iterator();
        Term term = termIterator.next();
        String termString = it.next();
        boolean finish =false;
        while (it.hasNext() && termIterator.hasNext())
        {
            if(term.compareTo(termString)==0)
            {
                term.setFreq(term.getFreq()+mapTerms.get(termString));
                term.addDocToTerm(currDoc.getDocName());
                term = termIterator.next();
                termString = it.next();
            }
            else if(term.compareTo(termString)==-1)
            {
                term = termIterator.next();
            }
            else if(term.compareTo(termString)==1)
            {
               tempTerms.add(createTerm(termString,mapTerms.get(termString)));
               termString = it.next();
            }
        }
        while (it.hasNext()|| !finish)
        {
            tempTerms.add(createTerm(termString,mapTerms.get(termString)));
            if(it.hasNext())
            {
                termString = it.next();
            }
            else
            {
                finish=true;
            }

        }
        terms.addAll(tempTerms);
    }

    public void test ()
    {
        Term t1 = new Term("t1",1,"doc1");
        Term t2 = new Term(("t2"),1,"doc1");
        Term t3 = new Term("t3",1,"doc3");
        Term t4 = new Term(("t4"),1,"doc4");
        terms.add(t1);
        terms.add(t2);
        terms.add(t3);
        terms.add(t4);
        allDocTerms.put("t5",4);
        allDocTerms.put("t",2);
        allDocTerms.put("t2",5);
        currDoc = new Document();
        currDoc.setDocName("doc2");
        mergeTerms();
    }
    
    

    public void createDocument(StringBuilder content)
    {
        currDoc = new Document();
        text = content.toString();
        text = text.replaceAll("\n"," ");
        getDocName();
        getHeader();
        getText();
        // find qoutes in text
        findQuotes();
        // split text by delimters
        tokens= StringUtils.splitString(text," ():?[]!; "); // to add ""
        parseText();
        updateDoc();
    }

    private void updateDoc() {
        currDoc.addTermsToDoc(allDocTerms);
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

    private String getNumberInPriceFormat(String number)
    {
        String ans="";
        for(int i=0; i<number.length();i++)
        {
            if((number.charAt(i)>='0' && number.charAt(i)<='9') || number.charAt(i)=='.')
            {
                ans+=number.charAt(i);
            }
        }
        double num=0;
        if(number.contains("m")||number.contains("M"))
        {
            num=Double.parseDouble(ans)*1000000;
            ans=num+"";
        }
        else if(number.contains("b")||number.contains("B"))
        {
            num=Double.parseDouble(ans)*1000000000;
            ans=num+"";
        }
        else
        {
             num=Double.parseDouble(ans);
        }
        if(num >= 1000000)
        {
            num=num/1000000;
            int intNum=(int)num;
            if(num==intNum)
            {
                ans=intNum+" M";
                return ans;
            }
            ans=num+" M";
        }
        return ans;
    }





    public void parseText()
    {
        currIndex=0;
        while (currIndex<tokens.length)
        {
            String concat="";
            String cleanToken=cleanWord(tokens[currIndex]);
            if(stopWords.contains(tokens[currIndex]))
            {
                continue;
            }
            else if(monthMap.containsKey(cleanToken.toLowerCase()))
            {
                int result = isNumber(tokens[currIndex + 1]);
                if (result == 0) //result (1-31)
                {
                    concat = monthMap.get(cleanToken.toLowerCase()) + "-" + cleanWord(tokens[currIndex + 1]);
                    currIndex += 2;
                } else if (result == 1) //result is bigger than 31
                {
                    concat = tokens[currIndex+1]+ "-" + monthMap.get(cleanToken.toLowerCase());
                    currIndex += 2;
                }
            }
            else if(tokens[currIndex].charAt(0)>='A' && tokens[currIndex].charAt(0)<='Z')// is a entity
            {
                concat=tokens[currIndex++];
                while(tokens[currIndex].charAt(0)>='A' && tokens[currIndex].charAt(0)<='Z')
                {
                    concat=concat+" "+tokens[currIndex++];
                }
                concat=concat.toUpperCase();
                if(entity.containsKey(concat))
                {
                    entity.replace(concat,entity.get(concat)+1);
                }
                else
                {
                    entity.put(concat,1);
                }
            }
           else if(tokens[currIndex].charAt(0)=='$')
            {
                if(unitMap.containsKey(tokens[currIndex+1]))
                {
                    concat=tokens[currIndex]+unitMap.get(tokens[currIndex+1]);
                    currIndex+=2;
                }
                else
                {
                    concat=tokens[currIndex];
                    currIndex++;
                }
                concat=getNumberInPriceFormat(concat)+" Dollars";
            }
            System.out.println(concat);
            if(allDocTerms.containsKey(concat))
            {
                allDocTerms.replace(concat,allDocTerms.get(concat)+1);
            }
            else
            {
                allDocTerms.put(concat,1);
            }
        }
    }

    private int isNumber(String word)
    {
       word=cleanWord(word);
       try {
           int number=Integer.parseInt(word);
           if(number<31 && number>0)
           {
               return 0;
           }
           return 1;
       }
       catch (Exception e)
       {
           return -1;
       }
    }



    public String cleanWord(String word)
    {
        if(word.charAt(0)=='.'|| word.charAt(0)==',')
        {
            word=word.substring(1);
        }
        if(word.isEmpty()==false &&(word.charAt(word.length()-1)=='.' || word.charAt(word.length()-1)==','))
        {
            word=word.substring(0,word.length()-1);
        }
        return word;
    }


    public void createStopWords( List<String> stringList) // create stop words hash
    {
        for (String word:stringList)
        {
            stopWords.add(word);
        }
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
        Pattern p = Pattern.compile("<TI>(.*)</TI>");
        Matcher m = p.matcher(text);
        while (m.find()) {
            header =m.group(1);
        }
        //parse header like text and insert him to header doc

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
