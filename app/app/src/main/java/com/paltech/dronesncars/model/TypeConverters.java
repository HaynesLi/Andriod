package com.paltech.dronesncars.model;

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
import java.util.ArrayList;
import java.util.List;

public class TypeConverters {

    private static final Type GEOPOINT_LIST_TYPE = new TypeToken<ArrayList<GeoPoint>>() {}.getType();

    @TypeConverter
    public String fromPolygon(Polygon value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<Polygon> serializer = new PolygonJsonGenerator();
        gsonBuilder.registerTypeAdapter(Polygon.class, serializer);

        return gsonBuilder.create().toJson(value);
    }

    @TypeConverter
    public Polygon fromPolygonString(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonDeserializer<Polygon> deserializer = new PolygonJsonGenerator();
        gsonBuilder.registerTypeAdapter(Polygon.class, deserializer);

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

            polygon.setHoles(context.deserialize(polygon_json.get("points").getAsJsonObject(),
                    GEOPOINT_LIST_TYPE));

            return polygon;
        }

        @Override
        public JsonElement serialize(Polygon src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject polygon_json = new JsonObject();

            polygon_json.add("points", context.serialize(src.getActualPoints()));
            // TODO add holes into json generation

            return polygon_json;
        }
    }
}
