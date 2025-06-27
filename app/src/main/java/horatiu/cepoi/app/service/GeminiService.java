package horatiu.cepoi.app.service;

import horatiu.cepoi.app.BuildConfig;
import okhttp3.*;
import org.json.*;

import java.io.IOException;

public class GeminiService {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    public interface GeminiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public static void sendMessageWithContext(JSONArray contents, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("contents", contents);
        } catch (JSONException e) {
            callback.onFailure("Eroare JSON: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.get("application/json"));

        HttpUrl url = HttpUrl.parse(BASE_URL).newBuilder()
                .addQueryParameter("key", API_KEY)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Cerere eșuată: " + e.getMessage());
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Eroare răspuns Gemini: " + response.code());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray candidates = json.getJSONArray("candidates");
                    JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                    JSONArray parts = content.getJSONArray("parts");
                    String reply = parts.getJSONObject(0).getString("text");
                    callback.onSuccess(reply.replaceAll("\\*\\*", ""));
                } catch (Exception e) {
                    callback.onFailure("Parse error: " + e.getMessage());
                }
            }
        });
    }

}
