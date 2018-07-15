package resources;

import java.io.File;

public final class Test2 {
	
	public String name;
	public File file;
	public String tag;
	protected void makeName() {
	     name = file.getName();
	     int dot = name.lastIndexOf(tag);
	     name = name.substring(0, dot);
	     System.out.println("test");
	}

}