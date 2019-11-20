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

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_RECORD_WRITE_AUDIO_PERMISSION = 200;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE};
    private RecyclerView recyclerView;
    private List<String> myFileList;
    private File file;
    private MediaAdapter adapter;
    private TextView choosenFileTextView;
    private ImageButton playButton;
    private ImageButton stopButton;


    private static final String TAG = "BoundService";

    private boolean mIsServiceBound;
    private DictophoneService mBoundService;


    private Messenger mServiceMessenger;
    private Messenger mMainActivityMessenger = new Messenger(new InternalMainActivityHandler());

    public static final int MSG_PLAY_RECORD = 202;
    public static final int MSG_STOP_RECORD = 203;
    public static final String EXTRA_PLAYER = "EXTRA_PLAYER";

    class InternalMainActivityHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
        }
    }


    private void sendPlayMessage() {
        String playerMessage = choosenFileTextView.getText().toString();
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.EXTRA_PLAYER, playerMessage);
        Message message = Message.obtain(null, MSG_PLAY_RECORD);
        message.replyTo = mMainActivityMessenger;
        message.setData(bundle);

        try {
            mMainActivityMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendStopMessage() {
        Message message = Message.obtain(null, MSG_STOP_RECORD);
        message.replyTo = mMainActivityMessenger;

        try {
            mMainActivityMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBoundService = ((DictophoneService.LocalBinder) iBinder).getBoundService();
            mIsServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBoundService = null;
            mIsServiceBound = false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        hasPermissions(this, permissions);
    }

    private void startPlayService() {
        Intent intentStartService = new Intent(MainActivity.this, PlayService.class);
        startService(intentStartService);
    }

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

        findViewById(R.id.record_btn).setOnClickListener(v -> {
            Intent intentStartService = new Intent(MainActivity.this, DictophoneService.class);
            startService(intentStartService);
            bindService(intentStartService, mServiceConnection, BIND_AUTO_CREATE);
            startPlayService();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
            initRecyclerView();
    }

    private void initRecyclerView() {
            String root_sd = Environment.getExternalStorageDirectory() + "/myRecords";
            file = new File(root_sd);
            File[] list = file.listFiles();
            for (File value : Objects.requireNonNull(list)) {
                myFileList.add(value.getName());
            }
            adapter.setItems(myFileList, choosenFileTextView);
            recyclerView.setAdapter(adapter);
    }


}
