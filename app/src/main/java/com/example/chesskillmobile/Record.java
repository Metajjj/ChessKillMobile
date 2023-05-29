package com.example.chesskillmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Record  extends AppCompatActivity {

    private ActivityResultLauncher<String> ARL;
    private void SetupPermGrabber(){
        //New way of checking permission - has to be created before fragment is
        ARL = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                res -> {
                    if (!res) {
                        //Not granted!
                        Toast.makeText(getApplicationContext(), "Need perms to work!", Toast.LENGTH_LONG).show();
                        //this.onDestroy();
                    }else{
                        //Accepted
                    }
                }
        );
    }

    //todo OnPickTeam ----- DetailedView?? (Includes TileName || else ignore)

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SetupPermGrabber();

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.record);

        context = getApplicationContext();
    }

    @Override
    protected void onStart() {
        super.onStart();

        new Thread(()-> {

            String s = "";

            try {
                BufferedReader bfr = new BufferedReader(new FileReader(new File(getFilesDir(), getString(R.string.RatioRecord))));
                s = bfr.readLine();
                bfr.close();
            } catch (Exception e) {
                s = "Err reading file!"; System.err.println("Err: "+e);
            }
            ((TextView) findViewById(R.id.RecordWLR)).setText(s);

            findViewById(R.id.RecordResetWLR).setOnClickListener((v) -> {
                try {
                    FileWriter FW = new FileWriter(new File(getFilesDir(), getString(R.string.RatioRecord)));
                    FW.write("Human: 0 | AI: 0 | Tie: 0"); FW.flush(); FW.close();
                    Toast.makeText(context,"WLR reset!",Toast.LENGTH_SHORT).show();
                    Thread.currentThread().start();
                } catch (Exception e) {
                    Toast.makeText(context, "Err resetting ratio", Toast.LENGTH_SHORT).show();
                }
            });

            findViewById(R.id.RecordResetAIBrain).setOnClickListener((v) -> {
                try {
                    FileWriter FW = new FileWriter(new File(getFilesDir(), getString(R.string.AIBrain)));
                    FW.write(""); FW.flush(); FW.close();
                    Toast.makeText(context,"Brain reset!",Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, "Err resetting brain", Toast.LENGTH_SHORT).show();
                }
            });

        }).start();

    }

    @Override
    public void onBackPressed() { startActivity(new Intent(context, Main.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)); }
}
