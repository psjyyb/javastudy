package org.opentutorials.javatutorials.scope;

public class ScopeDemo {
	int k = 10;
	static void a() {
		int i = 0;
	}
	
	public static void main(String[] args) {
		for(int i=0; i<5; i++) {
			a();
			System.out.println(i);
		}
	}

}
