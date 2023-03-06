package fr.emevel.locallink.locallink_android;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.emevel.locallink.client.LocalLinkClient;
import fr.emevel.locallink.client.LocalLinkClientData;
import fr.emevel.locallink.locallink_android.databinding.ActivityMainBinding;
import fr.emevel.locallink.network.DataSaving;
import fr.emevel.locallink.server.LocalLinkServerData;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;

    public static AndroidLocalLinkServer server;
    public static LocalLinkServerData serverData;
    public static Runnable serverSaveData;

    public static LocalLinkClient client;
    public static LocalLinkClientData clientData;
    public static Runnable clientSaveData;

    public static WifiManager.MulticastLock lock;

    public static List<ClientElement> clients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


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
        } else if (id == R.id.action_reset) {
            new Thread(() -> {
                clientData = new LocalLinkClientData();
                DataSaving<LocalLinkClientData> clientDataSaver = DataSaving.localFile(new File(getFilesDir(), "client.dat"));
                clientSaveData = clientDataSaver.saver(clientData);

                serverData = new LocalLinkServerData();
                DataSaving<LocalLinkServerData> dataSaver =  DataSaving.localFile(new File(getFilesDir(), "server.dat"));
                serverSaveData = dataSaver.saver(serverData);

                if (!clientData.getUuid().equals(serverData.getUuid())) {
                    clientData.setUuid(serverData.getUuid());
                }
                serverSaveData.run();
                clientSaveData.run();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        restartApp();
                    }
                });

            }).start();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 555;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
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

        if (server != null) {
            try {
                server.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (lock != null) {
            lock.release();
        }
    }

    public void startClient() throws IOException {
        File clientFile = new File(getFilesDir(), "client.dat");

        DataSaving<LocalLinkClientData> clientDataSaver = DataSaving.localFile(clientFile);
        clientData = clientDataSaver.load(LocalLinkClientData::new);
        clientSaveData = clientDataSaver.saver(clientData);


        if (!clientData.getUuid().equals(serverData.getUuid())) {
            clientData.setUuid(serverData.getUuid());
            clientSaveData.run();
        }

        System.out.println("Starting client...");

        client = new LocalLinkClient(clientData, Environment.getExternalStorageDirectory(), clientSaveData);

        client.start();
    }

    public void startServer() throws IOException {
        File serverFile = new File(getFilesDir(), "server.dat");

        DataSaving<LocalLinkServerData> dataSaver =  DataSaving.localFile(serverFile);
        serverData = dataSaver.load(LocalLinkServerData::new);
        serverSaveData = dataSaver.saver(serverData);

        System.out.println("Starting server...");

        FirstFragment.instance.loadAll();

        server = new AndroidLocalLinkServer(serverData, serverSaveData);

        server.start();
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

            try {
                startServer();
                startClient();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }
}