package com.vrp.app;

import com.vrp.app.components.Result;

public interface Solver {
    void run();

    void setSolution(Result solution);

    Result getSolution();
}
