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
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
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

    private Object[] PlStats, AiStats; //{TheirColour,IsKingDead?} (Color/int - TV.getCurrCol is int , Boolean)

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

        PlyrTurn = (PlStats[0].equals( Color.parseColor(s) )); //WORKS

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

                RecordOfTiles.add(TV); //Fill / mini-initialise RecordOfTiles

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


        for(TextView tv : RecordOfTiles) {

            ConcurrentHashMap<String, String> CHM = (ConcurrentHashMap<String, String>) tv.getTag();
            String s = CHM.get("ID");

            //Tag: A1 || G3 ..
            //String L = tag.substring(0,1), N = tag.substring(1);
            switch (s.substring(0, 1)) {
                case ("B"):
                case ("G"):
                    runOnUiThread(() -> {
                        tv.setText(getResources().getString(R.string.Pawn));
                        CHM.put("Piece", getResources().getString(R.string.Pawn));
                        tv.setTag(CHM);

                        RecordOfTiles.set(RecordOfTiles.indexOf(tv), tv);
                    });
                    break;
                case ("A"):
                case ("H"):
                    runOnUiThread(() -> {
                        tv.setText(LastRowPieces[Integer.parseInt(s.substring(1)) - 1]);
                        CHM.put("Piece", LastRowPieces[Integer.parseInt(s.substring(1)) - 1]);
                        tv.setTag(CHM);

                        RecordOfTiles.set(RecordOfTiles.indexOf(tv), tv);
                    });
                    break;
            }
        }


        ApplyTeamCols();
    }

    private void ApplyTeamCols(){

        for(TextView tv : RecordOfTiles) {
            String tag = ((ConcurrentHashMap<String, String>) tv.getTag()).get("ID");
            switch (tag.substring(0, 1)) {
                case "A": case "B":
                    //AI
                    runOnUiThread(() -> {
                        tv.setTextColor((int) AiStats[0]);
                        RecordOfTiles.set(RecordOfTiles.indexOf(tv), tv);
                    });
                    break;
                case "H": case "G":
                    //Pl
                    runOnUiThread(() -> {
                        tv.setTextColor((int) PlStats[0]);
                        RecordOfTiles.set(RecordOfTiles.indexOf(tv), tv);
                    });
                    break;
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

                //Appears to occur from clicking too fast..?

                Toast.makeText(this, "Srs err, reflection err\nAborting game..", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(()->{ startActivity(new Intent(this,Main.class)); },2000);
            }

            TileOne=null;
        } else if( tv.getCurrentTextColor() == (int)PlStats[0]
                ||
                 tv.getCurrentTextColor() == (int)PlStats[0] ) {
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
            } else if(Math.abs(L1[0]-L2[0])==1){
                return true;
            }
            //Check if moving diagonal (1 letter, 1 number & enemy piece)
        } else if (Math.abs(L1[1] - L2[1]) == 1 && Math.abs(L1[0]-L2[0]) == 1 && T2[2].equals(AiStats[0])){
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

        if (tv2.getCurrentTextColor() == Color.parseColor("#888888")) { TurnsTillStalemate=0; }

        //CHM = (ConcurrentHashMap<String, String>) tv1.getTag();
        ((ConcurrentHashMap<String, String>) tv2.getTag()).put("Piece", ((ConcurrentHashMap<String, String>) tv1.getTag()).get("Piece") );
        ((ConcurrentHashMap<String, String>) tv1.getTag()).put("Piece","");

        //While temp no BGR..has to use Txt..

        tv2.setText(tv1.getText()); tv1.setText(((ConcurrentHashMap<String, String>) tv1.getTag()).get("ID"));
        tv1.setTextColor(Color.parseColor("#888888")); tv2.setTextColor((int)T1[2]);

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

    private Integer TurnsTillStalemate=0;
    private String GameMoveRecord="";
    private boolean PlyrTurn=true;

    private ArrayList<TextView> RecordOfTiles = new ArrayList<>();

    private void PossibleMovesAllowed(){
        try {
            //Using .join as alt Async method..

            th = new Thread(() -> {
                //todo figure out a way to store differing vals..
                ConcurrentHashMap<Object[],ConcurrentHashMap<Object[],Object[]>> PMA = new ConcurrentHashMap<>();
                    //Need alternative for.. multiple of same key
                //Stores possible moves allowed

                for(TextView tv : RecordOfTiles){
                    if (tv.getCurrentTextColor() == (int)AiStats[0]){
                        //Check for each.. AI piece
                        for (TextView tv2 : RecordOfTiles) {
                            if( tv2.getCurrentTextColor() == (int) AiStats[0]){ continue; }
                            //Skip if same col
                            boolean Res = false;//Check if T1=>T2 good..

                            if(Res){
                                //PMA.put()
                            }
                        }
                    }
                }
            });
            th.start();

        }catch (Exception e){ System.err.println("PMA ERR: "+e); }

    }

    private void IsMoveSafe(){

    }

    private void AdvanceTurn(){
        TurnsTillStalemate++; PlyrTurn=!PlyrTurn; MainLoop();
    }

    private void MainLoop(){
        PossibleMovesAllowed();
        //TurnsTillStalemate+=200;
        if( AiStats[1].equals(true) || PlStats[1].equals(true) || TurnsTillStalemate>=200){
            Toast.makeText(context, (TurnsTillStalemate<200 ? ( (boolean)AiStats[1] ? "Player" : "AI" ) +" Wins!" : "TIE/DRAW!"), Toast.LENGTH_SHORT).show();

            //Write res to WLR file..
            WriteResWLR();

            //InteractBrainFile..
            InteractBrainFile(getString(R.string.Read),null);

            //Disable onclicks
            for(int i=0 ;i < ((TableLayout)findViewById(R.id.GameTable)).getChildCount(); i++) { TableRow tr = (TableRow) ((TableLayout) findViewById(R.id.GameTable)).getChildAt(i); for (int j = 0; j < tr.getChildCount(); j++) { TextView tv = (TextView) tr.getChildAt(j); tv.setOnClickListener(null); } }

            return;
        }
    }
    private boolean InteractBrainFile(String IOmode, @Nullable String Movement){
        GameMoveRecord="A4A6";

        if(IOmode.equals(getString(R.string.Read))){
            ArrayList<String> FailedMoves = new ArrayList<>();

            try{
                BufferedReader bfr = new BufferedReader(new FileReader(new File(getFilesDir(),getString(R.string.AIBrain))));
                String l = bfr.readLine();
                while(l!=null){ FailedMoves.add(l); l = bfr.readLine(); }
                bfr.close();

                //System.err.println("FM:\n "+FailedMoves);

                //System.out.println("FailedMoves.contains(GameMoveRecord) = "+FailedMoves.contains(GameMoveRecord));
                return ! FailedMoves.contains(GameMoveRecord+Movement);
                    //Returns false if shouldnt make the move (determines it leads to failure alrdy recorded)

                //Return bool..?
            }catch (Exception e){ System.err.println("ReadBrain err: "+e); }


        }else if(IOmode.equals(getString(R.string.Write))){
            //Ai lost..
            if(GameMoveRecord.length()<4){ Toast.makeText(context, "Ai lost unusually!", Toast.LENGTH_SHORT).show(); return false; }

            try{
                FileWriter fw = new FileWriter(new File(getFilesDir(),getString(R.string.AIBrain)));
                    //Record the move it made before enemy got its king
                fw.append("\n").append(GameMoveRecord.substring(0,GameMoveRecord.length()-4));
                fw.flush();fw.close();

            }catch (Exception e){ Toast.makeText(context, "Ai failed to learn!", Toast.LENGTH_SHORT).show();; System.err.println("WriteBrain err: "+e); }

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
