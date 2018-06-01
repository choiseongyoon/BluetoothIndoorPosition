package com.example.wearable.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.distance.AndroidModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/*
 * 2m 간격으로 비콘 8개 설치
 * 0.6초 간격으로 rssi값이 큰 3개의 비콘을 스캔
 * 스캔된 비콘3개를 삼변측량 공식에 대입
 * 이렇게 구한 현재 좌표의 3번의 값을 평균
 * 1.8초 간격으로 현재 위치 update
 *
 * Firebase에 user1/user2 같이 각각의 user 폴더에 저장
 * 하위 폴더에는 년-월-일을 저장하고 그 아래 폴더에 시:분:초로 세분화
 *
 * 2m를 200 좌표로 표시
 * 화면 표시를 위해 2배르 곱하고 x값에는 400을 더하고 y값에는 100을 더함
 *
 * */

public class InActivity extends Calculate implements BeaconConsumer,View.OnClickListener {
    int speed = 6;
    int period = 3;
    long Time = System.currentTimeMillis();
    String day_s ;
    String time_s;
    SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timetime = new SimpleDateFormat("HH:mm:ss");


    ImageView iv_position;
    ImageView iv_candy1,iv_candy3,iv_beetroot1,iv_beetroot2,iv_beetroot3,iv_lemon1,iv_lemon2,iv_lemon3;

    int x_initial_postion = 400;
    int y_initial_postion = 100;
    double ratio =2;
    double triptx;
    double tripty;

    int r_candy1[]= {0, 0};
    int r_candy3[]= {200, 0};

    int r_lemon1[]= {0, 200};
    int r_lemon2[]= {200, 200};
    int r_lemon3[]= {0, 400};

    int r_beetroot1[]= {200, 400};
    int r_beetroot2[]= {0, 600};
    int r_beetroot3[]= {200, 600};

    int p[]={0,0,0,0,0,0};

    String name[]={" "," "," "};


    double x_distance = 0;
    double y_distance = 0;

    String candy1 = "DC:14:7B:CF:B4:B1";
    String candy2 = "F3:E2:3C:5F:77:14";
    String candy3 = "E9:6F:9C:B7:B0:C7";

    String beetroot1 = "E7:4B:95:B1:41:25";
    String beetroot2 = "FF:8D:2E:22:37:6F";
    String beetroot3 = "F1:70:66:F2:7E:CD";

    String lemon1 = "EE:CB:CC:05:B1:5E";
    String lemon2 = "DE:DD:80:81:1C:F1";
    String lemon3 = "E5:C1:4A:63:B5:7F";

    private static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private static final String TAG = "InActivity";

    private static final int REQUEST_ENABLE_BT = 100;

    BluetoothAdapter mBluetoothAdapter;

    BeaconAdapter beaconAdapter;

    BeaconManager mBeaconManager;

    Vector<Item> items;


    TextView position,a_info,b_info,c_info;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in);
        initUI();

        AndroidModel am = AndroidModel.forThisDevice();
        Log.d("getManufacturer()",am.getManufacturer());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBeaconManager = BeaconManager.getInstanceForApplication(this);
            mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));
        }

        try {
            mBeaconManager.setForegroundScanPeriod(speed * 100l); // 100 mS
            mBeaconManager.setForegroundBetweenScanPeriod(0l); // 0ms
            mBeaconManager.updateScanPeriods();
        }
        catch (RemoteException e) {
            Log.e(TAG, "Cannot talk to service");
        }

        mBeaconManager.bind(InActivity.this);


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

    private void initUI() {

        position= (TextView) findViewById(R.id.position);

        a_info= (TextView) findViewById(R.id.a_info);
        b_info= (TextView) findViewById(R.id.b_info);
        c_info= (TextView) findViewById(R.id.c_info);

        iv_lemon1=(ImageView)findViewById(R.id.lemon1);
        iv_lemon2=(ImageView)findViewById(R.id.lemon2);
        iv_lemon3=(ImageView)findViewById(R.id.lemon3);

        iv_candy1=(ImageView)findViewById(R.id.candy1);
        iv_candy3=(ImageView)findViewById(R.id.candy3);

        iv_beetroot1=(ImageView)findViewById(R.id.beetroot1);
        iv_beetroot2=(ImageView)findViewById(R.id.beetroot2);
        iv_beetroot3=(ImageView)findViewById(R.id.beetroot3);

        iv_beetroot1.setX((float) (ratio*r_beetroot1[0]+x_initial_postion));
        iv_beetroot1.setY((float) (ratio*r_beetroot1[1]+y_initial_postion));
        iv_beetroot2.setX((float) (ratio*r_beetroot2[0]+x_initial_postion));
        iv_beetroot2.setY((float) (ratio*r_beetroot2[1]+y_initial_postion));
        iv_beetroot3.setX((float) (ratio*r_beetroot3[0]+x_initial_postion));
        iv_beetroot3.setY((float) (ratio*r_beetroot3[1]+y_initial_postion));

        iv_candy1.setX((float) (ratio*r_candy1[0]+x_initial_postion));
        iv_candy1.setY((float) (ratio*r_candy1[1]+y_initial_postion));

        iv_candy3.setX((float) (ratio*r_candy3[0]+x_initial_postion));
        iv_candy3.setY((float) (ratio*r_candy3[1]+y_initial_postion));

        iv_lemon1.setX((float) (ratio*r_lemon1[0]+x_initial_postion));
        iv_lemon1.setY((float) (ratio*r_lemon1[1]+y_initial_postion));
        iv_lemon2.setX((float) (ratio*r_lemon2[0]+x_initial_postion));
        iv_lemon2.setY((float) (ratio*r_lemon2[1]+y_initial_postion));
        iv_lemon3.setX((float) (ratio*r_lemon3[0]+x_initial_postion));
        iv_lemon3.setY((float) (ratio*r_lemon3[1]+y_initial_postion));

        iv_position= (ImageView) findViewById(R.id.imageView);


    }

    public void addDevice(Vector<Item> items) {
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item it1, Item it2) {
                if (it1.getDistance() > it2.getDistance() ) {
                    return 1;
                } else if(it2.getDistance() > it1.getDistance() ) {
                    return -1;
                }
                return 0;            }
        });


    }

    List<Double> x_list = new ArrayList<Double>();
    List<Double> y_list = new ArrayList<Double>();
    @Override
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
                        addDevice(items);

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int N = items.size();
                            if(N>=3){
                                if (match(items)==0){
                                    match(items);
                                    int []point_1 = {p[0],p[1]};
                                    int []point_2 = {p[2],p[3]};
                                    int []point_3 = {p[4],p[5]};

                                    double[] xy =calculate(point_1,point_2,point_3 ,items.get(0).getDistance(), items.get(1).getDistance() , items.get(2).getDistance() );

                                    if (!Double.isNaN(xy[0])&&!Double.isNaN(xy[1])&&xy[0]>0&&xy[1]>0){

                                        triptx =  ratio * xy[0] + x_initial_postion;
                                        tripty = ratio * xy[1] + y_initial_postion;
                                        p[0]= (int) (ratio * p[0] + x_initial_postion);
                                        p[1]= (int) (ratio * p[1] + y_initial_postion);

                                        triptx=(p[0]+triptx)/2;
                                        tripty=(p[1]+tripty)/2;

                                        x_list.add(triptx);
                                        y_list.add(tripty);
                                        position.setText(Double.parseDouble(decimalFormat.format(triptx)) + " / " + Double.parseDouble(decimalFormat.format(tripty)));

                                        a_info.setText("1st : " + name[0]);
                                        b_info.setText("2nd : " + name[1]);
                                        c_info.setText("3rd : " + name[2]);
                                    }}

                                if((x_list.size()>=period)&&(x_list.size()%period==0)) {
                                    x_distance = Double.parseDouble(decimalFormat.format(getAverage(x_list, period)));
                                    y_distance = Double.parseDouble(decimalFormat.format(getAverage(y_list, period)));
                                    iv_position.setX((float) (x_distance));
                                    iv_position.setY((float) (y_distance));

                                    iv_position.setVisibility(View.VISIBLE);
                                    Time = System.currentTimeMillis();
                                    day_s = dayTime.format(new Date(Time));
                                    time_s = timetime.format(new Date(Time));

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");


                                    databaseReference.child("user2").child(day_s).child(time_s).child("x").setValue(x_distance);
                                    databaseReference.child("user2").child(day_s).child(time_s).child("y").setValue(y_distance);
                                    databaseReference.child("user2").child(day_s).child("last_x").setValue(x_distance);
                                    databaseReference.child("user2").child(day_s).child("last_y").setValue(y_distance);
                                }

                            }

                            if(x_list.size()>period*10){
                                x_list.clear();
                                y_list.clear();
                            }
                            beaconAdapter = new BeaconAdapter(items, InActivity.this);
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
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }


            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View view) {

    }


    public int match(Vector<Item> items) {
        for (int j = 0; j < 3; j++) {
            if (items.get(j).getAddress().equals(candy1)) {
                p[2 * j] = r_candy1[0];
                p[2 * j + 1] = r_candy1[1];
                name[j] = "candy1";
            } else if (items.get(j).getAddress().equals(candy3)) {
                p[2 * j] = r_candy3[0];
                p[2 * j + 1] = r_candy3[1];
                name[j] = "candy3";
            } else if (items.get(j).getAddress().equals(lemon1)) {
                p[2 * j] = r_lemon1[0];
                p[2 * j + 1] = r_lemon1[1];
                name[j] = "lemon1";
            } else if (items.get(j).getAddress().equals(lemon2)) {
                p[2 * j] = r_lemon2[0];
                p[2 * j + 1] = r_lemon2[1];
                name[j] = "lemon2";
            } else if (items.get(j).getAddress().equals(lemon3)) {
                p[2 * j] = r_lemon3[0];
                p[2 * j + 1] = r_lemon3[1];
                name[j] = "lemon3";
            } else if (items.get(j).getAddress().equals(beetroot1)) {
                p[2 * j] = r_beetroot1[0];
                p[2 * j + 1] = r_beetroot1[1];
                name[j] = "beetroot1";
            } else if (items.get(j).getAddress().equals(beetroot2)) {
                p[2 * j] = r_beetroot2[0];
                p[2 * j + 1] = r_beetroot2[1];
                name[j] = "beetroot2";
            } else if (items.get(j).getAddress().equals(beetroot3)) {
                p[2 * j] = r_beetroot3[0];
                p[2 * j + 1] = r_beetroot3[1];
                name[j] = "beetroot3";
            } else {
                return -1;
            }
        }
        return 0;
    }
}


