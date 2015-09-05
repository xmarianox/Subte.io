package la.funka.subteio.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Mariano Molina on 4/9/15.
 * Twitter: @xsincrueldadx
 */
public class LastUpdateDate extends RealmObject {
    @PrimaryKey
    private int date_id;

    private String date_text;

    public String getDate_text() {
        return date_text;
    }

    public void setDate_text(String date_text) {
        this.date_text = date_text;
    }

    public int getDate_id() {
        return date_id;
    }

    public void setDate_id(int date_id) {
        this.date_id = date_id;
    }
}
