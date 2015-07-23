package la.funka.subteio.model;

import io.realm.RealmObject;

/**
 * Created by marianomolina on 22/7/15.
 * twitter: @xsincrueldadx
 */
public class Linea extends RealmObject {
    private String name;
    private String status;
    private double frequency;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getFrequency(){return frequency;}

    public void setFrequency(double frequency){this.frequency = frequency;}

}
