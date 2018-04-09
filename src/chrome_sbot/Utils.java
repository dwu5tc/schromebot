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
	}asd
	
	public static void debugPrint(int num) {
		System.out.println("-- " + num + " --");
	}
	
	// validate that all properties are present
	public static Boolean jsonFieldsNotNull(JSONObject obj, String[] fields) {
		for (String field : fields) {
			if (obj.isNull(field)) {
				return false;
			}
		}
		return true;
	}
	
	public static JSONObject fetchJsonFromFile(String path) throws Exception { // be more specific with exception
		String json = "";
        json = new String(Files.readAllBytes(Paths.get(path)));
        JSONObject obj = new JSONObject(json);
        return obj;
	}
	
	public static JSONObject fetchJsonFromWeb(String path) throws Exception { // be more specific with exception
		// make some http request
		String json = "";
		JSONObject obj = new JSONObject(json);
		return obj;
	} 
}
