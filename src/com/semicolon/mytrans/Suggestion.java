package com.semicolon.mytrans;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class Suggestion extends FragmentActivity implements OnClickListener {
	GoogleMap gm;
	ArrayList<LatLng> list ;
	Button save;
	Button printAll;
	Button del;
	EditText name;
	EditText price;
	EditText Console;
	Button removelast;
	Button clear;
	Button print;
	Marker marker;
	ArrayList<Marker> alm=new ArrayList<Marker>();
	ArrayList<Polyline> removables=new ArrayList<Polyline>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggestion);
		init();
	}

	public void init() {
		SupportMapFragment smf = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		gm = smf.getMap();
		list = new ArrayList<LatLng>();
		navi2khart(gm);
		gm.setOnMapLongClickListener(new OnMapLongClickListener() {
			int i = -1;
			public void onMapLongClick(LatLng arg0) {
				i++;
//				MarkerOptions mp = new MarkerOptions().position(arg0);
//				alm.add(gm.addMarker(mp));
				toString();
				PolylineOptions pl = new PolylineOptions();
				if (i >= 1) {if(list.size()==0){}else{
					pl.add(list.get(list.size()-1));
					pl.add(arg0);
				}
				}
				list.add(arg0);
				removables.add(gm.addPolyline(pl));
				
			}
		});
		save = (Button) findViewById(R.id.save_route);
		del = (Button) findViewById(R.id.del);
		printAll = (Button) findViewById(R.id.print_data);
		save.setOnClickListener(this);
		printAll.setOnClickListener(this);
		del.setOnClickListener(this);
		name = (EditText) findViewById(R.id.route_name);
		price = (EditText) findViewById(R.id.route_price);
		Console = (EditText) findViewById(R.id.console);
		clear = (Button) findViewById(R.id.clear);
		removelast = (Button) findViewById(R.id.removelast);
		print = (Button) findViewById(R.id.printroute);
		clear.setOnClickListener(this);
		removelast.setOnClickListener(this);
		print.setOnClickListener(this);

	}

	public static void navi2khart(GoogleMap gomap) {
		LatLng khartoum = new LatLng(15.591512, 32.530025);
		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(khartoum, 11);
		gomap.moveCamera(cu);

	}
	public void drawPath(ArrayList<LatLng> input) {
		PolylineOptions pl = new PolylineOptions();
		MarkerOptions mo;
		for (int i = 0; i < input.size(); i++) {
			mo = new MarkerOptions();
			mo.position(input.get(i));
			gm.addMarker(mo);
			pl.add(input.get(i));
		}
		gm.addPolyline(pl);

	}

	public JSONObject readJSON() throws IOException, JSONException {
		StringBuffer s = null;
		JSONObject result = null;
		Log.i("pose", "am here in reding function");
		FileInputStream fis = openFileInput("Routes");
		BufferedInputStream bis = new BufferedInputStream(fis);
		s = new StringBuffer();
		while (bis.available() != 0) {
			char c = (char) bis.read();
			s.append(c);
		}
		bis.close();
		fis.close();
		ArrayList<String> names = new ArrayList<String>();
		result = new JSONObject(s.toString());
		Log.i("pose", "finished reading");
		return result;
	}

	public void UpdateJSON() throws IOException, JSONException {
		Route r = new Route(list, name.getText().toString(), price.getText()
				.toString());
		StringBuffer s = new StringBuffer();
		try {
			FileInputStream fis = openFileInput("Routes");
			BufferedInputStream bis = new BufferedInputStream(fis);
			s = new StringBuffer();
			while (bis.available() != 0) {
				char c = (char) bis.read();
				s.append(c);
			}
			bis.close();
			fis.close();
			JSONObject j = new JSONObject();
			j = new JSONObject(s.toString());
			j.put(r.name, Suggestion.toString(r));
			FileOutputStream fos = openFileOutput("Routes", MODE_PRIVATE);
			fos.write(j.toString().getBytes());
			fos.close();
			Toast.makeText(getBaseContext(), "saved 1", 1000).show();
		}catch(FileNotFoundException e){
			JSONObject j = new JSONObject();
			j.put(r.name, Suggestion.toString(r));
			FileOutputStream fos = openFileOutput("Routes", MODE_PRIVATE);
			fos.write(j.toString().getBytes());
			fos.close();
			Toast.makeText(getBaseContext(), "saved 2", 1000).show();
		}

	}
	public void delete_route(String name){
		StringBuffer s = new StringBuffer();
		try{
			FileInputStream fis = openFileInput("Routes");
			BufferedInputStream bis = new BufferedInputStream(fis);
			s = new StringBuffer();
			while (bis.available() != 0) {
				char c = (char) bis.read();
				s.append(c);
			}
			bis.close();
			fis.close();
			JSONObject j = new JSONObject(s.toString());
			j.remove(name);
			FileOutputStream fos = openFileOutput("Routes", MODE_PRIVATE);
			fos.write(j.toString().getBytes());
			fos.close();
			Toast.makeText(getBaseContext(), "Route deleted : "+name, 1000).show();
		}catch(Exception e){}	
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

	private static String toString(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save_route:
			try {
				UpdateJSON();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			break;
		case R.id.print_data:
			Log.i("pose", "am here in click");
			try {
				Route danger =(Route)fromString(readJSON().getString(name.getText().toString()));
				danger.cordinates2path();
				Console.setText(danger.name + "  "+ danger.price);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case R.id.del:
			delete_route(name.getText().toString());
			break;
		case R.id.clear:
			gm.clear();
			break;
		case R.id.removelast:
			try{
			//alm.get(alm.size()-1).remove();
			removables.get(removables.size()-1).remove();
			//alm.remove(alm.size()-1);
			removables.remove(removables.size()-1);
			list.remove(list.size()-1);}catch(Exception e){Console.setText("empty route !");}
			break;
		case R.id.printroute:
			int index=0;
			String routename=Console.getText().toString();
			for(int i=0;i<MainActivity.all_routes.size();i++){
				if(MainActivity.all_routes.get(i).name==routename){
					index=i;
					
				}
			}
			drawRoute(gm, MainActivity.all_routes.get(index));
			Console.setText(" route :"+MainActivity.all_routes.get(index).name +" is printed :" + MainActivity.all_routes.get(index));
			break;
		}
		
		

	}
	public void drawRoute(GoogleMap gm, Route t) {
		PolylineOptions pl = new PolylineOptions();
		for (int i = 0; i < t.path.size(); i++) {
			pl.add(t.path.get(i));
		}
		gm.addPolyline(pl);
	}

}
