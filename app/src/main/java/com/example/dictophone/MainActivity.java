package com.example.dictophone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_RECORD_WRITE_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToWriteAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private RecyclerView recyclerView;
    private List<String> myFileList;
    private File file;

    private static final String TAG = "BoundService";

    private boolean mIsServiceBound;
    private DictophoneService mBoundService;


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

        ActivityCompat.requestPermissions(MainActivity.this,
                permissions,
                1);

        findViewById(R.id.record_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentStartService = new Intent(MainActivity.this, DictophoneService.class);
                startService(intentStartService);
            }
        });

        if (!myFileList.isEmpty()) {
            initRecyclerView(); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBoundService != null) {
        if (mBoundService.getFileName() != null) {
            myFileList.add(mBoundService.getFileName());
        }
        if (!myFileList.isEmpty()) {
            initRecyclerView();
        }}
    }

    private void initRecyclerView() {
        MediaAdapter adapter = new MediaAdapter(myFileList);
        recyclerView.setAdapter(adapter);
    }
}
