package resources;

public class StringEquals1 {
	public String main(String s){
		final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 1) {
                sb.append(s.charAt(i));
            }
            sb.append(s.charAt(i));
        }

        String path = sb.toString();
        if (path.equals("abc")) {
            path = path.replace('/', '\\');
        }
        return path;
	}
}
