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


@GetMapping(value="/v1/testIntent")
    public ResponseEntity test(@RequestParam String query) {

        Map<String, QueryResult> map = new HashMap<>();
        try{
        map = detectIntent(AppConstants.PROJECT_NAME,AppConstants.LOCATION,AppConstants.FORM_AGENT,
                "12345",query,AppConstants.LANGUAGE_CODE);
        Map<String, String> responseMap = new HashMap<>();
        for (Map.Entry<String, QueryResult> entry : map.entrySet()) {
            QueryResult res = entry.getValue();
            String str = res.getMatch().getParameters().getFieldsMap().toString();
            str = str.replaceAll("\\s","");
            String extracted = null;
            int index1=str.indexOf("original");
            if(index1!=-1){
                str = str.substring(index1,str.length()-1);
                index1 = str.indexOf("string_value:");
                str=str.substring(index1,str.length()-1);
                str = str.replaceAll("\"","").replaceAll("string_value:","");
                index1=str.indexOf("}");
                extracted= str.substring(0,index1);
            } else {
                index1 = str.indexOf("string_value:");
                if(index1!=-1){
                    str=str.substring(index1,str.length()-1);
                    extracted = str.replaceAll("\"","").replaceAll("string_value:","").replaceAll("}","");
                }
            }

            responseMap.put(AppConstants.EXTRACTED_VALUE,extracted);
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
