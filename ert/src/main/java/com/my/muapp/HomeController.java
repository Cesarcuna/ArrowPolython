package com.my.muapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	@RequestMapping(value = "/bbva", method = RequestMethod.GET)
	public JsonObject bbva() {
		String url = "https://connect.bbva.com/token?grant_type=client_credentials";

		URL obj;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();	
			// optional default is GET
			con.setRequestMethod("POST");	
			//add request header
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Basic YXBwLmJidmEucHJ1ZWJhcG9seTolRmhzbUBsRSRxJW9IS04xalkqV08lYk14NkFzRE9KbkhBdmkzUipUVDhYRzdmemwxZSVNbXZQSDVkQEJUZjQ4");
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);	
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();	
			while ((inputLine = in.readLine()) != null) response.append(inputLine);			
			in.close();
			JsonParser parser = new JsonParser();
			JsonObject o = parser.parse(response.toString()).getAsJsonObject();
			String token= o.get("access_token").getAsString();
			String toke_type= o.get("token_type").getAsString();
			
			url = "https://apis.bbva.com/paystats_sbx/4/tiles/40.425/-3.715/consumption_pattern?max_date=201510&min_date=201501";
			obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();	
			con.setRequestMethod("GET");			
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization",toke_type+ " " + token);
			responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);	
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			inputLine=null;
			response = new StringBuffer();	
			while ((inputLine = in.readLine()) != null) response.append(inputLine);			
			in.close();	
			parser = new JsonParser();
			o = parser.parse(response.toString()).getAsJsonObject();
			JsonArray array= o.get("data").getAsJsonArray();
			System.out.println("array  " + array);	
			tensor();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void tensor(){
		try (Graph g = new Graph()) {
		      final String value = "Hello from " + TensorFlow.version();

		      // Construct the computation graph with a single operation, a constant
		      // named "MyConst" with a value "value".
		      try (Tensor t = Tensor.create(value.getBytes("UTF-8"))) {
		        // The Java API doesn't yet include convenience functions for adding operations.
		        g.opBuilder("Const", "MyConst").setAttr("dtype", t.dataType()).setAttr("value", t).build();
		      }catch(Exception e){
			    	e.printStackTrace();
			  }

		      // Execute the "MyConst" operation in a Session.
		      try (Session s = new Session(g);
		        Tensor output = s.runner().fetch("MyConst").run().get(0)) {
		        System.out.println(new String(output.bytesValue(), "UTF-8"));
		      }catch(Exception e){
			    	e.printStackTrace();
			    }
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
	}
}
