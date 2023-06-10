package com.example.chesskillmobile;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PawnPromoFrag extends DialogFragment {

    private Context context;

    @Override @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        context = getContext().getApplicationContext();

        //PassBy bundle.. tv doesnt work
        PmTag = ((ConcurrentHashMap<String, String>) this.getArguments().getSerializable("PromoteMe"));
        UseIcons = this.getArguments().getBoolean("UI");

        return inflater.inflate(R.layout.pawnpromo_frag, container, false);
    }

    private LayerDrawable GetPieceIcon(int i){
        LayerDrawable LD = (LayerDrawable) ResourcesCompat.getDrawable(getResources(),R.drawable.pieces_ai,null).getConstantState().newDrawable().mutate();
        ((ColorDrawable) LD.getDrawable(0)).setColor(Color.parseColor("#888888"));

        for(int j=1;j<LD.getNumberOfLayers();j++){
            //System.out.println("LD:"+LD.getNumberOfLayers()+" j:"+j);
            if(j==i){ continue; }
            LD.getDrawable(j).setAlpha(0);
        }

        return LD;
    }

    private ConcurrentHashMap<String,String> PmTag=null; private TextView PromoteMe=null; Boolean UseIcons=false;

    private ArrayList<String> LRP;

    @Override
    public void onStart() {
        super.onStart();

        for(int i = 0; i<((TableLayout)getActivity().findViewById(R.id.GameTable)).getChildCount(); i++){
            TableRow tr = (TableRow) ((TableLayout)getActivity().findViewById(R.id.GameTable)).getChildAt(i);
            for(int j=0;j<tr.getChildCount();j++){
                TextView tv = (TextView) tr.getChildAt(j);

                if(Objects.equals(((ConcurrentHashMap<String, String>) tv.getTag()).get("ID"), PmTag.get("ID"))){ PromoteMe = tv; break; }
            }
        }

        //((TextView)getActivity().findViewById(R.id.PawnPromoTitle)).append(" ("+((ConcurrentHashMap<String,String>)PromoteMe.getTag()).get("ID")+")");

        LRP = new ArrayList<>(Arrays.asList(getString(R.string.Rook), getString(R.string.Knight), getString(R.string.Bishop), getString(R.string.King), getString(R.string.Queen), getString(R.string.Bishop), getString(R.string.Knight), getString(R.string.Rook))); LRP.add(0,""); LRP.add(1,getString(R.string.Pawn));

        //Setup drawables.. //Setup img srcs
        if(UseIcons) {
            getActivity().findViewById(R.id.PawnPromoRook).setBackground(GetPieceIcon(LRP.indexOf(getString(R.string.Rook))));
            getActivity().findViewById(R.id.PawnPromoKnight).setBackground(GetPieceIcon(LRP.indexOf(getString(R.string.Knight))));
            getActivity().findViewById(R.id.PawnPromoBishop).setBackground(GetPieceIcon(LRP.indexOf(getString(R.string.Bishop))));
            getActivity().findViewById(R.id.PawnPromoQueen).setBackground(GetPieceIcon(LRP.indexOf(getString(R.string.Queen))));
        }else{
            ((TextView)getActivity().findViewById(R.id.PawnPromoRook)).setText(getString(R.string.Rook));
            ((TextView)getActivity().findViewById(R.id.PawnPromoBishop)).setText(getString(R.string.Bishop));
            ((TextView)getActivity().findViewById(R.id.PawnPromoQueen)).setText(getString(R.string.Queen));
            ((TextView)getActivity().findViewById(R.id.PawnPromoKnight)).setText(getString(R.string.Knight));
        }

        //Setup onclicks..
        getActivity().findViewById(R.id.PawnPromoBg).setOnClickListener(null);
        getActivity().findViewById(R.id.PawnPromoTitle).setOnClickListener(null);

        getActivity().findViewById(R.id.PawnPromoRook).setOnClickListener((view -> {
            PromotePawn(getString(R.string.Rook), (TextView) view);
            getActivity().findViewById(R.id.PawnPromoBg).setOnClickListener(v->{CloseFrag();});
        }));

        getActivity().findViewById(R.id.PawnPromoKnight).setOnClickListener((view -> {
            PromotePawn(getString(R.string.Knight), (TextView) view);
            getActivity().findViewById(R.id.PawnPromoBg).setOnClickListener(v->{CloseFrag();});
        }));

        getActivity().findViewById(R.id.PawnPromoBishop).setOnClickListener((view -> {
            PromotePawn(getString(R.string.Bishop), (TextView) view);
            getActivity().findViewById(R.id.PawnPromoBg).setOnClickListener(v->{CloseFrag();});
        }));

        getActivity().findViewById(R.id.PawnPromoQueen).setOnClickListener((view -> {
            PromotePawn(getString(R.string.Queen), (TextView) view);
            getActivity().findViewById(R.id.PawnPromoBg).setOnClickListener(v->{CloseFrag();});
        }));

    }

    protected void PromotePawn(String NewPiece, TextView tv){
        Toast.makeText(context, getString(R.string.Pawn)+" promoted to: "+NewPiece, Toast.LENGTH_SHORT).show();
        PmTag.put("Piece",NewPiece);

        if (UseIcons){
            LayerDrawable LD = (LayerDrawable) tv.getBackground().getConstantState().newDrawable().mutate();
            ((ColorDrawable)LD.getDrawable(0)).setColor(Integer.parseInt(PmTag.get("OriginalBg")));

            switch (PmTag.get("ID").substring(0,1)){
                case "A":
                case "H":
                    ((BitmapDrawable)LD.getDrawable(LRP.indexOf(NewPiece))).setColorFilter(this.getArguments().getInt("PawnCol"), PorterDuff.Mode.SRC_IN);

                default:
            }

            PromoteMe.setBackground(LD);
        }else{
            PromoteMe.setText(tv.getText());
        }

        Toast.makeText(context, "Click background to return!", Toast.LENGTH_SHORT).show();
    }

    private void CloseFrag() {
        getParentFragmentManager().beginTransaction().remove(this).commit();

        //Put FL into index 0 so its overwritten and hidden
        FrameLayout FL = getActivity().findViewById(R.id.GameFragHolder);
        ((ViewGroup)FL.getParent()).removeView(FL); ((ViewGroup)getActivity().findViewById(R.id.GameBg)).addView(FL,0);
    }
}
