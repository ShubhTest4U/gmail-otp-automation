package Gmail_API;

//Import Google API client libraries for Gmail, OAuth2, HTTP, and JSON processing.
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GmailOTPFetcher {
	private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance(); // GsonFactory for JSON parsing.
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
	private static final String CREDENTIALS_FILE_PATH = "credentials.json";

	/*
	 * Authenticates with the Gmail API and retrieves user credentials. If
	 * credentials don't exist, it will guide the user through the OAuth2 flow.
	 * @param HTTP_TRANSPORT The HTTP transport layer. @return A Credential object for authenticated access.
	 * @throws IOException If there's an issue reading the credentials file or
	 * during authentication.
	 */

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		/*
		 * Build the authorization flow. specifies the HTTP transport, JSON factory,
		 * client secrets, and required scopes. It sets up a data store to save and
		 * reuse tokens and requests offline access.
		 */
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline").build();
		// Authorize the application. It'll open a browser for the user to grant permissions if tokens not present or expired.
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	public static String fetchLatestOTP(String user) throws Exception {
		String searchQuery = "from:support@topmate.io subject:OTP for Topmate login"; // Search Query
		String otpRegex = "font-weight: bold; text-align: center;\">(\\d{6})</div>"; // Extract 6 digit OTP
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport(); // Initialize HTTP
																								// transport
		Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build(); // Build Gmail service client using authenticated cred.
		ListMessagesResponse messagesResponse = service.users().messages().list(user).setQ(searchQuery)
				.setMaxResults(5L).execute();
		List<Message> messages = messagesResponse.getMessages(); // Get the list of messages.
		if (messages == null || messages.isEmpty()) {
			throw new Exception("No OTP email found");
		}
		for (Message message : messages) { // Fetch the full content of each message.
			Message fullMessage = service.users().messages().get(user, message.getId()).setFormat("FULL").execute();
			String body = getTextFromMessage(fullMessage); // Extract the plain text body from the full message.
			// System.out.println(" Full email content:\n" + body);
			Pattern pattern = Pattern.compile(otpRegex); // Compile the regex pattern.
			Matcher matcher = pattern.matcher(body); // If the OTP pattern is found, return the extracted OTP
			if (matcher.find()) { // If the OTP pattern is found, return the extracted OTP.
				return matcher.group(1); // group(1) contains the actual 6-digit OTP.
			}
		}
		throw new Exception("OTP not found in recent emails");
	}

	/*
	 * Extracts the plain text content from a Gmail API Message object. It handles
	 * both multi-part and single-part message bodies
	 */

	private static String getTextFromMessage(Message message) throws IOException {
		// Check if the message has multiple parts (e.g., plain text and HTML).
		if (message.getPayload().getParts() != null) {
			for (com.google.api.services.gmail.model.MessagePart part : message.getPayload().getParts()) {
				// Look for parts with a body and data.
				if (part.getBody() != null && part.getBody().getData() != null) {
					// Decode the Base64 URL-safe encoded data and return as a String.
					return new String(java.util.Base64.getUrlDecoder().decode(part.getBody().getData()));
				}
			}
		}
		// If it's a single-part message, check the main payload body.
		if (message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
			// Decode the Base64 URL-safe encoded data and return as a String.
			return new String(java.util.Base64.getUrlDecoder().decode(message.getPayload().getBody().getData()));
		}
		// Return an empty string if no text content is found.
		return "";
	}
}