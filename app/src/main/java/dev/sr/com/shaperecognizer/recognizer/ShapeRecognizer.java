package dev.sr.com.shaperecognizer.recognizer;


import android.graphics.PointF;
import android.util.Log;

import java.util.LinkedList;

public class ShapeRecognizer {
    private static final String TAG = "ShapeRecognizer";
    private int nPoints = 64;
    private int viewWidth, viewHeight;
    private LinkedList<Gesture> gestures;
    private OnShapeDetectedListener listener;
    public ShapeRecognizer(int viewWidth, int viewHeight){
        this.viewHeight = viewHeight;
        this.viewWidth = viewWidth;
        gestures = new LinkedList<>();
    }
    //Thêm định nghĩa hình dạng
    public void addGesture(String name, LinkedList<PointF> points){
        LinkedList<PointF> pointsCopied = new LinkedList<>(points);
        LinkedList<PointF> invertedPoints = new LinkedList<>();
        for(PointF p: pointsCopied){
            invertedPoints.push(p);
        }
        Gesture invertedGesture = new Gesture(name,vectorize(resample(invertedPoints)));
        Gesture gesture = new Gesture(name,vectorize(resample(pointsCopied)));
        gestures.add(gesture);
        gestures.add(invertedGesture);
    }
    //Tìm tập điểm giống nhất với tập vào
    public void resolve(LinkedList<PointF> points){
        String match = "none";
        if(points.size() > 1){
            LinkedList<PointF> map = resample(points);
            LinkedList<Float> ivect = vectorize(map);
            float maxScore = 0;
            for(int i = 0; i < gestures.size(); i++){
                float distance = optCosDist(gestures.get(i).getMap(),ivect);
                float score = 1/distance;
                Log.w(TAG,gestures.get(i).getName()+" "+score);
                if(score > maxScore){
                    maxScore = score;
                    match = gestures.get(i).getName();
                }
            }
        }
        if(listener != null){
            listener.shapeDetected(match);
        }
    }


    private float distance(PointF u, PointF v){
        float x = u.x - v.x;
        float y = u.y - v.y;
        return (float)Math.sqrt(x*x + y*y);
    }

    private float pathLength(LinkedList<PointF> points){
        float distance = 0;
        for(int i = 1; i < points.size(); i++){
            distance += distance(points.get( i - 1),points.get(i));
        }
        return distance;
    }
    //Lấy mẫu một số điểm từ input
    private LinkedList<PointF> resample(LinkedList<PointF> points){
        float subLength = pathLength(points)/(nPoints - 1);
        float distance= 0;
        LinkedList<PointF> newPoints = new LinkedList<PointF>();
        PointF elem = new PointF(points.get(0).x,points.get(0).y);
        newPoints.add(elem);
        int i = 1;
        while (i < points.size() && newPoints.size() < nPoints - 1){
            float subDistance = distance(points.get( i -1),points.get(i));
            if(distance + subDistance >= subLength){
                PointF elem2 = new PointF();
                elem2.x = points.get(i - 1).x + ((subLength - distance)/subDistance)*(points.get(i).x - points.get(i-1).x);
                elem2.y = points.get(i - 1).y + ((subLength - distance)/subDistance)*(points.get(i).y - points.get(i-1).y);
                newPoints.add(elem2);
                points.add(i,elem2);
                distance = 0;
            }else{
                distance += subDistance;
            }
            i++;
        }
        PointF elem3 = new PointF();
        elem3.x = points.getLast().x;
        elem3.y = points.getLast().y;
        newPoints.add(elem3);
        return newPoints;
    }

    //Tìm trọng tâm của tập điểm
    private PointF findCenter(LinkedList<PointF> points){
        PointF center = new PointF(0,0);
        for(int i =0; i < points.size();i++){
            center.x += points.get(i).x;
            center.y += points.get(i).y;
        }
        center.x = center.x/ (points.size()-1);
        center.y = center.y/ (points.size() - 1);
        return center;
    }
    //Chuẩn hóa vị trí các điểm
    //Băng cách tịnh tiến theo trọng tâm vừa tìm được
    //Sao cho cuối cùng trọng tâm mới sẽ là điểm (0,0)
    private LinkedList<PointF> translate (LinkedList<PointF> points, PointF center){
        for(int i = 0; i < points.size();i++){
            points.get(i).x = points.get(i).x -  center.x;
            points.get(i).y = points.get(i).y - center.y;
        }
        return points;
    }

    //Chuyển dãy điểm về dãy trọng số

    private LinkedList<Float> vectorize(LinkedList<PointF> points){
        LinkedList<Float> vector = new LinkedList<Float>();
        //Tìm trọng tâm
        PointF center = findCenter(points);
        //Chuẩn hóa về (0,0)
        translate(points,center);
        //Tính góc so với trọng tâm (bây giờ là (0,0))
        float delta = (float)Math.atan2(points.get(1).x,points.get(1).y);
        float sum = 0;
        for(int i = 0; i < points.size(); i++){
            //Không hiểu nó làm gì nhưng có vẻ chạy đúng
            float newX = (float)(points.get(i) .x*Math.cos(delta) - points.get(i).y*Math.sin(delta));
            float newY = (float)(points.get(i).x*Math.sin(delta) + points.get(i).y*Math.cos(delta));
            vector.add(newX);
            vector.add(newY);
            sum = sum + newX*newX + newY*newY;
        }
        float magnitude = (float)Math.sqrt(sum);
        LinkedList<Float> normalizedVector = new LinkedList<Float>();
        for (float f: vector){
            normalizedVector.add(f/magnitude);
        }
        return normalizedVector;
    }

    //Thuật toán đánh giá độ tương đống của 2 dãy trọng số
    private float optCosDist(LinkedList<Float> gestureV, LinkedList<Float> inputV){
        float a = 0, b = 0;
        try{
            for(int i = 0; i < gestureV.size(); i+=2){
                b += gestureV.get(i)*inputV.get(i + 1) - gestureV.get(i + 1)*inputV.get(i);
                a += gestureV.get(i)*inputV.get(i) + gestureV.get(i + 1)*inputV.get(i + 1);
            }
        }catch (IndexOutOfBoundsException iobe){
            Log.w(TAG,"Gesture V problem");
        }
        float angle = (float)Math.atan2(b,a);
        return (float)Math.acos(a*Math.cos(angle)+ b*Math.sin(angle));
    }
    public interface OnShapeDetectedListener{
        void shapeDetected(String name);
    }

    public void setOnShapeChangedListener(OnShapeDetectedListener listener) {
        this.listener = listener;
    }

}


