package com.example.ccp.map_module;

import com.example.ccp.map_module.model.JLine;
import com.example.ccp.map_module.model.Point;

import java.util.ArrayList;
import java.util.List;

public class MM {
    private double x1; // 직선 start point x
    private double y1; // 직선 start point y
    private double x2; // 직선 end point x
    private double y2; // 직선 end point y
    private double x4; // 보정 x
    private double y4; // 보정 y
    private double a; //직선의 기울기
    private double b; //직선의 상수
    private double a2; //직선2의 기울기
    private double b2; //직선2의 상수

    private ArrayList<JLine> lines = new ArrayList<JLine>(); //설정된 경로들
    private ArrayList<Integer> location_lines = new ArrayList<Integer>();//각 경로들의 제일 가까운 위치 저장 ( start:0 end:1 )
    private ArrayList<Double> calc_lines = new ArrayList<Double>(); //각 경로들의 제일 가까운 거리 저장

    private JLine currentLine = null; // 현재 경로
    private Point currentPoint = null; //현재 맵매칭 좌표

    private final Object sync = new Object();

    public double getX() {	return x4; }
    public double getY() {	return y4; }
    public void clearLine() { lines.clear(); }
    public void inputLine(JLine line) {	lines.add(line); } //경로 데이터 입력
    public void inputLineAll(List<JLine> list) { synchronized(sync) { lines.addAll(list); } } // 경로 데이터 전부 입력
    public void resetData() { lines.clear(); currentLine = null; currentPoint = null; }
    public boolean isChecked() { return !lines.isEmpty(); }
    private void closeStart(double x, double y) { //제일 가까운 입구 구하기
        location_lines.clear();
        calc_lines.clear();

        for(JLine l : lines) { // start point와 end point에 대한 거리 비교 후 각 arrayList에 add
            double d1 = Math.sqrt(Math.pow((l.getStart_x() - x),2) + Math.pow((l.getStart_y() - y), 2));
            double d2 = Math.sqrt(Math.pow((l.getEnd_x() - x),2) + Math.pow((l.getEnd_y() - y), 2));

            double min_d = Math.min(d1, d2); //두 점 중 작은 수 판별

            if(min_d == d1) {
                location_lines.add(0);
                calc_lines.add(min_d);
            }else {
                location_lines.add(1);
                calc_lines.add(min_d);
            }
        }
        double min_calc = calc_lines.get(0);
        int line_index = 0;
//        int line_check = -1;

        for(int i=0; i < calc_lines.size(); i++) {
            double d = calc_lines.get(i);
            if(min_calc > d) {
                min_calc = d;
                line_index = i;
//                line_check = location_lines.get(line_index);
            }
        }
        //현재 진입하는 경로 저장
        currentLine = lines.get(line_index);

    }
    private JLine closeLine(double x, double y) {//경로간 최단거리값 중 제일 가까운 직선 구하기
        double min_d = 0.0;
        int index = 0;

        for(int i = 0;i<lines.size();i++) {
            double result_d = minStraight(x,y,lines.get(i));
            if(i<1) {
                min_d = result_d;
            }else if(min_d > result_d) {
                min_d = result_d;
                index = i;
            }
        }
        return lines.get(index);
    }
    private double minStraight(double x, double y,JLine line) {//직선과 좌표 사이 최단거리
        double d;

        double x1 = line.getStart_x();
        double y1 = line.getStart_y();
        double x2 = line.getEnd_x();
        double y2 = line.getEnd_y();

        double a;
        double b;
        double c;

        double m;
        double n;

        if((x2-x1)==0) {//직선이 y축과 평행
            a = 1.0;
            b = 0.0;
            c = -x1;
        }else if((y2-y1)==0) {
            a = 0.0;
            b = 1.0;
            c = -y1;
        }else {
            a = ( y2 - y1 ) / (x2 - x1); //직선의 기울기 구하기
            b = y1 - ( a * x1 ); // 직선의 기울기에 좌표값 대입하여 직선의 상수 구하기
            c = -(a*x1+b*y1);
        }
        m = a*x + b*y + c;
        if(m<0) m = m*-1;

        n = Math.sqrt(Math.pow(a,2) + Math.pow(b, 2));

        d = m / n;
        return d;
    }

    private void pointLogic(double x, double y) {//경로에 수직으로 내리는 좌표 값
        this.x1 = currentLine.getStart_x(); //현재 경로에 대한 start / end point 불러오기
        this.y1 = currentLine.getStart_y();
        this.x2 = currentLine.getEnd_x();
        this.y2 = currentLine.getEnd_y();

        if((x2-x1)==0) {//직선이 y축과 평행할때 : a = 0; b = 0; a2 = 0; b2 = 0;
            x4 = x1;
            y4 = y;
        }else if((y2-y1)==0) {//직선이 x축과 평행할때 : a = 0; b = 0; a2 = 0; b2 = 0;
            x4 = x;
            y4 = y1;
        }else {
            a = ( y2 - y1 ) / (x2 - x1); //직선의 기울기 구하기
            b = y1 - ( a * x1 ); // 직선의 기울기에 좌표값 대입하여 직선의 상수 구하기
            a2 =  -1 / a ; //직선 간의 수직이라는 가정하에 직선2의 기울기 구하기
            b2 = y - (a2 * x ); //수직한 직선의 방정식에 GPS 값 입력하여 직선2의 상수 구하기
            x4 = ( b - b2 ) / ( a2 - a ); //직선과 직선2의 연립방정식으로 교차하는 x4 구하기
            y4 = ( a * x4 ) + b; //교차점이기에 어느 식이던 값을 대입했을때 y4값을 구할 수 있다.
        }
        //보정된 x y값을 직전 좌표로 설정
        Point point = new Point(x4,y4);

        currentPoint = point;
    }

    public Point start(double x, double y) { //간이 맵매칭 경우의 수가 드럽게 많음 애시당초 자바에서 경우의 수를 계산하지않나봄
        synchronized(sync) {
            if(currentLine==null) { //진입 중
                closeStart(x,y); //진입 하는 시점에서 제일 가까운 경로를 현재 움직일 경로로 저장
                pointLogic(x,y);
            }else {
                currentLine = closeLine(x,y);
                pointLogic(x,y);
            }
            return currentPoint;
        }
    }
    public Point start2(double x, double y){ //입구 찾기 X 버전
        synchronized(sync) {
            currentLine = closeLine(x,y);
            pointLogic(x,y);
            return currentPoint;
        }
    }
    public void testLogic(){
        /*
        CASE 1
        입구로 진입하기 전 건물 벽면 경로를 지나게 될 경우
        실내로 진입하기도 전에 해당 경로로 좌표가 찍히게 될것

        CASE 2
        엘리베이터 사용 후 다른 층에 내렸을 때는 입구가 아닌
        경로 중간에 좌표가 찍혀야함
        * 입구를 찾는 Start point 와 End Point로 가까운 경로 찾는 로직 사용 X
         */


        /*
        0817 테스트로직
        1. 실내로 들어왔다는 기준으로 경로 데이터 세팅
        2. 현재 위치에서 부터 제일 가까운 경로 구하기
        3. 가까운 경로로 최단 위치 좌표 리턴
        4.

        1. 건물에 진입시 각 경로의 start point , end point로 범위 안에 들어왔는지 체크
         */
    }

}
