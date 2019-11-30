import com.sun.deploy.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {

    private static HashSet<String> stopWords = new HashSet<>();
    private static HashMap<String,String> monthMap = new HashMap<>();
    private static HashMap<String,String> unitMap = new HashMap<>();
    private static TreeSet<Term> terms = new TreeSet<>();
    private static HashMap<String, String> symbols=new HashMap<>();

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
    private Pattern intPattern= Pattern.compile("[0-9]+");
    private Pattern digitPattern= Pattern.compile("\\d+");
    private Pattern doublePattern= Pattern.compile("[0-9]*"+"."+"[0-9]*");
    private Pattern fractionPattern= Pattern.compile("[0-9]*"+"/"+"[0-9]*");

    public Parse()
    {
        transperToFormat= new HashMap<>();
        allDocTerms = new HashMap<>();
        quotes = new LinkedList<>();
        entity=new HashMap<>();
        loadMonthMap();
        loadUnits();
        loadSymbols();
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

    private void loadSymbols()
    {
        symbols.put("u.s","");
        symbols.put("u.s.","");
        symbols.put("dollars"," Dollars");
        symbols.put("dollar"," Dollars");
        symbols.put("%","%");
        symbols.put("percent","%");
        symbols.put("percentage","%");
        symbols.put("$"," Dollars");
    }

    private void loadUnits()
    {
        unitMap.put("million","M");
        unitMap.put("billion","B");
        unitMap.put("trillion","T");
        unitMap.put("m","M");
        unitMap.put("bn","B");


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
        else if(number.contains("t")||number.contains("T"))
        {
            num=Double.parseDouble(ans)*1000000000*100;
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
            if(stopWords.contains(tokens[currIndex]))
            {
                continue;
            }
            else if(containsNumber(tokens[currIndex]))
            {
               if(isPureNum(tokens[currIndex]))
               {
                   concat+= tokens[currIndex];
                   currIndex++;
                   if(fractionPattern.matcher(tokens[currIndex]).find()){
                       concat+=" "+tokens[currIndex];
                       currIndex++;
                       if(symbols.containsKey(tokens[currIndex].toLowerCase())) { //dollar, precent, %
                             concat = concat + symbols.get(tokens[currIndex].toLowerCase());
                             currIndex++;
                           if(symbols.containsKey(tokens[currIndex].toLowerCase())) { //dollar, precent, %
                               concat = concat + symbols.get(tokens[currIndex].toLowerCase());
                               currIndex++;
                           }
                       }
                   }
                   String secondToken="";
                   if(unitMap.containsKey(tokens[currIndex].toLowerCase()))
                   {
                       secondToken=unitMap.get(tokens[currIndex]);
                       currIndex++;
                   }
                   if(symbols.containsKey(tokens[currIndex].toLowerCase())) // if true is a price
                   {
                       if(symbols.get(tokens[currIndex])=="%")
                       {
                           concat=concat+"%";
                       }
                       else
                       {
                           String therdToken=symbols.get(tokens[currIndex].toLowerCase());
                           currIndex++;
                           if(symbols.containsKey(tokens[currIndex].toLowerCase()))
                           {
                               therdToken =symbols.get(tokens[currIndex].toLowerCase());
                               currIndex++;
                           }
                           concat=priceHandler(concat,secondToken,therdToken);
                       }

                   }
                   else if(monthMap.containsKey(tokens[currIndex].toLowerCase()))
                   {
                       concat=monthHandler(concat,monthMap.get(tokens[currIndex].toLowerCase()));
                   }
               }
               // its not a pure number
                concat=tokens[currIndex];
                currIndex++;
                // if contains $
                if(tokens[currIndex].contains("$"))
                {
                    if(concat.charAt(0)=='$')
                    {
                        concat=concat.substring(1);

                    }
                    else if(concat.charAt(concat.length()-1)=='$')
                    {
                        concat=concat.substring(0,concat.length()-1);
                    }
                    if(unitMap.containsKey(tokens[currIndex]))
                    {
                        priceHandler(concat,tokens[currIndex],"$");
                        currIndex++;
                    }
                    else
                    {
                        priceHandler(concat,"","$");
                    }
                }
                // if contains bn/m
                if(concat.contains("m")||concat.contains("bn"))
                {
                    if(symbols.containsKey(tokens[currIndex].toLowerCase()))
                    {
                        String price=symbols.get(tokens[currIndex].toLowerCase());
                        currIndex++;
                        if (symbols.containsKey(tokens[currIndex].toLowerCase()))
                        {
                            price+=symbols.get(tokens[currIndex].toLowerCase());
                            currIndex++;
                        }
                        if(concat.contains("m")) {
                            String num = concat.substring(0, concat.length() - 1);
                            concat=priceHandler(num,"M",price);
                        }
                        else
                        {
                            String num = concat.substring(0, concat.length() - 2);
                            concat=priceHandler(num,"B",price);
                        }

                    }
                }
                //if contanis -
                // if contains %
            }
            else if(monthMap.containsKey(tokens[currIndex].toLowerCase()))
            {
                concat = monthMap.get(tokens[currIndex].toLowerCase());
                currIndex++;
                if(isMonthNumber(tokens[currIndex])!=-1)
                    concat=monthHandler(tokens[currIndex],concat);

            }
            else if(tokens[currIndex].charAt(0)>='A' && tokens[currIndex].charAt(0)<='Z')// is a entity
            {
               entityHandler();
               continue;
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

    private void handleComplexNumber()
    {

    }




    private boolean containsNumber(String word)
    {
        for(int i=0; i<10; i++)
        {
            String s=String.valueOf(i);
            if(word.contains(s))
            {
                return true;
            }
        }
        return false;
    }


    private Boolean isPureNum(String val){
       for (int i=0; i<val.length();i++)
       {
           if((val.charAt(i)<='9' && val.charAt(i)>='0') || val.charAt(i)==','||val.charAt(i)=='.'||val.charAt(i)=='/')
           {
               continue;
           }
           return false;
       }
       return true;
    }

    private String cleanPureNumber(String number)
    {
        if(number.contains(","))
        {
            number=number.replaceAll(",","");
        }
        return number;
    }
    private String priceHandler(String number,String unit, String price)
    {
        number=cleanPureNumber(number);
        String concat="";
        double num=0;
        if(unit.contains("M"))
        {
            num=Double.parseDouble(number)*1000000;
            concat=num+"";
        }
        else if(unit.contains("B"))
        {
            num=Double.parseDouble(number)*1000000000;
            concat=num+"";
        }
        else if(unit.contains("T"))
        {
            num=Double.parseDouble(number)*1000000000*1000;
            concat=num+"";
        }
        else
        {
            num=Double.parseDouble(number);
            concat=num+"";
        }
        if(num >= 1000000)
        {
            num=num/1000000;
            int intNum=(int)num;
            if(num==intNum)
            {
                concat=intNum+" M"+price;
                return concat;
            }
            concat=num+" M"+price;
        }
        return concat;
    }

    private void entityHandler()
    {
        String concat="";
        concat=tokens[currIndex++];
        while(tokens[currIndex].charAt(0)>='A' && tokens[currIndex].charAt(0)<='Z')
        {
            concat=concat+" "+tokens[currIndex++];
        }
        if(entity.containsKey(concat))
        {
            entity.replace(concat,entity.get(concat)+1);
        }
        else
        {
            entity.put(concat,1);
        }
    }
    private String monthHandler(String number, String month)
    {
        String ans="";
        if(number.length()<=2)
        {
            if(number.length()==2)
            {
                ans=month+"-"+number;
                return ans;
            }

            ans=month+"-0"+number;
            return ans;
        }
        ans=number+"-"+month;
        return ans;
    }
    private String dollarHandler()
    {
        String concat="";
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
        return concat;
    }

    private int isMonthNumber(String word)
    {
       word=cleanWord(word);
       try {
           int number=Integer.parseInt(word);
           if(number<=31 && number>0)
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
