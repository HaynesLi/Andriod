package com.paltech.dronesncars.model;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

public class RoverRouteStatus {

    private List<GeoPoint> route;
    private Rover rover;
    private Polyline overlays[] = new Polyline[2];

    public RoverRouteStatus(List<GeoPoint> route, Rover rover) {
        this.route = route;
        this.rover = rover;
    }



    public RoverRouteStatus set_rover(Rover rover) {
        this.rover = rover;
        return this;
    }

    public RoverRouteStatus set_route(List<GeoPoint> route) {
        this.route = route;
        return this;
    }



    public Polyline[] get_overlays() {
        if(rover.currentWaypoint > route.size()) {
            this.overlays[0] = null;
            this.overlays[1] = null;
        } else if (rover.currentWaypoint == 0 || rover.currentWaypoint == route.size()) {
            this.overlays[0] = null;
            Polyline tmp = new Polyline();
            tmp.setPoints(route);
            if (rover.currentWaypoint == 0) {
                tmp.getOutlinePaint().setColor(0xff0000); // TODO is this red?
            } else {
                tmp.getOutlinePaint().setColor(0x3f6b1c); // TODO is this paltech-green?
            }

            this.overlays[1] = tmp;
        } else {
            Polyline tmp = new Polyline();
            List<GeoPoint> driven = route.subList(0, rover.currentWaypoint);
            driven.add(rover.position); // TODO make rover.position: GeoPoint instead of String!
            tmp.setPoints(driven);
            tmp.getOutlinePaint().setColor(0x3f6b1c);
            Polyline tmp2 = new Polyline();
            List<GeoPoint> not_driven = route.subList(rover.currentWaypoint+1, route.size()-1); // TODO do we need route.size() as ending boundary instead?
            not_driven.add(0, rover.position);
            tmp2.setPoints(not_driven);
            tmp2.getOutlinePaint().setColor(0xff0000);

            this.overlays[0] = tmp;
            this.overlays[1] = tmp2;
        }

        return this.overlays;
    }



}
