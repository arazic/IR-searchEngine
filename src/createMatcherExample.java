import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class createMatcherExample {

        public static void main(String[] args) {

            String text    =
                    "This is the text to be searched " +
                            "for occurrences of the http:// pattern.";

            String patternString = ".*http://.*";

            Pattern pattern = Pattern.compile(patternString);

            Matcher matcher = pattern.matcher(text);
            boolean matches = matcher.matches();

            System.out.println(matches);
        }
    }

