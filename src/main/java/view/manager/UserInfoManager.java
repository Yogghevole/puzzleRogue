package view.manager;

import javafx.scene.control.Label;

/**
 * Manages the display of user information in the UI.
 * Handles updating the nickname and points display based on the current session.
 */
public class UserInfoManager {
    
    private Label nickLabel;
    private Label pointsLabel;
    
    public UserInfoManager() {
    }
    
    public void setup(Label nickLabel, Label pointsLabel) {
        this.nickLabel = nickLabel;
        this.pointsLabel = pointsLabel;
        updateDisplay();
    }
    
    public void updateDisplay() {
        if (nickLabel == null || pointsLabel == null) return;
        
        try {
            model.domain.User user = model.service.SessionService.getCurrentUser();
            if (user != null) {
                nickLabel.setText(user.getNick());
                pointsLabel.setText(String.format("%,d pts", user.getPointsAvailable()));
            } else {
                 String nick = model.service.SessionService.getCurrentNick();
                 if (nick != null) nickLabel.setText(nick);
                 pointsLabel.setText("");
            }
        } catch (Exception e) {
            System.err.println("Error updating user info: " + e.getMessage());
        }
    }
}
