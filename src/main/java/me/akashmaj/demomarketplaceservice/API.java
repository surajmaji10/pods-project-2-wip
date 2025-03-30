package me.akashmaj.demomarketplaceservice;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class API {

    static RestTemplate restTemplate;
    static String accountServiceUrl;
    static String walletServiceUrl;

    public API(){
        restTemplate = new RestTemplate();
        accountServiceUrl = "http://localhost:8080";
        walletServiceUrl = "http://localhost:8082";
    }

    static public boolean getUserDiscountById(Integer user_id, boolean discountCheck) {

        String url = accountServiceUrl + "/users/" + user_id;
        System.out.println("CHECK URL: " + url);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(null, null);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {


                String responseBody = response.getBody();
                if (Objects.nonNull(responseBody) && !responseBody.isEmpty()) {
                    try {
                        JSONObject user = new JSONObject(responseBody);
                        boolean discountAvailed = user.getBoolean("discount_availed");
                        System.out.println("Discount availed: " + discountAvailed);
                        System.out.println(user);
                        return discountAvailed;
                    } catch (JSONException e) {
                        System.out.println("Error parsing JSON: " + e.getMessage());
                        return false;
                    }
                } else {
                    System.out.println("Response body is empty or null.");
                    return false;
                }
            }
            return false;
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    static public String getUserById(Integer user_id) {

        String url = accountServiceUrl + "/users/" + user_id;
        System.out.println("CHECK URL: " + url);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(null, null);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {


                String responseBody = response.getBody();
                if (Objects.nonNull(responseBody) && !responseBody.isEmpty()) {
                    try {
                        JSONObject user = new JSONObject(responseBody);
                        System.out.println(user);
                        if(user.get("id").equals(user_id)){
                            System.out.println("User exists");
                            return user.toString();
                        }
                        else{
                            System.out.println("User does not exist");
                        }

                    } catch (JSONException e) {
                        System.out.println("Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    System.out.println("Response body is empty or null.");
                }
            }
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    static public String getUserWalletById(Integer user_id) {

        String url = walletServiceUrl + "/wallets/" + user_id;
        System.out.println("CHECK URL: " + url);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(null, null);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {


                String responseBody = response.getBody();
                if (Objects.nonNull(responseBody) && !responseBody.isEmpty()) {
                    try {
                        JSONObject wallet = new JSONObject(responseBody);
                        System.out.println(wallet);
                        if(wallet.get("user_id").equals(user_id)){
                            System.out.println("Wallet exists");
                            return wallet.toString();
                        }
                        else{
                            System.out.println("Wallet does not exist");
                        }
                    } catch (JSONException e) {
                        System.out.println("Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    System.out.println("Response body is empty or null.");
                }
            }
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }


    public static Integer getUserBalanceById(Integer user_id) {
        String url =  walletServiceUrl + "/wallets/" + user_id;
        System.out.println("CHECK URL: "+ url);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(null, null);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (Objects.nonNull(responseBody) && !responseBody.isEmpty()) {
                    try {
                        JSONObject user = new JSONObject(responseBody);
                        return user.getInt("balance");

                    } catch (JSONException e) {
                        System.out.println("Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    System.out.println("Response body is empty or null.");
                }
            }


        }
        catch (RestClientException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public static boolean updateUserDiscount(Integer userId, boolean discountAvailed) {
        String url = accountServiceUrl + "/users";
        System.out.println("CHECK URL: "+ url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the body (JSON payload) with two attributes
        Map<String, Object> body = new HashMap<>();
        body.put("id", userId);
        body.put("discount_availed", discountAvailed);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            }
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

//    curl -i -X PUT http://localhost:8081/wallets/1001 \
//            -H "Content-Type: application/json" \
//            -d '{
//            "action": "debit",
//            "amount": 500000
//            }'

    public static boolean updateUserWallet(Integer userId, Integer totalOrderPrice, String action) {
        String url = walletServiceUrl + "/wallets/" + userId;
        System.out.println("CHECK URL: "+ url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the body (JSON payload) with two attributes
        Map<String, Object> body = new HashMap<>();
        body.put("action", action);
        body.put("amount", totalOrderPrice);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            }
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

}
