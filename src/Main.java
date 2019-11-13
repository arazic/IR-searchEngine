
public class Main {

    public static void main(String[] args) {
        System.out.println("We are Google!");
        Parse parse= new Parse();
        ReadFile rf= new ReadFile("C:/Users/user/Xcorpus", parse);
        rf.createDocuments();
	// write your code here
    }
}

