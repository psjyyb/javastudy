import java.util.Scanner;

public class Hellowolrd {

	public static void main(String[] args) {

		Scanner sc = new  Scanner(System.in);
		int a= sc.nextInt();
		String s = "*";
		for(int i = 1 ;  i<=a; i++) {
			for(int j=1; j<=a-i; j++) {
				System.out.print(" ");
			}			
			for(int k= 1; k<=i; k++) {
			System.out.print(s);
			}
			System.out.println();
		}			
	}	
}