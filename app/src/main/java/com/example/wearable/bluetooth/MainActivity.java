package com.example.wearable.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.RemoteException;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.client.Firebase;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

/*
 *
 *  altbeacon api를 이용해서 주변 비콘들을 조회
 *  MAC/RSSI/TXPOWER/DISTANCE를 LISTVIEW로 표시
 *  거리 공식은 Caculate Class에 저장
 *  Listview Item 하나를 누르면 distance의 변화를 graph로 표현
 *
 * */
public class MainActivity extends Calculate implements BeaconConsumer,View.OnClickListener, RecyclerItemClickListener.OnItemClickListener{

    private static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 100;

    BluetoothAdapter mBluetoothAdapter;

    BeaconAdapter beaconAdapter;

    BeaconManager mBeaconManager;

    Vector<Item> items;

    Button button;

    RecyclerView beaconListView;

    RecyclerView.LayoutManager layoutManager;
    LinearLayoutManager manager;

    int speed = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);

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
        catch (RemoteException e) {
            Log.e(TAG, "Cannot talk to service");
        }
        mBeaconManager.bind(MainActivity.this);
        beaconListView = findViewById(R.id.beaconListView);

        layoutManager = new LinearLayoutManager(this);

        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        beaconListView.setLayoutManager(manager);

        beaconListView.setItemAnimator(new DefaultItemAnimator());
        beaconListView.setAdapter(beaconAdapter);
        beaconListView.addOnItemTouchListener(new RecyclerItemClickListener(this,beaconListView, this));

        Button button =  findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,InActivity.class);
                startActivity(intent);
            }
        });


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
                            beaconAdapter = new BeaconAdapter(items, MainActivity.this);
                            beaconListView.setAdapter(beaconAdapter);
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


    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(MainActivity.this,GraphActivity.class);
        intent.putExtra("mac",items.get(position).getAddress());
        startActivity(intent);
    }

    @Override
    public void onLongClick(View view, int position) {

    }
}

