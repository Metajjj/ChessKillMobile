package com.example.chesskillmobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.concurrent.ConcurrentHashMap;

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

        ConcurrentHashMap<String,String> CHM = new ConcurrentHashMap<String,String>(){}; CHM.put("ID",ID); CHM.put("Piece","");
        //Tag need UID.. hashmap easier to maintain

        tv.setTag(CHM); tv.setLayoutParams(new TableRow.LayoutParams((int) (P*0.1), ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setMinimumWidth((int) (P*0.1)); tv.setMinimumHeight((int) (P*0.1));
        tv.setTextSize( 10 * getResources().getDisplayMetrics().density ); //10 min..
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); tv.setGravity(Gravity.CENTER);

        tv.setTypeface(null, Typeface.BOLD); tv.setOnClickListener(this::TileSelected);

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

                String s = ((ConcurrentHashMap<String,String>)TV.getTag()).get("ID");
                TV.setText(s); //todo Change from txt to Drawables(simple) w col filters..
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

                ConcurrentHashMap<String,String> CHM = (ConcurrentHashMap<String, String>) tv.getTag();
                String s = CHM.get("ID");

                //Tag: A1 || G3 ..
                //String L = tag.substring(0,1), N = tag.substring(1);
                switch (s.substring(0,1)){
                    case ("B"): case ("G"):
                        runOnUiThread(()->{
                            tv.setText(getResources().getString(R.string.Pawn));
                            CHM.put("Piece",getResources().getString(R.string.Pawn));
                            tv.setTag(CHM);
                        });
                        break;
                    case ("A"): case("H"):
                        runOnUiThread(()-> {
                            tv.setText( LastRowPieces[Integer.parseInt(s.substring(1)) - 1] );
                            CHM.put("Piece",LastRowPieces[Integer.parseInt(s.substring(1)) - 1]);
                            tv.setTag(CHM);
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
                TextView tv = (TextView) tr.getChildAt(j);
                String tag = ((ConcurrentHashMap<String, String>) tv.getTag()).get("ID");
                switch (tag.substring(0,1)){
                    case "A": case "B":
                        //AI
                        runOnUiThread(()->{ tv.setTextColor(Color.parseColor(AiStats[0].toString())); });
                        break;
                    case "H": case "G":
                        //Pl
                        runOnUiThread(()->{  tv.setTextColor(Color.parseColor(PlStats[0].toString())); });
                        break;
                }
            }
        }

        //System.out.println(( PlyrTurn ? "Ply 1st" : "Ai 1st" )); //works
    }

    Object[] TileOne = null; // {Tag/ID, Tag/PieceName, Stats/Col}
    private String c=null;

    //Handle moving pieces and stuff (only player can click button)
    private void TileSelected(View v){
        TextView tv = (TextView) v;

        //Make selected T1 lose alpha??

        if(TileOne != null){
            //Deal wiht tile2
            Object[] TileTwo = new Object[]{((ConcurrentHashMap<String, String>) tv.getTag()).get("ID"), ((ConcurrentHashMap<String, String>) tv.getTag()).get("Piece"), tv.getCurrentTextColor()};

                //Make sure isnt targeting own piece.. then check if can move to tileTwo (method via reflection string)


            try {

                if (! TileOne[2].equals(TileTwo[2]) && (boolean) this.getClass().getDeclaredMethod(TileOne[1]+"", Object[].class, Object[].class).invoke(this, TileOne, TileTwo)) {
                    MovePiece(TileOne,TileTwo);

                    Toast.makeText(this,TileOne[0]+"=>"+TileTwo[0]+"\nAllowed move!",Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(this,TileOne[0]+"=>"+TileTwo[0]+"\nInvalid move!",Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                System.err.println("ReflectionInvoke Err: "+e);
                Toast.makeText(this, "Srs err, reflection err\nAborting game..", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(()->{
                    startActivity(new Intent(this,Main.class));
                },2000);
            }


            TileOne=null;return;
        } else if(tv.getCurrentTextColor() == Color.parseColor(PlStats[0].toString())
                ||
                 tv.getCurrentTextColor() == Color.parseColor(PlStats[0].toString()) ) {
            //Is player's own piece

            TileOne = new Object[]{((ConcurrentHashMap<String, String>) tv.getTag()).get("ID"), ((ConcurrentHashMap<String, String>) tv.getTag()).get("Piece"), tv.getCurrentTextColor()};

            Toast.makeText(this,TileOne[0]+" ("+TileOne[1]+") selected!",Toast.LENGTH_SHORT).show();

        }
    }

    private boolean Pawn(Object[] T1, Object[] T2){
                        //Gets location/ID
        char[] L1 = T1[0].toString().toCharArray(), L2 = T2[0].toString().toCharArray(); //Char arrays for easier comparison via math

        //First check to see if Pawn is moving wrong direction..

                //If T1 is Pl.. make sure piece is moving forward (H=>A) .. <Pl will be greater than T2>
            //H is bottom.. so plyr is greater than T2
        if( T1[2].toString().equals(PlStats[0].toString()) ? L1[0] >= L2[0] : L1[0] <= L2[0] ){
            return false;
        }

        //If moving forward (number stays same) .. check if moving forward (letters) within 2 tiles .. make sure T2 is neutral--cant move to capture
            //Alrdy checked to make sure isnt moving backwards, so doesnt have to be as strict in checking
        if( Math.abs(L1[1] - L2[1]) ==0 && Math.abs(L1[0]-L2[0]) <=2 && ! (T2[2].toString().equals(PlStats[0].toString()) || T2[2].toString().equals(AiStats[0]) ) ){

            //Make sure is moving 2 tiles from beginning tile..
            //System.out.println( T1[2].toString()+"=="+Color.parseColor(PlStats[0]+"") +"=>"+ ( T1[2].equals(Color.parseColor(PlStats[0]+"")) ));
            if(Math.abs(L1[0]-L2[0])==2 && (
                ( T1[2].equals(Color.parseColor(PlStats[0]+"")) && L1[0]=='G' )
                ||
                ( T1[2].equals(Color.parseColor(AiStats[0]+"")) && L1[0]=='B' )
            )){
                return IsPieceInWay(L1,L2);

                //Make sure is distance of 1 away wherever the piece is
            } else if(Math.abs(L1[0]-L2[0])==1){
                return true;
            }
            //Check if moving diagonal (1 letter, 1 number & enemy piece)
        } else if (Math.abs(L1[1] - L2[1]) == 1 && Math.abs(L1[0]-L2[0]) == 1 && T2[2].toString().equals(AiStats[0])){
            return true;
        }

        return false;
    }
    private boolean Rook(Object[] T1, Object[] T2){
        char[] L1 = T1[0].toString().toCharArray(), L2 = T2[0].toString().toCharArray();

        //Makesure only Letter or Number is diff..
        if( (L1[0] == L2[0] && L1[1] != L2[1])  ||  (L1[0] != L2[0] && L1[1] == L2[1]) ){
            return IsPieceInWay(L1,L2);
        }

        return false;
    }
    private boolean Knight(Object[] T1, Object[] T2){
        char[] L1 = T1[0].toString().toCharArray(), L2 = T2[0].toString().toCharArray();

        //Make sure one is diff of 2 and other is diff of 1  ---- if true, return true.. else false..
        return (Math.abs(L1[0] - L2[0])==1 && Math.abs(L1[1] - L2[1])==2)
                ||
                (Math.abs(L1[0] - L2[0])==2 && Math.abs(L1[1] - L2[1])==1)
        ;
    }
    private boolean Bishop(Object[] T1, Object[] T2){
        char[] L1 = T1[0].toString().toCharArray(), L2 = T2[0].toString().toCharArray();

        //Make sure isnt same number or letter.. (rook movement) & abs(OldL-NewL) == abs(OldN-NewN)
                            //Moves equally left and right (diagonally) so can check for same diff between letters and numbers as abs
        if( (L1[0]-L2[0] != 0 && L1[1]-L2[1] !=0) && (Math.abs(L1[0]-L2[0]) == Math.abs(L1[1]-L2[1])) ){
            return IsPieceInWay(L1,L2);
        }

        return false;
    }
    private boolean King(Object[] T1, Object[] T2){
        char[] L1 = T1[0].toString().toCharArray(), L2 = T2[0].toString().toCharArray();

        //can only have a diff of 1 in any direction
        return Math.abs(L1[0]-L2[0])<=1 && Math.abs(L1[1]-L2[1])<=1; //No possible piece in way as only 1 tile diff
    }
    private boolean Queen(Object[] T1, Object[] T2){
        //Is combination of Rook & Bishop movement
        return Rook(T1,T2) || Bishop(T1,T2);
    }

    private boolean IsPieceInWay(char[] L1,char[] L2){
        boolean safe=true;

        ArrayList<String> TilePath = new ArrayList<>();

        //Get the info of the buttons in the way and add to TilePath..

        //get diff of Letters -- A=>H => H-A = 7.. +1 - A to get to H
        //get diff of Numbers -- 1=>8 => 8-1 = 7.. +1 - 1 to get to 8

        //Incr or Decr to reach T2 from T1 ; While hasnt reached destination/stopped (diff 0) ; make it change while T1 isnt T2..

        for( int a = L2[0]-L1[0]>0 ? 1 : -1, b =L2[0]-L1[0]>0 ? 1 : -1, i=0;
                Math.abs(L2[0]-L1[0])!=0 || Math.abs(L2[1]-L1[1])!=0;
                i++, L1[0] = Math.abs(L1[0]-L2[0])==0 ? L1[0] : (char) (L1[0] + a), L1[1] = Math.abs(L1[1]-L2[1])==0 ? L1[1] : (char) (L1[1] + b)  ){

            if(i>=8){break;} // > 8 == too far
            TilePath.add(L1[0]+""+L1[1]); //Converts each tile in way to string for array
        }

        //if(TilePath.size()==0){ safe=true; } //Nothing inbetween L1 & L2

        //Check each tile where tile matches tilepath.. make sure col is neutral
        TableLayout TL = findViewById(R.id.GameTable);
        for(String s : TilePath){
            //Check against tiles
            for(int i=0 ;i < TL.getChildCount();i++){
                TableRow tr = (TableRow) TL.getChildAt(i);
                for(int j=0;j<tr.getChildCount();j++){
                    TextView tv = (TextView) tr.getChildAt(j);

                    if( ((ConcurrentHashMap) tv.getTag()).get("ID") == s && tv.getCurrentTextColor() != Color.parseColor("#888888") ){
                        safe=false; break;
                    }
                }

            }
        }

        return safe;
    }

    private void MovePiece(Object[] T1, Object[] T2){
        //T1[0] ID
        TextView tv1=new TextView(this),tv2 =new TextView(this);
        ConcurrentHashMap<String,String> CHM = new ConcurrentHashMap<>();

        for(int i=0 ;i < ((TableLayout)findViewById(R.id.GameTable)).getChildCount(); i++) {
            TableRow tr = (TableRow) ((TableLayout) findViewById(R.id.GameTable)).getChildAt(i);
            for (int j = 0; j < tr.getChildCount(); j++) {
                TextView tv = (TextView) tr.getChildAt(j);

                if ( ((ConcurrentHashMap<String,String>)tv.getTag()).get("ID").equals(T1[0]) ){ tv1 = tv; }
                else if ( ((ConcurrentHashMap<String,String>)tv.getTag()).get("ID").equals(T2[0]) ){ tv2=tv; }
            }
        }

        if (tv2.getCurrentTextColor() == Color.parseColor("#888888")) { System.out.println("GREY t2"); /*TurnsTillStalemate=0*/ }

        //CHM = (ConcurrentHashMap<String, String>) tv1.getTag();
        ((ConcurrentHashMap<String, String>) tv2.getTag()).put("Piece", ((ConcurrentHashMap<String, String>) tv1.getTag()).get("Piece") );
        ((ConcurrentHashMap<String, String>) tv1.getTag()).put("Piece","");

        //Check if king dead..
        String s = T2[1].equals("King") ? "T" + (T2[2].equals(Color.parseColor(PlStats[2]+""))) : "F" ;

    }

    private boolean PlyrTurn=true;
}
