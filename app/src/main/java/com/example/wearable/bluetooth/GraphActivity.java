package com.example.wearable.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/*
 *
 * 0.7초 간격으로 스캔된 비콘의 거리 계산
 * 총 100회 표시
 *
 * */

public class GraphActivity extends Calculate implements BeaconConsumer {
    private static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 100;
    BluetoothAdapter mBluetoothAdapter;
    BeaconAdapter beaconAdapter;
    BeaconManager mBeaconManager;
    Vector<Item> items;
    int num=0;
    String mac;
    TextView tv_rssi,tv_distance,tv_txpower,tv_name;
    TextView tv_average;
    GraphView graph_distance;
    int speed = 7;
    int period = 10;
    final LineGraphSeries<DataPoint> series_d = new LineGraphSeries<>();
    static int i=0;
    List<Integer> rssi_list = new ArrayList<Integer>();
    List<Double> distance_list = new ArrayList<Double>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        tv_name = (TextView) findViewById(R.id.name);
        tv_rssi = (TextView) findViewById(R.id.rssi);
        tv_distance = (TextView) findViewById(R.id.distance);
        tv_txpower = (TextView) findViewById(R.id.txpower);
        graph_distance = (GraphView) findViewById(R.id.graph_distance);
        tv_average = (TextView) findViewById(R.id.average);



        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBeaconManager = BeaconManager.getInstanceForApplication(this);
            mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));
        }
        try {
            mBeaconManager.setForegroundScanPeriod(speed*100l);
            mBeaconManager.setForegroundBetweenScanPeriod(0l);
            mBeaconManager.updateScanPeriods();
        }
        catch (RemoteException e) { }
        mBeaconManager.bind(GraphActivity.this);
        Intent intent = getIntent();
        mac = intent.getStringExtra("mac");
        tv_name.setText(mac);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconManager.unbind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            mBeaconManager = BeaconManager.getInstanceForApplication(this);
            mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));
        }

    }

    public void onBeaconServiceConnect() {
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Iterator<Beacon> iterator = beacons.iterator();
                    items = new Vector<>();
                    while (iterator.hasNext()) {
                        Beacon beacon = iterator.next();
                        String address = beacon.getBluetoothAddress();
                        String name = beacon.getBluetoothName();
                        int rssi = beacon.getRssi();
                        int txPower = beacon.getTxPower();
                        double distance = Double.parseDouble(decimalFormat.format(getDistance(rssi,txPower)));
                        items.add(new Item(address, rssi, txPower, distance,name));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            double mean=0;
                            int mean_rssi=0;
                            int N = items.size();
                            for(int i=0;i<N;i++) {
                                if (items.get(i).getAddress().equals(mac))
                                    num = i;}
                            rssi_list.add(items.get(num).getRssi());
                            distance_list.add(items.get(num).getDistance());
                            if((rssi_list.size()>=(period)&&(rssi_list.size()%(period)==0))) {
                                mean_rssi = getrssimean(rssi_list,(period));
                                mean = getAverage(distance_list,(period));
                                tv_average.setText("Average : "+decimalFormat.format(mean_rssi)+ "db / "+decimalFormat.format(mean)+"m");
                            }

                            if(rssi_list.size()>period*10){
                                rssi_list.clear();
                                distance_list.clear();
                            }


                            double d = items.get(num).getDistance();
                            tv_rssi.setText(items.get(num).getRssi()+"dB");
                            tv_txpower.setText(items.get(num).getTxPower()+"dB");
                            tv_distance.setText(items.get(num).getDistance()+"m");

                            boolean startAhead = false;
                            graph_distance.addSeries(series_d);
                            graph_distance.getViewport().setXAxisBoundsManual(true);
                            graph_distance.getViewport().setMinX(0);
                            graph_distance.getViewport().setMaxX(period*10);
                            graph_distance.getViewport().setYAxisBoundsManual(true);
                            graph_distance.getViewport().setMaxY(4);
                            graph_distance.getViewport().setMinY(0);
                            graph_distance.setTitle("Distance/time");
                            series_d.appendData(new DataPoint(i,d), false, period*20);
                            i++;

                            beaconAdapter = new BeaconAdapter(items, GraphActivity.this);
                            beaconAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });


        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) { }

            @Override
            public void didExitRegion(Region region) { }

            @Override
            public void didDetermineStateForRegion(int state, Region region) { }
        });
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}
