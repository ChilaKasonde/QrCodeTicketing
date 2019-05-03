package com.devguilds.qrcodeticketing;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends ActionBarActivity {

    BarcodeDetector barcodeDetector;
    SurfaceView cameraPreview;
    CameraSource cameraSource;
    TextView txtResult;
    String txtResultpost;
    String json_string;
    int RequestCameraPermissionID = 1001;
    OkHttpClient client;
    String Link_URL;
    Button btnPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        txtResult = (TextView) findViewById(R.id.txtResult);
        btnPost = (Button)findViewById(R.id.btnPost);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build();
        //add Event
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> barcodeDetections) {
                final SparseArray<Barcode> qrcodes = barcodeDetections.getDetectedItems();
                if (qrcodes.size() != 0) {
                    txtResult.post(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
//                            vibrator.vibrate(100);
                            txtResult.setText(qrcodes.valueAt(0).displayValue);
                            txtResultpost = (qrcodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });
    }


    public void onButtonPost(View v) {
        networkTest();
    }

    private void networkTest() {
        if (!isNetworkAvailable()) {
            //Alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this);
            builder.setMessage("Internet Connection Required")
                    .setCancelable(false)
                    .setPositiveButton("Retry",new DialogInterface.OnClickListener() {

                                public void onClick(
                                        DialogInterface dialog,int id) {
                                    //reattempts to test network activity
                                    networkTest();
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();

        } else {
            // if online proceed with scan
            new BackgroundTaskNested().execute();
        }

    }

    class BackgroundTaskNested extends AsyncTask<Void,Void,String> {

        String json_url ="http://your url here will use a get.php?name="+txtResultpost;

        @Override
        protected String doInBackground(Void... voids) {

            try {
                URL url = new  URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                while((json_string=bufferedReader.readLine())!= null){
                    stringBuilder.append(json_string+"\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        public BackgroundTaskNested() {
            super();
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject parentObject = new JSONObject(result);
                JSONArray parentArray = parentObject.getJSONArray("server_response");
                JSONObject finalObject = parentArray.getJSONObject(0);
                String nameSend = finalObject.getString("name");
                String passwordSend = finalObject.getString("password");
                String contactSend = finalObject.getString("contact");
                String countrySend = finalObject.getString("country");
                Bundle bundle = new Bundle();
                Intent intent = new Intent(MainActivity.this, ViewDetails.class);
                bundle.putString("name", nameSend);
                bundle.putString("password", passwordSend);
                bundle.putString("contact", contactSend);
                bundle.putString("country", countrySend);
                intent.putExtras(bundle);
                startActivity(intent);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }


    // Private class isNetworkAvailable
    private boolean isNetworkAvailable() {
        // Using ConnectivityManager to check for Network Connection
        ConnectivityManager connectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

}