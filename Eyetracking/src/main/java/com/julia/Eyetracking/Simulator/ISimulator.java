package com.julia.Eyetracking.Simulator;

import com.julia.Eyetracking.DataModel.EyetrackingData;

/**
 * Interface for simulators to provide an update step function for the service to call
 */
public interface ISimulator {

    EyetrackingData update(float deltaTime);

}
