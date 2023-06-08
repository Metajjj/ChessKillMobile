package com.example.chesskillmobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SetupPermGrabber();

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.game);

        context = getApplicationContext();
    }

    private Object[] PlStats, AiStats; //{TheirColour,IsKingDead?} (Color/int - TV.getCurrCol is int , Boolean)

    private Thread th = new Thread(()->{ SetupBoard(); });

    @Override
    protected void onStart() {
        super.onStart();

        th.start();

        getSupportFragmentManager().beginTransaction().replace(R.id.GameFragHolder, PreGameFrag.class, null).commit();
        findViewById(R.id.GameFragHolder).bringToFront();
    }

    //To run when frag closes.. required via implementation
    @Override
    public void TeamChosen(Object[] Astats, Object[] Pstats, String s, Boolean dv, Boolean icons, Boolean ca) {
        //Play with data from Frag
        AiStats=Astats; PlStats=Pstats; DetailedView=dv; UseIcons=icons; CultivateAI=ca;

        PlyrTurn = (PlStats[0].equals( Color.parseColor(s) )); //WORKS

        TeamSelected();
    }

    private boolean DetailedView=true, UseIcons=false, CultivateAI=false;

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
        try {  //join stops curr (calling) thread till the ref/var thread process is complete..
            th = new Thread(()->{SetupPieces();});
            th.start();
            th.join(); //join makes UI thread wait for th thread to die..
            MainLoop();
        } catch (Exception e) { System.err.println("Line 138\n"+e); }
    }

    private TextView SetupTxtVw(String ID,int P,int bg){
        TextView tv = new TextView(this);

        ConcurrentHashMap<String,String> CHM = new ConcurrentHashMap<String,String>(){}; CHM.put("ID",ID); CHM.put("Piece",""); CHM.put("OriginalBg", String.valueOf(bg));
        //Tag need UID.. hashmap easier to maintain

        tv.setTag(CHM); tv.setLayoutParams(new TableRow.LayoutParams((int) (P*0.1), (int) (P*0.1)));
        tv.setMinimumWidth((int) (P*0.1)); tv.setMinimumHeight((int) (P*0.1));
        tv.setTextSize( 10 * getResources().getDisplayMetrics().density ); //10 min..
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); tv.setGravity(Gravity.CENTER);

        tv.setTypeface(null, Typeface.BOLD); tv.setOnClickListener(this::TileSelected);

        return tv;
    }

    private void SetupBoard(){
        //System.out.println("Setting board..");
        int P = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                    //math.min returns smallest number

        ArrayList<Integer> bgcols = new ArrayList<>(); bgcols.add(Color.WHITE); bgcols.add(Color.BLACK);

        TableLayout TL = findViewById(R.id.GameTable); TL.removeAllViews();
        for(char j='A';j<='H';j++){
            TableRow TR = new TableRow(this);
            TR.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,(int) (P*0.9)));
            for(int i = 1; i<= 8; i++){
                //System.out.println("Tile: "+j+""+i);
                TextView TV = SetupTxtVw(j+""+i, P, bgcols.get(0) );
                //System.out.println(bgcols.get(0) +":"+j+""+i);
                TV.setBackgroundColor( bgcols.get(0) );

                String s = ((ConcurrentHashMap<String,String>)TV.getTag()).get("ID");
                TV.setText( DetailedView ? s : "" );
                TV.setTextColor(Color.parseColor("#888888"));

                TR.addView(TV);

                RecordOfTiles.add(TV); //Fill / mini-initialise RecordOfTiles

                Collections.reverse(bgcols); //System.out.println(bgcols);
            }
            Collections.reverse(bgcols);
            runOnUiThread(()->{ TL.addView(TR); });
            try{ Thread.sleep(20); } catch (Exception e){System.err.println("Line 186\n"+e);}
        }
    }

    private ArrayList<String> LastRowPieces; //Need global for access elsewhere

    private void SetupPieces(){
        Thread th = new Thread(()->{ApplyTeamCols();});
        //ApplyTeamCols();
        try{ th.start(); th.join(); } catch (Exception e){System.err.println("Line 197: "+e);}

        //System.out.println("S P..");
        //IsMainThread(); //isnt main thread
        //String[] LastRowPieces = new String[]{getResources().getString(R.string.Rook), getResources().getString(R.string.Knight), getResources().getString(R.string.Bishop), getResources().getString(R.string.King), getResources().getString(R.string.Queen), getResources().getString(R.string.Bishop), getResources().getString(R.string.Knight), getResources().getString(R.string.Rook)};
        LastRowPieces = new ArrayList<>(Arrays.asList(getString(R.string.Rook), getString(R.string.Knight), getString(R.string.Bishop), getString(R.string.King), getString(R.string.Queen), getString(R.string.Bishop), getString(R.string.Knight), getString(R.string.Rook)));
            //Have to assign here else context err
        LastRowPieces.add(0,""); LastRowPieces.add(1,getString(R.string.Pawn));
            //RunOnUiThread means foreach completes before UI updates

        for(TextView tv : RecordOfTiles) {

            //BoardSetup runs before checking dv.. this will overwrite based on plyr choice
            if(! DetailedView){ runOnUiThread(()->{ tv.setText(""); }); }

            ConcurrentHashMap<String, String> CHM = (ConcurrentHashMap<String, String>) tv.getTag();
            String s = CHM.get("ID");

            //Tag: A1 || G3 ..
            //String L = tag.substring(0,1), N = tag.substring(1);
            switch (s.substring(0, 1)) {

                case ("B"):
                case ("G"):
                    runOnUiThread(() -> {
                        if( UseIcons) {
                            tv.setText("");
                            //ResourcesCompat.getDrawable(getResources(), R.drawable.pawn, getTheme()).setColorFilter(tv.getCurrentTextColor(), PorterDuff.Mode.SRC_IN);
                            //ResourcesCompat innately bitmap drawable.. cast to layer = err
                            LayerDrawable ld = (LayerDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.pieces_ai, getTheme()).getConstantState().newDrawable().mutate();
                                //create a copy of drawable, make it new.. make it mutate so doesnt affect orig

                            ((ColorDrawable)ld.getDrawable(0)).setColor(Integer.parseInt(CHM.get("OriginalBg")));
                            ld.getDrawable(1).setAlpha(255); ld.getDrawable(1).setColorFilter(tv.getCurrentTextColor(), PorterDuff.Mode.SRC_IN);
                                        //alpha is % by xml alpha value? 0-255

                            for(int i=2;i<ld.getNumberOfLayers();i++){
                                ld.getDrawable(i).setAlpha(0);
                            }

                            tv.setBackground(ld);

                        }else{ tv.setText(getResources().getString(R.string.Pawn)); }

                        CHM.put("Piece", getResources().getString(R.string.Pawn));
                        tv.setTag(CHM);

                        RecordOfTiles.set(RecordOfTiles.indexOf(tv), tv);
                    });
                    break;
                case ("A"):
                case ("H"):
                    int s2 = Integer.parseInt(s.substring(1));
                    CHM.put("Piece", LastRowPieces.get(s2 +1) );

                    runOnUiThread(() -> {
                        if(UseIcons){
                            tv.setText("");

                            LayerDrawable ld = (LayerDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.pieces_ai, getTheme()).getConstantState().newDrawable().mutate();
                            ((ColorDrawable)ld.getDrawable(0)).setColor(Integer.parseInt(CHM.get("OriginalBg")));

                            for(int i=1;i<ld.getNumberOfLayers();i++){
                                if( i == LastRowPieces.indexOf(CHM.get("Piece"))){
                                    ld.getDrawable(i).setAlpha(255); ld.getDrawable(i).setColorFilter(tv.getCurrentTextColor(), PorterDuff.Mode.SRC_IN);
                                }else{ ld.getDrawable(i).setAlpha(0); }
                            }

                            tv.setBackground(ld);
                        }else { tv.setText(LastRowPieces.get(s2 + 1)); }

                        tv.setTag(CHM);

                        RecordOfTiles.set(RecordOfTiles.indexOf(tv), tv);
                    });
                    break;
            }
        }
    }

    private void ApplyTeamCols(){
        //System.out.println("ATC");
        for(TextView tv : RecordOfTiles) {
            String tag = ((ConcurrentHashMap<String, String>) tv.getTag()).get("ID");
            switch (tag.substring(0, 1)) {
                case "A": case "B":
                    //AI
                    //runOnUiThread(() -> {
                        tv.setTextColor((int) AiStats[0]);
                        RecordOfTiles.set(RecordOfTiles.indexOf(tv), tv);
                    //});
                    break;
                case "H": case "G":
                    //Pl
                    //runOnUiThread(() -> {
                        tv.setTextColor((int) PlStats[0]);
                        RecordOfTiles.set(RecordOfTiles.indexOf(tv), tv);
                    //});
                    break;
            }

        }
        //System.out.println(AiStats[0]+" AI : PL "+PlStats[0]); //Isnt applying txtCols??
        //System.out.println(( PlyrTurn ? "Ply 1st" : "Ai 1st" )); //works
    }

    Object[] TileOne = null; // {Tag/ID, Tag/PieceName, Stats/Col}
    private TextView c=null; private LayerDrawable cc=null;

    //Handle moving pieces and stuff (only player can click button)

    private void TileSelected(View v){
        TileSelected(v,false);
    }

    private void TileSelected(View v,Boolean AiPass){
        if(AiPass){ PlyrTurn = !PlyrTurn; }
        if(!PlyrTurn){return;}
            //avoid select IMD after moving a piece and is AI turn.. hander delay in AdvancedTurn
        if(AiPass){ PlyrTurn = !PlyrTurn; }

        TextView tv = (TextView) v;

        //Make selected T1 lose alpha??

        if(TileOne != null){

            if(UseIcons){
                //System.out.println("cc: #"+ Integer.toHexString( ((ColorDrawable)(cc.getDrawable(0))).getColor() ).substring(2).toUpperCase() +"\ntv2: #"+ Integer.toHexString( ((ColorDrawable)tv.getBackground()).getColor() ).substring(2).toUpperCase() );
                    //tv2 is innately coldraw from setbgcol..??
                    //ShpeDrwableSavedAsGradient..? -- invis grad inside shape tag
                c.setBackground(cc);
            }else{
                c.setTextColor( ((ColorDrawable)c.getBackground()) .getColor());
                c.setBackgroundColor(Integer.parseInt(((ConcurrentHashMap<String, String>) c.getTag()).get("OriginalBg")));
            }
            c=null; cc=null;

            //Deal with tile2
            Object[] TileTwo = new Object[]{((ConcurrentHashMap<String, String>) tv.getTag()).get("ID"), ((ConcurrentHashMap<String, String>) tv.getTag()).get("Piece"), tv.getCurrentTextColor()};

                //Make sure isnt targeting own piece.. then check if can move to tileTwo (method via reflection string)
            try {
                if (! TileOne[2].equals(TileTwo[2]) && (boolean) this.getClass().getDeclaredMethod(TileOne[1]+"", Object[].class, Object[].class).invoke(this, TileOne, TileTwo)) {
                    MovePiece(TileOne,TileTwo);

                    //Toast.makeText(this,TileOne[0]+"=>"+TileTwo[0]+"\nAllowed move!",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,TileOne[0]+"=>"+TileTwo[0]+"\n("+TileOne[1]+") Invalid move!",Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                System.err.println("ReflectionInvoke Err: "+e);

                //Appears to occur from clicking too fast..?

                Toast.makeText(this, "Srs err, reflection err\nAborting game..", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(()->{ startActivity(new Intent(this,Main.class)); },2000);
            }

            TileOne=null;
        } else if( tv.getCurrentTextColor() == (int)PlStats[0]
                ||
                 tv.getCurrentTextColor() == (int)AiStats[0] ) {
            //Is a piece

            TileOne = new Object[]{((ConcurrentHashMap<String, String>) tv.getTag()).get("ID"), ((ConcurrentHashMap<String, String>) tv.getTag()).get("Piece"), tv.getCurrentTextColor()};

            //Toast.makeText(this,TileOne[0]+" ("+TileOne[1]+") selected!",Toast.LENGTH_SHORT).show();
                //Visibly shows tile selected
            c= tv;
            if(UseIcons){
                //System.out.println("LRP: "+LastRowPieces.indexOf(TileOne[1]));
                LayerDrawable LD = (LayerDrawable) tv.getBackground();
                cc = (LayerDrawable) LD.getConstantState().newDrawable().mutate(); //cc is orig LD of tile1.. c is tile1
                ((ColorDrawable)LD.getDrawable(0)).setColor((Integer) TileOne[2]);
                LD.getDrawable( LastRowPieces.indexOf(TileOne[1]) ).setColorFilter(Integer.parseInt(((ConcurrentHashMap<String, String>) tv.getTag()).get("OriginalBg")), PorterDuff.Mode.SRC_IN);
            }else{
                tv.setBackgroundColor(Color.parseColor("#"+Integer.toHexString(tv.getCurrentTextColor()).substring(2) ));
                tv.setTextColor(Integer.parseInt(((ConcurrentHashMap<String, String>) tv.getTag()).get("OriginalBg")));
            }
        }
    }

    private boolean Pawn(Object[] T1, Object[] T2){
                        //Gets location/ID
        char[] L1 = T1[0].toString().toCharArray(), L2 = T2[0].toString().toCharArray(); //Char arrays for easier comparison via math

        //First check to see if Pawn is moving wrong direction..

                //If T1 is Pl.. make sure piece is moving forward (H=>A) .. <Pl will be greater than T2>
            //H is bottom.. so plyr is greater than T2 .. so if L1[0] is < L2[0] .. err
        if( T1[2].equals(PlStats[0]) ? L1[0] <= L2[0] : L1[0] >= L2[0] ){
            //System.out.println("WRONGWAY");
            return false;
        }

        //If moving forward (number stays same) .. check if moving forward (letters) within 2 tiles .. make sure T2 is neutral--cant move to capture
            //Alrdy checked to make sure isnt moving backwards, so doesnt have to be as strict in checking
        if( Math.abs(L1[1] - L2[1]) ==0 && Math.abs(L1[0]-L2[0]) <=2 && ! (T2[2].equals(PlStats[0]) || T2[2].equals(AiStats[0]) ) ){

            //System.out.println("check if 2");

            //Make sure is moving 2 tiles from beginning tile..
            //System.out.println( T1[2].toString()+"=="+Color.parseColor(PlStats[0]+"") +"=>"+ ( T1[2].equals(Color.parseColor(PlStats[0]+"")) ));
            if(Math.abs(L1[0]-L2[0])==2 && (
                ( T1[2].equals(PlStats[0]) && L1[0]=='G' )
                ||
                ( T1[2].equals(AiStats[0]) && L1[0]=='B' )
            )){
                return IsPieceInWay(L1,L2);

                //Make sure is distance of 1 away wherever the piece is
            } else return Math.abs(L1[0] - L2[0]) == 1;
            //Check if moving diagonal (1 letter, 1 number & enemy piece)
        } else return Math.abs(L1[1] - L2[1]) == 1 && Math.abs(L1[0] - L2[0]) == 1 && T2[2].equals(AiStats[0]);
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

        for( int a = L2[0]-L1[0]>0 ? 1 : -1, b =L2[1]-L1[1]>0 ? 1 : -1, i=0;
                Math.abs(L2[0]-L1[0])!=0 || Math.abs(L2[1]-L1[1])!=0;
                i++, L1[0] = Math.abs(L1[0]-L2[0])==0 ? L1[0] : (char) (L1[0] + a),
                     L1[1] = Math.abs(L1[1]-L2[1])==0 ? L1[1] : (char) (L1[1] + b)  ){

            // > 8 == too far || Needs to not record the tile the piece is on (0)
            if(i>=8){break;} else if (i==0) {continue;}
            TilePath.add(L1[0]+""+L1[1]); //Converts each tile in way to string for array
        }

        //if(TilePath.size()==0){ safe=true; } //Nothing inbetween L1 & L2

        //Check each tile where tile matches tilepath.. make sure col is neutral

        for(String s : TilePath){
            //Check against tiles
            for(TextView tv : RecordOfTiles){
                    if( ((ConcurrentHashMap<String,String>) tv.getTag()).get("ID").equals(s) && tv.getCurrentTextColor() != Color.parseColor("#888888") ){
                        safe=false; break;
                    }
            }
        }

        return safe;
    }

    private void MovePiece(Object[] T1, Object[] T2){

        //T1[0] ID
        TextView tv1=new TextView(this),tv2 =new TextView(this);
        //ConcurrentHashMap<String,String> CHM = new ConcurrentHashMap<>();

        for(TextView tv : RecordOfTiles) {
            if (((ConcurrentHashMap<String, String>) tv.getTag()).get("ID").equals(T1[0])) { tv1 = tv; }
            else if (((ConcurrentHashMap<String, String>) tv.getTag()).get("ID").equals(T2[0])) { tv2 = tv; }
        }

        if (tv2.getCurrentTextColor() != Color.parseColor("#888888")) { TurnsTillStalemate=0; }

        //CHM = (ConcurrentHashMap<String, String>) tv1.getTag();
        ((ConcurrentHashMap<String, String>) tv2.getTag()).put("Piece", ((ConcurrentHashMap<String, String>) tv1.getTag()).get("Piece") );
        ((ConcurrentHashMap<String, String>) tv1.getTag()).put("Piece","");

        TextView Tv2 = tv2, Tv1 = tv1;
        runOnUiThread(()->{
            Toast.makeText(context,T1[0]+" => "+T2[0]+"\n("+T1[1]+")",Toast.LENGTH_SHORT).show();

            if (UseIcons){
                LayerDrawable LD = (LayerDrawable) Tv1.getBackground().getConstantState().newDrawable().mutate();
                ((ColorDrawable)LD.getDrawable(0)).setColor(Integer.parseInt(((ConcurrentHashMap<String, String>) Tv2.getTag()).get("OriginalBg")));

                Tv2.setBackground( LD );
                //Tv1.setBackgroundColor();
                Tv1.setBackgroundColor( Integer.parseInt( ((ConcurrentHashMap<String, String>) Tv1.getTag()).get("OriginalBg") ));
            }else {
                Tv2.setText(Tv1.getText());
                Tv1.setText(DetailedView ? ((ConcurrentHashMap<String, String>) Tv1.getTag()).get("ID") : "");
            }
            Tv1.setTextColor(Color.parseColor("#888888")); Tv2.setTextColor((int)T1[2]);
        });

        RecordOfTiles.set(RecordOfTiles.indexOf(tv1), tv1); RecordOfTiles.set(RecordOfTiles.indexOf(tv2), tv2);

        //Check if king dead..
        switch(T2[1].equals("King") ? "T" + (T2[2].equals(PlStats[0]) ? "_Pl" : "_Ai") : "F"){
            case "T_Pl":
                PlStats[1]=true; break;
            case "T_Ai":
                AiStats[1]=true; break;
            default:
                break;
        }

        GameMoveRecord += ((ConcurrentHashMap<String, String>) tv1.getTag()).get("ID")+((ConcurrentHashMap<String, String>) tv2.getTag()).get("ID");


        AdvanceTurn();
    }

    private Integer TurnsTillStalemate=0; private String GameMoveRecord=""; private boolean PlyrTurn=true;

    private final ArrayList<TextView> RecordOfTiles = new ArrayList<>();

    private void PossibleMovesAllowed(int AiCol){
        try {
            Looper.prepare();
            //Using .join as alt Async method..

            //th = new Thread(() -> {
                ConcurrentHashMap<Object[],ArrayList<Object[]>> PMA = new ConcurrentHashMap<>();
                    //Need alternative for.. multiple of same key
                //Stores possible moves allowed

                for(TextView tv : RecordOfTiles){
                    if (tv.getCurrentTextColor() == AiCol){
                        //Check for each.. AI piece
                        for (TextView tv2 : RecordOfTiles) {
                            if( tv2.getCurrentTextColor() == AiCol){ continue; }
                            //Skip if same col
                            Object[] T1 = new Object[]{
                                    ((ConcurrentHashMap<String,String>)tv.getTag()).get("ID"),
                                    ((ConcurrentHashMap<String,String>)tv.getTag()).get("Piece"),
                                    tv.getCurrentTextColor()
                            }
                            ,T2 = new Object[]{
                                    ((ConcurrentHashMap<String,String>)tv2.getTag()).get("ID"),
                                    ((ConcurrentHashMap<String,String>)tv2.getTag()).get("Piece"),
                                    tv2.getCurrentTextColor()
                            };

                            //Check if T1=>T2 good..
                            boolean Res=false;
                            try {
                                //System.out.println( "T1: "+new ArrayList<>(Arrays.asList(T1))+ "\nT2: "+new ArrayList<>(Arrays.asList(T2)));
                                Res = (Boolean) this.getClass().getDeclaredMethod(T1[1] + "", Object[].class, Object[].class).invoke(this, T1, T2);
                            } catch (Exception e){
                                System.err.println("ReflectionInvoke Err: "+e);

                                Res=false;
                            }

                            if(Res){
                                if (! PMA.containsKey(T1)){ PMA.put(T1, new ArrayList<>()); } //Adds Key w/ empty Val if doesnt exist
                                PMA.get(T1).add(T2); //Gets arrayList via Key then adds to the list (value) without using .put to override value
                            }
                        }
                    }
                }
                //System.out.println("PMA: "+PMA);
                if (AiCol == (int)AiStats[0]) {IsMoveSafe(PMA);} //If ai check if move safe..
                else{
                    //Else.. is Plyr/CultivateAI
                    //Pick a random and move.. run IsMoveSafe.. overload it for multi-use repurpose
                    //System.out.println("PMA: "+PMA);
                    IsMoveSafe(PMA,false);
                }
            //}); th.start();

        }catch (Exception e){ System.err.println("PMA ERR: "+e); }

    }

    private void IsMoveSafe(ConcurrentHashMap<Object[],ArrayList<Object[]>> PMA){
        IsMoveSafe(PMA, true);
    }
    private void IsMoveSafe(ConcurrentHashMap<Object[],ArrayList<Object[]>> PMA, Boolean makeSafe){
        //Pick random move..
        String PossibleMoves="";
        for (Map.Entry<Object[],ArrayList<Object[]>> kvp : PMA.entrySet()){ //Foreach entry in PMA
            for(Object[] o : kvp.getValue()){ //foreach value in arrayList
                PossibleMoves += kvp.getKey()[0].toString() + o[0].toString();
            }
        }

        //System.out.println(PossibleMoves);

        String ChosenMove="";
        for(boolean MoveAllowed = false; !MoveAllowed; PossibleMoves = PossibleMoves.replace(ChosenMove,"")){
            if (PossibleMoves.length() == 0){
                //No possible moves.. all = err
                WriteResWLR();
                InteractBrainFile(getString(R.string.Write),"");
                AiStats[1]=true; //Make king dead true.. auto lose
                MainLoop(); return;
            }
                //Divide PM into sets of 4 (A1=>A2) .. pick a group .. *4 to get where start of it is in PM , Len: 4

            int sb = (int) (Math.floor(Math.random()*PossibleMoves.length()/4 )) *4;
            ChosenMove = PossibleMoves.substring(sb, sb+4);

            MoveAllowed = !makeSafe || InteractBrainFile(getString(R.string.Read), ChosenMove);
                                        //Check if move is recorded..
                //False if shouldn't make the move
        }

        //System.out.println("CHM:" +ChosenMove);

        //Move if not bad..
        for (Map.Entry<Object[],ArrayList<Object[]>> kvp : PMA.entrySet()){ //Foreach entry in PMA
            for(Object[] o : kvp.getValue()) {
                //Have to find randomly picked tile(s) from all PMA.. then make move
                if (kvp.getKey()[0].equals(ChosenMove.substring(0,2)) && o[0].equals(ChosenMove.substring(2,4))) {
                    //System.out.println("CM: "+ChosenMove +"\nT1:"+kvp.getKey()[0]+" T2:"+o[0]);

                    TextView tv1 = null, tv2 = null;
                    for (TextView tv : RecordOfTiles) {
                        tv1 = (Objects.equals(((ConcurrentHashMap<String, String>) tv.getTag()).get("ID"), kvp.getKey()[0].toString())) ? tv : tv1;
                        tv2 = (Objects.equals(((ConcurrentHashMap<String, String>) tv.getTag()).get("ID"), o[0].toString())) ? tv : tv2;
                    }
                    TextView Tv1 = tv1, Tv2 = tv2;
                    runOnUiThread(() -> {
                        TileSelected(Tv1);
                        new Handler().postDelayed(() -> { TileSelected(Tv2); }, (CultivateAI) ? 600 : 1800);
                    });
                }
            }
        }
    }

    private void AiShowSelected(Object[] T1, Object[]T2){
        TextView Tv1 = new TextView(context);

        for(TextView tv : RecordOfTiles) {
            if (Objects.equals(((ConcurrentHashMap<String, String>) tv.getTag()).get("ID"), T1[0].toString())){ Tv1 = tv; }
        }

        //System.out.println(MessageFormat.format( "{0} | tv1-tv2" ,Objects.equals(Tv1,Tv2) ));

        if (Tv1.getCurrentTextColor()==(int)AiStats[0]) {
            c = Tv1;
            // CC=LD of Tv1; c=TxtVw Tv1

            if(UseIcons){
                LayerDrawable LD = (LayerDrawable) Tv1.getBackground();
                cc = (LayerDrawable) LD.getConstantState().newDrawable().mutate();
                LD.getDrawable(0).setColorFilter((Integer) AiStats[0], PorterDuff.Mode.SRC);
                LD.getDrawable( LastRowPieces.indexOf(T1[1].toString()) ).setColorFilter(Integer.parseInt(((ConcurrentHashMap<String, String>) Tv1.getTag()).get("OriginalBg")), PorterDuff.Mode.SRC_IN);
            }else{
                Tv1.setBackgroundColor(Color.parseColor("#"+Integer.toHexString(Tv1.getCurrentTextColor()).substring(2) ));
                Tv1.setTextColor(Integer.parseInt(((ConcurrentHashMap<String, String>) Tv1.getTag()).get("OriginalBg")));
            }
            new Handler().postDelayed(() -> {
                if(UseIcons){
                    c.setBackground(cc);
                }else{
                    c.setTextColor((Integer) AiStats[0]);
                    c.setBackgroundColor(Integer.parseInt(((ConcurrentHashMap<String, String>) c.getTag()).get("OriginalBg")));
                } cc=null;c=null;

                MovePiece(T1,T2);
            }, (CultivateAI) ? 600 : 1800);
        }
    }

    private void AdvanceTurn(){
        //todo Check if pawn reaches end.. give option to promote piece via frag
        TurnsTillStalemate++; PlyrTurn=!PlyrTurn;

        new Thread(()->{
                //Gap between rounds
            try{ Thread.sleep(600); } catch (Exception e){System.err.println("Line 676\n"+e);}
            runOnUiThread(this::MainLoop);
        }).start();
        //MainLoop();
    }

    private void MainLoop(){

        //System.out.println("GMR: "+GameMoveRecord);

        //TurnsTillStalemate+=200;
        if( AiStats[1].equals(true) || PlStats[1].equals(true) || TurnsTillStalemate>=200){
            Toast.makeText(context, (TurnsTillStalemate<200 ? ( (boolean)AiStats[1] ? "Player" : "AI" ) +" Wins!" : "TIE/DRAW!"), Toast.LENGTH_LONG).show();

            if(! CultivateAI) {
                //Write res to WLR file..
                WriteResWLR();
            }

            //write failure if ai loss to InteractBrainFile..
            if (AiStats[1].equals(true)) {
                InteractBrainFile(getString(R.string.Write), null);
            }

            //Disable onclicks
            for(int i=0 ;i < ((TableLayout)findViewById(R.id.GameTable)).getChildCount(); i++) { TableRow tr = (TableRow) ((TableLayout) findViewById(R.id.GameTable)).getChildAt(i); for (int j = 0; j < tr.getChildCount(); j++) { TextView tv = (TextView) tr.getChildAt(j); tv.setOnClickListener(null); } }

            new Thread(()-> {
                try {
                    runOnUiThread(()->{
                        Toast.makeText(context,"Returning to MainMenu in 30s!",Toast.LENGTH_LONG).show();
                    });
                    for (int i = 30; i > 0; i--) {
                        Thread.sleep(1000);
                        if(i==10){ runOnUiThread(()->{
                            Toast.makeText(context,"Returning to MainMenu in 10s!",Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                    runOnUiThread(() -> {
                        startActivity(new Intent(context, Main.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                    });
                } catch (Exception e) {
                    System.err.println("SRS err occurred w countdown!");
                }
            }).start();

            return;
        }

        if (! PlyrTurn){
            //Disable onclicks..
            for(TextView tv : RecordOfTiles){ tv.setOnClickListener(null); }

            th = new Thread(()->{ PossibleMovesAllowed((Integer) AiStats[0]); });
            try { th.join(); //join.. makes curr thread wait till the thread calling join dies.. is AWAIT
               th.start(); } catch (Exception e) { System.err.println("Line 734\n"+e); }
            //PossibleMovesAllowed();
        }else{
            //Enable onclicks..
            //CultivateAI=false; //Disable cultivate
            if(! CultivateAI){
                for(TextView tv : RecordOfTiles){ tv.setOnClickListener(this::TileSelected); } }
            else{
                //Changing col changes team selected?
                th = new Thread(()->{
                    //try{ Thread.sleep(1400); } catch (Exception e){}
                    //Object o = PlStats[0]; PlStats[0] = AiStats[0]; AiStats[0]=o;
                    System.out.println("Cultivate..");
                    PossibleMovesAllowed((Integer) PlStats[0]);

                }); th.start();
            }
        }
    }
    private boolean InteractBrainFile(String IOmode, @Nullable String Movement){
        if(IOmode.equals(getString(R.string.Read))){
            ArrayList<String> FailedMoves = new ArrayList<>();

            try{
                BufferedReader bfr = new BufferedReader(new FileReader(new File(getFilesDir(),getString(R.string.AIBrain))));
                String l = bfr.readLine();
                while(l!=null){ FailedMoves.add(l); l = bfr.readLine(); }
                bfr.close();

                //System.out.println("FM: \n"+FailedMoves);

                //System.err.println("FM:\n "+FailedMoves);

                //System.out.println("FailedMoves.contains(GameMoveRecord) = "+FailedMoves.contains(GameMoveRecord));
                return ! FailedMoves.contains(GameMoveRecord+Movement);
                    //Returns false if it shouldn't make the move (determines it leads to failure already recorded)

            }catch (Exception e){ System.err.println("ReadBrain err: "+e); }


        }else if(IOmode.equals(getString(R.string.Write))){
            //Ai lost..
            if(GameMoveRecord.length()<4){ Toast.makeText(context, "Ai lost unusually!", Toast.LENGTH_SHORT).show(); return false; }

            try{
                FileWriter fw = new FileWriter(new File(getFilesDir(),getString(R.string.AIBrain)));
                    //Record the move it made before enemy got its king
                fw.append("\n").append(GameMoveRecord.substring(0,GameMoveRecord.length()-4));
                fw.flush();fw.close();

            }catch (Exception e){ Toast.makeText(context, "Ai failed to learn!", Toast.LENGTH_SHORT).show();
                System.err.println("WriteBrain err: "+e); }

        }

        return false;
    }

    private void WriteResWLR(){
        try {
            String toWrite="", toFind = (AiStats[1].equals(true)) ? "Human" : PlStats[1].equals(true) ? "AI" : "Tie";
            BufferedReader bfr = new BufferedReader(new FileReader(new File(getFilesDir(),getString(R.string.RatioRecord))));
            String s = bfr.readLine(); bfr.close();
            //System.out.println("S: "+s);
            String[] S = s.split("\\|");
            for(String x : S){
                //System.err.println("x:"+x+"|"+toFind);
                if (x.contains(toFind)){
                    //System.out.println("FoundWinner");
                    Matcher m = Pattern.compile("\\d+").matcher(x);
                    if(m.find(0)){
                        int Ratio = Integer.parseInt( x.substring(m.start(),m.end()) );
                        x = x.substring(0,m.start()) + ++Ratio +" ";
                    }
                }
                toWrite+=x+"|";
            }
            toWrite = toWrite.substring(0,toWrite.length()-1);

            //System.out.println("WLR: "+toWrite);

            FileWriter fw = new FileWriter(new File(getFilesDir(), "WLR"));
            fw.write(toWrite); fw.flush(); fw.close();
        }catch (Exception e){
            System.err.println("WLR err: "+e);
            Toast.makeText(context, "Err occured recording results!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,Main.class));
    }
}
