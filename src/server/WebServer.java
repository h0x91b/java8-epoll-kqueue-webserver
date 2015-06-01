package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.aredis.cache.AsyncRedisClient;
import org.aredis.cache.AsyncRedisFactory;

import API.APICall;

public class WebServer implements Runnable {
    private Selector selector = Selector.open();
    private ServerSocketChannel server = ServerSocketChannel.open();
    private boolean isRunning = true;
    private boolean debug = true;
    private boolean isIdle = false;
    private static API.API API;
    private ArrayList<queueCmd> processing = new ArrayList<queueCmd>();
    AsyncRedisFactory f = new AsyncRedisFactory(null);
    AsyncRedisClient aredis = f.getClient("localhost");
    
    private class queueCmd {
    	APICall cmd;
    	HTTPRequest request;
    	HTTPResponse response;
    	HTTPSession session;
    }
	
	public final void run() {
		if(isRunning) {
			try {
				isIdle = true;
                selector.selectNow();
                Iterator<SelectionKey> i = selector.selectedKeys().iterator();
                while (i.hasNext()) {
                    SelectionKey key = i.next();
                    i.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    isIdle = false;
                    try {
                        // get a new connection
                        if (key.isAcceptable()) {
                            SocketChannel client = server.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                        } else if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            HTTPSession session = (HTTPSession) key.attachment();
                            if (session == null) {
                                session = new HTTPSession(client);
                                key.attach(session);
                            }
                            session.readData();
                            // decode the message
                        	HTTPRequest request = new HTTPRequest(session.getRaw());
                        	processRequest(session, request);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error handling client: " + key.channel());
                        if (debug) {
                            ex.printStackTrace();
                        } else {
                            System.err.println(ex);
                            System.err.println("\tat " + ex.getStackTrace()[0]);
                        }
                        if (key.attachment() instanceof HTTPSession) {
                            ((HTTPSession) key.attachment()).close();
                        }
                    }
                }
                if(!processing.isEmpty()) {
                	for(Iterator<queueCmd> it = processing.iterator(); it.hasNext();) {
                		queueCmd cmd = it.next();
                		if(!cmd.cmd.isComplete()) continue;
                		cmd.response.setResponseCode(cmd.cmd.getStatusCode());
                    	cmd.response.setResponseReason("OK");
                    	cmd.response.setContent(cmd.cmd.getResponse().getBytes());
                    	cmd.session.sendResponse(cmd.response);
                        cmd.session.close();
                        it.remove();
                	}
                }
            } catch (IOException ex) {
                shutdown();
                throw new RuntimeException(ex);
            }
		}
	}
	
	protected HTTPResponse processRequest(HTTPSession session, HTTPRequest request) throws IOException {
        HTTPResponse response = new HTTPResponse();
        if(request.getLocation().equals("/handler")) {
        	//curl -v -XPOST http://127.0.0.1:8080/handler -d'{"command":"PING","arguments":[{"key":"argument1","value":"some argument value"}]}'
        	String body = request.getContent();
        	
        	if(body.isEmpty() || !JSONValue.isValidJsonStrict(body)) {
        		response.setResponseCode(400);
            	response.setResponseReason("Bad request");
            	response.setContent("Body must be a valid JSON object".getBytes());
            	session.sendResponse(response);
                session.close();
            	return response;
        	}
        	
        	Object obj = JSONValue.parse(request.getContent());

        	JSONObject jObj = (JSONObject)obj;
        	String command = (String) jObj.get("command");
        	JSONArray array=(JSONArray)jObj.get("arguments");
        	
        	APICall cmd = API.process(command);
        	queueCmd queue = new queueCmd();
        	queue.cmd = cmd;
        	queue.request = request;
        	queue.response = response;
        	queue.session = session;
        	processing.add(queue);
        	cmd.process(aredis, array);
        } else {
        	response.setResponseCode(404);
        	response.setResponseReason("Not found");
        	response.setContent("Requested URL not found\r\n".getBytes());
        	session.sendResponse(response);
            session.close();
        }
        return response;
    }
	
	protected WebServer(InetSocketAddress address) throws IOException {
        server.socket().bind(address);
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
    }
	
	public final void shutdown() {
        isRunning = false;
        try {
            selector.close();
            server.close();
        } catch (IOException ex) {
        }
    }
	
	public static void main(String[] args) throws InterruptedException, IOException {
		WebServer server = new WebServer(new InetSocketAddress(8080));
		API = new API.API();
        while (true) {
            server.run();
            if(server.isIdle)
            	Thread.sleep(1);
        }
	}
	
}
