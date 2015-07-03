package la.funka.subteio.service;

import java.util.List;

import la.funka.subteio.model.Line;
import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by RetinaPro on 3/7/15.
 */
public interface SubwayStatusApi {
    @GET("/Subterraneos/Estado?site=Metrovias")
    void getSubwayStatus(Callback<Line> response);

    @GET("/Subterraneos/Estado?site=Metrovias")
    List<Line> listStatus();
}
