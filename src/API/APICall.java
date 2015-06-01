package API;

import org.aredis.cache.AsyncRedisClient;

import net.minidev.json.JSONArray;

public interface APICall {
	Boolean isComplete();
	String getResponse();
	int getStatusCode();
	void process(AsyncRedisClient redis, JSONArray args);
}