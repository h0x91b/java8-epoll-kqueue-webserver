package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

public final class HTTPSession {
	private Charset charset = Charset.forName("UTF-8");
	private CharsetEncoder encoder = charset.newEncoder();
    private final SocketChannel channel;
    private final ByteBuffer buffer = ByteBuffer.allocate(2048);
    private String raw;
    private int mark = 0;
    private int lastIndex = 0;

    public HTTPSession(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * Get more data from the stream.
     */
    public void readData() throws IOException {
        buffer.limit(buffer.capacity());
        int read = channel.read(buffer);
        if (read == -1) {
            throw new IOException("End of stream");
        }
        buffer.flip();
        buffer.position(mark);
        
    	raw = new String( buffer.array(), Charset.forName("UTF-8") );
    }
    
    public String readLine() throws IOException {
    	int index = raw.indexOf("\r\n", lastIndex);
    	if(index == -1) {
    		return null;
    	}
    	String ret = raw.substring(lastIndex, index);
    	lastIndex = index + 2;
    	return ret;
    }
    
    public String getRaw() {
    	return raw;
    }

    private void writeLine(String line) throws IOException {
        channel.write(encoder.encode(CharBuffer.wrap(line + "\r\n")));
    }

    public void sendResponse(HTTPResponse response) {
        response.addDefaultHeaders();
        try {
            writeLine(response.getVersion() + " " + response.getResponseCode() + " " + response.getResponseReason());
            for (Map.Entry<String, String> header : response.headers.entrySet()) {
                writeLine(header.getKey() + ": " + header.getValue());
            }
            writeLine("");
            channel.write(ByteBuffer.wrap(response.getContent()));
        } catch (IOException ex) {
            // slow silently
        }
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException ex) {
        }
    }
}
