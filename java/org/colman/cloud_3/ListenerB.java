package org.colman.liberty;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.mqlight.api.CompletionListener;
import com.ibm.mqlight.api.Delivery;
import com.ibm.mqlight.api.DestinationAdapter;
import com.ibm.mqlight.api.NonBlockingClient;
import com.ibm.mqlight.api.NonBlockingClientAdapter;
import com.ibm.mqlight.api.StringDelivery;
import com.ibm.mqlight.api.SubscribeOptions;

@WebServlet(urlPatterns = "/TagListener", loadOnStartup = 1)
public class ListenerB extends HttpServlet {
	
	private static final long serialVersionUID = 5159297990148132156L;
	
	private static final String SUBSCRIBE_TOPIC = "mqlight/samplePosts";
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write("All Good on B");
	}
	
	
	@Override
	public void init() throws ServletException {
		super.init();
		
		NonBlockingClient.create(null, new NonBlockingClientAdapter<Void>() {
			@Override
			public void onStarted(NonBlockingClient client, Void context) {
				SubscribeOptions opts = SubscribeOptions.builder().build();
				client.subscribe(SUBSCRIBE_TOPIC, opts, new DestinationAdapter<Void>() {
					public void onMessage(NonBlockingClient client, Void context, Delivery delivery) {
						System.out.println("messege recieved from queue");
						StringDelivery sd = (StringDelivery)delivery;
						try {
							ProcessMessage(sd.getData());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					};
				}, new CompletionListener<Void>() {
					@Override
					public void onSuccess(NonBlockingClient client, Void context) {
						System.out.println("read from queue");
					}
					
					@Override
					public void onError(NonBlockingClient client, Void context, Exception exception) {
						System.out.println("something went wrong with queue");
						
					}
				},null);
				
				
			}
		},null);
	
		
	}
	
	
	void ProcessMessage(String input) throws Exception {
		
		CloudantClient cc = ClientBuilder.account("282a72b9-3652-45ce-9da5-01e77457e72a-bluemix").username("wastezeardisimetingildow").password("50e47e4b6f5426cdcee3e3b3ee424bb3dece79d0").build();
		
		Database db = cc.database("algawidb", false);
		JsonParser parse = new JsonParser();
		Gson gson = new Gson();
	
		
		
		
		JsonObject elem = parse.parse(input).getAsJsonObject();
		JsonArray jr = elem.get("tags").getAsJsonArray();
		
		for (int i=0; i< jr.size(); i++)
		{
			HashMap<String, String> output = new HashMap<>();
			output.put("_id", String.format("user:%s:tag:%s", jr.get(i).getAsString(), UUID.randomUUID().toString()));
			output.put("tagger", elem.get("user").getAsString());
			output.put("type", "tag");
			output.put("content", elem.get("post").getAsString());
			db.post(output);
		}
		
		
	}
	
}
