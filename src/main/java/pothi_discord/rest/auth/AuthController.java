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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pothi_discord.rest.RestUtils;
import pothi_discord.utils.Param;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by Pascal Pothmann on 29.06.2017.
 */
@RestController
@CrossOrigin
public class AuthController {

    @SuppressWarnings("serial")
    public static class VerifyTokenHandler extends HttpServlet {
        public static final String PATH = "/verify_token";
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String authErrorString = getAuthorizationErrorString(req);
            resp.setContentType("application/json; charset=utf-8");
            if (authErrorString == null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print("{\"status\":\"ok\"}");
            }
            else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(new JSONObject().put("message", authErrorString).toString());
            }
        }
    }

    @RequestMapping(value = "/verify_token", method = RequestMethod.GET)
    public Callable<ResponseEntity> checkToken(@RequestHeader Map<String, String> headers,
                               @RequestParam Map<String, String> params) {
        String authenticationError = getAuthorizationErrorString(headers, params);


        if (authenticationError == null) {
            return () -> ResponseEntity.ok(new JSONObject()
                    .put("status", "ok")
                    .toString());
        }
        else {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authenticationError);
        }
    }

    @SuppressWarnings("serial")
    public static class AuthHandler extends HttpServlet {
        public static final String PATH = "/auth";
        private static final String TOKEN_URL = "https://discordapp.com/api/oauth2/token";
        private static final String[] NEEDED_SCOPES = new String[]{"email", "identify"};
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            JSONObject body = RestUtils.getResquestBody(req);
            JSONArray scopesJsonArray = body.getJSONArray("scope");
            ArrayList<String> scopes = new ArrayList<>();
            for (int i = 0; i < scopesJsonArray.length(); i++) {
                scopes.add(scopesJsonArray.getString(i));
            }
            boolean scopesMatch = true;

            for(String neededScope : NEEDED_SCOPES) {
                scopesMatch = scopesMatch && Param.isInList(scopes, neededScope);
            }

            if (!scopesMatch) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"message\":\"The scopes " + Arrays.toString(NEEDED_SCOPES) + " are required\"}");
                return;
            }
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(TOKEN_URL);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", body.getString("clientId")));
            params.add(new BasicNameValuePair("client_secret", Param.CLIENT_SECRET()));
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("redirect_uri", body.getString("redirectUri")));
            params.add(new BasicNameValuePair("code", body.getString("code")));

            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response= httpClient.execute(post);
            String requestResponse = EntityUtils.toString(response.getEntity());

            JSONObject tokenObject = new JSONObject(requestResponse);

            HttpGet get = new HttpGet("https://discordapp.com/api/users/@me");
            get.setHeader("Authorization", tokenObject.getString("token_type")
                    + " " + tokenObject.getString("access_token"));
            response = httpClient.execute(get);
            requestResponse = EntityUtils.toString(response.getEntity());

            JSONObject meObject = new JSONObject(requestResponse);

            Date now = new Date();
            long expiresIn = 604800000; // 604800000 = 1 week
            Date expirationDate = new Date(now.getTime() + expiresIn);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(new JSONObject().put("token", Jwts.builder()
                            .setSubject(meObject.getString("id"))
                            .setIssuedAt(new Date())
                            .setExpiration(expirationDate)
                            .signWith(SignatureAlgorithm.HS256, Param.SECRET_KEY())
                            .compact()).toString());
        }
    }

    @RequestMapping(value = "/auth", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Callable<ResponseEntity> auth(@RequestParam Map<String, String> requestParams,
                       @RequestBody Map<String, Object> body,
                       @RequestHeader Map<String, String> headers) throws IOException {
        String tokenUrl = "https://discordapp.com/api/oauth2/token";

        String[] needesScopes = new String[]{"email", "identify"}; //    <--------------------------------

        ArrayList<String> scopes = (ArrayList) body.get("scope");

        boolean scopesMatch = true;
        for(String neededScope : needesScopes) {
            scopesMatch = scopesMatch && Param.isInList(scopes, neededScope);
        }

        if(!scopesMatch) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
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

        // System.out.println(new JSONObject(body).toString(2));
        // System.out.println("Baum");

        post.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response= httpClient.execute(post);
        String requestResponse = EntityUtils.toString(response.getEntity());

        JSONObject tokenObject = new JSONObject(requestResponse);
        // System.out.println(tokenObject.toString(2));

        HttpGet get = new HttpGet("https://discordapp.com/api/users/@me");
        get.setHeader("Authorization", tokenObject.getString("token_type")
                + " " + tokenObject.getString("access_token"));
        response = httpClient.execute(get);
        requestResponse = EntityUtils.toString(response.getEntity());

        JSONObject meObject = new JSONObject(requestResponse);

        // System.out.println(meObject.toString(2));

        Date now = new Date();
        long expiresIn = 604800000; // 604800000 = 1 week
        Date expirationDate = new Date(now.getTime() + expiresIn);

        return () -> ResponseEntity.ok(
                new JSONObject().put("token", Jwts.builder()
                .setSubject(meObject.getString("id"))
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, Param.SECRET_KEY())
                .compact()).toString());

    }


    public static String getAuthorizationErrorString(HttpServletRequest req) {
        boolean hasAtuhHeader = checkForAuthorizationHeader(req);
        boolean hasAuthQuery = checkForAuthorizationQuery(req);
        if(!hasAtuhHeader && !hasAuthQuery){
            return "Please make sure your request has an Authorization header or an auth_token query string";
        }

        String token;

        if(hasAtuhHeader) {
            token = req.getHeader("Authorization").split(" ")[1];
        }
        else {
            token = req.getParameter("auth_token");
        }

        return getAuthorizationErrorString(token);
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
            tokenParser.parseClaimsJws(token);
        } catch (MissingClaimException | IncorrectClaimException |SignatureException e) {
            return "Not a valid token";
        } catch (MalformedJwtException e){
            return "Malformed token";
        } catch (ExpiredJwtException e) {
            return "Token has expired";
        }

        return null;
    }

    public static boolean checkForAuthorizationHeader(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null) {
            return false;
        }
        String[] authheaderValues = authHeader.split(" ");

        if(authheaderValues.length < 2) {
            return false;
        }
        return true;
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

    public static boolean checkForAuthorizationQuery(HttpServletRequest req) {
        if (req.getParameter("auth_token") == null) {
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

    public static String getToken(HttpServletRequest req) {
        if (checkForAuthorizationHeader(req)) {
            return req.getHeader("Authorization").split(" ")[1];
        }
        else if (checkForAuthorizationQuery(req)) {
            return req.getParameter("auth_token");
        }
        else {
            throw new RuntimeException();
        }
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
