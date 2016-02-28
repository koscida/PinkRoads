package kodestudios.pinkroads; /**
 * Created by tinameyer on 27.02.16.
 * A class to hold the marker information pop ups.
 */

// Imports
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.model.BitmapDescriptorFactory;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.Marker;
    import com.google.android.gms.maps.model.MarkerOptions;

    import java.util.Date;
    import java.util.GregorianCalendar;
    import java.util.Calendar;

public class MarkerInfo {
    String type;
    String message;
    Date date;
    LatLng latLng;
    String icon;

    //GoogleMap googleMap;

    public MarkerInfo(){
        this.type = "Danger";
        this.message = "Danger zone";
        this.date = new Date();
        latLng = new LatLng(0.0,0.0);
    }


    public MarkerInfo(String type, String message, Date date, double lat, double lng){
        this.type = type;
        this.message = message;
        this.date = date;
        latLng = new LatLng(lat,lng);
        if(type == "Attack"){
            this.icon = "Attack.png";
        } else if(type == "EmergencyButton"){
            this.icon = "EmergencyButton.png";
        }
        else if(type == "GangsHangout"){
            this.icon = "GangsHangout.png";
        }
        else if(type == "StoreHours"){
            this.icon = "StoreHours.png";
        }
        else if(type == "PoliceStation"){
            this.icon = "PoliceStation.png";
        }
    }

    //public Marker createMarker(){
        //Marker TP = googleMap.addMarker(new MarkerOptions().position(new LatLng(43.084483,-77.678554)).title(type)
          //      .icon(BitmapDescriptorFactory.fromAsset("images.jpg")).snippet(message));
        //return TP;
    //}


}
