package pothi_discord.rest.auth;

import io.jsonwebtoken.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pothi_discord.utils.Param;

import java.io.IOException;
import java.util.*;

/**
 * Created by Pascal Pothmann on 29.06.2017.
 */
@RestController
@CrossOrigin
public class AuthController {

    @RequestMapping(value = "/verify_token", method = RequestMethod.GET)
    public Object checkToken(@RequestHeader Map<String, String> headers,
                             @RequestParam Map<String, String> params) {
        String authenticationError = getAuthorizationErrorString(headers, params);

        System.out.println(authenticationError);

        if (authenticationError == null) {
            return new JSONObject()
                    .put("status", "ok")
                    .toString();
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authenticationError);
        }
    }

    @RequestMapping(value = "/auth", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Object auth(@RequestParam Map<String, String> requestParams,
                       @RequestBody Map<String, Object> body,
                       @RequestHeader Map<String, String> headers) throws IOException {
        String tokenUrl = "https://discordapp.com/api/oauth2/token";

        String[] needesScopes = new String[]{"email"}; //    <--------------------------------

        ArrayList<String> scopes = (ArrayList) body.get("scope");

        boolean scopesMatch = true;
        for(String neededScope : needesScopes) {
            scopesMatch = scopesMatch && Param.isInList(scopes, neededScope);
        }

        if(!scopesMatch) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("The scopes " + Arrays.toString(needesScopes) + " are required");
        }

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(tokenUrl);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", body.get("clientId").toString()));
        params.add(new BasicNameValuePair("client_secret", Param.CLIENT_SECRET()));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("redirect_uri", body.get("redirectUri").toString()));
        params.add(new BasicNameValuePair("code", body.get("code").toString()));

        System.out.println(new JSONObject(body).toString(2));
        System.out.println("Baum");

        post.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response= httpClient.execute(post);
        String requestResponse = EntityUtils.toString(response.getEntity());

        JSONObject tokenObject = new JSONObject(requestResponse);
        System.out.println(tokenObject.toString(2));

        HttpGet get = new HttpGet("https://discordapp.com/api/users/@me");
        //post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        get.setHeader("Authorization", "Bearer " + tokenObject.getString("access_token"));
        response = httpClient.execute(get);
        requestResponse = EntityUtils.toString(response.getEntity());

        JSONObject meObject = new JSONObject(requestResponse);

        System.out.println(meObject.toString(2));

        Date now = new Date();
        long expiresIn = 604800000; // 604800000 = 1 week
        Date expirationDate = new Date(now.getTime() + expiresIn);

        return new JSONObject().put("token", Jwts.builder()
                .setSubject(meObject.getString("id"))
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, Param.SECRET_KEY())
                .compact()).toString();

    }


    public static String getAuthorizationErrorString(Map<String, String> requestHeaders, Map<String, String> requestParams) {
        boolean hasAtuhHeader = checkForAuthorizationHeader(requestHeaders);
        boolean hasAuthQuery = checkForAuthorizationQuery(requestParams);
        if(!hasAtuhHeader && !hasAuthQuery){
            return "Please make sure your request has an Authorization header or an auth_token query string";
        }

        String token;

        if(hasAtuhHeader) {
            token = requestHeaders.get("Authorization").split(" ")[1];
        }
        else {
            token = requestParams.get("auth_token");
        }

        return getAuthorizationErrorString(token);
    }

    private static String getAuthorizationErrorString(String token) {

        JwtParser tokenParser = Jwts.parser().setSigningKey(Param.SECRET_KEY());

        try {
            Jws<Claims> claims = tokenParser.parseClaimsJws(token);

            if(new Date().compareTo(claims.getBody().getExpiration()) > 0){
                return "Token has expired";
            }

            System.out.println(claims.getBody().getSubject());
        } catch (MissingClaimException | IncorrectClaimException |SignatureException e) {
            return "Not a valid token";
        } catch (MalformedJwtException e){
            return "Malformed token";
        }

        return null;
    }

    public static boolean checkForAuthorizationHeader(Map<String, String> requestHeaders) {
        if(!requestHeaders.containsKey("Authorization")) {
            System.out.println("No header");
            return false;
        }
        String[] authheaderValues = requestHeaders.get("Authorization").split(" ");

        if(authheaderValues.length < 2) {
            return false;
        }
        return true;
    }

    public static boolean checkForAuthorizationQuery(Map<String, String> requestParams) {
        if(!requestParams.containsKey("auth_token")) {
            return false;
        }

        return true;
    }

    public static String getToken(Map<String, String> headers, Map<String, String> reauestParams) {
        if(checkForAuthorizationHeader(headers)) {
            return headers.get("Authorization").split(" ")[1];
        }
        else if (checkForAuthorizationQuery(reauestParams)) {
            return reauestParams.get("auth_token");
        }
        else {
            throw new RuntimeException();
        }
    }

}
