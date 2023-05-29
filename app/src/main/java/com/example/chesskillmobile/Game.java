package com.example.chesskillmobile;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class Game  extends AppCompatActivity {

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

        setContentView(R.layout.game);

        context = getApplicationContext();
    }

    protected static Object[] PlStats,AiStats; //{TheirColour,IsKingDead?} (Color/HexString , Boolean)

    @Override
    protected void onStart() {
        super.onStart();

        getSupportFragmentManager().beginTransaction().replace(R.id.GameFragHolder, PreGame.class,null).commit();
        findViewById(R.id.GameFragHolder).bringToFront();
        //todo Frag to cover setting up.. and grab team col pick -- make frag disappear based on time takes to make board?
    }

    //

    protected void TeamSelected(){

        for(Object o : PlStats)
            System.out.println("Pl: "+o.toString());
        for (Object o : AiStats)
            System.out.println("Ai: "+o.toString());
    }
}
