package com.julia.Eyetracking.Simulator;

import com.julia.Eyetracking.DataModel.EyetrackingData;

public interface ISimulator {

    EyetrackingData update(float deltaTime);

}
