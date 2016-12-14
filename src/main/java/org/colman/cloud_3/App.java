package org.colman.cloud_3;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Type;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Hello world!
 */
 
public class App 
{
	public static void main(String[] args) throws Exception {
		//PUTExample();
		//GETExample();
		POSTWithObject();
	}
	
	private static void GETExample() throws UnsupportedEncodingException, IOException, ClientProtocolException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet("https://282a72b9-3652-45ce-9da5-01e77457e72a-bluemix.cloudant.com/bank/_design/actions/_view/new-view?limit=20&reduce=false");
	 	HttpResponse resp = client.execute(getRequest);
		
	 	System.out.printf("requst response status code %d\n", resp.getStatusLine().getStatusCode());
	 	Gson gson = new Gson();
	 	
	 	
	 	
	 	//HashMap<String, Object> result = (gson.fromJson(new InputStreamReader(resp.getEntity().getContent()), HashMap.class));
	 	//System.out.println(result.get("rows"));	
	 	//Account[] accounts = gson.fromJson(result.get("rows").toString(), Account[].class);
	 	
	 	
	 	JsonElement result = (gson.fromJson(new InputStreamReader(resp.getEntity().getContent()), JsonElement.class));
	 	
	 	JsonArray jarray =  result.getAsJsonObject().get("rows").getAsJsonArray();
	 	
	 	for (JsonElement elem : jarray) {
	 		Operation tran = gson.fromJson(elem, Operation.class);
	 		System.out.println(tran);
	 	}
	}

	private static void PUTExample() throws UnsupportedEncodingException, IOException, ClientProtocolException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPut putRequest = new HttpPut("https://282a72b9-3652-45ce-9da5-01e77457e72a-bluemix.cloudant.com/bank/account:shauli");
		putRequest.addHeader("Content-Type", "application/json");
		HashMap<String, String> newAccount = new HashMap<String, String>();
		newAccount.put("_id", "account:shauli");
		newAccount.put("type", "account");
		newAccount.put("name", "shauli");
		
		Gson gson = new Gson();
		StringEntity reqBody = new StringEntity(gson.toJson(newAccount));
		putRequest.setEntity(reqBody);
		HttpResponse resp = client.execute(putRequest);
		System.out.println(resp.getStatusLine());
	}

	private static void POSTWithObject() throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost("https://nirsela993.cloudant.com/nirseladb");
		postRequest.addHeader("Content-Type", "application/json");
		Gson gson = new Gson();
		Transcation t = new Transcation();
		t.amount = 1000;
		t.from = "omer";
		t.to = "nir";
		t.type = "transaction";
		t._id = "account:omer:transaction";
		
		postRequest.setEntity(new StringEntity(gson.toJson(t,Transcation.class)));
		
		HttpResponse resp = client.execute(postRequest);
		System.out.println(resp.getStatusLine());
	}
}


class Operation{
	String id;
	Object key;
	int value;
	
	@Override
	public String toString() {
		return String.format("OperationID = %s, Value = %d",id,value);
	}
}

class Transcation {
	String _id;
//	String _rev;
	String type;
	String from;
	String to;
	int amount;
}
