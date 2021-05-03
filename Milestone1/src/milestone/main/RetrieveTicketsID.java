package milestone.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RetrieveTicketsID {

	static Integer max = 1;
	static Integer index;
	private static final Logger LOGGER = Logger.getLogger(RetrieveTicketsID.class.getName());


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
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));) {
			String jsonText = readAll(rd);
			return new JSONArray(jsonText);
		}

		finally {
			is.close();
		}
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));) {
			String jsonText = readAll(rd);
			return new JSONObject(jsonText);
		} finally {
			is.close();
		}
	}

	public static Date parseStringToDate(String string) throws ParseException {

		String format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
		return new SimpleDateFormat(format).parse(string);
	}

	public static List<Date> ticketArray(String url, int i, int j) throws IOException, JSONException, ParseException {

		JSONObject json = readJsonFromUrl(url);
		JSONArray issues = json.getJSONArray("issues");
		ArrayList<Date> ticketarray = new ArrayList<>();
		max = json.getInt("total");
		for (; i < max && i < j; i++) {
			JSONObject field = issues.getJSONObject(i % 1000);
			String fieldobject = field.getJSONObject("fields").get("resolutiondate").toString();
			ticketarray.add(parseStringToDate(fieldobject));
		}
		index = i;
		return ticketarray;
	}

	public static List<Integer> getTimeArray(Calendar cal, List<Date> ticketarray, int arraysize) {
		// Ritorna un array con elemento 0 = data iniziale, elemento 1 = data finale,
		// elemento 3 = numero di mesi
		cal.setTime(ticketarray.get(0));
		ArrayList<Integer> timearray = new ArrayList<>();
		timearray.add(cal.get(Calendar.YEAR));
		cal.setTime(ticketarray.get(arraysize - 1));
		timearray.add(cal.get(Calendar.YEAR));
		timearray.add(((timearray.get(1) + 1) - timearray.get(0)) * 12);
		return timearray;
	}
	


	public static List<Integer> setArray(int arraysize, Calendar cal, List<Date> ticketarray, List<Integer> timearray,
			List<Integer> arrayparziale) {
		int k = 0;
		for (int i = 0; i < arraysize; i=i+k+1) {
			int ticketcounter = 1;
			k = 0;
			cal.setTime(ticketarray.get(i));
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			for (int j = i + 1; j < arraysize; j++) {
				cal.setTime(ticketarray.get(j));
				int secondyear = cal.get(Calendar.YEAR);
				int secondmonth = cal.get(Calendar.MONTH);
				if (month == secondmonth && year == secondyear) {
					ticketcounter = ticketcounter + 1;
					k = k + 1;
				} 
				
				else
					break;
			}
			int index = ((year - timearray.get(0)) * 12) + month;
			arrayparziale.set(index, ticketcounter);
		}
		return arrayparziale;
	}

	public static void csvWriter(List<Integer> arrayfinale, int startyear) throws IOException {
		int sumElemDataArrayFinal = 0;
		try (BufferedWriter br = new BufferedWriter(new FileWriter("/Users/mirko/Desktop/result_file.csv"))) {
			// Write header of the csv file produced in output
			StringBuilder sb = new StringBuilder();
			sb.append("type fixed: ");
			sb.append(",");
			sb.append("Resolution date");
			sb.append("\n");
			br.write(sb.toString());

			int indexYear = 0;
			int indexMonth = 1;
			for (int elemDataArrayFinal : arrayfinale) {
				sumElemDataArrayFinal = sumElemDataArrayFinal + elemDataArrayFinal;
				if (indexMonth == 13) {
					indexMonth = 1;
					indexYear++;
				}
				int year = startyear + indexYear;
				String dateForDataSet = indexMonth + "/" + year;

				// Write data in csv file produced in output
				StringBuilder sb2 = new StringBuilder();
				sb2.append(elemDataArrayFinal);
				sb2.append(",");
				sb2.append(dateForDataSet);
				sb2.append("\n");
				br.write(sb2.toString());

				indexMonth++;
			}
		}
	}

	// main
	public static void main(String[] args) throws IOException, JSONException, ParseException {
		Integer j = 0;
		Integer i = 0;
		String projName = "RAMPART";

		// Get JSON API for closed bugs w/ AV in the project
		do {
			// Only gets a max of 1000 at a time, so must do this multiple times if bugs
			// >1000
			j = i + 1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + projName
					+ "%22AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
					+ i.toString() + "&maxResults=" + j.toString();
			List<Date> ticketarray = ticketArray(url, i, j);
			ticketarray.sort(null);
			int arraysize = ticketarray.size();
			i = index;
			List<Integer> arrayparziale = new ArrayList<>();
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
			List<Integer> timearray = getTimeArray(cal, ticketarray, arraysize);
			for (int ii = 0; ii < timearray.get(2); ii++) {
				arrayparziale.add(0);
			}
			List<Integer> arrayfinale = setArray(arraysize, cal, ticketarray, timearray, arrayparziale);
			int somma = 0;
			for (int e : arrayfinale) {
				somma = somma + e;
			}
			for (Date elem : ticketarray) {
				LOGGER.log(Level.INFO, String.valueOf(elem));
			}
			csvWriter(arrayfinale,timearray.get(0));
			LOGGER.log(Level.INFO, "CSV written");

		} while (i < max);
	}

}
