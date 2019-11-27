package com.example.dictophone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements FileChoosenListener {


    public static final int MSG_PLAY_RECORD = 202;
    public static final int MSG_STOP_RECORD = 203;
    public static final String EXTRA_PLAYER = "EXTRA_PLAYER";
    private final String root_sd = Environment.getExternalStorageDirectory() + "/myRecords";
    private static final int REQUEST_RECORD_WRITE_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private RecyclerView recyclerView;
    private List<String> myFileList;
    private File file;
    private MediaAdapter adapter;
    private TextView choosenFileTextView;
    private boolean isServiceBound;
    private DictophoneService mBoundService;
    private Messenger mainActivityMessengerPlay = new Messenger(new InternalMainActivityHandler());
    private Messenger mainActivityMessengerStop = new Messenger(new InternalMainActivityHandler());
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mainActivityMessengerPlay = new Messenger(iBinder);
            mainActivityMessengerStop = new Messenger(iBinder);
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBoundService = null;
            isServiceBound = false;
        }
    };

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void sendPlayMessage() {
        String playerMessage = choosenFileTextView.getText().toString();
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.EXTRA_PLAYER, playerMessage);
        Message message = Message.obtain(null, PlayService.MSG_START_PLAYER);
        message.setData(bundle);
        try {
            mainActivityMessengerPlay.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendStopMessage() {
        Message message = Message.obtain(null, PlayService.MSG_STOP_PLAYER);
        try {
            mainActivityMessengerStop.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        hasPermissions(this, permissions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myFileList = new ArrayList<>();
        recyclerView = findViewById(R.id.media_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MediaAdapter();
        choosenFileTextView = findViewById(R.id.choosen_file);
        findViewById(R.id.play_btn).setOnClickListener(v -> sendPlayMessage());
        findViewById(R.id.stop_btn).setOnClickListener(v -> sendStopMessage());

        ActivityCompat.requestPermissions(MainActivity.this,
                permissions,
                REQUEST_RECORD_WRITE_AUDIO_PERMISSION);

        Intent intentStartPlayService = new Intent(this, PlayService.class);
        startService(intentStartPlayService);
        Intent bindIntent = new Intent(this, PlayService.class);
        bindService(bindIntent, mServiceConnection, BIND_AUTO_CREATE);

        findViewById(R.id.record_btn).setOnClickListener(v -> {
            Intent intentStartService = new Intent(MainActivity.this, DictophoneService.class);
            startService(intentStartService);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecyclerView();
    }

    private void initRecyclerView() {
        file = new File(root_sd);

        File[] list = file.listFiles();
        if (list != null) {
            for (File value : Objects.requireNonNull(list)) {
                myFileList.add(value.getName());
            }
            adapter.setItems(myFileList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void setFileName(String fileName) {
        choosenFileTextView.setText(fileName);
    }

    class InternalMainActivityHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_PLAY_RECORD:
                    mainActivityMessengerPlay = msg.replyTo;
                    break;
                case MSG_STOP_RECORD:
                    mainActivityMessengerStop = msg.replyTo;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
