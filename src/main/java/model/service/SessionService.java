package model.service;

public class SessionService {
    private static String currentNick;
    private static String lastSelectedBuffId;

    public static void setCurrentNick(String nick) {
        currentNick = nick;
    }

    public static String getCurrentNick() {
        return currentNick;
    }

    public static void clear() {
        currentNick = null;
        lastSelectedBuffId = null;
        setLastSelectedCharacter(null);
    }

    public static void setLastSelectedBuff(String buffId) {
        lastSelectedBuffId = buffId;
    }

    public static String getLastSelectedBuff() {
        return lastSelectedBuffId;
    }

    public static void setLastSelectedCharacter(String charId) {
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(SessionService.class);
            String key = (currentNick != null ? currentNick : "_global_") + ":lastCharacter";
            if (charId == null) {
                prefs.remove(key);
            } else {
                prefs.put(key, charId);
            }
        } catch (Exception ignore) {}
    }

    public static String getLastSelectedCharacter() {
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(SessionService.class);
            String key = (currentNick != null ? currentNick : "_global_") + ":lastCharacter";
            return prefs.get(key, null);
        } catch (Exception e) {
            return null;
        }
    }
}