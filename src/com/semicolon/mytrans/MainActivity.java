package com.semicolon.mytrans;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity implements OnClickListener,
		OnItemSelectedListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener 
		{
	Button sugg, navi, refind,startbt,finishbt;
	GoogleMap SearchMap, NaviMap;
	public final int MODE_START = 0;
	public final int MODE_DEST = 1;
	LatLng start, dest, location;
	Spinner sp1, sp2, sp3;
	Route to_navi1, to_navi2 = null;
	final int DEFAULT_SPEED = 4000 / 6;
	static ArrayList<Route> sorted_dest, sorted_start, all_routes;
	EditText timer, out;
	LocationClient lc;
	int i;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		all_routes = findAll();
		Log.i("dedc", all_routes.size() + "");
		init();
	}

	public void init() {
		SupportMapFragment smf = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		SearchMap = smf.getMap();
		SearchMap.setMyLocationEnabled(true);
		navi2khart();
		timer = (EditText) findViewById(R.id.timer);
		out = (EditText) findViewById(R.id.out);
	    sugg = (Button) findViewById(R.id.sugg);
		navi = (Button) findViewById(R.id.navi);
		startbt = (Button) findViewById(R.id.start);
	    finishbt = (Button) findViewById(R.id.finish);
		initspinner1();
		sp1 = (Spinner) findViewById(R.id.spinner1);
		sp2 = (Spinner) findViewById(R.id.spinner2);
		sp3 = (Spinner) findViewById(R.id.spinner3);
		SearchMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			int i = -1;

			public void onMapLongClick(LatLng arg0) {
				if (sp1.getSelectedItem().toString().equals("start")) {
					start = arg0;
					initspinner2();
				} else {
					dest = arg0;
					initspinner3();
				}
			}
		});
		sugg.setOnClickListener(this);
		navi.setOnClickListener(this);
		startbt.setOnClickListener(this);
		finishbt.setOnClickListener(this);

	}

	public void navi2khart() {
		LatLng khartoum = new LatLng(15.591512, 32.530025);
		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(khartoum, 11);
		SearchMap.moveCamera(cu);
	}

	public ArrayList<Route> findAll() {
		JSONObject toRe = readJSON();
		ArrayList<Route> result = new ArrayList<Route>();
		for (Iterator i = toRe.keys(); i.hasNext();) {
		
			Log.i("key", i.next().toString());
			
		}

		try {
			JSONObject toRead = readJSON();
			for (Iterator i = toRead.keys(); i.hasNext();) {
				Log.i("key", "printing");
				String tmp = (String) i.next();
				String s = toRead.getString(tmp);
				Route res = (Route) fromString(s);
				res.cordinates2path();

				result.add(res);

			}

		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("length", "" + result.size());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public JSONObject readJSON() {
		StringBuffer s = null;
		JSONObject result = null;
		Log.i("pose", "am here in reding function");
		try {
			FileInputStream fis = openFileInput("Routes");

			BufferedInputStream bis = new BufferedInputStream(fis);
			s = new StringBuffer();
			while (bis.available() != 0) {
				char c = (char) bis.read();
				s.append(c);
			}
			result = new JSONObject(s.toString());
			Log.i("cmfdmc", " " + s.length());
		} catch (Exception e) {
		}
		return result;

	}

	public void initspinner1() {
		sp1 = (Spinner) findViewById(R.id.spinner1);
		ArrayList<String> al = new ArrayList<String>();
		al.add("start");
		al.add("destination");
		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, al);
		sp1.setAdapter(adapter1);
	}

	public void initspinner2() {
		sp2 = (Spinner) findViewById(R.id.spinner2);
		sorted_start = Auto_Search(all_routes, MODE_START);
		ArrayList<String> options = new ArrayList<String>();
		for (Route r : sorted_start) {
			options.add(r.name);
		}
		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, options);
		sp2.setAdapter(adapter1);
		sp2.setOnItemSelectedListener(this);
//		sp2.setOnItemClickListener(this);
	}

	public void initspinner3() {
		sp3 = (Spinner) findViewById(R.id.spinner3);
		sorted_dest = Auto_Search(all_routes, MODE_DEST);
		ArrayList<String> options = new ArrayList<String>();
		for (Route r : sorted_dest) {
			options.add(r.name);
		}
		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, options);
		sp3.setAdapter(adapter1);
		sp3.setOnItemSelectedListener(this);
	}

	public ArrayList<Route> Auto_Search(ArrayList<Route> routes, int mode) {
		ArrayList<Route> result = routes;
		switch (mode) {
		case MODE_START:

			Collections.sort(result, new Comparator<Route>() {
				LatLng location = start;

				@Override
				public int compare(Route lhs, Route rhs) {
					if (lhs.distanseFrom(location) > rhs.distanseFrom(location))
						return 1;
					else if (lhs.distanseFrom(location) < rhs
							.distanseFrom(location))
						return -1;
					return 0;
				}

			});
			break;
		case MODE_DEST:
			Collections.sort(result, new Comparator<Route>() {
				LatLng location = dest;

				@Override
				public int compare(Route one, Route two) {
					if (one.distanseFrom(location) == two
							.distanseFrom(location))
						return 0;
					else if (one.distanseFrom(location) > two
							.distanseFrom(location))
						return 1;
					else
						return -1;
				}
			});
			break;
		}
		return result; 
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sugg:
			Intent i = new Intent("android.intent.action.SUGG");
			startActivity(i);
			break;
		case R.id.navi:
			try{
			if (to_navi1.name.equals(to_navi2.name))
				navi(to_navi1);
			else
				navi(to_navi1, to_navi2);}
			catch(Exception e){
				Toast.makeText(getBaseContext(), "didnt choose routes", 1000).show();
			}

			setContentView(R.layout.navigation);
			SupportMapFragment smf = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.navimap);
			NaviMap = smf.getMap();
			Suggestion.navi2khart(NaviMap);
			break;
		case R.id.start:
			Calendar c=Calendar.getInstance();
			
			break;
		case R.id.finish:
			Calendar c1=Calendar.getInstance();

			
			break;
		}
	}

	public void navi(final Route r) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				//Location l = lc.getLastLocation();
				//LatLng loc = new LatLng(l.getLatitude(), l.getLongitude());
				int duration = (int) r.gettotalDistanceFrom(0) / DEFAULT_SPEED;
				int hours = (int) duration / 3600;
				duration = duration - hours * 3600;
				int minutes = (int) duration / 60;
				duration = duration - minutes * 60;
				int seconds = duration;
				String time2show = String.format("dd:dd:dd", hours, minutes,
						seconds);
				timer.setText(time2show);
				i++;
				out.setText("" + i);
			}
		});
		while (true) {
			t.run();
			try {
				t.sleep(100000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void navi(Route r1, Route r2) {

	}

	public static Object fromString(String s) throws IOException,
			ClassNotFoundException {
		byte[] data = Base64.decode(s, Base64.DEFAULT);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
				data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	public void drawRoute(GoogleMap gm, Route t) {
		PolylineOptions pl = new PolylineOptions();
		for (int i = 0; i < t.path.size(); i++) {
			pl.add(t.path.get(i));
		}
		gm.addPolyline(pl);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		SearchMap.clear();
		switch (parent.getId()) {
		case R.id.spinner2:
			drawRoute(SearchMap, sorted_start.get(position));
			to_navi1 = sorted_start.get(position);
			break;
		case R.id.spinner3:
			drawRoute(SearchMap, sorted_dest.get(position));
			to_navi2 = sorted_dest.get(position);
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	
	
}

