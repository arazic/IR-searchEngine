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

        transperToFormat.put("million",1);
        transperToFormat.put("billion",1000);
        transperToFormat.put("trillion",1000000);
    }

    private void loadMonthMap()
    {
        monthMap.put("January","01");
        monthMap.put("Jan","01");
        monthMap.put("February","02");
        monthMap.put("Feb","02");
        monthMap.put("March","03");
        monthMap.put("Mar","03");
        monthMap.put("April","04");
        monthMap.put("Apr","04");
        monthMap.put("May","05");
        monthMap.put("June","06");
        monthMap.put("Jun","06");
        monthMap.put("July","07");
        monthMap.put("Jul","07");
        monthMap.put("August","08");
        monthMap.put("Aug","08");
        monthMap.put("Septmber","09");
        monthMap.put("Sep","09");
        monthMap.put("October","10");
        monthMap.put("Oct","10");
        monthMap.put("Novmber","11");
        monthMap.put("Nov","11");
        monthMap.put("December","12");
        monthMap.put("Dec","12");
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





    public void parseText()
    {
        currIndex=0;
        while (currIndex<tokens.length)
        {
            String concat="";
            String cleanToken="";
            if(stopWords.contains(tokens[currIndex]))
            {
                continue;
            }
            else if(monthMap.containsKey(cleanToken.toLowerCase()))
            {
                int result = isNumber(tokens[currIndex + 1]);
                if (result == 0) {
                    concat = monthMap.get(cleanToken.toLowerCase()) + "-" + cleanWord(tokens[currIndex + 1]);
                    currIndex += 2;
                } else if (result == 1) {
                    concat = result + "-" + monthMap.get(cleanToken.toLowerCase());
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
           else if(tokens[currIndex].equals("$"))
            {
                currIndex++;
                if(isNum(tokens[currIndex]))
                {
                    concat=tokens[currIndex]+" ";//split the number to format and units
                    currIndex++;
                    if(isNum(tokens[currIndex]))
                    {
                        concat=concat+tokens[currIndex]+" ";
                        currIndex++;
                    }
                    if(unitMap.containsKey(tokens[currIndex]))
                    {
                        concat =concat+unitMap.get(tokens[currIndex]);
                        currIndex++;
                    }
                }
            }
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
