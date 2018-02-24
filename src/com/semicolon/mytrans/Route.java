package com.semicolon.mytrans;
import java.io.Serializable;
import java.util.ArrayList;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Route implements Serializable {
	public String name;
	public transient ArrayList<LatLng> path;
	public String price;
	public double []cordinates;


	public Route(ArrayList<LatLng> al,String name,String price){
		this.name=name;
		this.price=price;
		this.path=al;
		path2cordinates();
	}
	public void path2cordinates(){
		cordinates =new double[path.size()*2];
		int index=0;
		for (LatLng i : path) {
			cordinates[index]=i.latitude;
			cordinates[index+1]=i.longitude;
			index+=2;
		}
	}
	public void cordinates2path(){
		ArrayList<LatLng>result =new ArrayList<LatLng>();
		for (int i = 0; i < cordinates.length; i+=2) {
			LatLng  tmp =new LatLng(cordinates[i], cordinates[i+1]);
			result.add(tmp);
		}
		this.path=result;
	}
	
	public float gettotalDistanceFrom(int index){
		float result=0;
		Log.i("am distance", " "+path.size());
		for (int i=index;i<path.size()-1;i+=2) {
			Location one = new Location("");
			one.setLatitude(path.get(i).latitude);
			one.setLongitude(path.get(i).longitude);
			Location two = new Location("");
			two.setLatitude(path.get(i+1).latitude);
			two.setLongitude(path.get(i+1).longitude);
			result+=one.distanceTo(two);
		}
		return result;
	}

	public float distanseFrom(LatLng location) {
		float result;
		Location point;
		Location start = new Location("");
		start.setLatitude(location.latitude);
		start.setLongitude(location.longitude);
		point= new Location("");
		point.setLatitude(path.get(0).latitude);
		point.setLongitude(path.get(0).longitude);
		result=start.distanceTo(point);		
		for(int i=1;i<path.size();i++){
		    point= new Location("");
			point.setLatitude(path.get(i).latitude);
			point.setLongitude(path.get(i).longitude);
			float tmp=start.distanceTo(point);
			if(tmp<result)
			result=tmp;
		}		
		return result;
	}
	public int nearest_index(LatLng location) {
		float result;
		LatLng ans;
		int res=0;
		Location point;
		Location start = new Location("");
		start.setLatitude(location.latitude);
		start.setLongitude(location.longitude);
		point= new Location("");
		ans=path.get(0);
		point.setLatitude(ans.latitude);
		point.setLongitude(ans.longitude);
		result=start.distanceTo(point);		
		for(int i=1;i<path.size();i++){
			LatLng tmpans=path.get(i);
		    point= new Location("");
			point.setLatitude(tmpans.latitude);
			point.setLongitude(tmpans.longitude);
			float tmp=start.distanceTo(point);
			if(tmp<result){
			result=tmp;
			ans=tmpans;
			res=i;
			}
		}		
		return res;
	}	

	
}

