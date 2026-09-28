package org.telegram.messenger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class VerifiedUsersManager {
    // Ссылка на твой файл (убедись, что ник и название репо верные)
    private static final String JSON_URL = "https://raw.githubusercontent.com/Yarik528/burmaldaGram/main/verified_users.json";
    
    // Храним ID и текст сообщения
    private static Map<Long, String> verifiedUsers = new HashMap<>();
    private static boolean isLoaded = false;

    public static void loadVerifiedUsers() {
        new Thread(() -> {
            try {
                URL url = new URL(JSON_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line);
                }
                in.close();
                
                JSONObject json = new JSONObject(content.toString());
                JSONArray users = json.getJSONArray("verified");
                
                verifiedUsers.clear();
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    long id = user.getLong("id");
                    String message = user.getString("message");
                    verifiedUsers.put(id, message);
                }
                
                isLoaded = true;
                FileLog.d("VerifiedUsersManager: Loaded " + verifiedUsers.size() + " users");
            } catch (Exception e) {
                FileLog.e("Failed to load verified users", e);
            }
        }).start();
    }

    // Проверка, есть ли галочка
    public static boolean isVerified(long userId) {
        return verifiedUsers.containsKey(userId);
    }
    
    // Получение текста для баннера
    public static String getMessage(long userId) {
        return verifiedUsers.getOrDefault(userId, "");
    }
    
    public static boolean isReady() {
        return isLoaded;
    }
}
