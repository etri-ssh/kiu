package com.example.ccp.map_module.model;

public class JLine implements Comparable<JLine>{
    private double start_x;
    private double start_y;

    private double end_x;
    private double end_y;

    public JLine(double x,double y, double x1, double y1) {
        start_x = x;
        start_y = y;
        end_x = x1;
        end_y = y1;
    }

    public double getStart_x() {
        return start_x;
    }

    public void setStart_x(double start_x) {
        this.start_x = start_x;
    }

    public double getStart_y() {
        return start_y;
    }

    public void setStart_y(double start_y) {
        this.start_y = start_y;
    }

    public double getEnd_x() {
        return end_x;
    }

    public void setEnd_x(double end_x) {
        this.end_x = end_x;
    }

    public double getEnd_y() {
        return end_y;
    }

    public void setEnd_y(double end_y) {
        this.end_y = end_y;
    }
    @Override
    public int compareTo(JLine jLine) {
        if((this.start_x==jLine.start_x)
                &&(this.start_y==jLine.start_y)
                &&(this.end_x==jLine.end_x)
                &&(this.end_y==jLine.end_y)
        ) { return 0; //같음
        }else return -1;
    }

    @Override
    public String toString() {
        return "JLine{" +
                "start_x=" + start_x +
                ", start_y=" + start_y +
                ", end_x=" + end_x +
                ", end_y=" + end_y +
                '}';
    }
}
