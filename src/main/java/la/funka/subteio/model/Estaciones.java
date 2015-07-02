package la.funka.subteio.model;

public class Estaciones {
    private int line_id;
    private String line_name;
    private String station_name;
    // Location
    private double latitude;
    private double logitude;
    // Accesibilidad
    private int ascensores;
    private int escaleras;
    private String adaptado;
    private String accesible;


    public int getLine_id() {
        return line_id;
    }

    public void setLine_id(int line_id) {
        this.line_id = line_id;
    }

    public String getLine_name() {
        return line_name;
    }

    public void setLine_name(String line_name) {
        this.line_name = line_name;
    }

    public String getStation_name() {
        return station_name;
    }

    public void setStation_name(String station_name) {
        this.station_name = station_name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLogitude() {
        return logitude;
    }

    public void setLogitude(double logitude) {
        this.logitude = logitude;
    }

    public int getAscensores() {
        return ascensores;
    }

    public void setAscensores(int ascensores) {
        this.ascensores = ascensores;
    }

    public int getEscaleras() {
        return escaleras;
    }

    public void setEscaleras(int escaleras) {
        this.escaleras = escaleras;
    }

    public String getAdaptado() {
        return adaptado;
    }

    public void setAdaptado(String adaptado) {
        this.adaptado = adaptado;
    }

    public String getAccesible() {
        return accesible;
    }

    public void setAccesible(String accesible) {
        this.accesible = accesible;
    }
}
