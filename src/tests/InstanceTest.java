package tests;

public class InstanceTest {
	public static void main(String[] args) {
		Animal c = new Cat();
		System.out.println(c instanceof Cat);
		System.out.println(c instanceof Dog);
		System.out.println(c instanceof Animal);
		System.out.println(getComponent(Dog.class));
	}
	
	static boolean getComponent(Class<?> type) {
		Cat cat = new Cat();
		System.out.println(type.getName());
		if(cat.getClass() == type) {
			return true;
		}
		return false;
	}
}

class Cat extends Animal {
	
}

class Dog extends Animal {
	
}

abstract class Animal {
	
}
