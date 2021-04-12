package com.example.covid19tracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.leo.simplearcloader.SimpleArcLoader;
import com.ybs.countrypicker.CountryPicker;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "https://coronavirus-19-api.herokuapp.com/countries";

    private TextView totalCases, newCases, totalDeaths, newDeaths, totalRecovered, countryName;
    private SimpleArcLoader simpleArcLoader1, simpleArcLoader2;
    private ScrollView scrollView;
    private PieChart pieChart;

    private String selectedCountry = "Global";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countryName = findViewById(R.id.countryName_id);
        ImageView refresh = findViewById(R.id.refresh_id);
        totalCases = findViewById(R.id.totalCases_id);
        newCases = findViewById(R.id.newCases_id);
        totalDeaths = findViewById(R.id.totalDeath_id);
        newDeaths = findViewById(R.id.newDeath_id);
        totalRecovered = findViewById(R.id.totalRecovered_id);
        Button button = findViewById(R.id.button_id);
        simpleArcLoader1 = findViewById(R.id.arcLoader1_id);
        simpleArcLoader2 = findViewById(R.id.arcLoader2_id);
        scrollView = findViewById(R.id.scrollView_id);
        pieChart = findViewById(R.id.pieChart);

        button.setOnClickListener(v -> {
            CountryPicker picker = CountryPicker.newInstance("Select Country");
            picker.setListener((name, code, dialCode, flagDrawableResID) -> {
                countryName.setText(name);
                selectedCountry = name;
                picker.dismiss();

                getData();
            });
            picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
        });

        refresh.setOnClickListener(v -> {
            pieChart.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
            getData();
        });

        getData();
    }

    private void getData() {

        simpleArcLoader1.start();
        simpleArcLoader2.start();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL, null, response -> {
            try {
                String TotalConfirmed = "0", NewConfirmed = "0", TotalDeaths = "0", NewDeaths = "0", TotalRecovered = "0";

                if (!selectedCountry.equals("Global")) {

                    boolean countryExists = false;

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsn = response.getJSONObject(i);

                        if (selectedCountry.equals(jsn.getString("country"))) {

                            TotalConfirmed = jsn.getString("cases");
                            NewConfirmed = jsn.getString("todayCases");
                            TotalDeaths = jsn.getString("deaths");
                            NewDeaths = jsn.getString("todayDeaths");
                            TotalRecovered = jsn.getString("recovered");

                            countryExists = true;
                            break;
                        }
                    }

                    if (!countryExists) {
                        Toast.makeText(MainActivity.this, "Unable to provide data for selected country." , Toast.LENGTH_LONG).show();
                    }

                } else {

                    JSONObject globalJSONObject = response.getJSONObject(0);

                    TotalConfirmed = globalJSONObject.getString("cases");
                    NewConfirmed = globalJSONObject.getString("todayCases");
                    TotalDeaths = globalJSONObject.getString("deaths");
                    NewDeaths = globalJSONObject.getString("todayDeaths");
                    TotalRecovered = globalJSONObject.getString("recovered");
                }

                totalCases.setText(TotalConfirmed);
                newCases.setText(NewConfirmed);
                totalDeaths.setText(TotalDeaths);
                newDeaths.setText(NewDeaths);
                totalRecovered.setText(TotalRecovered);

                pieChart.clearChart();

                pieChart.addPieSlice(new PieModel("totalCases", Integer.parseInt(TotalConfirmed), Color.parseColor("#FFA726")));
                pieChart.addPieSlice(new PieModel("totalDeaths", Integer.parseInt(TotalDeaths), Color.parseColor("#500302")));
                pieChart.addPieSlice(new PieModel("totalRecovered", Integer.parseInt(TotalRecovered), Color.parseColor("#006400")));

                simpleArcLoader1.stop();
                simpleArcLoader1.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);

                pieChart.startAnimation();

                simpleArcLoader2.stop();
                simpleArcLoader2.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                simpleArcLoader1.stop();
                simpleArcLoader1.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);

                simpleArcLoader2.stop();
                simpleArcLoader2.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
            }

        }, error -> {
            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            simpleArcLoader1.stop();
            simpleArcLoader1.setVisibility(View.GONE);
            pieChart.setVisibility(View.VISIBLE);

            simpleArcLoader2.stop();
            simpleArcLoader2.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

}