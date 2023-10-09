package com.uog.gpt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView tv_Question, tv_Response;
    TextInputEditText ed_Query;
    String url = "https://api.openai.com/v1/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_Question = findViewById(R.id.tv_Question);
        tv_Response = findViewById(R.id.tv_Response);
        ed_Query = findViewById(R.id.ed_Query);

        tv_Response.setMovementMethod(new ScrollingMovementMethod());

        ed_Query.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                Log.d("EditText", "Editor action pressed: " + i);


                if (i == EditorInfo.IME_ACTION_SEND  || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    tv_Response.setText("Please wait...");
                    Log.d("EditText", "Enter pressed. Text: " + ed_Query.getText().toString());

                    if (ed_Query.getText().toString().length() > 0) {
                        getResponse(ed_Query.getText().toString());
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter your query ..", Toast.LENGTH_SHORT).show();

                    }
                }

                return false;
            }
        });

    }


    private void getResponse(String query) {
        Log.d("test", "onCreate: ");

// setting text on for question on below line.
        tv_Question.setText(query);
        ed_Query.setText("");

        // Add a delay between requests to avoid hitting rate limits
        try {
            Thread.sleep(1000); // Sleep for 1 second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


// creating a queue for request queue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
// creating a json object on below line.
        JSONObject jsonObject = new JSONObject();
// adding params to json object.
        try {
            jsonObject.put("model", "text-davinci-003");
            jsonObject.put("prompt", query);
            jsonObject.put("temperature", 0.0);  // Use 0.0 instead of 0 for the temperature
            jsonObject.put("max_tokens", 100);
            jsonObject.put("top_p", 1.0);  // Use 1.0 instead of 1 for top_p
            jsonObject.put("frequency_penalty", 0.0);
            jsonObject.put("presence_penalty", 0.0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String responseMsg =
                            response.getJSONArray("choices").getJSONObject(0).getString("text");
                    tv_Response.setText(responseMsg);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAGAPI", "Error is :" + error.getMessage() + "\n" + error);

                if (error.networkResponse != null) {
                    Log.e("TAGAPI", "Status Code: " + error.networkResponse.statusCode);
                    Log.e("TAGAPI", "Response Data: " + new String(error.networkResponse.data));
                }

            }
        }) {


//Passing some request headers

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                //adding headers on below line.
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer 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");

                return params;
            }
        };

        // on below line adding retry policy for our request.
        postRequest.setRetryPolicy(new RetryPolicy() {


            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(postRequest);
    }
}