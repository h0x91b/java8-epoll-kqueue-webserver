package server;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HTTPRequest {
	 
    private final String raw;
    private String method;
    private String location;
    private String version;
    private String content;
    private Map<String, String> headers = new HashMap<String, String>();

    public HTTPRequest(String raw) {
        this.raw = raw;
        parse();
    }

    private void parse() {
        // parse the first line
        StringTokenizer tokenizer = new StringTokenizer(raw);
        method = tokenizer.nextToken().toUpperCase();
        location = tokenizer.nextToken();
        version = tokenizer.nextToken();
        // parse the headers
        String[] lines = raw.split("\r\n");
        int offset = lines[0].length() + 2;
        for (int i = 1; i < lines.length; i++) {
        	if(lines[i].isEmpty()) {
        		//end of head
        		offset+=2;
        		break;
        	}
            String[] keyVal = lines[i].split(": ", 2);
            headers.put(keyVal[0], keyVal[1]);
            offset+=lines[i].length() + 2;
        }
        if(method.equals("POST") || method.equals("PUT")) {
        	String contentLengthStr = headers.get("Content-Length");
        	if(contentLengthStr == null) {
        		content = "";
        	} else {
        		//TODO: use content length for body...
        		//int contentLength = Integer.parseInt(contentLengthStr);
        		content = raw.substring(offset);
        	}
        } else {
        	content = "";
        }
    }

    public String getMethod() {
        return method;
    }

    public String getLocation() {
        return location;
    }

    public String getHead(String key) {
        return headers.get(key);
    }
    
    public String getContent() {
    	return content;
    }
    
}
