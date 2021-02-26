package com.DB.FruitSalad.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.DB.FruitSalad.AppConstants;
import com.google.api.gax.core.FixedCredentialsProvider;
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
			return createDataMap(AppConstants.NAVIGATION_KEY, queryResult.getResponseMessages(0).getText().getText(0), null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public SessionsClient sessionClient(String sessionId) {
		try {
			SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
					.setCredentialsProvider(
							FixedCredentialsProvider.create(GoogleCredentials.fromStream(new FileInputStream(
									new File("src\\main\\resources\\hack-fruitsalad-2805a0bee0a7.json")))))
					.build();
			return SessionsClient.create(sessionsSettings);
		} catch (Exception e) {
			return null;
		}
	}

	public QueryInput getQueryInput(String text) {
		TextInput.Builder textInput = TextInput.newBuilder().setText(text);
		return QueryInput.newBuilder().setText(textInput).setLanguageCode(AppConstants.EN_US).build();
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
	
	private Map<String, String> createDataMap(String pageType, String responseText, String capturedValue){
		Map<String, String> map = new HashMap<>();
		map.put(AppConstants.PAGE_TYPE, pageType);
		map.put(AppConstants.RESPONSE_TEXT_KEY, responseText);
		map.put(AppConstants.CAPTURED_TEXT_KEY, capturedValue);
		return map;
	}
}
