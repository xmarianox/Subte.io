package la.funka.subteio.service;

import java.util.List;

import la.funka.subteio.model.SubwayLine;
import la.funka.subteio.model.SubwayStation;
import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by RetinaPro on 3/7/15.
 * twitter: @xsincrueldadx
 */
public interface SubwayApi {
    @GET("/Subterraneos/Estado?site=Metrovias")
    void loadSubwayStatus(Callback<List<SubwayLine>> callback);

    @GET("/subwayStations")
    void loadStations(Callback<List<SubwayStation>> listCallback);
}
