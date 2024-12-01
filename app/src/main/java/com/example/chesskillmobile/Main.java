package com.example.chesskillmobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MediaContent;
import com.google.android.gms.ads.MuteThisAdListener;
import com.google.android.gms.ads.MuteThisAdReason;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
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

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SetupPermGrabber();

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.main);

        context = getApplicationContext();

        SetupFiles();


        //TODO Setup Ads ?? admob
        /*
        ConstraintLayout CL = findViewById(R.id.MainAdContainer); CL.removeAllViews();

        AdView Ad = new AdView(this); Ad.setAdUnitId("ca-app-pub-3940256099942544/9214589741"); Ad.setAdSize(AdSize.BANNER);
        Ad.setBackgroundColor(Color.parseColor("#666666"));
        CL.setBackgroundColor(Color.parseColor("#FFFFFF"));
        CL.addView(Ad, new ConstraintLayout.LayoutParams(CL.getWidth(),CL.getHeight()));

        Ad.loadAd(new AdRequest.Builder().build());*/

        AdView ad = findViewById(R.id.MainAdView);
        //ad.setAdSize(AdSize.BANNER);

        AdRequest AR = new AdRequest.Builder().build();
        ad.loadAd(AR);
    }

    private void SetupFiles(){
        File Ai = new File(getFilesDir(),getString(R.string.AIBrain)), Ratio = new File(getFilesDir(),getString(R.string.RatioRecord));
        //System.out.println( Ai.getAbsolutePath() +"\n"+ Ratio.getAbsolutePath());
        //System.out.println( getFilesDir() );

        for(File f : getFilesDir().listFiles()){

            if (! ( f.getName().equals(Ai.getName()) || f.getName().equals(Ratio.getName()) ) ){ f.delete(); System.out.println(f.getName()+" destroyed..");} }

        //Files dont exist on app restart?..

        if(! Ai.exists()) {
            try {
                FileWriter FW = new FileWriter(Ai);
                FW.write(""); FW.flush(); FW.close();
            } catch (Exception e) { Toast.makeText(context,"Err making brain",Toast.LENGTH_SHORT).show(); }
        } if(! Ratio.exists()) {
            try {
                FileWriter FW = new FileWriter(Ratio);
                FW.write("Human: 0 |AI: 0 |Tie: 0"); FW.flush(); FW.close();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
