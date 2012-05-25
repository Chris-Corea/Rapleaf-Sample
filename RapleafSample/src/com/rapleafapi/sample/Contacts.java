package com.rapleafapi.sample;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rapleaf.api.personalization.RapleafApi;

public class Contacts {

	/**
	 * Developer API key distributed by Rapleaf.
	 * 
	 * Go to http://www.rapleaf.com/developers/personalization-api/ for
	 * information on how to get your own API key
	 */
	private static final String API_KEY = "SET ME!!!";

	/**
	 * Rapleaf Personalization-API object
	 */
	private final RapleafApi api;

	/**
	 * Will contain a list of contacts found from Google. This is passed into
	 * Rapleaf's API functions. Null if the no contacts are found or error
	 */
	private List<Map<String, String>> contactsList;

	/**
	 * Instantiates the Rapleaf api object with the API_KEY and sets the
	 * contactsList to the one received after talking to Google
	 */
	public Contacts(List<Map<String, String>> contactsList) {
		api = new RapleafApi(API_KEY);
		this.contactsList = contactsList;
	}

	/**
	 * Runs a bulk query using the contactsList on the Rapleaf API and outputs
	 * the percentage of male and female contacts found using Rapleaf's service.
	 * 
	 * @param verbose
	 *            Command line argument; when true, each contact with
	 *            information from Rapleaf's service is displayed through
	 *            standard output.
	 */
	public void getRapleafResults(boolean verbose) {
		try {
			JSONArray response = api.bulkQuery(contactsList);
			int male_count = 0;
			int female_count = 0;

			double total = 0;
			double avg_male = 0;
			double avg_female = 0;

			for (int i = 0; i < response.length(); i++) {
				JSONObject obj = response.getJSONObject(i);

				if (obj.length() != 0) {
					String gender = obj.getString("gender");
					if (gender.equalsIgnoreCase("male"))
						male_count++;
					else
						female_count++;
					if (verbose) {
						System.out.println(contactsList.get(i).get("first"));
						System.out.println("\tgender : " + gender);
					}
				}
			}
			total = response.length();
			avg_male = male_count / total;
			avg_female = female_count / total;

			System.out.printf("\n\tMale make up of contacts: %.2f%n",
					avg_male * 100);
			System.out.printf("\tFemale make up of contacts: %.2f%n",
					avg_female * 100);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		boolean verbose = false;
		if (args.length > 0)
			if (args[0].equalsIgnoreCase("-v")
					|| args[0].equalsIgnoreCase("-verbose"))
				verbose = true;
		try {
			Scanner scan = new Scanner(System.in);

			System.out.println("Enter your GMail account: ");
			String username = scan.nextLine();
			String password = PasswordField.readPassword("Enter your password");

			GoogleContacts googleContacts = new GoogleContacts(username,
					password);
			List<Map<String, String>> contactsList = googleContacts
					.getEntries();

			Contacts myContacts = new Contacts(contactsList);
			myContacts.getRapleafResults(verbose);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
