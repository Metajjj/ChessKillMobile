package com.example.chesskillmobile;

import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class PawnPromoFrag extends DialogFragment {

    private Context context;

    @Override @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        context = getContext().getApplicationContext();

        //PassBy bundle.. tv itself?

        return inflater.inflate(R.layout.pawnpromo_frag, container, false);
    }

    private LayerDrawable GetPieceIcon(int i){
        LayerDrawable LD = (LayerDrawable) ResourcesCompat.getDrawable(getResources(),R.drawable.pieces_ai,null).getConstantState().newDrawable().mutate();

        for(int j=1;i<LD.getNumberOfLayers();j++){
            if(j==i){ continue; }
            LD.getDrawable(j).setAlpha(0);
        }

        return LD;
    }

    ArrayList<String> LRP;

    @Override
    public void onStart() {
        super.onStart();

        LRP = new ArrayList<>(Arrays.asList(getString(R.string.Rook), getString(R.string.Knight), getString(R.string.Bishop), getString(R.string.King), getString(R.string.Queen), getString(R.string.Bishop), getString(R.string.Knight), getString(R.string.Rook))); LRP.add(0,""); LRP.add(1,getString(R.string.Pawn));

        //Setup drawables.. //Setup img srcs
        getActivity().findViewById(R.id.PawnPromoRook).setBackground(GetPieceIcon(LRP.indexOf(getString(R.string.Rook))));
        getActivity().findViewById(R.id.PawnPromoKnight).setBackground(GetPieceIcon(LRP.indexOf(getString(R.string.Knight))));
        getActivity().findViewById(R.id.PawnPromoBishop).setBackground(GetPieceIcon(LRP.indexOf(getString(R.string.Bishop))));
        getActivity().findViewById(R.id.PawnPromoQueen).setBackground(GetPieceIcon(LRP.indexOf(getString(R.string.Queen))));

        //Setup onclicks..
        getActivity().findViewById(R.id.PawnPromoBg).setOnClickListener(v->{CloseFrag();});
        getActivity().findViewById(R.id.PawnPromoTitle).setOnClickListener(null);

        getActivity().findViewById(R.id.PawnPromoRook).setOnClickListener((view -> {
            //Handle click
        }));

        getActivity().findViewById(R.id.PawnPromoKnight).setOnClickListener((view -> {
            //Handle click
        }));

        getActivity().findViewById(R.id.PawnPromoBishop).setOnClickListener((view -> {
            //Handle click
        }));

        getActivity().findViewById(R.id.PawnPromoQueen).setOnClickListener((view -> {
            //Handle click
        }));

    }

    private void CloseFrag() {
        getParentFragmentManager().beginTransaction().remove(this).commit();

        //Put FL into index 0 so its overwritten and hidden
        FrameLayout FL = getActivity().findViewById(R.id.GameFragHolder);
        ((ViewGroup)FL.getParent()).removeView(FL); ((ViewGroup)getActivity().findViewById(R.id.GameBg)).addView(FL,0);
    }
}
