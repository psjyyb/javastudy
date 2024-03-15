package org.opentutorials.javatutorials.scope;

public class ScopeDemo4 {
	static String title = "coding everybody";
	static void a () {
		
	}
	public static void main(String[] args) {
		a();
		String title = "coding everybody~~";
		System.out.println(title);

	}

}
