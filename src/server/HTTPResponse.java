package server;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPResponse {
	 
    private String version = "HTTP/1.1";
    private int responseCode = 200;
    private String responseReason = "OK";
    public Map<String, String> headers = new LinkedHashMap<String, String>();
    private byte[] content;

    void addDefaultHeaders() {
        headers.put("Date", new Date().toString());
        headers.put("Server", "Java NIO Webserver by md_5");
        headers.put("Connection", "Keep-Alive");
        headers.put("Content-Length", Integer.toString(content.length));
    }
    
    public String getVersion() {
    	return version;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseReason() {
        return responseReason;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public byte[] getContent() {
        return content;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseReason(String responseReason) {
        this.responseReason = responseReason;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }
}
