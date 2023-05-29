package com.example.chesskillmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.util.Objects;
import java.util.Set;

public class Main extends AppCompatActivity {

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

        setContentView(R.layout.main);

        context = getApplicationContext();

        SetupFiles();
    }

    private void SetupFiles(){
        File Ai = new File(getFilesDir(),getString(R.string.AIBrain)), Ratio = new File(getFilesDir(),getString(R.string.RatioRecord));
        System.out.println( Ai.getAbsolutePath() +"\n"+ Ratio.getAbsolutePath());
        System.out.println( getFilesDir() );

        for(File f : getFilesDir().listFiles()){
            //System.out.println(f.getName()+"=="+Ai.getName()+"? "+f.getName().equals(Ai.getName())+" | "+f.getName()+"=="+Ratio.getName()+"? "+f.getName().equals(Ratio.getName()));
            if (! ( f.getName().equals(Ai.getName()) || f.getName().equals(Ratio.getName()) ) ){ f.delete(); System.out.println(f.getName()+" destroyed..");} }

        //Files dont exist on app restart?..

        if(! Ai.exists()) {
            try {
                FileWriter FW = new FileWriter(Ai);
                FW.write("a"); FW.flush(); FW.close();
            } catch (Exception e) { Toast.makeText(context,"Err making brain",Toast.LENGTH_SHORT).show(); }
        } if(! Ratio.exists()) {
            try {
                FileWriter FW = new FileWriter(Ratio);
                FW.write("Human: 0 | AI: 0 | Tie: 0"); FW.flush(); FW.close();
            } catch (Exception e) { Toast.makeText(context,"Err making ratio",Toast.LENGTH_SHORT).show(); }
        }

        if(Ai.exists() && Ratio.exists()){ Toast.makeText(context,"Ready!",Toast.LENGTH_SHORT).show(); }

        //System.out.println( ((getFilesDir().listFiles().length > 0) ? "NotNull" : "Null") );
        //for( File f : getFilesDir().listFiles()){ System.out.println(f.getName()); }

    }

    @Override
    protected void onStart() {
        super.onStart();

        findViewById(R.id.MainStart).setOnClickListener((v)->{
            startActivity(new Intent(context, Game.class));
        });

        findViewById(R.id.MainRecord).setOnClickListener((v)->{
            startActivity(new Intent(context, Record.class));
        });

        findViewById(R.id.MainInfo).setOnClickListener((v)->{
            startActivity(new Intent(context, Info.class));
        });

        findViewById(R.id.MainInfoExtra).setOnClickListener((v)->{
            findViewById(R.id.MainInfo).performClick();
        });
    }
}
