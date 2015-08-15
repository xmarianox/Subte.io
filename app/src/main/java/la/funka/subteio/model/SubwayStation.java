package la.funka.subteio.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by marianomolina on 14/8/15.
 * twitter: @xsincrueldadx
 */
public class SubwayStation extends RealmObject{

	@SerializedName("id_station")
	@Expose
	@PrimaryKey
	private int id_station;

	@SerializedName("station_name")
	@Expose
	private String station_name;

	@SerializedName("id_line")
	@Expose
	private int id_line;

	@SerializedName("line_name")
	@Expose
	private String line_name;

	@SerializedName("lon")
	@Expose
	private double lon;

	@SerializedName("lat")
	@Expose
	private double lat;

	@SerializedName("address")
	@Expose
	private String address;

	@SerializedName("elevador")
	@Expose
	private boolean elevador;

	@SerializedName("escalator")
	@Expose
	private boolean escalator;

	@SerializedName("toilets")
	@Expose
	private boolean toilets;

	@SerializedName("consultation")
	@Expose
	private boolean consultation;

	@SerializedName("wifi")
	@Expose
	private boolean wifi;

	@SerializedName("bus_lines")
	@Expose
	private String bus_lines;

	public int getId_station() {
		return id_station;
	}

	public void setId_station(int id_station) {
		this.id_station = id_station;
	}

	public String getStation_name() {
		return station_name;
	}

	public void setStation_name(String station_name) {
		this.station_name = station_name;
	}

	public int getId_line() {
		return id_line;
	}

	public void setId_line(int id_line) {
		this.id_line = id_line;
	}

	public String getLine_name() {
		return line_name;
	}

	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isElevador() {
		return elevador;
	}

	public void setElevador(boolean elevador) {
		this.elevador = elevador;
	}

	public boolean isEscalator() {
		return escalator;
	}

	public void setEscalator(boolean escalator) {
		this.escalator = escalator;
	}

	public boolean isToilets() {
		return toilets;
	}

	public void setToilets(boolean toilets) {
		this.toilets = toilets;
	}

	public boolean isConsultation() {
		return consultation;
	}

	public void setConsultation(boolean consultation) {
		this.consultation = consultation;
	}

	public boolean isWifi() {
		return wifi;
	}

	public void setWifi(boolean wifi) {
		this.wifi = wifi;
	}

	public String getBus_lines() {
		return bus_lines;
	}

	public void setBus_lines(String bus_lines) {
		this.bus_lines = bus_lines;
	}
}
