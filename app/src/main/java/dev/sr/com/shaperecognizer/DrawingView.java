package dev.sr.com.shaperecognizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.LinkedList;

import dev.sr.com.shaperecognizer.recognizer.ShapeRecognizer;


public class DrawingView extends View implements View.OnTouchListener,ShapeRecognizer.OnShapeDetectedListener{
    private LinkedList<PointF> points;
    private Path path;
    private Paint drawPaint;

    private ShapeRecognizer shapeRecognizer;

    private TextView nameText;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        shapeRecognizer = new ShapeRecognizer(0,0);

        shapeRecognizer.setOnShapeChangedListener(this);

        initGestures();

        path = new Path();
        points = new LinkedList<PointF>();
        drawPaint = new Paint();
        drawPaint.setColor(0xFF660000);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(10);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        setOnTouchListener(this);
    }
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawPath(path, drawPaint);
    }
    //Vẽ và lưu lại các điểm trên màn hình mà người dùng chạm vào
    //Nhận dạng dựa vào tập điểm này
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        PointF p = new PointF(event.getX(),event.getY());
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                path.reset();
                while (!points.isEmpty()){
                    points.remove(0);
                }
                path.moveTo(p.x,p.y);
                break;
            case MotionEvent.ACTION_UP:
                shapeRecognizer.resolve(points);
                break;
            case MotionEvent.ACTION_MOVE:
                float oldX = points.get(points.size() - 1).x;
                float oldY = points.get(points.size() - 1).y;
                path.quadTo(oldX, oldY, (p.x + oldX) / 2, (p.y + oldY) / 2);
                break;
        }
        points.add(p);
        invalidate();
        return true;
    }
    //Định nghĩa các hình để nhận dạng
    private void initGestures(){
        //Đường thẳng dùng 2 điểm định nghĩa
        LinkedList<PointF> lineShape = new LinkedList<PointF>();
        lineShape.add(new PointF());
        lineShape.add(new PointF(0,50));
        shapeRecognizer.addGesture("Line",lineShape);

        //Chữ V cần 3 điểm
        LinkedList<PointF> reversedVShape = new LinkedList<PointF>();
        reversedVShape.add(new PointF(0, 100));
        reversedVShape.add(new PointF(50, 0));
        reversedVShape.add(new PointF(100, 100));
        shapeRecognizer.addGesture("V ngược",reversedVShape);

        LinkedList<PointF> vShape = new LinkedList<PointF>();
        vShape.add(new PointF());
        vShape.add(new PointF(50,100));
        vShape.add(new PointF(100,0));
        shapeRecognizer.addGesture("V ",vShape);

        LinkedList<PointF> zShape = new LinkedList<PointF>();
        zShape.add(new PointF());
        zShape.add(new PointF(50,0));
        zShape.add(new PointF(0,60));
        zShape.add(new PointF(50,60));
        shapeRecognizer.addGesture("Z ",zShape);

        LinkedList<PointF> zigzagShape = new LinkedList<PointF>();
        zigzagShape.add(new PointF());
        zigzagShape.add(new PointF(50, 100));
        zigzagShape.add(new PointF(100, 0));
        zigzagShape.add(new PointF(150, 100));
        zigzagShape.add(new PointF(200, 0));
        zigzagShape.add(new PointF(250, 100));
        zigzagShape.add(new PointF(300, 0));
        shapeRecognizer.addGesture("Zigzag boss",zigzagShape);



        //Hình tròn hởn
        //Vẽ gần đúng bằng 1 số hữu hạn điểm (72)
        float x = 0, y = -100;
        int totalPoints = 72;
        float step = (float)Math.PI*2/totalPoints;
        LinkedList<PointF> incompleteCircleShape = new LinkedList<PointF>();
        for(int angle = 5; angle < totalPoints-5; angle++){
            float newX = x*(float)Math.cos(angle*step)-y*(float)Math.sin(angle*step);
            float newY = y*(float)Math.cos(angle*step)+x*(float)Math.sin(angle*step);
            incompleteCircleShape.add(new PointF(newX, newY));
        }
        shapeRecognizer.addGesture("Tròn hở",incompleteCircleShape);


        //Tròn thừa
        x = 0;
        y = -100;
        LinkedList<PointF> wtfCircle = new LinkedList<PointF>();
        totalPoints = 72;
        step = (float)(Math.PI*2)/totalPoints;
        wtfCircle.add(new PointF(-50,-150));
        for(int angle = 3; angle < totalPoints - 3; angle++)
        {
            float newX = x*(float)Math.cos(angle*step)-y*(float)Math.sin(angle*step);
            float newY = y*(float)Math.cos(angle*step)+x*(float)Math.sin(angle*step);
            PointF point = new PointF(newX, newY);
            wtfCircle.add(point);
        }
        wtfCircle.add(new PointF(50,-150));
        shapeRecognizer.addGesture("TRòn thừa",wtfCircle);

        //Con boss giống chữ f
        LinkedList<PointF> fShape = new LinkedList<PointF>();
        fShape.add(new PointF(100, 100));
        fShape.add(new PointF(150, 100));
        float r = 25;
        for(int angle = 0; angle < 54; angle++){
            float alpha = angle*step;
            fShape.add(new PointF(r * (float) Math.sin(alpha) + 150, 100 + r * (float) (Math.cos(alpha) - 1)));
        }
        fShape.add(new PointF(125, 150));

        for(int angle = 0; angle < 54; angle++){
            float alpha = angle*step;
            fShape.add(new PointF(r * (float) (Math.cos(alpha)) + 100, 150 + r * (float) Math.sin(alpha)));
        }
        fShape.add(new PointF(175, 125));
        shapeRecognizer.addGesture("Boss f", fShape);

        LinkedList<PointF> fShape2 = new LinkedList<PointF>();
        fShape2.add(new PointF(100, 100));
        fShape2.add(new PointF(150, 100));
        r = 10;
        for(int angle = 0; angle < 54; angle++){
            float alpha = angle*step;
            fShape2.add(new PointF(r * (float) Math.sin(alpha) + 150, 100 + r * (float) (Math.cos(alpha) - 1)));
        }
        fShape2.add(new PointF(140, 135));

        for(int angle = 0; angle < 54; angle++){
            float alpha = angle*step;
            fShape2.add(new PointF(r * (float) (Math.cos(alpha)) + 100, 150 + r * (float) Math.sin(alpha)));
        }
        fShape2.add(new PointF(175, 125));

        shapeRecognizer.addGesture("Boss f", fShape2);

        //Con boss hình loằng ngoằng
        LinkedList<PointF> wthShape = new LinkedList<PointF>();
        wthShape.add(new PointF(50, 100));
        wthShape.add(new PointF(50, 25));
        for(int angle = 0; angle < 54; angle++){
            float alpha = angle*step;
            wthShape.add(new PointF(r * (float) Math.cos(alpha) + 25, 25 - r * (float) Math.sin(alpha)));
        }
        wthShape.add(new PointF(125, 50));
        for(int angle = 0; angle < 54; angle++){
            float alpha = angle*step;
            wthShape.add(new PointF(r * (float) Math.sin(alpha) + 125, 25 + r * (float) Math.cos(alpha)));
        }
        wthShape.add(new PointF(100, 100));
        shapeRecognizer.addGesture("Boss loằng ngoằng khác", wthShape);

        //Boss hình giống số 8
        LinkedList<PointF> numberEightShape = new LinkedList<PointF>();
        numberEightShape.add(new PointF(5,0));
        numberEightShape.add(new PointF(15,10));
        r = 20;
        for(int angle = 0; angle < 36; angle++){
            float alpha = angle*step;
            numberEightShape.add(new PointF(r * (float) (Math.sin(alpha)) + 15, 10 + r - r * (float) Math.cos(alpha)));
        }
        r=25;
        for(int angle = totalPoints; angle > 0; angle--){
            float alpha = angle*step;
            numberEightShape.add(new PointF(r * (float) (Math.sin(alpha)) + 15, 50 + r - r * (float) Math.cos(alpha)));
        }
        r = 20;
        for(int angle = 36; angle < totalPoints; angle++){
            float alpha = angle*step;
            numberEightShape.add(new PointF(r * (float) (Math.sin(alpha)) + 15, 10 + r - r * (float) Math.cos(alpha)));
        }
        numberEightShape.add(new PointF(25,0));
        shapeRecognizer.addGesture("Boss số 8",numberEightShape);

    }
    @Override
    public void shapeDetected(String name) {
        nameText.setText(name);
    }

    public void setNameText(TextView nameText) {
        this.nameText = nameText;
    }
}
