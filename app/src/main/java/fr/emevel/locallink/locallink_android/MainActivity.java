package fr.emevel.locallink.locallink_android;

import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import fr.emevel.locallink.client.ClientMain;
import fr.emevel.locallink.client.LocalLinkClient;
import fr.emevel.locallink.client.LocalLinkClientData;
import fr.emevel.locallink.locallink_android.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import android.Manifest;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;

    private static LocalLinkClient client;
    private static WifiManager.MulticastLock lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                EXTERNAL_STORAGE_PERMISSION_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (client != null) {
            try {
                client.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (lock != null) {
            lock.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != EXTERNAL_STORAGE_PERMISSION_CODE) {
            return;
        }

        new Thread(() -> {
            WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);

            lock = wifi.createMulticastLock("LocalLink-JmDNS-Lock");
            lock.setReferenceCounted(true);
            lock.acquire();

            File file = new File(getFilesDir(), "client.dat");
            try {
                LocalLinkClientData data = ClientMain.loadDataFromFile(file);

                Runnable saveData = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ClientMain.saveDataToFile(data, file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                client = new LocalLinkClient(data, Environment.getExternalStorageDirectory(), saveData);

                client.start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }
}