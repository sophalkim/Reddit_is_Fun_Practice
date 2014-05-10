package ssk.project.Practice.util;

import java.util.ArrayList;
import java.util.Iterator;

public class StringUtils {

	public static boolean isEmpty(CharSequence s) {
		return s == null || s.equals("");
	}
	
	public static boolean listContainsIgnoreCase(ArrayList<String> list, String str) {
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if (string.equalsIgnoreCase(str)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsCase(ArrayList<String> list, String str) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(str)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsCaseEnhance(ArrayList<String> list, String str) {
		for (String i: list) {
			if (i.equals(str)) {
				return true;
			}
		}
		return false;
	}
}
