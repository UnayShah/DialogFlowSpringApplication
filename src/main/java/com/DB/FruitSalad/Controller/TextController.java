package com.DB.FruitSalad.Controller;

import java.util.Map;

import com.DB.FruitSalad.Service.DialogFlowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
}
