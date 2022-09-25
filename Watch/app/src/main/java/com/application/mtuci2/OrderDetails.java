package com.application.mtuci2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OrderDetails extends Activity {
    private ProgressDialog pDialog;
    public static int orderID = -1;
    JSONParser jParser = new JSONParser();
    public static HashMap<String, String> orderInfo = new HashMap<String, String>();
    JSONArray order = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new getOrderInfo().execute();
            }
        }, 0, 5, TimeUnit.SECONDS);
        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrderDetails.this, MapScreen.class);
                startActivity(intent);
            }
        });
        Button button = (Button) findViewById(R.id.button5);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderID = -1;
                onBackPressed();
            }
        });
    }
    class getOrderInfo extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(CustomerOrderDetails.this);
            pDialog.setMessage("Загрузка заказа. Подождите...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }
        @SuppressLint("SetTextI18n")
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", String.valueOf(orderID)));
            JSONObject json = jParser.makeHttpRequest("http://5.180.137.9/get_order.php", "POST", params);

            try {
                int success = json.getInt("success");

                if (success == 1) {
                    order = json.getJSONArray("orders");

                    JSONObject c = order.getJSONObject(0);

                    String id = c.getString("id");
                    String courier = c.getString("courier");
                    String price = c.getString("price");
                    String status = c.getString("status");
                    String address = c.getString("address");
                    String coords = c.getString("coords");
                    String address2 = c.getString("address2");
                    String coords2 = c.getString("coords2");
                    String coords_courier = c.getString("coords_courier");

                    orderInfo.put("id", id);
                    orderInfo.put("courier", courier);
                    orderInfo.put("price", price);
                    orderInfo.put("status", status);
                    orderInfo.put("address", address);
                    orderInfo.put("coords", coords);
                    orderInfo.put("address2", address2);
                    orderInfo.put("coords2", coords2);
                    orderInfo.put("coords_courier", coords_courier);
                    TextView txtID = (TextView) findViewById(R.id.textView6);
                    TextView txtCourier = (TextView) findViewById(R.id.textView8);
                    TextView txtPrice = (TextView) findViewById(R.id.textView9);
                    TextView txtAddress = (TextView) findViewById(R.id.textView10);
                    TextView txtStatus = (TextView) findViewById(R.id.textView14);
                    txtID.setText("Order №"+orderInfo.get("id"));
                    txtCourier.setText("Courier: "+orderInfo.get("courier"));
                    txtPrice.setText("Price: "+orderInfo.get("price")+"$");
                    txtAddress.setText("Address: "+orderInfo.get("address"));
                    txtStatus.setText(orderInfo.get("status"));
                } else {
                    // заказ не найден
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            //pDialog.dismiss();
        }

    }
}