package com.example.wearable.bluetooth;

import android.support.v7.app.AppCompatActivity;

import java.util.List;

/*
 *
 * calcuate 함수는 삼변 측량공식을 함수로 구현
 * average 함수의 첫번째 파라미터는 값의 list를 표시 두번째 파라미터는 평균을 구하고자 하는 n을 표시
 *
 * */


public class Calculate extends AppCompatActivity {

    public double[] calculate(int[] P1, int[] P2, int[] P3, double d1, double d2, double d3) {

        double[] ex   = new double[2];
        double[] ey   = new double[2];
        double[] p3p1 = new double[2];
        double jval  = 0;
        double temp  = 0;
        double ival  = 0;
        double p3p1i = 0;
        double triptx;
        double tripty;
        double xval;
        double yval;
        double t1;
        double t2;
        double t3;
        double t;
        double exx;
        double d;
        double eyy;

        for (int i = 0; i < P1.length; i++) {
            t1   = P2[i];
            t2   = P1[i];
            t    = t1 - t2;
            temp += (t*t);
        }
        d = Math.sqrt(temp);
        for (int i = 0; i < P1.length; i++) {
            t1    = P2[i];
            t2    = P1[i];
            exx   = (t1 - t2)/(Math.sqrt(temp));
            ex[i] = exx;
        }
        for (int i = 0; i < P3.length; i++) {
            t1      = P3[i];
            t2      = P1[i];
            t3      = t1 - t2;
            p3p1[i] = t3;
        }
        for (int i = 0; i < ex.length; i++) {
            t1 = ex[i];
            t2 = p3p1[i];
            ival += (t1*t2);
        }
        for (int  i = 0; i < P3.length; i++) {
            t1 = P3[i];
            t2 = P1[i];
            t3 = ex[i] * ival;
            t  = t1 - t2 -t3;
            p3p1i += (t*t);
        }
        for (int i = 0; i < P3.length; i++) {
            t1 = P3[i];
            t2 = P1[i];
            t3 = ex[i] * ival;
            eyy = (t1 - t2 - t3)/Math.sqrt(p3p1i);
            ey[i] = eyy;
        }
        for (int i = 0; i < ey.length; i++) {
            t1 = ey[i];
            t2 = p3p1[i];
            jval += (t1*t2);
        }
        xval = (Math.pow(d1, 2) - Math.pow(d2, 2) + Math.pow(d, 2))/(2*d);
        yval = ((Math.pow(d1, 2) - Math.pow(d3, 2) + Math.pow(ival, 2) + Math.pow(jval, 2))/(2*jval)) - ((ival/jval)*xval);

        t1 = P1[0];
        t2 = ex[0] * xval;
        t3 = ey[0] * yval;
        triptx = (t1 + t2 + t3);

        t1 = P1[1];
        t2 = ex[1] * xval;
        t3 = ey[1] * yval;
        tripty = ( t1 + t2 + t3);


        double result[]={triptx,tripty};
        return  result;

    }

    public double getAverage(List<Double> list ,int period) {
        double sum = 0;
        double average = 0;
        int n = list.size();
        for (int i = 1; i< period+1; i++) {
            sum += list.get(n-i);
        }
        average = sum/(period*1.0);
        return average;
    }

    public int getrssimean(List<Integer> list,int period) {
        int sum = 0;
        int average = 0;
        int n = list.size();
        for (int i = 1; i< period+1; i++) {
            sum += list.get(n-i);
        }
        average = sum/(period);
        return average;
    }

    public double getDistance (int rssi, int txpower) {

        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txpower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }

    }

}