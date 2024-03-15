package org.opentutorials.javatutorials.scope;

class C{
	int v = 10; // 전역변수
	
	void m() {
		int v=20; // 지역변수 (this 가 없을때에 우선 순위)
		System.out.println(v);
		System.out.println(this.v );
	}
}
class d extends C{
	int v =30;
	
	void n() {
		int v=40;
		System.out.println(super.v);
		System.out.println(this.v);
		System.out.println(v);
	}
}
public class ScopeDemo7 {

	public static void main(String[] args) {
		 d c1 = new d();
		 c1.n();
		 

	}

}
  //this 와 super