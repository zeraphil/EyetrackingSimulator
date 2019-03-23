package com.julia.Eyetracking.Simulator;

import com.julia.Eyetracking.DataModel.EyetrackingData;

public interface ISimulatorDataListener {
    void onData(EyetrackingData data);
}
