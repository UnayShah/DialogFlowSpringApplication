package com.DB.FruitSalad.Controller;


import com.DB.FruitSalad.AppConstants;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.cx.v3beta1.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.api.gax.rpc.ApiException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class IntentAPI {

    // DialogFlow API Detect Intent sample with text inputs.
    public static Map<String, QueryResult> detectIntent(
            String projectId,
            String locationId,
            String agentId,
            String sessionId,
            String text,
            String languageCode)
            throws IOException, ApiException {
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
            // Set the session name using the projectID (my-project-id), locationID (global), agentID
            // (UUID), and sessionId (UUID).

            SessionName session = SessionName.of(projectId, locationId, agentId, sessionId);
            System.out.println("Session Path: " + session.toString());

            // Detect intents for each text input.
            // take input from ui here


            // Set the text (hello) for the query.
            TextInput.Builder textInput = TextInput.newBuilder().setText(text);

            // Build the query with the TextInput and language code (en-US).
            QueryInput queryInput =
                    QueryInput.newBuilder().setText(textInput).setLanguageCode(languageCode).build();

            // Build the DetectIntentRequest with the SessionName and QueryInput.
            DetectIntentRequest request =
                    DetectIntentRequest.newBuilder()
                            .setSession(session.toString())
                            .setQueryInput(queryInput)
                            .build();

            // Performs the detect intent request.
            DetectIntentResponse response = sessionsClient.detectIntent(request);

            // Display the query result.
            com.google.cloud.dialogflow.cx.v3beta1.QueryResult queryResult = response.getQueryResult();

            System.out.println("====================");
            System.out.format("Query Text: '%s'\n", queryResult.getText());
            System.out.format(
                    "Detected Intent: %s (confidence: %f)\n",
                    queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());

            queryResults.put(text, queryResult);

        return queryResults;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping(value="/v1/setStatus")
    public boolean setStatus(@RequestParam String query) throws IOException{
        boolean val = false;
        Map<String, QueryResult> result = detectIntent(AppConstants.PROJECT_NAME,AppConstants.LOCATION,AppConstants.MANUAL_OR_AUDIO_AGENT,
                "12345",query,AppConstants.LANGUAGE_CODE);

        /**
         * this implementation can be done after creating new agent
         *
         */
        return val;
    }

    @GetMapping(value="/v1/testIntent")
    public ResponseEntity test(@RequestParam String query) {

        Map<String, QueryResult> map = new HashMap<>();
        try{
        map = detectIntent(AppConstants.PROJECT_NAME,AppConstants.LOCATION,AppConstants.FORM_AGENT,
                "12345",query,AppConstants.LANGUAGE_CODE);

        Map<String, String> responseMap = new HashMap<>();
        for (Map.Entry<String, QueryResult> entry : map.entrySet()) {
            QueryResult res = entry.getValue();
            /**
             *
             * Remaining
             * add match - > field -> string_value
             *
             */
            responseMap.put(AppConstants.PAGE_TYPE, AppConstants.FORM_KEY);
            responseMap.put(AppConstants.MATCH_TYPE, res.getMatch().getMatchType().toString());
            responseMap.put(AppConstants.RESPONSE_TEXT_KEY, res.getResponseMessages(res.getResponseMessagesCount() - 1).getText().getText(0));
        }
        return new ResponseEntity<>(responseMap, HttpStatus.ACCEPTED);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
