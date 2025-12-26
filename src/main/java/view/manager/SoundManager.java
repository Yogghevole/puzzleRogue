package view.manager;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Singleton manager for handling all game audio.
 * Manages sound effects (SFX) and background music, including volume control and muting.
 */
public class SoundManager {
    private static final SoundManager INSTANCE = new SoundManager();

    private Clip invalidClickClip;
    private Clip healClip;
    private Clip hintClip;
    private Clip sacrificeClip;
    private Clip scoreClip;
    private Clip characterSelectionClip;
    private Clip settingsClip;
    private Clip winSelectItemClip;
    private Clip numberClickClip;
    private Clip correctClip;
    private Clip errorClip;
    private Clip lossClip;
    private Clip winClip;
    private Clip musicClip;

    private final java.util.Map<String, Clip> charSelectClips = new java.util.HashMap<>();
    private final java.util.Map<Clip, Double> clipGains = new java.util.HashMap<>();

    private double sfxVolume = 0.5;
    private double musicVolume = 0.5;
    private boolean muted = false;
    private long lastInvalidClickMs = 0L;
    private int invalidCooldownMs = 130;

    private static final double BASE_SFX_GAIN = 0.1;
    private static final double BASE_MUSIC_GAIN = 0.1;

    private SoundManager() {
        invalidClickClip = loadClip("/assets/sfx/ui_button_invalid.wav", 1.0);
        healClip = loadClip("/assets/sfx/items/heal.wav", 1.0);
        hintClip = loadClip("/assets/sfx/items/hint.wav", 1.0);
        sacrificeClip = loadClip("/assets/sfx/items/sacriface.wav", 1.0);
        scoreClip = loadClip("/assets/sfx/items/score.wav", 1.0);
        characterSelectionClip = loadClip("/assets/sfx/character_selection.wav", 1.0);
        settingsClip = loadClip("/assets/sfx/settings.wav", 0.5);
        winSelectItemClip = loadClip("/assets/sfx/win_select_item.wav", 1.0);
        numberClickClip = loadClip("/assets/sfx/numberclick.wav", 1.0);
        correctClip = loadClip("/assets/sfx/correct.wav", 1.0);
        errorClip = loadClip("/assets/sfx/error.wav", 1.0);
        lossClip = loadClip("/assets/sfx/loss.wav", 1.0);
        winClip = loadClip("/assets/sfx/win.wav", 1.0);

        loadCharacterSelectClip("CRUSADER", "/assets/sfx/char_selection/crusader.wav");
        loadCharacterSelectClip("HIGHWAYMAN", "/assets/sfx/char_selection/highwayman.wav");
        loadCharacterSelectClip("JESTER", "/assets/sfx/char_selection/jester.wav");
        loadCharacterSelectClip("OCCULTIST", "/assets/sfx/char_selection/occultist.wav");
        loadCharacterSelectClip("PLAGUEDOCTOR", "/assets/sfx/char_selection/plague_doctor.wav");
    }

    private Clip loadClip(String resourcePath, double gainMultiplier) {
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url != null) {
                Clip clip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                    clip.open(ais);
                }
                clipGains.put(clip, gainMultiplier);
                applyVolume(clip, sfxVolume);
                return clip;
            }
        } catch (Exception e) {}
        return null;
    }

    private void loadCharacterSelectClip(String id, String resourcePath) {
        double gain = "PLAGUEDOCTOR".equals(id) ? 0.3 : 1.0;
        Clip clip = loadClip(resourcePath, gain);
        if (clip != null) {
            charSelectClips.put(id, clip);
        }
    }

    public static SoundManager getInstance() { return INSTANCE; }

    public void playInvalidClick() {
        if (muted) return;
        long now = System.currentTimeMillis();
        if (now - lastInvalidClickMs < invalidCooldownMs) return;
        playClip(invalidClickClip);
        lastInvalidClickMs = now;
    }

    public void playHeal() { playClip(healClip); }
    public void playHint() { playClip(hintClip); }
    public void playSacrifice() { playClip(sacrificeClip); }
    public void playScore() { playClip(scoreClip); }
    public void playCharacterSelection() { playClip(characterSelectionClip); }
    public void playSettings() { playClip(settingsClip); }
    public void playWinSelectItem() { playClip(winSelectItemClip); }
    public void playNumberClick() { playClip(numberClickClip); }
    public void playCorrect() { playClip(correctClip); }
    public void playError() { playClip(errorClip); }
    public void playLoss() { playClip(lossClip); }
    public void playWin() { playClip(winClip); }
    
    public void playSettingsToggle() { playSettings(); }
    public void playWinItemSelection() { playWinSelectItem(); }

    public void playCharacterName(String charId) {
        if (muted || charId == null) return;
        Clip clip = charSelectClips.get(charId);
        playClip(clip);
    }
    
    public void playCharacterSelectFor(String charId, Runnable onComplete) {
        playCharacterName(charId);
        long delay = 2000;
        new Thread(() -> {
            try { Thread.sleep(delay); } catch (InterruptedException e) {}
            if (onComplete != null) javafx.application.Platform.runLater(onComplete);
        }).start();
    }
    
    /**
     * Plays background music for a specific level category.
     * Selects the appropriate music track based on the dungeon type (e.g., RUINS, COVE).
     *
     * @param category The level category (e.g., "RUINS", "COVE").
     */
    public void playLevelMusicForCategory(String category) {
        String path = "/assets/music/town/town1.wav"; 
        if ("RUINS".equalsIgnoreCase(category)) path = "/assets/music/crypts/crypts1.wav";
        else if ("COVE".equalsIgnoreCase(category)) path = "/assets/music/cove/cove1.wav";
        else if ("WARRENS".equalsIgnoreCase(category)) path = "/assets/music/warrens/warrens1.wav";
        else if ("WEALD".equalsIgnoreCase(category)) path = "/assets/music/weald/weald1.wav";
        
        startMusic(path);
    }
    
    public void playHomeMusic() {
        startMusic("/assets/music/home_screen/home_screen.wav");
    }

    private void playClip(Clip clip) {
        if (muted || clip == null) return;
        try {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            clip.start();
        } catch (Exception ignore) {}
    }

    public void startMusic(String resourcePath) {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url != null) {
                musicClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                    musicClip.open(ais);
                }
                applyVolume(musicClip, musicVolume);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                if (muted) {
                    javax.sound.sampled.BooleanControl muteControl = (javax.sound.sampled.BooleanControl) musicClip.getControl(javax.sound.sampled.BooleanControl.Type.MUTE);
                    muteControl.setValue(true);
                }
            }
        } catch (Exception ignore) {}
    }

    public void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }

    public void fadeOutMusic(int durationMs) {
        if (musicClip == null || !musicClip.isRunning()) return;
        musicClip.stop();
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (musicClip != null) {
            try {
                 javax.sound.sampled.BooleanControl muteControl = (javax.sound.sampled.BooleanControl) musicClip.getControl(javax.sound.sampled.BooleanControl.Type.MUTE);
                 muteControl.setValue(muted);
            } catch (Exception ignore) {}
        }
    }

    public boolean isMuted() { return muted; }

    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0.0, Math.min(1.0, volume));
        updateLoadedClipsVolume();
    }

    public double getSfxVolume() { return sfxVolume; }

    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        if (musicClip != null) {
            applyVolume(musicClip, musicVolume);
        }
    }

    public double getMusicVolume() { return musicVolume; }

    private void updateLoadedClipsVolume() {
        applyVolume(invalidClickClip, sfxVolume);
        applyVolume(healClip, sfxVolume);
        applyVolume(hintClip, sfxVolume);
        applyVolume(sacrificeClip, sfxVolume);
        applyVolume(scoreClip, sfxVolume);
        applyVolume(characterSelectionClip, sfxVolume);
        applyVolume(settingsClip, sfxVolume);
        applyVolume(winSelectItemClip, sfxVolume);
        applyVolume(numberClickClip, sfxVolume);
        applyVolume(correctClip, sfxVolume);
        applyVolume(errorClip, sfxVolume);
        applyVolume(lossClip, sfxVolume);
        applyVolume(winClip, sfxVolume);
        for (Clip c : charSelectClips.values()) {
            applyVolume(c, sfxVolume);
        }
    }

    private void applyVolume(Clip clip, double masterVolume) {
        if (clip == null) return;
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            double gainMult = clipGains.getOrDefault(clip, 1.0);
            double effectiveVolume = masterVolume * gainMult * (clip == musicClip ? BASE_MUSIC_GAIN : BASE_SFX_GAIN);
            if (effectiveVolume <= 0.0001) {
                gainControl.setValue(gainControl.getMinimum());
            } else {
                float db = (float) (20.0 * Math.log10(effectiveVolume));
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), db)));
            }
        } catch (IllegalArgumentException e) {
        } catch (Exception ignore) {}
    }
}