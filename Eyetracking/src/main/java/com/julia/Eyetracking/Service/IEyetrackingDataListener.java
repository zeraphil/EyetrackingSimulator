package com.julia.Eyetracking.Service;

import com.julia.Eyetracking.DataModel.EyetrackingData;

/**
 * Interface to listen to eyetracking data message events from ServiceConnectionManager
 */
public interface IEyetrackingDataListener {

    void onEyetrackingDataMessage(EyetrackingData data);

}
