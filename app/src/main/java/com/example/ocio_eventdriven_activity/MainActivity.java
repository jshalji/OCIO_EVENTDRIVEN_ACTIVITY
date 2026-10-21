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

    private static final String CUSTOMER_API_BASE_URL =
            "http://10.0.2.2:3000";

    private LinearLayout loginLayout;
    private ScrollView signUpScrollView;
    private LinearLayout otpLayout;
    private LinearLayout successLayout;
    private LinearLayout dashboardLayout;
    private ScrollView addCustomerLayout;
    private LinearLayout customerListLayout;

    private EditText usernameEditText;
    private EditText loginPasswordEditText;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText confirmEmailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private EditText otpEditText;

    private EditText customerIdEditText;
    private EditText customerLastNameEditText;
    private EditText customerFirstNameEditText;
    private EditText customerMobileEditText;
    private EditText customerAddressEditText;

    private TextView otpDisplayTextView;
    private TextView otpTimerTextView;
    private TextView usersStatusTextView;
    private TextView usersListTextView;
    private TextView customerListTextView;

    private Button loginButton;
    private Button openSignUpButton;
    private Button signUpButton;
    private Button cancelButton;
    private Button verifyOtpButton;
    private Button refreshOtpButton;

    private Button addCustomerButton;
    private Button viewCustomersButton;
    private Button viewUsersButton;
    private Button dashboardLogoutButton;
    private Button saveCustomerButton;
    private Button cancelCustomerButton;
    private Button refreshCustomersButton;
    private Button backFromCustomersButton;
    private Button backFromUsersButton;

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
        dashboardLayout = findViewById(R.id.dashboardLayout);
        addCustomerLayout = findViewById(R.id.addCustomerLayout);
        customerListLayout = findViewById(R.id.customerListLayout);

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

        customerIdEditText =
                findViewById(R.id.customerIdEditText);
        customerLastNameEditText =
                findViewById(R.id.customerLastNameEditText);
        customerFirstNameEditText =
                findViewById(R.id.customerFirstNameEditText);
        customerMobileEditText =
                findViewById(R.id.customerMobileEditText);
        customerAddressEditText =
                findViewById(R.id.customerAddressEditText);

        customerListTextView =
                findViewById(R.id.customerListTextView);

        loginButton = findViewById(R.id.loginButton);
        openSignUpButton = findViewById(R.id.openSignUpButton);
        signUpButton = findViewById(R.id.signUpButton);
        cancelButton = findViewById(R.id.cancelButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        refreshOtpButton = findViewById(R.id.refreshOtpButton);

        addCustomerButton = findViewById(R.id.addCustomerButton);
        viewCustomersButton = findViewById(R.id.viewCustomersButton);
        viewUsersButton = findViewById(R.id.viewUsersButton);
        dashboardLogoutButton =
                findViewById(R.id.dashboardLogoutButton);
        saveCustomerButton = findViewById(R.id.saveCustomerButton);
        cancelCustomerButton =
                findViewById(R.id.cancelCustomerButton);
        refreshCustomersButton =
                findViewById(R.id.refreshCustomersButton);
        backFromCustomersButton =
                findViewById(R.id.backFromCustomersButton);

        backFromUsersButton =
                findViewById(R.id.backFromUsersButton);
    }

    private void setupButtons() {
        loginButton.setOnClickListener(v -> login());

        openSignUpButton.setOnClickListener(v -> {
            hideAllScreens();
            signUpScrollView.setVisibility(View.VISIBLE);
        });

        signUpButton.setOnClickListener(v -> signUp());
        cancelButton.setOnClickListener(v -> showLoginScreen());
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
        refreshOtpButton.setOnClickListener(v -> generateNewOtp());

        addCustomerButton.setOnClickListener(
                v -> showAddCustomerScreen());

        viewCustomersButton.setOnClickListener(
                v -> showCustomersScreen());

        viewUsersButton.setOnClickListener(
                v -> showUsersScreen());

        dashboardLogoutButton.setOnClickListener(
                v -> showLoginScreen());

        saveCustomerButton.setOnClickListener(v -> saveCustomer());

        cancelCustomerButton.setOnClickListener(
                v -> showDashboard());

        refreshCustomersButton.setOnClickListener(
                v -> loadCustomers());

        backFromCustomersButton.setOnClickListener(
                v -> showDashboard());

        // FIX: Return from Registered Users to the dashboard.
        backFromUsersButton.setOnClickListener(
                v -> showDashboard());
    }

    private void hideAllScreens() {
        loginLayout.setVisibility(View.GONE);
        signUpScrollView.setVisibility(View.GONE);
        otpLayout.setVisibility(View.GONE);
        successLayout.setVisibility(View.GONE);
        dashboardLayout.setVisibility(View.GONE);
        addCustomerLayout.setVisibility(View.GONE);
        customerListLayout.setVisibility(View.GONE);
    }

    private void showLoginScreen() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        hideAllScreens();
        loginLayout.setVisibility(View.VISIBLE);
    }

    private void showDashboard() {
        hideAllScreens();
        dashboardLayout.setVisibility(View.VISIBLE);
    }

    private void showAddCustomerScreen() {
        hideAllScreens();
        addCustomerLayout.setVisibility(View.VISIBLE);
    }

    private void showCustomersScreen() {
        hideAllScreens();
        customerListLayout.setVisibility(View.VISIBLE);
        loadCustomers();
    }

    private void showUsersScreen() {
        hideAllScreens();
        successLayout.setVisibility(View.VISIBLE);
        loadUsers();
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
                        API_BASE_URL + "/register",
                        "POST", requestBody);

                boolean success =
                        response.optBoolean("success", false);

                String message = response.optString(
                        "message",
                        success ? "Registration successful."
                                : "Registration failed.");

                runOnUiThread(() -> {
                    signUpButton.setEnabled(true);

                    Toast.makeText(this, message,
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
                    Toast.makeText(this,
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
                        API_BASE_URL + "/login",
                        "POST", requestBody);

                boolean success =
                        response.optBoolean("success", false);

                String message = response.optString(
                        "message", "Invalid email or password.");

                runOnUiThread(() -> {
                    loginButton.setEnabled(true);

                    if (success) {
                        showOtpScreen();
                    } else {
                        Toast.makeText(this, message,
                                Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    Toast.makeText(this,
                            "Login error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showOtpScreen() {
        hideAllScreens();
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

            showDashboard();
        } else {
            Toast.makeText(this, "Invalid OTP. Try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCustomer() {
        String customerId =
                customerIdEditText.getText().toString().trim();
        String lastName =
                customerLastNameEditText.getText().toString().trim();
        String firstName =
                customerFirstNameEditText.getText().toString().trim();
        String mobile =
                customerMobileEditText.getText().toString().trim();
        String address =
                customerAddressEditText.getText().toString().trim();

        if (customerId.isEmpty()) {
            customerIdEditText.setError("Enter customer ID");
            return;
        }

        if (lastName.isEmpty()) {
            customerLastNameEditText.setError("Enter last name");
            return;
        }

        if (firstName.isEmpty()) {
            customerFirstNameEditText.setError("Enter first name");
            return;
        }

        final int id;

        try {
            id = Integer.parseInt(customerId);
        } catch (NumberFormatException e) {
            customerIdEditText.setError(
                    "Customer ID must be a number");
            return;
        }

        if (id < 0) {
            customerIdEditText.setError(
                    "Customer ID cannot be negative");
            return;
        }

        JSONObject customer = new JSONObject();

        try {
            customer.put("customerid", id);
            customer.put("lastname", lastName);
            customer.put("firstname", firstName);
            customer.put("mobileno", mobile);
            customer.put("address", address);
        } catch (Exception e) {
            Toast.makeText(this, "Could not prepare customer data.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        saveCustomerButton.setEnabled(false);

        executorService.execute(() -> {
            try {
                JSONObject response = sendRequest(
                        CUSTOMER_API_BASE_URL + "/customers",
                        "POST", customer);

                String message = response.optString(
                        "message", "Customer saved successfully.");

                runOnUiThread(() -> {
                    saveCustomerButton.setEnabled(true);

                    Toast.makeText(this, message,
                            Toast.LENGTH_LONG).show();

                    if (response.has("customer")) {
                        customerIdEditText.setText("");
                        customerLastNameEditText.setText("");
                        customerFirstNameEditText.setText("");
                        customerMobileEditText.setText("");
                        customerAddressEditText.setText("");

                        showCustomersScreen();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    saveCustomerButton.setEnabled(true);
                    Toast.makeText(this,
                            "Save failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadCustomers() {
        customerListTextView.setText("Loading customers...");
        refreshCustomersButton.setEnabled(false);

        executorService.execute(() -> {
            try {
                JSONArray customers = getArrayRequest(
                        CUSTOMER_API_BASE_URL + "/customers");

                StringBuilder result = new StringBuilder();

                for (int i = 0; i < customers.length(); i++) {
                    JSONObject customer = customers.getJSONObject(i);

                    result.append("Customer ID: ")
                            .append(customer.optString("customerid", ""))
                            .append("\n")
                            .append("Name: ")
                            .append(customer.optString("firstname", ""))
                            .append(" ")
                            .append(customer.optString("lastname", ""))
                            .append("\n")
                            .append("Mobile: ")
                            .append(customer.optString("mobileno", ""))
                            .append("\n")
                            .append("Address: ")
                            .append(customer.optString("address", ""))
                            .append("\n\n");
                }

                String finalResult = result.toString();

                runOnUiThread(() -> {
                    refreshCustomersButton.setEnabled(true);

                    if (finalResult.isEmpty()) {
                        customerListTextView.setText(
                                "No customers found.");
                    } else {
                        customerListTextView.setText(
                                finalResult.trim());
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    refreshCustomersButton.setEnabled(true);
                    customerListTextView.setText(
                            "Could not load customers.\n"
                                    + e.getMessage());
                });
            }
        });
    }

    private void loadUsers() {
        usersStatusTextView.setText("Loading users...");
        usersListTextView.setText("");

        executorService.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_BASE_URL + "/");
                connection = (HttpURLConnection)
                        url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty(
                        "Accept", "application/json");

                int statusCode = connection.getResponseCode();

                InputStream inputStream =
                        statusCode >= 200 && statusCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream();

                if (inputStream == null) {
                    throw new Exception(
                            "Server returned HTTP " + statusCode);
                }

                StringBuilder responseText = new StringBuilder();

                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             inputStream,
                                             StandardCharsets.UTF_8))) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        responseText.append(line);
                    }
                }

                if (statusCode < 200 || statusCode >= 300) {
                    throw new Exception(
                            "Server returned HTTP " + statusCode);
                }

                JSONArray users =
                        new JSONArray(responseText.toString());

                StringBuilder result = new StringBuilder();

                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);

                    String firstName = getJsonString(
                            user, "FirstName", "firstName", "firstname");

                    String lastName = getJsonString(
                            user, "LastName", "lastName", "lastname");

                    String email = getJsonString(
                            user, "Email", "email");

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

                    usersListTextView.setText(
                            finalResult.isEmpty()
                                    ? "No users found."
                                    : finalResult.trim());
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
            String requestUrl,
            String method,
            JSONObject requestBody) throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection)
                    url.openConnection();

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

            InputStream inputStream =
                    statusCode >= 200 && statusCode < 400
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            StringBuilder responseText = new StringBuilder();

            if (inputStream != null) {
                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             inputStream,
                                             StandardCharsets.UTF_8))) {
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

            if (statusCode >= 400 && !response.has("message")) {
                String error = response.optString("error", "");
                String details = response.optString("details", "");

                String message = error;

                if (!details.isEmpty()) {
                    message += ": " + details;
                }

                response.put(
                        "message",
                        message.isEmpty()
                                ? "HTTP " + statusCode
                                : message);
            }

            return response;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JSONArray getArrayRequest(String requestUrl)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection)
                    url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty(
                    "Accept", "application/json");

            int statusCode = connection.getResponseCode();

            InputStream inputStream =
                    statusCode >= 200 && statusCode < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            StringBuilder responseText = new StringBuilder();

            if (inputStream != null) {
                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             inputStream,
                                             StandardCharsets.UTF_8))) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        responseText.append(line);
                    }
                }
            }

            if (statusCode < 200 || statusCode >= 300) {
                throw new Exception(
                        "HTTP " + statusCode + ": " + responseText);
            }

            return new JSONArray(responseText.toString());

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