package com.example.chesskillmobile;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Game  extends AppCompatActivity implements PreGameFrag.OnCallbackReceived {
    //Implement to send data from Frag to Activity

    private ActivityResultLauncher<String> ARL;

    private void SetupPermGrabber() {
        //New way of checking permission - has to be created before fragment is
        ARL = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                res -> {
                    if (!res) {
                        //Not granted!
                        Toast.makeText(getApplicationContext(), "Need perms to work!", Toast.LENGTH_LONG).show();
                        //this.onDestroy();
                    } else {
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

    private Object[] PlStats, AiStats; //{TheirColour,IsKingDead?} (Color/HexString , Boolean)

    private Thread th = new Thread(()->{ SetupBoard(); });

    @Override
    protected void onStart() {
        super.onStart();

        th.start();

        getSupportFragmentManager().beginTransaction().replace(R.id.GameFragHolder, PreGameFrag.class, null).commit();
        findViewById(R.id.GameFragHolder).bringToFront();
        //todo Frag to cover setting up.. and grab team col pick -- make frag disappear based on time takes to make board?
    }

    //To run when frag closes.. required via implementation
    @Override
    public void TeamChosen(Object[] Astats, Object[] Pstats, String s) {
        //Play with data from Frag
        AiStats=Astats; PlStats=Pstats;

        PlyrTurn = (PlStats[0].toString().equals(s)); //WORKS

        TeamSelected();
    }

    private void IsMainThread(){ System.out.println( Thread.currentThread() == Looper.getMainLooper().getThread() ); }

    protected void TeamSelected() {
        //System.out.println(Thread.currentThread() == Looper.getMainLooper().getThread()); //is MainThread
        /*
        try {

            BufferedReader bfr = new BufferedReader(new FileReader(new File(getFilesDir(), "tmp")));
            PlStats = new Object[]{bfr.readLine(), Boolean.parseBoolean(bfr.readLine())};
            AiStats = new Object[]{bfr.readLine(), Boolean.parseBoolean(bfr.readLine())};
            bfr.close();

        } catch (Exception e) {
            System.out.println("ERR: " + e);
            return;
        }
        */
        //for (Object o : PlStats) {System.out.println("Pl: " + o.toString());} for (Object o : AiStats){ System.out.println("Ai: " + o.toString());}

        //Add pieces..
        try { th.join(); //join stops curr (calling) thread till the ref/var thread process is complete..
            th = new Thread(()->{SetupPieces();});
            th.start();
        } catch (Exception e) { System.err.println(e); }
    }

    private TextView SetupTxtVw(String ID,int P){
        TextView tv = new TextView(this);
        tv.setTag('I'+'D',ID); tv.setLayoutParams(new TableRow.LayoutParams((int) (P*0.1), ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setMinimumWidth((int) (P*0.1)); tv.setMinimumHeight((int) (P*0.1));
        tv.setTextSize( 10 * getResources().getDisplayMetrics().density ); //10 min..
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); tv.setGravity(Gravity.CENTER);

        tv.setTypeface(null, Typeface.BOLD);

        //todo some kind of auto_wrap basing it on txt size..

        return tv;
    }

    private void SetupBoard(){
        //System.out.println("Setting board..");
        int P = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                    //math.min returns smallest number

        ArrayList<Integer> bgcols = new ArrayList<>();
        bgcols.add(Color.WHITE); bgcols.add(Color.BLACK);

        TableLayout TL = findViewById(R.id.GameTable); TL.removeAllViews();
        for(char j='A';j<='H';j++){
            TableRow TR = new TableRow(this);
            TR.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,(int) (P*0.9)));
            for(int i=1;i<=0+8;i++){
                //System.out.println("Tile: "+j+""+i);
                TextView TV = SetupTxtVw(j+""+i, P);
                //System.out.println(bgcols.get(0) +":"+j+""+i);
                TV.setBackgroundColor( bgcols.get(0) );

                TV.setText(TV.getTag().toString()); //todo Change from txt to Drawables(simple) w col filters..
                TV.setTextColor(Color.parseColor("#888888"));

                TR.addView(TV);

                Collections.reverse(bgcols); //System.out.println(bgcols);
            }
            Collections.reverse(bgcols);
            runOnUiThread(()->{ TL.addView(TR); });
            try{ Thread.sleep(20); } catch (Exception e){}
        }
    }

    private void SetupPieces(){
        //System.out.println("S P..");
        //IsMainThread(); //isnt main thread

        String[] LastRowPieces = new String[]{getResources().getString(R.string.Rook),getResources().getString(R.string.Knight),getResources().getString(R.string.Bishop),getResources().getString(R.string.King),getResources().getString(R.string.Queen),getResources().getString(R.string.Bishop),getResources().getString(R.string.Knight),getResources().getString(R.string.Rook)};

        for(int i=0 ;i < ((TableLayout)findViewById(R.id.GameTable)).getChildCount(); i++){
            TableRow tr = (TableRow) ((TableLayout)findViewById(R.id.GameTable)).getChildAt(i);
            for (int j=0 ;j < tr.getChildCount();j++){
                TextView tv = (TextView) tr.getChildAt(j);
                //Tag: A1 || G3 ..
                //String L = tag.substring(0,1), N = tag.substring(1);
                switch (tv.getTag('I'+'D').toString().substring(0,1)){
                    case ("B"): case ("G"):
                        runOnUiThread(()->{
                            tv.setText(getResources().getString(R.string.Pawn));
                            tv.setTag('P'+'i'+'e'+'c'+'e',getResources().getString(R.string.Pawn));
                        });
                        break;
                    case ("A"): case("H"):
                        runOnUiThread(()-> {
                            tv.setText(
                                LastRowPieces[Integer.parseInt(tv.getTag().toString().substring(1)) - 1]
                            );
                            tv.setTag('P'+'i'+'e'+'c'+'e',LastRowPieces[Integer.parseInt(tv.getTag().toString().substring(1)) - 1]);
                        });
                        break;
                }
            }
        }

        ApplyTeamCols();
    }

    private void ApplyTeamCols(){
        for(int i=0 ;i < ((TableLayout)findViewById(R.id.GameTable)).getChildCount(); i++) {
            TableRow tr = (TableRow) ((TableLayout) findViewById(R.id.GameTable)).getChildAt(i);
            for (int j = 0; j < tr.getChildCount(); j++) {
                TextView tv = (TextView) tr.getChildAt(j); String tag = tv.getTag().toString();
                switch (tag.substring(0,1)){
                    case "A": case "B":
                        //AI
                        tv.setTextColor(Color.parseColor(AiStats[0].toString()));
                        break;
                    case "H": case "G":
                        //Pl
                        tv.setTextColor(Color.parseColor(PlStats[0].toString()));
                        break;
                }
            }
        }

        //System.out.println(( PlyrTurn ? "Ply 1st" : "Ai 1st" )); //works
    }

    Object[] TileOne = null; // {Tag, PieceName, Col}
    //Handle moving pieces and stuff (only player can click button)
    private void TileSelected(View v){
        TextView tv = (TextView) v;

        //Make selected T1 lose alpha??

        if(TileOne != null){
            //Deal wiht tile2
            Object[] TileTwo = new Object[]{};

            TileOne=null;return;
        } else if(tv.getCurrentTextColor() == Color.parseColor(PlStats[0].toString())
                ||
                 tv.getCurrentTextColor() == Color.parseColor(PlStats[0].toString()) ){
            //Is player's own piece
        }

    }

    private boolean PlyrTurn=true;
}
