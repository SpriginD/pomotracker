package com.erman.pomotracker;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    NumberPicker timerPicker, sessionPicker;
    ProgressBar taskProgressBar, sessionProgressBar;
    ImageButton toggleTaskButton, stopTaskButton, settingsButton;
    private TextView taskProgressBarText, sessionCountDownText, taskSessionCountText;
    private CountDownTimer countDownTimer = null;

    long pauseTime;
    boolean onTask;

    int minTaskTime = 5, maxTaskTime = 60, stepTaskTime = 5;
    private int totalSessions, completedSessions;

    private final String[] displayedTimerValues = new String[(maxTaskTime - minTaskTime) / stepTaskTime + 1];

    private long timeLeftInMillis;
    private boolean timerRunning;
    private Logger logger;
    private LayerDrawable sessionProgressbarFGLayer;
    private Drawable startDrawable;
    private Drawable pauseDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupTimerSession();
        setupProgressBar();
        setupButtons();

        logger = new Logger(this);

        sessionProgressbarFGLayer = (LayerDrawable) ContextCompat.getDrawable(this, R.drawable.custom_timer_foreground);

        startDrawable = ContextCompat.getDrawable(this, R.drawable.baseline_play_arrow_24);
        pauseDrawable = ContextCompat.getDrawable(this, R.drawable.baseline_pause_24);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        // NumberPickers
        timerPicker = findViewById(R.id.timerPicker);
        sessionPicker = findViewById(R.id.sessionPicker);

        // ProgressBars
        taskProgressBar = findViewById(R.id.taskProgressBar);
        sessionProgressBar = findViewById(R.id.sessionProgressBar);

        // Buttons
        toggleTaskButton = findViewById(R.id.toggleTaskButton);
//        notificationButton = findViewById(R.id.notificationButton);
        stopTaskButton = findViewById(R.id.stopTaskButton);
        settingsButton = findViewById(R.id.settingsButton);

        // TextViews
        taskProgressBarText = findViewById(R.id.taskProgressBarText);
        sessionCountDownText = findViewById(R.id.sessionCountDownText);
        taskSessionCountText = findViewById(R.id.taskSessionCountText);
    }

    void setupTimerSession() {
        for (int i = 0, value = minTaskTime; value <= maxTaskTime; i++, value += 5) {
            displayedTimerValues[i] = String.valueOf(value);
        }

        timerPicker.setMinValue(0);
        timerPicker.setMaxValue(displayedTimerValues.length - 1);
        timerPicker.setDisplayedValues(displayedTimerValues);
        timerPicker.setValue(4);

        sessionPicker.setMinValue(1);
        sessionPicker.setMaxValue(4);
        sessionPicker.setValue(2);

        totalSessions = sessionPicker.getValue();
    }

    void setupProgressBar() {
        taskProgressBar.setMax(100);
        sessionProgressBar.setMax(100);
    }

    void setupButtons() {
//        notificationButton.setOnClickListener(v -> {
//            startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
//            finish();
//        });

        toggleTaskButton.setOnClickListener(v -> toggleTask());
        stopTaskButton.setOnClickListener(v -> stopTask());

        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            finish();
        });
    }

    private void toggleTask() {
        if((taskProgressBar.getProgress() == 0 || taskProgressBar.getProgress() == 100) && countDownTimer == null) {
            int timerMinutes = Integer.parseInt(displayedTimerValues[timerPicker.getValue()]);
            totalSessions = sessionPicker.getValue();

            toggleTaskButton.setBackground(pauseDrawable);

            timerPicker.setEnabled(false);
            sessionPicker.setEnabled(false);

            completedSessions = 0;

            timeLeftInMillis = timerMinutes * 60000L;

            taskProgressBar.setProgress(0);
            sessionProgressBar.setProgress(0);

            taskSessionCountText.setText(String.format("%s / %s seans", completedSessions+1, totalSessions));

            startSession(timeLeftInMillis);
        } else {
            if(timerRunning) {
                pauseTime = timeLeftInMillis;
                changeColorSession(R.color.container_border);
                toggleTaskButton.setBackground(startDrawable);
                timerRunning = false;
                countDownTimer.cancel();
            } else {
                if(onTask) {
                    startSession(pauseTime);
                } else {
                    startTakeBreak(pauseTime);
                }

                toggleTaskButton.setBackground(pauseDrawable);
                timerRunning = true;
            }
        }
    }

    private void stopTask() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        timerPicker.setEnabled(true);
        sessionPicker.setEnabled(true);

        toggleTaskButton.setBackground(startDrawable);

        timerRunning = false;
        timeLeftInMillis = 0;

        changeColorSession(R.color.container_border);

        updateCountdownText();
        taskProgressBarText.setText("%0");

        taskProgressBar.setProgress(0);
        sessionProgressBar.setProgress(0);

        taskSessionCountText.setText("Görev bekleniyor");
    }

    private void startSession(long startTime) {
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        changeColorSession(R.color.foreground);

        onTask = true;

        countDownTimer = new CountDownTimer(startTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                countDownTimer = null;
                completedSessions++;

                if (completedSessions < totalSessions) {
                    taskSessionCountText.setText("Mola süresi");

                    logger.logEvent("Mola vakti!");

                    startTakeBreak((long) (.1 * 60000L));
                } else {
                    timerRunning = false;
                    logger.logEvent("Görevi başarıyla tamamladınız.");
                    changeColorSession(R.color.container_border);
                    taskSessionCountText.setText("Tamamlandı");
                    timerPicker.setEnabled(true);
                    sessionPicker.setEnabled(true);
                    updateProgressBar();
                }
            }
        }.start();

        timerRunning = true;
        updateProgressBar();
    }

    private void startTakeBreak(long startTime) {
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        onTask = false;

        changeColorSession(R.color.confirm);

        countDownTimer = new CountDownTimer(startTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                logger.logEvent("Çalışmaya devam :)");
                countDownTimer = null;
                taskSessionCountText.setText(String.format("%s / %s seans", completedSessions+1, totalSessions));
                startSession((long) (.1 * 60000L));
            }
        }.start();

        timerRunning = true;
    }

    private void changeColorSession(int color) {
        if (sessionProgressbarFGLayer != null) {
            // İç öğe olan GradientDrawable'ı al
            GradientDrawable gradientDrawable = (GradientDrawable) sessionProgressbarFGLayer.findDrawableByLayerId(R.id.ring_shape_item);

            if (gradientDrawable != null) {
                // Renk değerini değiştir
                gradientDrawable.setColor(ContextCompat.getColor(this, color));
            }

            // ImageView'e yeni drawable'ı ayarla
            sessionProgressBar.setProgressDrawable(sessionProgressbarFGLayer);
        }
    }

    private void updateCountdownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d : %02d", minutes, seconds);
        sessionCountDownText.setText(timeFormatted);
    }

    private void updateProgressBar() {
        int totalSeconds = Integer.parseInt(displayedTimerValues[timerPicker.getValue()]) * 60;
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (minutes * 60 + (int) (timeLeftInMillis / 1000) % 60);

        int progress = (int) (((double) (completedSessions) / totalSessions) * 100);
        int addProgress = (int) (((double) (totalSeconds - seconds) / totalSeconds) * 100 / totalSessions);
        int totalProgress = Math.min((progress + addProgress), 100);

        taskProgressBar.setProgress(totalProgress);
        taskProgressBarText.setText(String.format("%%%s", totalProgress));
        sessionProgressBar.setProgress((int) (((double) (totalSeconds - seconds) / totalSeconds) * 100));
    }
}