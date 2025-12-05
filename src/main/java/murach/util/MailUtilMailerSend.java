package murach.util;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.mail.MessagingException;
import java.io.IOException;

public class MailUtilMailerSend {

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String ENDPOINT = "https://api.mailersend.com/v1/email";

    public static void sendMail(String to, String from,
                                String subject, String body, boolean bodyIsHTML)
            throws MessagingException {

        String apiKey = System.getenv("MAILERSEND_API_KEY");
        
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getProperty("MAILERSEND_API_KEY");
        }
        
        if (apiKey == null || apiKey.isBlank()) {
            throw new MessagingException("API key not found on Render");
        }
        
        try {
            JSONObject payload = new JSONObject();

            // from
            JSONObject fromObj = new JSONObject();
            fromObj.put("email", from);
            payload.put("from", fromObj);

            // to (array of recipients)
            JSONArray toArray = new JSONArray();
            JSONObject toObj = new JSONObject().put("email", to);
            toArray.put(toObj);
            payload.put("to", toArray);

            if (subject != null) payload.put("subject", subject);

            // MailerSend accepts text and html fields.
            if (bodyIsHTML) {
                payload.put("html", body);
            } else {
                payload.put("text", body);
            }

            RequestBody requestBody = RequestBody.create(payload.toString(), JSON);
            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int code = response.code();
                String resp = response.body() != null ? response.body().string() : "";

                if (code < 200 || code >= 300) {
                    // include response body for debugging
                    throw new MessagingException("MailerSend error: HTTP " + code + " - " + resp);
                }
                // success: do nothing (or log)
            }

        } catch (IOException ex) {
            throw new MessagingException("IO error while sending email: " + ex.getMessage());
        }
    }
}
