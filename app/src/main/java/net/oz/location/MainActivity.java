package net.oz.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.codec.DecoderException;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 1)Выводит местоположение и json строку  на экран,
 * 2)Записывает  файл,
 * 3)Находит хеш
 */

public class MainActivity extends AppCompatActivity {

    TextView tVLatitude;
    TextView tVLongitude;
    TextView tvGson;
    private LocationManager locationManager;
    TempInfo tempInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tVLatitude = (TextView) findViewById(R.id.textViewLatitude);
        tVLongitude = (TextView) findViewById(R.id.textViewLongitude);
        tvGson = (TextView) findViewById(R.id.textViewGson);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tVLatitude.setText("Status: " + String.valueOf(status));
            }
        }
    };

    /**
     * Получает коордынаты, выводит их на экран,
     * записывает информацию в json и сохраняет в файл
     * @param location
     */
    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            tVLatitude.setText("Latitude: "+location.getLatitude());
            tVLongitude.setText("Longitude: "+location.getLongitude());
            String deviceId = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            tempInfo = new TempInfo(deviceId, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            String tempGson = gson.toJson(tempInfo);
            writeToFile(tempGson);
            try{
                String hash = MyCryptography.getHash("locInfo.txt");
                tvGson.setText("Json information: " + tempGson + "Hash: " + hash);
            }
            catch (DecoderException e){
                e.printStackTrace();
            }
        }
    }


    /**
     * Записывает в файл
     * @param tempGson
     */
    private void writeToFile(String tempGson){
        try{
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(openFileOutput("locInfo.txt",MODE_PRIVATE)));
            bw.write(tempGson);
            bw.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
