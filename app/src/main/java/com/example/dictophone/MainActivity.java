package com.example.dictophone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
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
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        findViewById(R.id.record_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentStartService = new Intent(MainActivity.this, DictophoneService.class);
                startService(intentStartService);
            }
        });

        String root_sd = Environment.getExternalStorageDirectory().toString();
        file = new File(root_sd);
        File[] list = file.listFiles();

        for (File value : Objects.requireNonNull(list)) {
            myFileList.add(value.getName());
        }

        initRecyclerView(myFileList);
    }

    private void initRecyclerView(List<String> fileName) {
        MediaAdapter adapter = new MediaAdapter();
        adapter.setfileNameList(fileName);
        recyclerView.setAdapter(adapter);
    }
}
