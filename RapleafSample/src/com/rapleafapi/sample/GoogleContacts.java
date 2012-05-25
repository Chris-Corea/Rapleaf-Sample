package com.rapleafapi.sample;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleContacts {

	/**
	 * Base URL for the feed
	 */
	private final URL feedUrl;

	/**
	 * Service used to communicate with contacts feed.
	 */
	private final ContactsService service;

	/**
	 * Contains list of contacts to be searched in Rapleaf's Personalization API
	 * Maps [Rapleaf keyword] -> [content to lookup in Rapleaf's database]
	 */
	private List<Map<String, String>> contactsList;

	/**
	 * Allows for (in this particular example) retrieval of a set number of
	 * contacts
	 */
	private final Query myQuery;

	/**
	 * Constructor.
	 * 
	 * Creates a new service and query to retrieve Google Contact information
	 * for specified account. {@code feedURL} is created with a "full"
	 * projection.
	 * 
	 * @param email
	 *            User's email account. Will retrieve contacts from this
	 *            account.
	 * @param password
	 * 
	 * @throws MalformedURLException
	 */
	public GoogleContacts(String email, String password)
			throws MalformedURLException {
		String url = "https://www.google.com/m8/feeds/contacts/" + email
				+ "/full";

		feedUrl = new URL(url);
		myQuery = new Query(feedUrl);
		service = new ContactsService("Rapleaf Contacts Service");

		contactsList = new ArrayList<Map<String, String>>();

		try {
			service.setUserCredentials(email, password);
			myQuery.setMaxResults(100);
		} catch (AuthenticationException e) {
			System.err.println("Username or password are incorrect");
			System.exit(0);
		}
	}

	/**
	 * Print the contents of an entry
	 * 
	 * @param contact
	 *            The ContactEntry to parse.
	 */
	private void printContact(ContactEntry contact) {
		if (contact.hasDeleted()) {
			System.err.print("ID: " + contact.getId());
			System.err.println(" Deleted:");
		}

		if (!contact.hasEmailAddresses() || contact.getName() == null)
			return;

		for (Email email : contact.getEmailAddresses()) {
			if (email.getPrimary()) {
				Map<String, String> nameAndEmail = new HashMap<String, String>();
				nameAndEmail.put("email", email.getAddress());
				nameAndEmail.put("first", contact.getName().getGivenName()
						.getValue());
				contactsList.add(nameAndEmail);
			}
		}
	}

	/**
	 * Queries Google Contacts with {@code feedURL} and sends each entry in the
	 * results feed to be parsed
	 * 
	 * @return
	 * 		{@code contactsList} or null 
	 */
	public List<Map<String, String>> getEntries() {
		ContactFeed resultFeed;
		try {
			resultFeed = service.query(myQuery, ContactFeed.class);

			for (ContactEntry entry : resultFeed.getEntries())
				printContact(entry);

			return contactsList;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return null;
	}
}
