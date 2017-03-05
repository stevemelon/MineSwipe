package com.example.fanghao01.cccl;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Created by fanghao01 on 17/3/1.
 */

public class MineView extends View {

    private static final String TAG = "MineView";
    private Context mContext;

    private int mapRow = 30;
    private int mapCow = 16;

    private int tileWidth = 100;
    private float mTouchSlop = 10;//当手指拖动值大于TouchSlop值时，认为应该进行了move操作

    int mapWidth = mapCow * tileWidth;//地图实际宽度
    int mapHeight = mapRow * tileWidth;//地图实际高度

    private int[][] adjoin = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {-1, -1}, {-1, 1}, {1, 1}, {1, -1}};//周围8个方块
    private int[][] fourAdjoin = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};
    private int[][] fourCorner = {{-1, 1}, {1, 1}, {-1, -1}, {1, -1}};//下左 下右 上左 上右
    private Mine[][] mines = new Mine[mapRow][mapCow];

    private List<Point> points = new ArrayList<>();//是雷的点
    private List<Point> pointsRemain = new ArrayList<>();

    private Signal signal;

    private int minesNum = 99/*mapCow * mapRow /6*/;

    private Paint bmpPaint;
    private Paint minePaint;
    private Paint mathPaint;
    private Paint blankPaint;
    private Bitmap flagBitmap;

    private Boolean isStarted = false;
    private Boolean isFinished = false;

    private Boolean flagSwitch = false;


    public MineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        //提前初始化，否则每次加载会非常卡顿！
        flagBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flag);

        bmpPaint = new Paint();
        bmpPaint.setAntiAlias(true);
        bmpPaint.setColor(Color.WHITE);
        bmpPaint.setStrokeWidth(3);

        //画雷
        minePaint = new Paint();
        minePaint.setColor(Color.BLACK);
        minePaint.setStyle(Paint.Style.FILL);

        //数字 paint
        mathPaint = new Paint(Paint.DEV_KERN_TEXT_FLAG);
        mathPaint.setColor(Color.BLUE);
        mathPaint.setAntiAlias(true);
        mathPaint.setTextSize(60f);
        mathPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mathPaint.setTextAlign(Paint.Align.CENTER);

        //空白
        blankPaint = new Paint();
        blankPaint.setColor(Color.GRAY);
        blankPaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCow; j++) {
                Mine mine = new Mine(0, false, false);
                mines[i][j] = mine;

                Point point = new Point(i, j);
                points.add(point);
            }
        }
        if (signal != null) {
            signal.onFlag(minesNum);
        }
        generateTile(null);

    }

    public Boolean getStarted() {
        return isStarted;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    public void setFlagSwitch(Boolean flagSwitch) {
        this.flagSwitch = flagSwitch;
    }

    public void refresh(Point except) {
        if (!isStarted && !isFinished) {
            return;
        }
        isStarted = false;
        isFinished = false;
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCow; j++) {
                mines[i][j].setOpen(false);
                mines[i][j].setFlag(false);
                mines[i][j].value = 0;
            }
        }
        generateTile(except);
        invalidate();
        if (signal != null) {
            signal.onFinish();
            signal.onStart();
        }
    }

    private void generateTile(Point except) {
        generateMine(except);
        generateNum();
    }

    //生成雷
    private void generateMine(Point except) {
        if (pointsRemain != null) {
            points.addAll(pointsRemain);
            pointsRemain.clear();
        }

        for (int i = 0; i < minesNum; i++) {
            Random random = new Random();
            Point point = points.get(random.nextInt(points.size()));

            if (except != null && except.getX() == point.getX() && except.getY() == point.getY()) {
                points.remove(point);
                pointsRemain.add(point);
                continue;
            }
            mines[point.getX()][point.getY()].setValue(-1);

            pointsRemain.add(point);
            points.remove(point);
        }
    }

    /***
     * 生成数字
     */
    private void generateNum() {
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCow; j++) {
                if (mines[i][j].value == -1) {
                    continue;
                }
                int sum = 0;
                for (int k = 0; k < adjoin.length; k++) {
                    if (checkPoint(i + adjoin[k][0], j + adjoin[k][1])) {//越界访问
                        if (mines[i + adjoin[k][0]][j + adjoin[k][1]].value == -1) {
                            sum++;
                        }
                    }
                }
                mines[i][j].value = sum;
            }
        }
    }

    float startX;
    float startY;
    int NumDownX, NumDownY;
    int NumUpX, NumUpY;
    boolean isMoved;//是否移动了
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isMoved = false;

                if (isFinished) {
                    break;
                }
                startX = event.getX();
                startY = event.getY();

                float x = event.getX() + getScrollX();
                float y = event.getY() + getScrollY();

                NumDownX = (int) (y / tileWidth);//行号
                NumDownY = (int) (x / tileWidth);//列号
                break;
            case MotionEvent.ACTION_UP:
                if (isMoved) {
                    break;
                }
                x = event.getX() + getScrollX();
                y = event.getY() + getScrollY();

                NumUpX = (int) (y / tileWidth);//行号
                NumUpY = (int) (x / tileWidth);//列号
                if (NumDownX != NumUpX || NumDownY != NumDownY || !checkPoint(NumUpX, NumUpY)){
                    break;
                }
                if (!isStarted && mines[NumUpX][NumUpY].value == -1) {
                    Point point = new Point(NumUpX, NumUpY);

                    isStarted = true;
                    signal.onStart();

                    refresh(point);
                }

                if (signal != null && !isStarted) {
                    isStarted = true;
                    signal.onStart();
                }

                if (checkPoint(NumUpX, NumUpY)) {

                    if (mines[NumUpX][NumUpY].value != -1 && mines[NumUpX][NumUpY].isOpen()
                            && !mines[NumUpX][NumUpY].isFlag()) {//在已经出现的数字或者空白上点击
                        int sum = 0;
                        for (int k = 0; k < adjoin.length; k++) {
                            int pointX = NumUpX + adjoin[k][0];
                            int pointY = NumUpY + adjoin[k][1];
                            if (checkPoint(pointX, pointY)) {
                                if (mines[pointX][pointY].isFlag()) {
                                    sum++;
                                }
                            }
                        }
                        if (sum == mines[NumUpX][NumUpY].value) {
                            for (int k = 0; k < adjoin.length; k++) {
                                int pointX = NumUpX + adjoin[k][0];
                                int pointY = NumUpY + adjoin[k][1];
                                if (checkPoint(pointX, pointY)) {
                                    if (mines[pointX][pointY].value == -1 && !mines[pointX][pointY].flag) {
                                        preFinish();
                                        showFailure();
                                        break;
                                    }
                                    open(new Point(pointX, pointY));
                                }
                            }
                            invalidate();
                        }
                    }
                    if (!flagSwitch) {//扫雷模式
                        if (mines[NumUpX][NumUpY].flag){
                            break;
                        }
                        if (mines[NumUpX][NumUpY].value != -1) {
                            open(new Point(NumUpX, NumUpY));
                        } else {//碰到雷 gg 了
                            preFinish();
                            showFailure();
                        }
                    } else {//插旗模式
                        if (!mines[NumUpX][NumUpY].isOpen()) {
                            mines[NumUpX][NumUpY].setFlag(!mines[NumUpX][NumUpY].isFlag());
                        }
                    }
                    invalidate();

                }
                break;

            case MotionEvent.ACTION_MOVE:
                Log.i("fanghao ", "onTouchEventX: " + getScrollX());
                Log.i("fanghao ", "startX: " + event.getX());
                Log.i("fanghao ", "onTouchEventY: " + getScrollY());
                Log.i("fanghao ", "startY: " + event.getY());
                if (Math.abs(event.getX() - startX) >= mTouchSlop || Math.abs(event.getY() - startY) >= mTouchSlop) {
                    isMoved = true;
                }

                float futureX = getScrollX() + startX - event.getX();
                float futureY = getScrollY() + startY - event.getY();
                if (futureX < -10) {
                    futureX = -10;
                }
                if (mapWidth < getWidth()) {
                    futureX = 0;
                } else if (futureX > mapWidth - getWidth() + 10) {
                    futureX = mapWidth - getWidth() + 10;
                }
                if (futureY < -10) {
                    futureY = -10;
                }
                if (mapHeight < getHeight()) {
                    futureY = 0;
                } else if (futureY > mapHeight - getHeight() + 10) {
                    futureY = mapHeight - getHeight() + 10;
                }
                scrollTo((int) futureX, (int) futureY);
                startX = event.getX();
                startY = event.getY();

                break;
        }
        return true;
    }

    private void preFinish() {
        isFinished = true;
        isStarted = false;
        if (signal != null) {
            signal.onFinish();
        }
    }

    private boolean checkPoint(int numX, int numY) {
        return numX < mapRow && numX >= 0 && numY < mapCow && numY >= 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG + "fanghao", "onDraw: ");
        int width = mapCow * tileWidth;
        int height = mapRow * tileWidth;
        int sum = 0;
        int flagSum = 0;//标记的类数量
        //画地图
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCow; j++) {
                if (isFinished) {
                    isStarted = false;
                    if (signal != null) {
                        signal.onFinish();
                    }
                    drawShow(canvas, i, j);
                } else {
                    drawShow(canvas, i, j);
                    if (mines[i][j].isOpen() && mines[i][j].value != -1) {//如果没有打开
                        sum++;
                    }
                }
                if (mines[i][j].isFlag()) {
                    flagSum++;
                }
            }
        }
        if (signal != null) {
            signal.onFlag(minesNum - flagSum);
        }
        if (sum + minesNum == mapRow * mapCow) {
            preFinish();
            showSuccess();
        }
        //划线
        for (int x = 0; x <= mapCow; x++) {
            canvas.drawLine(x * tileWidth, 0, x * tileWidth, height, bmpPaint);
        }
        for (int i = 0; i <= mapRow; i++) {
            canvas.drawLine(0, i * tileWidth, width, i * tileWidth, bmpPaint);
        }

    }

    private void drawBlank(Canvas canvas, int i, int j, Paint blankPaint) {
        canvas.drawRect(j * tileWidth, i * tileWidth,
                (j + 1) * tileWidth,
                (i + 1) * tileWidth, blankPaint);
    }

    private void drawFlag(Canvas canvas, int i, int j) {
        RectF rectF = new RectF(j * tileWidth, i * tileWidth,
                (j + 1) * tileWidth,
                (i + 1) * tileWidth);
        canvas.drawBitmap(flagBitmap, null, rectF, null);
    }

    /***
     * @param canvas
     * @param i      行号
     * @param j      列号
     */
    private void drawShow(Canvas canvas, int i, int j) {
        if (mines[i][j].isOpen() && !mines[i][j].isFlag()) {
            if (mines[i][j].value == -1) {//画雷
                canvas.drawRect(j * tileWidth, i * tileWidth,
                        (j + 1) * tileWidth,
                        (i + 1) * tileWidth, minePaint);
            } else {//画数字
                int value = mines[i][j].value;
                if (value == 0) {
                    canvas.drawText("", (float) ((j + 0.5) * tileWidth), (float) ((i + 0.75) * tileWidth), mathPaint);
                } else {
                    canvas.drawText(value + "", (float) ((j + 0.5) * tileWidth), (float) ((i + 0.75) * tileWidth), mathPaint);
                }

            }

        } else if (mines[i][j].isFlag()) {
            drawFlag(canvas, i, j);
        } else {
            drawBlank(canvas, i, j, blankPaint);
        }

    }

    /***
     * @param point   位置 自动展开算法
     */
    public void open(Point point) {
        if (!mines[point.getX()][point.getY()].isFlag()) {
            mines[point.getX()][point.getY()].setOpen(true);
        }
        if (mines[point.getX()][point.getY()].value != 0) {
            return;
        }
        Queue<Point> pointQueue = new LinkedList<>();
        pointQueue.add(point);
        while (pointQueue.size() != 0) {
            Point po = pointQueue.poll();
            Boolean[] fourAdjoinShow = {false, false, false, false};//下上左右
            for (int i = 0; i < fourAdjoin.length; i++) {
                int x = po.getX() + fourAdjoin[i][0];
                int y = po.getY() + fourAdjoin[i][1];
                if (checkPoint(x, y) && mines[x][y].value != -1  && !mines[x][y].isFlag()) {
                    if (mines[x][y].value > 0) {
                        fourAdjoinShow[i] = true;
                    }else if (mines[x][y].value == 0 && !mines[x][y].isOpen()) {
                        pointQueue.add(new Point(x, y));
                    }
                    mines[x][y].setOpen(true);

                }
            }
            //下左 下右 上左 上右
            Boolean[] fourConerShow = {fourAdjoinShow[0] && fourAdjoinShow[2],
                    fourAdjoinShow[0] && fourAdjoinShow[3],fourAdjoinShow[1] && fourAdjoinShow[2],
                    fourAdjoinShow[1] && fourAdjoinShow[3]};
            for (int i = 0; i < fourCorner.length; i++) {
                int x = po.getX() + fourCorner[i][0];
                int y = po.getY() + fourCorner[i][1];
                if (fourConerShow[i] && mines[x][y].value >= 0 && !mines[x][y].isOpen() && !mines[x][y].isFlag()) {
                    mines[x][y].setOpen(true);
                    if (mines[x][y].value == 0) {
                        pointQueue.add(new Point(x, y));
                    }
                }
            }
        }
    }

    private void showFailure() {
        new AlertDialog.Builder(mContext)
                .setMessage("恭喜你，爆炸了")
                .setCancelable(false)
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    private void showSuccess() {
        new AlertDialog.Builder(mContext)
                .setMessage("恭喜你，你找出了所有雷")
                .setCancelable(false)
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    interface Signal {

        void onStart();

        void onFinish();

        void onFlag(int num);
    }
}
