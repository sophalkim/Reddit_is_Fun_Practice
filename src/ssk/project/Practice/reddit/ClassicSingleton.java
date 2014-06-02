package ssk.project.Practice.reddit;

public class ClassicSingleton {

	private static ClassicSingleton classicSingleton = null;
	
	protected ClassicSingleton() {
	}
	
	public static ClassicSingleton getInstance() {
		if (classicSingleton == null) {
			classicSingleton = new ClassicSingleton();
		}
		return classicSingleton;
	}
}
