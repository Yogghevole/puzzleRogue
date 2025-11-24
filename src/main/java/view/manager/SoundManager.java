package view.manager;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

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
        try {
            java.net.URL url = getClass().getResource("/assets/sfx/ui_button_invalid.wav");
            if (url != null) {
                invalidClickClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                    invalidClickClip.open(ais);
                }
                clipGains.put(invalidClickClip, 1.0);
                applyVolume(invalidClickClip, sfxVolume);
            }
            java.net.URL healUrl = getClass().getResource("/assets/sfx/items/heal.wav");
            if (healUrl != null) {
                healClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(healUrl)) {
                    healClip.open(ais);
                }
                clipGains.put(healClip, 1.0);
                applyVolume(healClip, sfxVolume);
            }
            java.net.URL hintUrl = getClass().getResource("/assets/sfx/items/hint.wav");
            if (hintUrl != null) {
                hintClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(hintUrl)) {
                    hintClip.open(ais);
                }
                clipGains.put(hintClip, 1.0);
                applyVolume(hintClip, sfxVolume);
            }
            java.net.URL sacrUrl = getClass().getResource("/assets/sfx/items/sacriface.wav");
            if (sacrUrl != null) {
                sacrificeClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(sacrUrl)) {
                    sacrificeClip.open(ais);
                }
                clipGains.put(sacrificeClip, 1.0);
                applyVolume(sacrificeClip, sfxVolume);
            }
            java.net.URL scoreUrl = getClass().getResource("/assets/sfx/items/score.wav");
            if (scoreUrl != null) {
                scoreClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(scoreUrl)) {
                    scoreClip.open(ais);
                }
                clipGains.put(scoreClip, 1.0);
                applyVolume(scoreClip, sfxVolume);
            }

            java.net.URL charSelUrl = getClass().getResource("/assets/sfx/character_selection.wav");
            if (charSelUrl != null) {
                characterSelectionClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(charSelUrl)) {
                    characterSelectionClip.open(ais);
                }
                clipGains.put(characterSelectionClip, 1.0);
                applyVolume(characterSelectionClip, sfxVolume);
            }

            java.net.URL settingsUrl = getClass().getResource("/assets/sfx/settings.wav");
            if (settingsUrl != null) {
                settingsClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(settingsUrl)) {
                    settingsClip.open(ais);
                }
                clipGains.put(settingsClip, 0.5);
                applyVolume(settingsClip, sfxVolume);
            }

            java.net.URL winSelItemUrl = getClass().getResource("/assets/sfx/win_select_item.wav");
            if (winSelItemUrl != null) {
                winSelectItemClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(winSelItemUrl)) {
                    winSelectItemClip.open(ais);
                }
                clipGains.put(winSelectItemClip, 1.0);
                applyVolume(winSelectItemClip, sfxVolume);
            }

            java.net.URL numberClickUrl = getClass().getResource("/assets/sfx/numberclick.wav");
            if (numberClickUrl != null) {
                numberClickClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(numberClickUrl)) {
                    numberClickClip.open(ais);
                }
                clipGains.put(numberClickClip, 1.0);
                applyVolume(numberClickClip, sfxVolume);
            }

            java.net.URL correctUrl = getClass().getResource("/assets/sfx/correct.wav");
            if (correctUrl != null) {
                correctClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(correctUrl)) {
                    correctClip.open(ais);
                }
                clipGains.put(correctClip, 1.0);
                applyVolume(correctClip, sfxVolume);
            }

            java.net.URL errorUrl = getClass().getResource("/assets/sfx/error.wav");
            if (errorUrl != null) {
                errorClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(errorUrl)) {
                    errorClip.open(ais);
                }
                clipGains.put(errorClip, 1.0);
                applyVolume(errorClip, sfxVolume);
            }

            java.net.URL lossUrl = getClass().getResource("/assets/sfx/loss.wav");
            if (lossUrl != null) {
                lossClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(lossUrl)) {
                    lossClip.open(ais);
                }
                clipGains.put(lossClip, 1.0);
                applyVolume(lossClip, sfxVolume);
            }

            java.net.URL winUrl = getClass().getResource("/assets/sfx/win.wav");
            if (winUrl != null) {
                winClip = AudioSystem.getClip();
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(winUrl)) {
                    winClip.open(ais);
                }
                clipGains.put(winClip, 1.0);
                applyVolume(winClip, sfxVolume);
            }

            loadCharacterSelectClip("CRUSADER", "/assets/sfx/char_selection/crusader.wav");
            loadCharacterSelectClip("HIGHWAYMAN", "/assets/sfx/char_selection/highwayman.wav");
            loadCharacterSelectClip("JESTER", "/assets/sfx/char_selection/jester.wav");
            loadCharacterSelectClip("OCCULTIST", "/assets/sfx/char_selection/occultist.wav");
            loadCharacterSelectClip("PLAGUEDOCTOR", "/assets/sfx/char_selection/plague_doctor.wav");
        } catch (Exception ignore) {}
    }

    private void loadCharacterSelectClip(String id, String resourcePath) {
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url == null) return;
            Clip clip = AudioSystem.getClip();
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                clip.open(ais);
                clipGains.put(clip, "PLAGUEDOCTOR".equals(id) ? 0.3 : 1.0);
                applyVolume(clip, sfxVolume);
            }
            charSelectClips.put(id, clip);
        } catch (Exception ignore) {}
    }

    public static SoundManager getInstance() { return INSTANCE; }

    public void playInvalidClick() {
        if (muted) return;
        long now = System.currentTimeMillis();
        if (now - lastInvalidClickMs < invalidCooldownMs) return;
        if (invalidClickClip != null) {
            try {
                if (invalidClickClip.isRunning()) invalidClickClip.stop();
                invalidClickClip.setFramePosition(0);
                invalidClickClip.start();
                lastInvalidClickMs = now;
            } catch (Exception ignore) {}
        }
    }

    public void setSfxVolume(double v) {
        sfxVolume = Math.max(0.0, Math.min(1.0, v));
        if (invalidClickClip != null) applyVolume(invalidClickClip, sfxVolume);
        if (healClip != null) applyVolume(healClip, sfxVolume);
        if (hintClip != null) applyVolume(hintClip, sfxVolume);
        if (sacrificeClip != null) applyVolume(sacrificeClip, sfxVolume);
        if (scoreClip != null) applyVolume(scoreClip, sfxVolume);
        if (characterSelectionClip != null) applyVolume(characterSelectionClip, sfxVolume);
        if (settingsClip != null) applyVolume(settingsClip, sfxVolume);
        if (winSelectItemClip != null) applyVolume(winSelectItemClip, sfxVolume);
        if (numberClickClip != null) applyVolume(numberClickClip, sfxVolume);
        if (correctClip != null) applyVolume(correctClip, sfxVolume);
        if (errorClip != null) applyVolume(errorClip, sfxVolume);
        if (lossClip != null) applyVolume(lossClip, sfxVolume);
        if (winClip != null) applyVolume(winClip, sfxVolume);
        for (Clip c : charSelectClips.values()) applyVolume(c, sfxVolume);
    }

    public double getSfxVolume() { return sfxVolume; }

    public void mute(boolean m) {
        muted = m;
    }

    private void applyVolume(Clip clip, double vol) {
        try {
            FloatControl ctl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            double base = (clip == musicClip) ? BASE_MUSIC_GAIN : BASE_SFX_GAIN;
            double gain = base * clipGains.getOrDefault(clip, 1.0);
            double effective = muted ? 0.0 : (vol * gain);
            double clamped = Math.max(0.0001, Math.min(1.0, effective));
            float dB = (float) (20.0 * Math.log10(clamped));
            dB = Math.max(ctl.getMinimum(), Math.min(ctl.getMaximum(), dB));
            ctl.setValue(dB);
        } catch (Exception ignore) {}
    }

    public void playHeal() {
        if (muted) return;
        if (healClip != null) {
            try { if (healClip.isRunning()) healClip.stop(); } catch (Exception ignore) {}
            try { healClip.setFramePosition(0); } catch (Exception ignore) {}
            try { healClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playHint() {
        if (muted) return;
        if (hintClip != null) {
            try { if (hintClip.isRunning()) hintClip.stop(); } catch (Exception ignore) {}
            try { hintClip.setFramePosition(0); } catch (Exception ignore) {}
            try { hintClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playSacrifice() {
        if (muted) return;
        if (sacrificeClip != null) {
            try { if (sacrificeClip.isRunning()) sacrificeClip.stop(); } catch (Exception ignore) {}
            try { sacrificeClip.setFramePosition(0); } catch (Exception ignore) {}
            try { sacrificeClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playScore() {
        if (muted) return;
        if (scoreClip != null) {
            try { if (scoreClip.isRunning()) scoreClip.stop(); } catch (Exception ignore) {}
            try { scoreClip.setFramePosition(0); } catch (Exception ignore) {}
            try { scoreClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playCharacterSelection() {
        if (muted) return;
        if (characterSelectionClip != null) {
            try { if (characterSelectionClip.isRunning()) characterSelectionClip.stop(); } catch (Exception ignore) {}
            try { characterSelectionClip.setFramePosition(0); } catch (Exception ignore) {}
            try { characterSelectionClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playSettingsToggle() {
        if (muted) return;
        if (settingsClip != null) {
            try { if (settingsClip.isRunning()) settingsClip.stop(); } catch (Exception ignore) {}
            try { settingsClip.setFramePosition(0); } catch (Exception ignore) {}
            try { settingsClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playWinItemSelection() {
        if (muted) return;
        if (winSelectItemClip != null) {
            try { if (winSelectItemClip.isRunning()) winSelectItemClip.stop(); } catch (Exception ignore) {}
            try { winSelectItemClip.setFramePosition(0); } catch (Exception ignore) {}
            try { winSelectItemClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playNumberClick() {
        if (muted) return;
        if (numberClickClip != null) {
            try { if (numberClickClip.isRunning()) numberClickClip.stop(); } catch (Exception ignore) {}
            try { numberClickClip.setFramePosition(0); } catch (Exception ignore) {}
            try { numberClickClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playCorrect() {
        if (muted) return;
        if (correctClip != null) {
            try { if (correctClip.isRunning()) correctClip.stop(); } catch (Exception ignore) {}
            try { correctClip.setFramePosition(0); } catch (Exception ignore) {}
            try { correctClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playError() {
        if (muted) return;
        if (errorClip != null) {
            try { if (errorClip.isRunning()) errorClip.stop(); } catch (Exception ignore) {}
            try { errorClip.setFramePosition(0); } catch (Exception ignore) {}
            try { errorClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playLoss() {
        if (muted) return;
        if (lossClip != null) {
            try { if (lossClip.isRunning()) lossClip.stop(); } catch (Exception ignore) {}
            try { lossClip.setFramePosition(0); } catch (Exception ignore) {}
            try { lossClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playWin() {
        if (muted) return;
        if (winClip != null) {
            try { if (winClip.isRunning()) winClip.stop(); } catch (Exception ignore) {}
            try { winClip.setFramePosition(0); } catch (Exception ignore) {}
            try { winClip.start(); } catch (Exception ignore) {}
        }
    }

    public void playCharacterSelectFor(String id) {
        if (muted) return;
        Clip clip = charSelectClips.get(id);
        if (clip != null) {
            try { if (clip.isRunning()) clip.stop(); } catch (Exception ignore) {}
            try { clip.setFramePosition(0); } catch (Exception ignore) {}
            try { clip.start(); } catch (Exception ignore) {}
        }
    }

    public void playCharacterSelectFor(String id, Runnable onFinished) {
        if (muted) {
            if (onFinished != null) { try { javafx.application.Platform.runLater(onFinished); } catch (Exception ignore) {} }
            return;
        }
        Clip clip = charSelectClips.get(id);
        if (clip != null) {
            try { if (clip.isRunning()) clip.stop(); } catch (Exception ignore) {}
            try { clip.setFramePosition(0); } catch (Exception ignore) {}
            try {
                clip.addLineListener(ev -> {
                    if (ev != null && ev.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                        if (onFinished != null) { try { javafx.application.Platform.runLater(onFinished); } catch (Exception ignore) {} }
                    }
                });
                clip.start();
            } catch (Exception ignore) {}
        } else {
            if (onFinished != null) { try { javafx.application.Platform.runLater(onFinished); } catch (Exception ignore) {} }
        }
    }

    public void stopMusic() {
        if (musicClip != null) {
            try { musicClip.stop(); } catch (Exception ignore) {}
            try { musicClip.close(); } catch (Exception ignore) {}
            musicClip = null;
        }
    }

    public void fadeOutMusic(int durationMs) {
        final Clip target = musicClip;
        if (target == null) return;
        new Thread(() -> {
            int d = Math.max(50, durationMs);
            double start = muted ? 0.0 : musicVolume;
            double end = 0.0001;
            int steps = 20;
            long sleep = d / steps;
            for (int i = 0; i < steps; i++) {
                double t = (double) (i + 1) / steps;
                double v = start + (end - start) * t;
                applyVolume(target, v);
                try { Thread.sleep(sleep); } catch (InterruptedException ignore) {}
            }
            try { target.stop(); } catch (Exception ignore) {}
            try { target.close(); } catch (Exception ignore) {}
            if (musicClip == target) {
                musicClip = null;
            }
        }).start();
    }

    public void setMusicVolume(double v) {
        musicVolume = Math.max(0.0, Math.min(1.0, v));
        if (musicClip != null) applyVolume(musicClip, muted ? 0.0 : musicVolume);
    }

    public double getMusicVolume() { return musicVolume; }

    public void playLevelMusicForCategory(String category) {
        if (category == null) return;
        stopMusic();
        if (muted) return;
        String base;
        if ("levels".equalsIgnoreCase(category)) {
            String[] cats = new String[]{"cove", "warrens", "weald"};
            base = "/assets/music/" + cats[new java.util.Random().nextInt(cats.length)] + "/";
        } else {
            base = "/assets/music/" + category + "/";
        }
        java.util.List<String> files = listAudioFiles(base);
        if (files.isEmpty()) return;
        String pick = files.get(new java.util.Random().nextInt(files.size()));
        try {
            java.net.URL url = getClass().getResource(pick);
            if (url == null) return;
            musicClip = AudioSystem.getClip();
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) { musicClip.open(ais); }
            clipGains.put(musicClip, 1.0);
            applyVolume(musicClip, 0.0001);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
            fadeInCurrentMusic(800);
        } catch (Exception ignore) {}
    }


    public void playHomeMusic() {
        stopMusic();
        if (muted) return;
        String base = "/assets/music/home_screen/";
        java.util.List<String> files = listAudioFiles(base);
        if (files.isEmpty()) return;
        String pick = files.get(new java.util.Random().nextInt(files.size()));
        try {
            java.net.URL url = getClass().getResource(pick);
            if (url == null) return;
            musicClip = AudioSystem.getClip();
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                musicClip.open(ais);
            }
            clipGains.put(musicClip, 1.0);
            applyVolume(musicClip, 0.0001);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
            fadeInCurrentMusic(2000);
        } catch (Exception ignore) {}
    }

    private void fadeInCurrentMusic(int durationMs) {
        if (musicClip == null) return;
        if (muted) return;
        new Thread(() -> {
            int d = Math.max(50, durationMs);
            double start = 0.0001;
            double end = musicVolume;
            int steps = 20;
            long sleep = d / steps;
            for (int i = 0; i < steps; i++) {
                double t = (double) (i + 1) / steps;
                double v = start + (end - start) * t;
                applyVolume(musicClip, v);
                try { Thread.sleep(sleep); } catch (InterruptedException ignore) {}
            }
        }).start();
    }

    private java.util.List<String> listAudioFiles(String basePath) {
        java.util.List<String> out = new java.util.ArrayList<>();
        try {
            java.net.URL url = getClass().getResource(basePath);
            if (url == null) return out;
            java.nio.file.Path dir = java.nio.file.Paths.get(url.toURI());
            try (java.util.stream.Stream<java.nio.file.Path> s = java.nio.file.Files.list(dir)) {
                s.filter(java.nio.file.Files::isRegularFile)
                 .map(p -> basePath + p.getFileName().toString())
                 .filter(f -> f.toLowerCase().endsWith(".wav"))
                 .forEach(out::add);
            }
        } catch (Exception ignore) {}
        return out;
    }
}
