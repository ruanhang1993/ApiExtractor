package resources;

import java.io.File;

public class DeleteFileCase2 {
	public void test(String s){
		File f = new File(s);
		boolean deleted = f.delete();
		if(!deleted){
			System.out.println(s);
		}
	}
}
