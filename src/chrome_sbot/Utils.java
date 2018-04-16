package chrome_sbot;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

public class Utils {
	public static void main(String[] args) {
		
	}
	
	private Utils() {} // private constructor 
	
	public static void debugPrint(String string) {
		System.out.println("-- " + string + " --");
	}
	
	public static void debugPrint(int num) {
		System.out.println("-- " + num + " --");
	}
	
	// validate that all properties are present
	public static Boolean jsonFieldsNotNull(JSONObject obj, String[] fields) {
		for (String field : fields) {
			if (obj.isNull(field)) {
				System.out.println(field + " IS NULL***");
				return false;
			}
		}
		return true;
	}
	
	public static JSONObject fetchJsonObjFromFile(String path) throws Exception { // be more specific with exception
		String json = "";
        json = new String(Files.readAllBytes(Paths.get(path)));
        debugPrint(json);
//      System.out.println(json);
        JSONObject obj = new JSONObject(json);
        return obj;
	}
	
	public static JSONObject fetchJsonObjFromWeb(String path) throws Exception { // be more specific with exception
		// make some http request
		String json = "";
//		System.out.println(json);
		JSONObject obj = new JSONObject(json);
		return obj;
	}
	
}
