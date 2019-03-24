package com.julia.unityapp;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.Window;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.HelperMethods;
import com.julia.Eyetracking.Service.IEyetrackingDataListener;
import com.julia.Eyetracking.Service.ServiceConnectionManager;
import com.unity3d.player.UnityPlayer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;

public class EyetrackingUnityPlayerActivity extends UnityPlayerActivity {
    /**
     * Service connection parameters
     */
    ServiceConnectionManager serviceConnection;

    /**
     * Listener that will handler our eyetracking data parcel callback
     */
    IEyetrackingDataListener eyetrackingDataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.eyetrackingDataListener = new IEyetrackingDataListener() {
            @Override
            public void onEyetrackingDataMessage(EyetrackingData data) {
                onNewEyetrackingData(data);
            }
        };

        //for the unity plugin, don't do storage, don't use the flat buffer service
        this.serviceConnection = new ServiceConnectionManager(this, this.eyetrackingDataListener, false, false);
    }

    public void OnTogglePressed()
    {
        if (this.serviceConnection.areServicesBound())
        {
            this.serviceConnection.connectToServices(false);
        }
        else
        {
            this.serviceConnection.connectToServices(true);
        }
    }

    /**
     * Handle a message containing data, including serializing it and updating the visualization
     * @param data
     */
    private void onNewEyetrackingData(EyetrackingData data)
    {
        ByteBuffer buf = data.toFlatBuffer();
        if(buf!= null)
        {
            String byteString = new String(buf.array(), StandardCharsets.UTF_8);
            UnityPlayer.UnitySendMessage("EyetrackingDataManager", "OnNewEyetrackingData", byteString+":"+buf.position() );
        }
    }

}
