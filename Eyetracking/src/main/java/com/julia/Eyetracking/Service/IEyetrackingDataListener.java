package com.julia.Eyetracking.Service;

import com.julia.Eyetracking.DataModel.EyetrackingData;

public interface IEyetrackingDataListener {

    void onEyetrackingDataMessage(EyetrackingData data);

}
