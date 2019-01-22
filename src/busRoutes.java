/*
 * 	Jonathan Wishcamper
 *	CS320 Assignment 1
 *		Winter 2019
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class busRoutes {
	public static void main(String[] args) throws Exception {
		URL schu = new URL("https://www.communitytransit.org/busservice/schedules");
		HttpURLConnection sch = (HttpURLConnection)schu.openConnection();
		sch.setRequestProperty("user-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		BufferedReader in = new BufferedReader(new InputStreamReader(sch.getInputStream(), Charset.forName("UTF-8")));
		String inputLine = "";
		String text = "";
		while((inputLine = in.readLine()) != null)
			text += inputLine + "\n";
		in.close();
		sch.disconnect();
		//System.out.println(text); //full contents of the web page
		boolean b = false;
		Scanner s = new Scanner(System.in);
		System.out.print("Please enter the letter that your destination starts with: ");
		char userInput = s.next().charAt(0); //just gets the first character of user input
		if(!Character.isLetter(userInput)) {
			System.out.println("Invalid input - please enter a letter only.");
			System.exit(0);
		}
		userInput = Character.toUpperCase(userInput); //make sure it's uppercase for consistency
		Pattern dest = Pattern.compile("<h3>("+userInput+".*)</h3>([\\s\\S]*?)<hr"); //regex to find destination
		Pattern busNum = Pattern.compile("<strong><a.*>(.*)</a></strong>"); //regex to find bus number
		Matcher destMatch = dest.matcher(text);
		String destSpecific = "";

		while(destMatch.find()) {
			b=true;
			destSpecific = destMatch.group(2); //everything after one destination and before next
			Matcher busNumMatch = busNum.matcher(destSpecific); //search only after </h3> and before next destination for bus numbers
			System.out.println("Destination: " + destMatch.group(1));
			while(busNumMatch.find()) 
				System.out.println("Bus Number: " + busNumMatch.group(1));	
			System.out.println("--------------------------");
		}
		if(!b) { //if no matches, there were no destinations starting with the user's input
			System.out.println("There are no bus routes for a destination starting with "+userInput+".");
			System.exit(0);
		}
		System.out.print("Please enter a route ID as a string: ");
		s.nextLine();
		String routeInput = s.nextLine();
		if(routeInput.length() == 7) { //change / to - to match url pattern
			String temp = routeInput;
			routeInput = temp.substring(0,3)+"-"+temp.substring(4);
		}		
		else if(routeInput.contains("*")) { //truncate *'s because they do not appear in url
			String temp = routeInput;
			routeInput = temp.substring(0,3);
		}
		URL rte = new URL("https://www.communitytransit.org/busservice/schedules/route/"+routeInput);
		sch = (HttpURLConnection)rte.openConnection();

		BufferedReader br = new BufferedReader(new InputStreamReader(sch.getInputStream(), Charset.forName("UTF-8")));
		inputLine = "";
		text = "";
		while((inputLine = br.readLine()) != null)
			text += inputLine + "\n";
		br.close();
		sch.disconnect();
		
		Pattern badRoute = Pattern.compile("Invalid"); //if the web page contains the word invalid, url is bad
		Matcher badMatch = badRoute.matcher(text);     //this searches the whole page to check for error, leading to slow process sometimes
		if(badMatch.find())
			System.out.println("Invalid route number - please enter a valid route.");
		else {
			System.out.println("\nThe link for your route is: "+rte+"\n");
			Pattern route = Pattern.compile("<h2>Weekday<small>(.*)</small></h2>([\\s\\S]*?)</thead>"); //regex for destination
			Pattern stop = Pattern.compile("<p>(.*)</p>"); //regex for stop info
			Matcher routeMatch = route.matcher(text);
			String routeSpecific = "";
			while(routeMatch.find()) {
				System.out.println("Destination: " + routeMatch.group(1));
				routeSpecific = routeMatch.group(2);
				Matcher stopMatch = stop.matcher(routeSpecific); //search only after destination and before next destination
				for(int i = 1;stopMatch.find();i++) {
					String temp = stopMatch.group(1);
					if(temp.contains("&amp;"))
						temp = temp.replace("&amp;", "&");
					System.out.println("Stop number "+i+" is: " +temp);	
				}
				System.out.println("--------------------------");
			}
		}
		s.close();
	}
}
