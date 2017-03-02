package com.example.fanghao01.cccl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int START = 0x123;
    private static final int END = 0x124;

    int time = 0;
    MineView mineView;
    TextView refreshView, timeView;
    Timer timer;
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START) {
                time ++;
            } else if (msg.what == END) {
                time = 0;
            }
            timeView.setText(time+"");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mineView = (MineView) findViewById(R.id.mine_view);
        refreshView = (TextView) findViewById(R.id.refresh_view);
        timeView = (TextView) findViewById(R.id.time_view);

        refreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(END);
                mineView.refresh();
                time = 0;
            }
        });
        mineView.setSignal(new MineView.Signal() {
            @Override
            public void onStart() {
                if (mineView.getStarted()){
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
        });
    }
}
