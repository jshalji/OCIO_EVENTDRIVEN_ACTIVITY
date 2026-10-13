package com.example.ocio_eventdriven_activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String API_BASE_URL =
            "https://user-api-qpau.onrender.com";

    private LinearLayout loginLayout;
    private ScrollView signUpScrollView;
    private LinearLayout otpLayout;
    private LinearLayout successLayout;

    private EditText usernameEditText;
    private EditText loginPasswordEditText;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText confirmEmailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private EditText otpEditText;

    private TextView otpDisplayTextView;
    private TextView otpTimerTextView;
    private TextView usersStatusTextView;
    private TextView usersListTextView;

    private Button loginButton;
    private Button openSignUpButton;
    private Button signUpButton;
    private Button cancelButton;
    private Button verifyOtpButton;
    private Button refreshOtpButton;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private String generatedOtp = "";
    private boolean otpExpired = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupButtons();
        showLoginScreen();
    }

    private void initializeViews() {
        loginLayout = findViewById(R.id.loginLayout);
        signUpScrollView = findViewById(R.id.signUpScrollView);
        otpLayout = findViewById(R.id.otpLayout);
        successLayout = findViewById(R.id.successLayout);

        usernameEditText = findViewById(R.id.usernameEditText);
        loginPasswordEditText =
                findViewById(R.id.loginPasswordEditText);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        confirmEmailEditText =
                findViewById(R.id.confirmEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText =
                findViewById(R.id.confirmPasswordEditText);

        otpEditText = findViewById(R.id.otpEditText);
        otpDisplayTextView =
                findViewById(R.id.otpDisplayTextView);
        otpTimerTextView =
                findViewById(R.id.otpTimerTextView);

        usersStatusTextView =
                findViewById(R.id.usersStatusTextView);
        usersListTextView =
                findViewById(R.id.usersListTextView);

        loginButton = findViewById(R.id.loginButton);
        openSignUpButton =
                findViewById(R.id.openSignUpButton);
        signUpButton = findViewById(R.id.signUpButton);
        cancelButton = findViewById(R.id.cancelButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        refreshOtpButton = findViewById(R.id.refreshOtpButton);
    }

    private void setupButtons() {
        loginButton.setOnClickListener(v -> login());

        openSignUpButton.setOnClickListener(v -> {
            loginLayout.setVisibility(View.GONE);
            otpLayout.setVisibility(View.GONE);
            successLayout.setVisibility(View.GONE);
            signUpScrollView.setVisibility(View.VISIBLE);
        });

        signUpButton.setOnClickListener(v -> signUp());

        cancelButton.setOnClickListener(v -> showLoginScreen());

        verifyOtpButton.setOnClickListener(v -> verifyOtp());

        refreshOtpButton.setOnClickListener(v -> generateNewOtp());
    }

    private void showLoginScreen() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        loginLayout.setVisibility(View.VISIBLE);
        signUpScrollView.setVisibility(View.GONE);
        otpLayout.setVisibility(View.GONE);
        successLayout.setVisibility(View.GONE);
    }

    private void signUp() {
        String firstName =
                firstNameEditText.getText().toString().trim();
        String lastName =
                lastNameEditText.getText().toString().trim();
        String email =
                emailEditText.getText().toString().trim();
        String confirmEmail =
                confirmEmailEditText.getText().toString().trim();
        String password =
                passwordEditText.getText().toString();
        String confirmPassword =
                confirmPasswordEditText.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || confirmEmail.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.equalsIgnoreCase(confirmEmail)) {
            confirmEmailEditText.setError("Emails do not match");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        signUpButton.setEnabled(false);

        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("FirstName", firstName);
                requestBody.put("LastName", lastName);
                requestBody.put("Email", email);
                requestBody.put("Password", password);

                JSONObject response = sendRequest(
                        "/register", "POST", requestBody);

                boolean success =
                        response.optBoolean("success", false);

                String message = response.optString(
                        "message",
                        success ? "Registration successful."
                                : "Registration failed.");

                runOnUiThread(() -> {
                    signUpButton.setEnabled(true);

                    Toast.makeText(MainActivity.this, message,
                            Toast.LENGTH_LONG).show();

                    if (success) {
                        usernameEditText.setText(email);
                        loginPasswordEditText.setText("");

                        firstNameEditText.setText("");
                        lastNameEditText.setText("");
                        emailEditText.setText("");
                        confirmEmailEditText.setText("");
                        passwordEditText.setText("");
                        confirmPasswordEditText.setText("");

                        showLoginScreen();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    signUpButton.setEnabled(true);

                    Toast.makeText(MainActivity.this,
                            "Signup error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void login() {
        String email =
                usernameEditText.getText().toString().trim();
        String password =
                loginPasswordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Enter your email and password.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("Email", email);
                requestBody.put("Password", password);

                JSONObject response = sendRequest(
                        "/login", "POST", requestBody);

                boolean success =
                        response.optBoolean("success", false);

                String message = response.optString(
                        "message", "Invalid email or password.");

                runOnUiThread(() -> {
                    loginButton.setEnabled(true);

                    if (success) {
                        showOtpScreen();
                    } else {
                        Toast.makeText(MainActivity.this,
                                message, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);

                    Toast.makeText(MainActivity.this,
                            "Login error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showOtpScreen() {
        loginLayout.setVisibility(View.GONE);
        signUpScrollView.setVisibility(View.GONE);
        successLayout.setVisibility(View.GONE);
        otpLayout.setVisibility(View.VISIBLE);

        generateNewOtp();
    }

    private void generateNewOtp() {
        otpEditText.setText("");
        otpExpired = false;

        int otpNumber = 100000 + new Random().nextInt(900000);
        generatedOtp = String.valueOf(otpNumber);

        otpDisplayTextView.setText("Your OTP: " + generatedOtp);
        otpTimerTextView.setText("Time remaining: 15 seconds");

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                otpTimerTextView.setText(
                        "Time remaining: "
                                + ((millisUntilFinished + 999) / 1000)
                                + " seconds");
            }

            @Override
            public void onFinish() {
                otpExpired = true;
                otpTimerTextView.setText(
                        "OTP expired. Refresh to get a new one.");
                otpDisplayTextView.setText("OTP expired");
            }
        }.start();
    }

    private void verifyOtp() {
        String enteredOtp =
                otpEditText.getText().toString().trim();

        if (otpExpired) {
            Toast.makeText(this,
                    "OTP expired. Tap Refresh OTP to get a new one.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (enteredOtp.isEmpty()) {
            otpEditText.setError("Enter the OTP");
            return;
        }

        if (enteredOtp.equals(generatedOtp)) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            otpLayout.setVisibility(View.GONE);
            successLayout.setVisibility(View.VISIBLE);
            loadUsers();

        } else {
            Toast.makeText(this, "Invalid OTP. Try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUsers() {
        usersStatusTextView.setText("Loading users...");
        usersListTextView.setText("");

        executorService.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_BASE_URL + "/");
                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty(
                        "Accept", "application/json");

                int statusCode = connection.getResponseCode();

                InputStream inputStream;

                if (statusCode >= 200 && statusCode < 300) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                if (inputStream == null) {
                    throw new Exception(
                            "Server returned HTTP " + statusCode);
                }

                StringBuilder responseText = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                inputStream, StandardCharsets.UTF_8))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        responseText.append(line);
                    }
                }

                if (statusCode < 200 || statusCode >= 300) {
                    throw new Exception(
                            "Server returned HTTP " + statusCode);
                }

                // API returns a raw JSON array.
                JSONArray users =
                        new JSONArray(responseText.toString());

                StringBuilder result = new StringBuilder();

                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);

                    String firstName = getJsonString(user,
                            "FirstName", "firstName", "firstname");
                    String lastName = getJsonString(user,
                            "LastName", "lastName", "lastname");
                    String email = getJsonString(user,
                            "Email", "email");

                    String fullName =
                            (firstName + " " + lastName).trim();

                    if (fullName.isEmpty()) {
                        fullName = "User";
                    }

                    result.append(i + 1)
                            .append(". ")
                            .append(fullName)
                            .append("\n")
                            .append("   Email: ")
                            .append(email)
                            .append("\n\n");
                }

                String finalResult = result.toString();

                runOnUiThread(() -> {
                    usersStatusTextView.setText("Users List");

                    if (finalResult.isEmpty()) {
                        usersListTextView.setText("No users found.");
                    } else {
                        usersListTextView.setText(
                                finalResult.trim());
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    usersStatusTextView.setText("Users List");
                    usersListTextView.setText(
                            "Could not load users.\n"
                                    + e.getMessage());
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String getJsonString(
            JSONObject object, String... possibleKeys) {

        for (String key : possibleKeys) {
            String value = object.optString(key, "");

            if (!value.isEmpty() && !value.equals("null")) {
                return value;
            }
        }

        return "";
    }

    private JSONObject sendRequest(
            String endpoint, String method, JSONObject requestBody)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(API_BASE_URL + endpoint);
            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty(
                    "Accept", "application/json");

            if (requestBody != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8");

                try (OutputStream outputStream =
                             connection.getOutputStream()) {
                    outputStream.write(
                            requestBody.toString().getBytes(
                                    StandardCharsets.UTF_8));
                }
            }

            int statusCode = connection.getResponseCode();

            InputStream inputStream;

            if (statusCode >= 200 && statusCode < 400) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            StringBuilder responseText = new StringBuilder();

            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                inputStream, StandardCharsets.UTF_8))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        responseText.append(line);
                    }
                }
            }

            if (responseText.length() == 0) {
                throw new Exception(
                        "Server returned HTTP " + statusCode);
            }

            JSONObject response =
                    new JSONObject(responseText.toString());

            if (statusCode >= 400 && !response.has("success")) {
                response.put("success", false);
            }

            return response;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        executorService.shutdown();
        super.onDestroy();
    }
}