package com.DB.FruitSalad.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.DB.FruitSalad.AppConstants;
import com.DB.FruitSalad.Service.DialogFlowService;
import com.google.cloud.dialogflow.cx.v3.QueryResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TextController {

	@Autowired
	private DialogFlowService dialogFlowService;

	@PostMapping("/navigationController")
	public ResponseEntity<Map<String, String>> navigationController(
			@RequestParam("navigationPageName") String navigationPageName,
			@RequestParam("sessionId") String sessionId) {
		return new ResponseEntity<>(dialogFlowService.navigationResponse(navigationPageName, sessionId),
				HttpStatus.ACCEPTED);
	}

	@PostMapping("/formController")
	public ResponseEntity<Map<String, String>> formController(@RequestParam("formFieldName") String formFieldName,
			@RequestParam("sessionId") String sessionId) {
		return new ResponseEntity<>(dialogFlowService.navigationResponse(formFieldName, sessionId),
				HttpStatus.ACCEPTED);
	}

	@GetMapping(value = "/v1/testIntent")
	public ResponseEntity<Map<String, String>> test(@RequestParam String query) {

		Map<String, QueryResult> map = new HashMap<>();
		try {
			map = dialogFlowService.detectIntent(AppConstants.PROJECT_NAME, AppConstants.LOCATION,
					AppConstants.FORM_AGENT, "12345", query, AppConstants.LANGUAGE_CODE);

			Map<String, String> responseMap = new HashMap<>();
			for (Map.Entry<String, QueryResult> entry : map.entrySet()) {
				QueryResult res = entry.getValue();
				/**
				 *
				 * Remaining add match - > field -> string_value
				 *
				 */
				responseMap.put(AppConstants.PAGE_TYPE, AppConstants.FORM_KEY);
				responseMap.put(AppConstants.MATCH_TYPE, res.getMatch().getMatchType().toString());
				responseMap.put(AppConstants.RESPONSE_TEXT_KEY,
						res.getResponseMessages(res.getResponseMessagesCount() - 1).getText().getText(0));
			}
			return new ResponseEntity<>(responseMap, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/v1/setStatus")
	public boolean setStatus(@RequestParam String query) throws IOException {
		boolean val = false;
		Map<String, QueryResult> result = dialogFlowService.detectIntent(AppConstants.PROJECT_NAME,
				AppConstants.LOCATION, AppConstants.MANUAL_OR_AUDIO_AGENT, "12345", query, AppConstants.LANGUAGE_CODE);

		/**
		 * this implementation can be done after creating new agent
		 *
		 */
		return val;
	}
}
