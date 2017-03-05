package com.example.fanghao01.cccl;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int START = 0x123;
    private static final int END = 0x124;
    private static final int FLAG = 0x125;

    boolean flagSwicth = false;
    int time = 0;
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START) {
                time++;
            } else if (msg.what == END) {
                time = 0;
            }
            timeView.setText(time + "S");
        }
    };
    MineView mineView;
    TextView refreshView, timeView, num_view, flagView;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mineView = (MineView) findViewById(R.id.mine_view);
        refreshView = (TextView) findViewById(R.id.refresh_view);
        timeView = (TextView) findViewById(R.id.time_view);
        num_view = (TextView) findViewById(R.id.num_view);
        flagView = (TextView) findViewById(R.id.flag_view);

        refreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(END);
                mineView.refresh(null);
                time = 0;
            }
        });
        flagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flagSwicth = !flagSwicth;
                flagView.setBackgroundColor(flagSwicth ? Color.GRAY : Color.WHITE);
                mineView.setFlagSwitch(flagSwicth);
            }
        });
        mineView.setSignal(new MineView.Signal() {
            @Override
            public void onStart() {
                if (mineView.getStarted()) {
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(START);
                        }
                    }, 1000, 1000);

                }
            }

            @Override
            public void onFinish() {
                timer.cancel();
            }

            @Override
            public void onFlag(int num) {
                num_view.setText(num + "");
            }
        });
    }
}
