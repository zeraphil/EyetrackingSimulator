package com.julia.Eyetracking.Service;

import com.julia.Eyetracking.DataModel.EyetrackingData;

/**
 * Interface to modularize services
 */
public interface IEyetrackingService {
    void sendData(EyetrackingData data);
}
