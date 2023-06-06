package com.example.chesskillmobile;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.FileWriter;

public class PreGameFrag extends DialogFragment {

    private Context context;

    @Override @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        context = getContext().getApplicationContext();

        //TransitionInflater TI = TransitionInflater.from(context); setEnterTransition(TI.inflateTransition(R.transition.transition)); setExitTransition(TI.inflateTransition(R.anim.frag_out));
        //Animations give more control than transitions

        return inflater.inflate(R.layout.pregame_frag, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //onClick FUNCTIONS

        getActivity().findViewById(R.id.PregameBg).setOnClickListener(null);

        getActivity().findViewById(R.id.PregameWB).setOnClickListener((v)->{
            if(!(WBsafe && BBsafe)){ Toast.makeText(context, "Hex colours unsafe!", Toast.LENGTH_SHORT).show(); return;}

            Pstats= new Object[]{ Color.parseColor( ((TextView)getActivity().findViewById(R.id.PregameWBcol)).getText() +""),false};
            Astats= new Object[]{Color.parseColor( ((TextView)getActivity().findViewById(R.id.PregameBBcol)).getText() +""),false};

            /*try {
                FileWriter fw = new FileWriter(new File(getActivity().getFilesDir(),"tmp") );
                fw.write(((TextView)getActivity().findViewById(R.id.PregameWBcol)).getText()+"\n"+"false"+"\n"+((TextView)getActivity().findViewById(R.id.PregameBBcol)).getText()+"\n"+"false"); fw.flush(); fw.close();
            } catch (Exception e) { Toast.makeText(context,"Srs err occured!",Toast.LENGTH_SHORT).show(); return; }
            */

            CloseFrag();
        });

        getActivity().findViewById(R.id.PregameBB).setOnClickListener((v)->{
            if(!(WBsafe && BBsafe)){ Toast.makeText(context, "Hex colours unsafe!", Toast.LENGTH_SHORT).show(); return;}

            Astats= new Object[]{ Color.parseColor( ((TextView)getActivity().findViewById(R.id.PregameWBcol)).getText() +""),false};
            Pstats= new Object[]{Color.parseColor( ((TextView)getActivity().findViewById(R.id.PregameBBcol)).getText() +""),false};

            /*
            try {
                FileWriter fw = new FileWriter(new File(getActivity().getFilesDir(),"tmp") );
                fw.write(((TextView)getActivity().findViewById(R.id.PregameBBcol)).getText()+"\n"+"false"+"\n"+((TextView)getActivity().findViewById(R.id.PregameWBcol)).getText()+"\n"+"false"); fw.flush(); fw.close();
            } catch (Exception e) { Toast.makeText(context,"Srs err occured!",Toast.LENGTH_SHORT).show(); return; }
             */

            CloseFrag();
        });

        ((EditText)getActivity().findViewById(R.id.PregameBBcol)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //start = starting index of the part of text that is changing
                //count = prev length
                //after = new length
                //System.out.println(MessageFormat.format( "BTC : charSeq {0} | start {1} | count {2} | after {3}" ,charSequence,start,count,after ));
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //start = starting index of the part of text that is changing
                //before = prev length
                //count = new length
                //System.out.println(MessageFormat.format( "OTC : charSeq {0} | start {1} | before {2} | count {3}" ,charSequence,start,before,count ));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //System.out.println(MessageFormat.format( "ATC : editable {0}" ,editable.toString() ));
                String s = editable.toString();
                try {
                    ((TextView)getActivity().findViewById(R.id.PregameBB)).setTextColor( Color.parseColor(s) );
                    BBsafe=true;
                }catch (Exception e){ BBsafe=false;
                    //System.out.println("s:"+s+" e:"+e);
                }
            }
        });

        ((EditText)getActivity().findViewById(R.id.PregameWBcol)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //start = starting index of the part of text that is changing
                //count = prev length
                //after = new length
                //System.out.println(MessageFormat.format( "BTC : charSeq {0} | start {1} | count {2} | after {3}" ,charSequence,start,count,after ));
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //start = starting index of the part of text that is changing
                //before = prev length
                //count = new length
                //System.out.println(MessageFormat.format( "OTC : charSeq {0} | start {1} | before {2} | count {3}" ,charSequence,start,before,count ));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //System.out.println(MessageFormat.format( "ATC : editable {0}" ,editable.toString() ));
                String s = editable.toString();
                try {
                    ((TextView)getActivity().findViewById(R.id.PregameWB)).setTextColor( Color.parseColor(s) );
                    WBsafe=true;
                }catch (Exception e){ WBsafe=false;
                    //System.out.println("s:"+s+" e:"+e);
                }
            }
        });

        ((TextView)getActivity().findViewById(R.id.PregameWB)).setTextColor( Color.parseColor(
            ((EditText)getActivity().findViewById(R.id.PregameWBcol)).getText() +""
        ));
        ((TextView)getActivity().findViewById(R.id.PregameBB)).setTextColor( Color.parseColor(
                ((EditText)getActivity().findViewById(R.id.PregameBBcol)).getText() +""
        ));
    }

    private Boolean WBsafe=true, BBsafe=true;

    private void CloseFrag(){
        //
        Bundle b = new Bundle(); b.putString("ReqKey","Results");
        getParentFragmentManager().setFragmentResult("ReqKey",b);
        getParentFragmentManager().beginTransaction().remove(this).commit();
        //Put FL into index 0 so its overwritten and hidden
        FrameLayout FL = getActivity().findViewById(R.id.GameFragHolder);
        ((ViewGroup)FL.getParent()).removeView(FL); ((ViewGroup)getActivity().findViewById(R.id.GameBg)).addView(FL,0);

        String s = ((EditText)getActivity().findViewById(R.id.PregameWBcol)).getText()+"";

        OCR.TeamChosen( Astats, Pstats, s, ((CheckBox)getActivity().findViewById(R.id.PregameCheckBox)).isChecked(), ((CheckBox)getActivity().findViewById(R.id.PregameCheckBox2)).isChecked(), ((CheckBox)getActivity().findViewById(R.id.PregameCheckBox3)).isChecked()  );
        //new Game().TeamChosen(); //err accessing file
    }

    private Object[] Astats,Pstats;

    public interface OnCallbackReceived{
        void TeamChosen(Object[] a,Object[] b, String s, Boolean dv, Boolean ic, Boolean ca);  //Sending back the TxtCol of white to determine who plays first
    }

    OnCallbackReceived OCR; //Has to be casted in onAttach
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{ OCR = (OnCallbackReceived) context; //Moves interface to activity context
        }catch (Exception e){ System.err.println(e); }
    }
}

