package org.colman.cloud_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/*
 * Nir Sela, ID: 203305701
 * Omer Mintz, ID: 311510879
 */


class Operation{
	String key;
	int value;
	
	@Override
	public String toString() {
		return String.format("Account_id = %s, amount = %d",key,value);
	}
}

class Transcation {
	public Transcation(String id, String from, String to, int amount) {
		this._id = id;
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.type = "transaction";
	}
	String _id;
	String type;
	String from;
	String to;
	int amount;
}

class Account {
	public Account(String name, String id) {
		this.name = name;
		this._id = id;
	}
	
	String name;
	String _id;
}


public class App 
{
	public static void main(String[] args) throws Exception {
		LocalDate localDate = LocalDate.now();
        System.out.println(DateTimeFormatter.ofPattern("d/M/yyy").format(localDate));
		Scanner reader = new Scanner(System.in);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int input;
		String id;
		do {
			System.out.println("\nWelcome to the Bank. \nEnter a number in order to execute an operation\n" +
							   "1:Add a new account.\n2:Delete an account.\n3.Add a transaction.\n" +
							   "4:Remove a transaction.\n5:Calc amount.\n6:Get account by date.\n7:Exit.");
			input = reader.nextInt();
			switch (input) {
				case 1:
					System.out.println("Enter your name: ");
					String name = br.readLine();
					System.out.println("Enter your ID: ");
					id = br.readLine();
					AddAccount(new Account(name, id));
					break;
				case 2:
					System.out.println("Enter your ID: ");
					id = br.readLine();
					DeleteAccount(id);
					break;
				case 3:
					System.out.println("Enter a transaction ID: ");
					id = br.readLine();
					System.out.println("Enter from account ID: ");
					String from = br.readLine();
					System.out.println("Enter to account ID: ");
					String to = br.readLine();
					System.out.println("Enter the amount: ");
					int amount = reader.nextInt();
					AddTransaction(new Transcation(id, from, to, amount));
					break;
				case 4:
					System.out.println("Enter a transaction ID: ");
					id = br.readLine();
					DeleteTransaction(id);
					break;
				case 5:
					CalcAmount();
					break;
				case 6:
					
					break;
			}	
		} while (input != 7);
		
		reader.close();
	}
	
	private static void CalcAmount() throws UnsupportedEncodingException, IOException, ClientProtocolException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet("https://nirsela993.cloudant.com/nirseladb/_design/calc%20amount/_view/calc-amount?limit=20&group=true&reduce=true");
	 	HttpResponse resp = client.execute(getRequest);
	 	Gson gson = new Gson();
	 	JsonElement result = (gson.fromJson(new InputStreamReader(resp.getEntity().getContent()), JsonElement.class));
	 	JsonArray jarray =  result.getAsJsonObject().get("rows").getAsJsonArray();
	 	
	 	for (JsonElement elem : jarray) {
	 		Operation tran = gson.fromJson(elem, Operation.class);
	 		System.out.println(tran);
	 	}
	}

	private static void AddTransaction(Transcation transcation) throws ClientProtocolException, IOException {
		if (!IsAccountExist(transcation.from) || !IsAccountExist(transcation.to)){
			System.out.println("Error: one of the accounts IDs (from / to) doesn't exist.");
			return;
		}
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost("https://nirsela993.cloudant.com/nirseladb");
		postRequest.addHeader("Content-Type", "application/json");
		Gson gson = new Gson();
		postRequest.setEntity(new StringEntity(gson.toJson(transcation, Transcation.class)));
		HttpResponse resp = client.execute(postRequest);
		
		if (resp.getStatusLine().getStatusCode() == 409){
			System.out.println(String.format("Error: Could not add the transaction because the ID (%s) is already exist!", transcation._id));
			return;
		}
		System.out.println(resp.getStatusLine().getReasonPhrase());
	}
	
	private static boolean IsAccountExist(String id) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet("https://nirsela993.cloudant.com/nirseladb/" + id);
	 	HttpResponse resp = client.execute(getRequest);
	 	if (resp.getStatusLine().getStatusCode() == 404){
	 		return false;
	 	}

		return true;
	}
	
	private static void DeleteTransaction(String id) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet("https://nirsela993.cloudant.com/nirseladb/" + id);
	 	HttpResponse resp = client.execute(getRequest);
	 	Gson gson = new Gson();
	 	if (resp.getStatusLine().getStatusCode() == 404){
	 		System.out.println("Could not find transaction with ID " + id);
	 		return;
	 	}
	 	
	 	JsonElement result = (gson.fromJson(new InputStreamReader(resp.getEntity().getContent()), JsonElement.class));
	 	String _rev =  result.getAsJsonObject().get("_rev").getAsString();
	 	
		HttpDelete httpDelete = new HttpDelete("https://nirsela993.cloudant.com/nirseladb/" + id + "?rev=" + _rev);
		httpDelete.setHeader("Accept", "application/json");
		HttpResponse response = client.execute(httpDelete);
		System.out.println(response.getStatusLine().getReasonPhrase());
	}
	
	private static void AddAccount(Account account) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost("https://nirsela993.cloudant.com/nirseladb");
		postRequest.addHeader("Content-Type", "application/json");
		Gson gson = new Gson();
		postRequest.setEntity(new StringEntity(gson.toJson(account, Account.class)));
		HttpResponse resp = client.execute(postRequest);
		if (resp.getStatusLine().getStatusCode() == 409){
			System.out.println(String.format("Error: Could not add the account because the ID (%s) is already exist!", account._id));
			return;
		}
		System.out.println(resp.getStatusLine().getReasonPhrase());
	}
	
	private static void DeleteAccount(String id) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet("https://nirsela993.cloudant.com/nirseladb/" + id);
	 	HttpResponse resp = client.execute(getRequest);
	 	Gson gson = new Gson();
	 	if (resp.getStatusLine().getStatusCode() == 404){
	 		System.out.println("Could not find account with ID " + id);
	 		return;
	 	}
	 	
	 	JsonElement result = (gson.fromJson(new InputStreamReader(resp.getEntity().getContent()), JsonElement.class));
	 	String _rev =  result.getAsJsonObject().get("_rev").getAsString();
	 	
		HttpDelete httpDelete = new HttpDelete("https://nirsela993.cloudant.com/nirseladb/" + id + "?rev=" + _rev);
		httpDelete.setHeader("Accept", "application/json");
		HttpResponse response = client.execute(httpDelete);
		System.out.println(response.getStatusLine().getReasonPhrase());
	}
	
	private static void GetAmountByDate() {
		
	}
}
