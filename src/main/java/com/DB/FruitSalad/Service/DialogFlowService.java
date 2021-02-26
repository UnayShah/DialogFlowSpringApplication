package com.DB.FruitSalad.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.DB.FruitSalad.AppConstants;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.cx.v3.DetectIntentRequest;
import com.google.cloud.dialogflow.cx.v3.DetectIntentResponse;
import com.google.cloud.dialogflow.cx.v3.MatchIntentRequest;
import com.google.cloud.dialogflow.cx.v3.MatchIntentResponse;
import com.google.cloud.dialogflow.cx.v3.QueryInput;
import com.google.cloud.dialogflow.cx.v3.QueryResult;
import com.google.cloud.dialogflow.cx.v3.SessionName;
import com.google.cloud.dialogflow.cx.v3.SessionsClient;
import com.google.cloud.dialogflow.cx.v3.SessionsSettings;
import com.google.cloud.dialogflow.cx.v3.TextInput;
import com.google.protobuf.Struct;
import com.google.cloud.dialogflow.cx.v3.Match.MatchType;

@Service
public class DialogFlowService {

	public Map<String, String> navigationResponse(String navigationPageName, String sessionId) {
		try {
			SessionsClient sessionsClient = sessionClient(sessionId);
			SessionName sessionName = SessionName.of(AppConstants.PROJECT_NAME, AppConstants.LOCATION,
					AppConstants.NAVIGATION_AGENT, sessionId);
			QueryInput queryInput = getQueryInput(navigationPageName);
			MatchIntentResponse matchIntentResponse = getMatchIntentResponse(sessionName, sessionsClient, queryInput);
			QueryResult queryResult = getQueryResponse(navigationPageName, sessionName, sessionsClient, queryInput,
					matchIntentResponse);
			System.out.println(queryResult.getMatch().getParameters());
			return createDataMap(AppConstants.NAVIGATION_KEY, queryResult.getResponseMessages(0).getText().getText(0),
					getExtractedValue(queryResult.getMatch().getParameters()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, String> formResponse(String formFieldName, String sessionId) {
		try {
			SessionsClient sessionsClient = sessionClient(sessionId);
			SessionName sessionName = SessionName.of(AppConstants.PROJECT_NAME, AppConstants.LOCATION,
					AppConstants.FORM_AGENT, sessionId);
			QueryInput queryInput = getQueryInput(formFieldName);
			MatchIntentResponse matchIntentResponse = getMatchIntentResponse(sessionName, sessionsClient, queryInput);
			QueryResult queryResult = getQueryResponse(formFieldName, sessionName, sessionsClient, queryInput,
					matchIntentResponse);
			System.out.println(queryResult.getMatch().getParameters());
			return createDataMap(AppConstants.FORM_KEY,
					queryResult.getResponseMessages(queryResult.getResponseMessagesCount() - 1).getText().getText(0),
					getExtractedValue(queryResult.getMatch().getParameters()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getExtractedValue(Struct parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public SessionsClient sessionClient(String sessionId) {
		try {
			SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
					.setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.fromStream(
							new FileInputStream(new File("src\\main\\resources\\hack-fruitsalad-2805a0bee0a7.json")))))
					.build();
			return SessionsClient.create(sessionsSettings);
		} catch (Exception e) {
			return null;
		}
	}

	public QueryInput getQueryInput(String text) {
		TextInput.Builder textInput = TextInput.newBuilder().setText(text);
		return QueryInput.newBuilder().setText(textInput).setLanguageCode(AppConstants.LOCATION).build();
	}

	public MatchIntentResponse getMatchIntentResponse(SessionName sessionName, SessionsClient sessionsClient,
			QueryInput queryInput) {
		MatchIntentRequest matchIntent = MatchIntentRequest.newBuilder().setSession(sessionName.toString())
				.setQueryInput(queryInput).build();
		return sessionsClient.matchIntent(matchIntent);
	}

	public QueryResult getQueryResponse(String text, SessionName sessionName, SessionsClient sessionsClient,
			QueryInput queryInput, MatchIntentResponse matchIntentResponse) {
		if (matchIntentResponse.getMatchesCount() == 1
				&& !matchIntentResponse.getMatches(0).getMatchType().equals(MatchType.NO_MATCH)) {
			DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder()
					.setSession(sessionName.toString()).setQueryInput(queryInput).build();
			DetectIntentResponse detectIntentResponse = sessionsClient.detectIntent(detectIntentRequest);
			return detectIntentResponse.getQueryResult();
		}
		return null;
	}

	private Map<String, String> createDataMap(String pageType, String responseText, String capturedValue) {
		Map<String, String> map = new HashMap<>();
		map.put(AppConstants.PAGE_TYPE, pageType);
		map.put(AppConstants.RESPONSE_TEXT_KEY, responseText);
		map.put(AppConstants.CAPTURED_TEXT_KEY, capturedValue);
		return map;
	}
	

	// DialogFlow API Detect Intent sample with text inputs.
	public static Map<String, QueryResult> detectIntent(String projectId, String locationId, String agentId,
			String sessionId, String text, String languageCode) throws IOException, ApiException {
		SessionsSettings.Builder sessionsSettingsBuilder = SessionsSettings.newBuilder();
		if (locationId.equals("global")) {
			sessionsSettingsBuilder.setEndpoint("dialogflow.googleapis.com:443");
		} else {
			sessionsSettingsBuilder.setEndpoint(locationId + "-dialogflow.googleapis.com:443");
		}
		try {
			SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
					.setCredentialsProvider(
							FixedCredentialsProvider.create(GoogleCredentials.fromStream(new FileInputStream(
									new File("C:\\Users\\Bhagyashree\\Downloads\\hack-fruitsalad-2805a0bee0a7.json")))))
					.build();

			Map<String, QueryResult> queryResults = new HashMap<String, QueryResult>();
			// Instantiates a client
			SessionsClient sessionsClient = SessionsClient.create(sessionsSettings);
			// Set the session name using the projectID (my-project-id), locationID
			// (global), agentID
			// (UUID), and sessionId (UUID).

			SessionName session = SessionName.of(projectId, locationId, agentId, sessionId);
			System.out.println("Session Path: " + session.toString());

			// Detect intents for each text input.
			// take input from ui here

			// Set the text (hello) for the query.
			TextInput.Builder textInput = TextInput.newBuilder().setText(text);

			// Build the query with the TextInput and language code (en-US).
			QueryInput queryInput = QueryInput.newBuilder().setText(textInput).setLanguageCode(languageCode).build();

			// Build the DetectIntentRequest with the SessionName and QueryInput.
			DetectIntentRequest request = DetectIntentRequest.newBuilder().setSession(session.toString())
					.setQueryInput(queryInput).build();

			// Performs the detect intent request.
			DetectIntentResponse response = sessionsClient.detectIntent(request);

			// Display the query result.
			QueryResult queryResult = response.getQueryResult();

			System.out.println("====================");
			System.out.format("Query Text: '%s'\n", queryResult.getText());
//			System.out.format("Detected Intent: %s (confidence: %f)\n", queryResult.getIntent().getDisplayName(),
//					queryResult.getIntentDetectionConfidence());

			queryResults.put(text, queryResult);

			return queryResults;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
