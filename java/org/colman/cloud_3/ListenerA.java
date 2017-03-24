package org.colman.liberty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.taglibs.standard.lang.jpath.expression.Parser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.mqlight.api.CompletionListener;
import com.ibm.mqlight.api.Delivery;
import com.ibm.mqlight.api.DestinationAdapter;
import com.ibm.mqlight.api.NonBlockingClient;
import com.ibm.mqlight.api.NonBlockingClientAdapter;
import com.ibm.mqlight.api.StringDelivery;
import com.ibm.mqlight.api.SubscribeOptions;
import com.ibm.wsspi.http.HttpResponse;



@WebServlet(urlPatterns = "/PostListener", loadOnStartup = 1)
public class ListenerA extends HttpServlet {
	
	private static final long serialVersionUID = 5159297990148132156L;
	
	private static final String SUBSCRIBE_TOPIC = "mqlight/samplePosts";
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write("All Good on A");
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
	
	/*
	 * PostListener will demonstrate accessing the DB using generic HTTP Methods (just as we did in EX3)
	 * 
	 * the Expected JSON format for this message is:
	 * {"user" : "", "post" : "" , "tags" : ["","",""...]}
	 * this listener will store in DB a doc in the following format:
	 * {"_id" : "user:...:post:..." "_rev" : "", "content" : "..." }
	 */
	
	void ProcessMessage(String input) {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			System.out.println("New Post Arrived");
			HttpPost postRequest = new HttpPost("https://282a72b9-3652-45ce-9da5-01e77457e72a-bluemix.cloudant.com/algawidb/");
			postRequest.addHeader("Content-Type","application/json");
			
			JsonParser parse = new JsonParser();
			Gson gson = new Gson();
		
			
			
			
			JsonObject elem = parse.parse(input).getAsJsonObject();
			HashMap<String, String> output = new HashMap<>();
			output.put("_id", String.format("user:%s:post:%s", elem.get("user").getAsString(), UUID.randomUUID().toString()));
			output.put("content", elem.get("post").getAsString());
			output.put("type", "post");
			postRequest.setHeader("Accept","application/json");
			postRequest.setHeader("Content-Type", "application/json");
			System.out.println(gson.toJson(output));
			postRequest.setEntity(new StringEntity(gson.toJson(output)));
			org.apache.http.HttpResponse resp = client.execute(postRequest);
			System.out.println(resp.getStatusLine());
			BufferedReader br = new BufferedReader(new InputStreamReader(((StringEntity)resp.getEntity()).getContent()));
			String line = null;
			while ((line = br.readLine())!= null)
				System.out.println(line);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 
		
	}
	
}
