
public class Main {

    public static void main(String[] args) {
        System.out.println("We are Google!");
        Parse parse= new Parse();
       // ReadFile rf= new ReadFile("C:/Users/gal/Desktop/FB396001", parse);
        ReadFile rf= new ReadFile("C:/Users/user/Xcorpus", parse);
        rf.createDocuments();
        System.out.println("chen is my queen!");
	// write your code here
    }
}

