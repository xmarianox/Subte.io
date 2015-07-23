package la.funka.subteio.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by marianomolina on 22/7/15.
 * twitter: @xsincrueldadx
 */
public class SubwayLine extends RealmObject{


    @SerializedName("LineName")
    @Expose
    @PrimaryKey
    private String lineName;

    @SerializedName("LineStatus")
    @Expose
    private String lineStatus;

    @SerializedName("LineFrequency")
    @Expose
    private String lineFrequency;

    /**
     *
     * @return
     * The lineName
     */
    public String getLineName() {
        return lineName;
    }

    /**
     *
     * @param LineName
     * The lineName
     */
    public void setLineName(String LineName) {
        this.lineName = LineName;
    }

    /**
     *
     * @return
     * The lineStatus
     */
    public String getLineStatus() {
        return lineStatus;
    }

    /**
     *
     * @param LineStatus
     * The lineStatus
     */
    public void setLineStatus(String LineStatus) {
        this.lineStatus = LineStatus;
    }

    /**
     *
     * @return
     * The lineFrequency
     */
    public String getLineFrequency() {
        return lineFrequency;
    }

    /**
     *
     * @param LineFrequency
     * The lineFrequency
     */
    public void setLineFrequency(String LineFrequency) {
        this.lineFrequency = LineFrequency;
    }

}
