package com.paltech.dronesncars.model;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TypeConverters {

    private static final Type GEOPOINT_LIST_TYPE = new TypeToken<ArrayList<GeoPoint>>() {}.getType();
    private static final Type WAYPOINT_LIST_TYPE = new TypeToken<ArrayList<Waypoint>>() {}.getType();
    private static final Type GEOPOINT_LIST_LIST_TYPE = new TypeToken<ArrayList<ArrayList<GeoPoint>>>() {}.getType();

    @TypeConverter
    public InetAddress from_String_to_InetAddress(String value){
        try {
            return InetAddress.getByName(value);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    @TypeConverter
    public String from_InetAddress_to_String(InetAddress value) {
        if (value == null) {
            return "127.0.0.1";
        }
        return value.getHostAddress();
    }

    @TypeConverter
    public String fromWaypointList(List<Waypoint> value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<Waypoint> serializer = new WaypointJsonGenerator();
        JsonSerializer<GeoPoint> geoPointJsonSerializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(Waypoint.class, serializer);
        gsonBuilder.registerTypeAdapter(GeoPoint.class, geoPointJsonSerializer);

        return gsonBuilder.create().toJson(value);
    }

    @TypeConverter
    public List<Waypoint> fromWaypointListString(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonDeserializer<Waypoint> deserializer = new WaypointJsonGenerator();
        JsonDeserializer<GeoPoint> geoPointJsonDeserializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(Waypoint.class, deserializer);
        gsonBuilder.registerTypeAdapter(GeoPoint.class, geoPointJsonDeserializer);

        return gsonBuilder.create().fromJson(value, WAYPOINT_LIST_TYPE);
    }

    @TypeConverter
    public String fromPolygon(Polygon value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<Polygon> serializer = new PolygonJsonGenerator();
        JsonSerializer<GeoPoint> geoPointJsonSerializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(Polygon.class, serializer);
        gsonBuilder.registerTypeAdapter(GeoPoint.class, geoPointJsonSerializer);

        return gsonBuilder.create().toJson(value);
    }

    @TypeConverter
    public Polygon fromPolygonString(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonDeserializer<Polygon> deserializer = new PolygonJsonGenerator();
        JsonDeserializer<GeoPoint> geoPointJsonDeserializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(Polygon.class, deserializer);
        gsonBuilder.registerTypeAdapter(GeoPoint.class, geoPointJsonDeserializer);

        return gsonBuilder.create().fromJson(value, Polygon.class);
    }

    @TypeConverter
    public GeoPoint fromGeoPointString(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonDeserializer<GeoPoint> deserializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(GeoPoint.class, deserializer);

        return gsonBuilder.create().fromJson(value, GeoPoint.class);
    }

    @TypeConverter
    public String fromGeoPoint(GeoPoint value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<GeoPoint> serializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(GeoPoint.class, serializer);
        return gsonBuilder.create().toJson(value);
    }

    @TypeConverter
    public List<GeoPoint> fromGeoPointRouteString(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonDeserializer<GeoPoint> deserializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(GeoPoint.class, deserializer);



        return gsonBuilder.create().fromJson(value, GEOPOINT_LIST_TYPE);
    }

    @TypeConverter
    public String fromGeoPointRoute(List<GeoPoint> value) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonSerializer<GeoPoint> serializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(GeoPoint.class, serializer);

        return gsonBuilder.create().toJson(value);
    }

    private class GeoPointJsonGenerator implements JsonSerializer<GeoPoint>, JsonDeserializer<GeoPoint> {

        @Override
        public JsonElement serialize(GeoPoint src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject geopoint_json = new JsonObject();
            geopoint_json.addProperty("latitude", src.getLatitude());
            geopoint_json.addProperty("longitude", src.getLongitude());
            geopoint_json.addProperty("altitude", src.getAltitude());

            return geopoint_json;
        }

        @Override
        public GeoPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject geopoint_json = json.getAsJsonObject();

            return new GeoPoint(geopoint_json.get("latitude").getAsDouble(),
                    geopoint_json.get("longitude").getAsDouble(),
                    geopoint_json.get("altitude").getAsDouble());
        }
    }

    private class PolygonJsonGenerator implements JsonSerializer<Polygon>, JsonDeserializer<Polygon> {
        @Override
        public Polygon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject polygon_json = json.getAsJsonObject();
            Polygon polygon = new Polygon();

            List<GeoPoint> geoPoints = context.deserialize(polygon_json.get("points").getAsJsonArray(),
                    GEOPOINT_LIST_TYPE);
            List<List<GeoPoint>> holes = context.deserialize(polygon_json.get("holes").getAsJsonArray(),
                    GEOPOINT_LIST_LIST_TYPE);

            polygon.setPoints(geoPoints);
            polygon.setHoles(holes);

            return polygon;
        }

        @Override
        public JsonElement serialize(Polygon src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject polygon_json = new JsonObject();

            polygon_json.add("points", context.serialize(src.getActualPoints()));
            polygon_json.add("holes", context.serialize(src.getHoles()));

            return polygon_json;
        }
    }

    private class WaypointJsonGenerator implements JsonSerializer<Waypoint>, JsonDeserializer<Waypoint> {
        @Override
        public Waypoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject waypoint_json = json.getAsJsonObject();
            GeoPoint position = context.deserialize(waypoint_json.getAsJsonObject("position"), GeoPoint.class);
            int corresponding_route_id = waypoint_json.get("corresponding_route_id").getAsInt();
            boolean is_navigation_point = waypoint_json.get("is_navigation_point").getAsBoolean();
            boolean milestone_completed = waypoint_json.get("milestone_completed").getAsBoolean();
            int waypoint_number = waypoint_json.get("waypoint_number").getAsInt();

            Waypoint waypoint = new Waypoint(corresponding_route_id, waypoint_number, position,is_navigation_point);
            waypoint.milestone_completed = milestone_completed;

            return waypoint;
        }

        @Override
        public JsonElement serialize(Waypoint src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject waypoint_json = new JsonObject();

            waypoint_json.add("position", context.serialize(src.position));
            waypoint_json.add("corresponding_route_id", context.serialize(src.corresponding_route_id));
            waypoint_json.add("is_navigation_point", context.serialize(src.is_navigation_point));
            waypoint_json.add("milestone_completed", context.serialize(src.milestone_completed));
            waypoint_json.add("waypoint_number", context.serialize(src.waypoint_number));

            return waypoint_json;
        }
    }
}
