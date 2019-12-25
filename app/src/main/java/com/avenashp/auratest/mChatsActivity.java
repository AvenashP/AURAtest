package com.avenashp.auratest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avenashp.auratest.ModelClass.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class mChatsActivity extends AppCompatActivity {

    private static final String TAG = "❌SEEKER-CHAT❌";
    private TextView textmsg,morsemsg,morseletter;
    private LinearLayout touch;
    private Vibrator vibrator;
    private String[] ch = new String[1000];
    private String[] str = new String[1000];
    private String mMsg,tMsg,xMessage="",xRecived;
    private int SPACE=0,index1=0,index2=0,DSPACE=0;
    private Map<String, String> xDict = new HashMap();
    private String xUserId,xChatid,xDate,xTime,xMorseCode;
    private FirebaseAuth fireAuth;
    private FirebaseUser fireUser;
    private DatabaseReference dbUserDetails,dbChatManager;
    private Query lastquery;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m_chat);

        xChatid = getIntent().getExtras().getString("chatid");

        textmsg = findViewById(R.id.textmsg);
        morsemsg = findViewById(R.id.morsemsg);
        morseletter =findViewById(R.id.morseletter);
        touch = findViewById(R.id.touch);
        Arrays.fill(ch,"");
        Arrays.fill(str,"");
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        morseletter.setVisibility(TextView.INVISIBLE);
        fireAuth = FirebaseAuth.getInstance();
        fireUser = fireAuth.getCurrentUser();
        xUserId = fireUser.getUid();
        dbUserDetails = FirebaseDatabase.getInstance().getReference("User Details");
        dbChatManager = FirebaseDatabase.getInstance().getReference("Chat Manager");
        lastquery = dbChatManager.child(xChatid).orderByKey().limitToLast(1);

        funCreateDictionary();

        morseletter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence cS, int i, int i1, int i2) {

                if(getKey(xDict,cS.toString()) == null){
                    ch[index1] = "";
                    str[index2] = morseletter.getText().toString();
                }
                else{
                    ch[index1] = getKey(xDict,cS.toString());
                    str[index2] = morseletter.getText().toString();
                }
                tMsg = TextUtils.join("",ch);
                mMsg = TextUtils.join("",str);
                textmsg.setText(tMsg);
                morsemsg.setText(mMsg);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        touch.setOnTouchListener(new OnSwipeTouchListener(this) {
            @SuppressLint("SimpleDateFormat")
            public void onSwipeTop() {
                xMessage = "";
                xMessage = textmsg.getText().toString();
                xMorseCode = morsemsg.getText().toString();
                Arrays.fill(ch,"");
                Arrays.fill(str,"");
                index1=0;
                index2=0;
                morseletter.setText("");

                //Log.i(TAG, "onSWIPE TOP: "+xMessage);

                xDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                xTime = new SimpleDateFormat("hh:mm a").format(new Date());

                ChatModel chm  = new ChatModel(xMessage,xTime,xUserId,xDate,xMorseCode);
                dbChatManager.child(xChatid).push().setValue(chm);
                vibrator.vibrate(1000);
            }

            public void onSwipeRight() {
                if(!morsemsg.getText().toString().equals("") && DSPACE!=1){
                    vibrator.vibrate(25);
                    if(SPACE == 0){
                        ch[++index1] = "";
                        str[++index2] = " ";
                        str[++index2] = "";
                    }
                    else {
                        ch[index1++] = " ";
                        str[index2++] = " ";
                        DSPACE = 1;
                    }
                    morseletter.setText("");
                    morsemsg.append(" ");
                    SPACE = 1;
                }
            }

            public void onSwipeLeft() {
                if(!morsemsg.getText().toString().equals("")){
                    vibrator.vibrate(25);
                    if(index1 == 0){
                        morseletter.setText("");
                        startActivity(new Intent(mChatsActivity.this,mContactsActivity.class));
                        finish();
                    }
                    else{
                        while(index1 > 0){
                            if(ch[index1].equals("")){
                                index1--;
                            }
                            char c = ch[index1].charAt(0);
                            if(c>=33 && c<=122){
                                ch[index1] = "";
                                break;
                            }
                            else{
                                ch[index1]="";
                                index1--;
                            }
                        }
                        while(index2 >= 0){
                            Log.i(TAG, "START "+index2);
                            if(str[index2].equals("")){
                                Log.i(TAG, "CURRENT BOX closed");
                                index2--;
                            }
                            String s = getKey(xDict,str[index2]);
                            if(s==null){
                                Log.i(TAG, "onSwipeLeft: "+s+" "+str[index2]);
                                str[index2] = "";
                                index2--;
                            }
                            else{
                                Log.i(TAG, "onSwipeLeft: "+s+" "+str[index2]);
                                str[index2]="";
                                break;
                            }
                            Log.i(TAG, "END "+index2);
                        }
                        morseletter.setText(str[index2]);

                    }
                }
            }

            public void onSwipeBottom() {
                Toast.makeText(getApplicationContext(), "Swiped bottom", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onSwipeBottom: ");
            }

        });

        lastquery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(!xUserId.equals(dataSnapshot.child("sender").getValue().toString())){
                    xRecived = dataSnapshot.child("morse_code").getValue().toString();
                    Toast.makeText(mChatsActivity.this,dataSnapshot.child("message").getValue().toString(),Toast.LENGTH_SHORT).show();
                    char[] morsearr = xRecived.toCharArray();
                    for(char c : morsearr){
                        Log.i(TAG, "Moresarr: "+c);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        OnSwipeTouchListener(Context ctx) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 300;
            private static final int SWIPE_VELOCITY_THRESHOLD = 300;
            private static final int SWIPE_THRESHOLD1 = 1000;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                SPACE=0;
                DSPACE=0;
                morseletter.append("•");
                vibrator.vibrate(100);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                SPACE=0;
                DSPACE=0;
                morseletter.append("−");
                vibrator.vibrate(300);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD1 && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }
        public void onSwipeRight() {}
        public void onSwipeLeft() {}
        public void onSwipeTop() {}
        public void onSwipeBottom() {}
    }


    private void funCreateDictionary() {
        xDict.put("A", "•−");
        xDict.put("B", "−•••");
        xDict.put("C", "−•−•");
        xDict.put("D", "−••");
        xDict.put("E", "•");
        xDict.put("F", "••−•");
        xDict.put("G", "−−•");
        xDict.put("H", "••••");
        xDict.put("I", "••");
        xDict.put("J", "•−−−");
        xDict.put("K", "−•−");
        xDict.put("L", "•−••");
        xDict.put("M", "−−");
        xDict.put("N", "−•");
        xDict.put("O", "−−−");
        xDict.put("P", "•−−•");
        xDict.put("Q", "−−•−");
        xDict.put("R", "•−•");
        xDict.put("S", "•••");
        xDict.put("T", "−");
        xDict.put("U", "••−");
        xDict.put("V", "•••−");
        xDict.put("W", "•−−");
        xDict.put("X", "−••−");
        xDict.put("Y", "−•−−");
        xDict.put("Z", "−−••");

        xDict.put("0", "−−−−−");
        xDict.put("1", "•−−−−");
        xDict.put("2", "••−−−");
        xDict.put("3", "•••−−");
        xDict.put("4", "••••−");
        xDict.put("5", "•••••");
        xDict.put("6", "−••••");
        xDict.put("7", "−−•••");
        xDict.put("8", "−−−••");
        xDict.put("9", "−−−−•");

        xDict.put("@", "•−−•−•");
        xDict.put("_", "••−−•−");
        xDict.put("&", "•−•••");
        xDict.put("-", "−••••−");
        xDict.put("=", "−•••−");
        xDict.put("+", "•−•−•");
        xDict.put("(", "−•−−•");
        xDict.put(")", "−•−−•−");
        xDict.put("/", "−••−•");
        xDict.put(".", "•−•−•−");
        xDict.put(",", "−−••−−");
        xDict.put("`", "•−−−−•");
        xDict.put(":", "−−−•••");
        xDict.put(";", "−•−•−•");
        xDict.put("!", "−•−•−−");
        xDict.put("?", "••−−••");
    }


    private static <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}