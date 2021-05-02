package Milestone1_main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class RetrieveTicketsID {

   private static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }

   public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try {
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
         String jsonText = readAll(rd);
         JSONArray json = new JSONArray(jsonText);
         return json;
       } finally {
         is.close();
       }
   }
   
   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try {
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
         String jsonText = readAll(rd);
         JSONObject json = new JSONObject(jsonText);
         return json;
       } finally {
         is.close();
       }
   }
   
   public static Date parseStringToDate(String string) throws ParseException{
	   
	   String format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	   Date date = new SimpleDateFormat(format).parse(string); 
	   
	   return date;
   }

   //main
  public static void main(String[] args) throws IOException, JSONException, ParseException {
		   
		   String projName ="RAMPART";
	   Integer j = 0, i = 0, total = 1;
      //Get JSON API for closed bugs w/ AV in the project
      do {
         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
         j = i + 1000;
         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projName + "%22AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                + i.toString() + "&maxResults=" + j.toString();

         //String [] ticket_array = new String[1000];
         JSONObject json = readJsonFromUrl(url);
         JSONArray issues = json.getJSONArray("issues");
         ArrayList<Date> ticket_array = new ArrayList<Date>();
         total = json.getInt("total");
         for (; i < total && i < j; i++) {
         	JSONObject field = issues.getJSONObject(i%1000);
         	String field_object = field.getJSONObject("fields").get("resolutiondate").toString();
         	ticket_array.add(parseStringToDate(field_object));
       	 }
         ticket_array.sort(null);
         int arraysize = ticket_array.size();
         
         //parte da sistemare
         ArrayList<Integer> arrayfinale = new ArrayList<Integer>();
         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
         cal.setTime(ticket_array.get(0));
         int start_year = cal.get(Calendar.YEAR);
         cal.setTime(ticket_array.get(arraysize-1));
         int end_year = cal.get(Calendar.YEAR);
         int my_month = ((end_year+1)-start_year)*12;
         for(int ii=0; ii<my_month; ii++) 
         {
        	 arrayfinale.add(0);
         }
         for(int counter =0; counter<arraysize;counter++) 
         {
        	 int ticket_counter=1;
        	 cal.setTime(ticket_array.get(counter));
        	 int year = cal.get(Calendar.YEAR);
             int month = cal.get(Calendar.MONTH);
             for(int counter2=1+counter; counter2<arraysize; counter2++) 
             {
            	 
            	 cal.setTime(ticket_array.get(counter2));
            	 int year2 = cal.get(Calendar.YEAR);
                 int month2 = cal.get(Calendar.MONTH);
            	 if(month==month2 && year==year2) 
            	 {
            		 ticket_counter = ticket_counter+1;
            		 counter=counter+1;
            	 }
            	 else
            		 break;
             }
             int index = ((year-start_year)*12)+month;
             arrayfinale.set(index, ticket_counter);
         }
        
         int somma=0;
         for(int e : arrayfinale) 
         {
        	  somma = somma+e;
         }
         for(Date elem : ticket_array) 
         {
        	 System.out.println(elem);
         } 
         
         
       
         
      } while (i < total);
      return;
   }

 
}
