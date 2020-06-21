package oop;

public class Shape {
	public static void main(String[] args) {
		Circle circle = new Circle();
		System.out.println(circle.a);
	}
}

class Circle{
	protected int a;
	public Circle() {
		this.a  = 1;
	}
}
