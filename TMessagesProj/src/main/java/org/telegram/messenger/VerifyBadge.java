package org.telegram.messenger;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.LayoutHelper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class VerifyBadge {
    private static final String JSON_URL = "https://raw.githubusercontent.com/Yarik528/burmaldaGram/main/verified_users.json";
    private static Map<Long, String> verifiedUsers = new HashMap<>();
    private static boolean isLoaded = false;

    // Загрузка списка при старте приложения
    public static void load() {
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
                FileLog.d("VerifyBadge: Loaded " + verifiedUsers.size() + " users");
            } catch (Exception e) {
                FileLog.e("Failed to load verified users", e);
            }
        }).start();
    }

    // Проверка верификации
    public static boolean isVerified(long userId) {
        return verifiedUsers.containsKey(userId);
    }

    // Получение сообщения
    public static String getMessage(long userId) {
        return verifiedUsers.getOrDefault(userId, "");
    }

    // Главная функция: добавляет галочку рядом с nameView
    public static void attach(Activity activity, View nameView, long userId) {
        if (!isVerified(userId)) return;
        
        ImageView check = new ImageView(activity);
        check.setImageResource(R.drawable.ic_verified);
        check.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        int s = AndroidUtilities.dp(16);
        
        // Добавляем галочку в тот же контейнер, где nameView
        View parent = (View) nameView.getParent();
        if (parent instanceof android.view.ViewGroup) {
            android.view.ViewGroup container = (android.view.ViewGroup) parent;
            int index = container.indexOfChild(nameView);
            container.addView(check, index + 1, LayoutHelper.createFrame(s, s, Gravity.LEFT | Gravity.TOP, 4, 0, 0, 0));
        }
        
        // Клик по галочке
        check.setOnClickListener(v -> {
            String msg = getMessage(userId);
            if (!msg.isEmpty()) {
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
          }
