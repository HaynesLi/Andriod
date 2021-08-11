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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class used by the Room Database to turn complex objects such as GeoPoints from osmdroid into
 * datatypes that can be saved in a sql database and the other way around.
 */
public class TypeConverters {

    private static final Type GEOPOINT_LIST_TYPE = new TypeToken<ArrayList<GeoPoint>>() {}.getType();
    private static final Type WAYPOINT_LIST_TYPE = new TypeToken<ArrayList<Waypoint>>() {}.getType();
    private static final Type GEOPOINT_LIST_LIST_TYPE = new TypeToken<ArrayList<ArrayList<GeoPoint>>>() {}.getType();
    private static final Type STRING_LIST_TYPE = new TypeToken<ArrayList<String>>() {}.getType();
    private static final Type BOOLEAN_LIST_TYPE = new TypeToken<ArrayList<Boolean>>() {}.getType();


    /**
     * Turn a json string of a Boolean-List into a Boolean-List
     * @param value the json string of a Boolean-List
     * @return the corresponding Boolean-List
     */
    @TypeConverter
    public List<Boolean> from_String_to_Boolean_List(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.create().fromJson(value, BOOLEAN_LIST_TYPE);
    }

    /**
     * Turn a Boolean-List into the a json string
     * @param boolean_list the Boolean-List to turn into a json string
     * @return the corresponding json string
     */
    @TypeConverter
    public String from_Boolean_List_to_String(List<Boolean> boolean_list) {
        GsonBuilder gson_builder = new GsonBuilder();
        return gson_builder.create().toJson(boolean_list);
    }

    /**
     * Turn a json string of a String-List into a String-List
     * @param value the json string of a String-List
     * @return the corresponding String-List
     */
    @TypeConverter
    public List<String> from_String_to_String_List(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.create().fromJson(value, STRING_LIST_TYPE);
    }

    /**
     * Turn a String-List into the a json string
     * @param string_list the String-List to turn into a json string
     * @return the corresponding json string
     */
    @TypeConverter
    public String from_String_List_to_String(List<String> string_list) {
        GsonBuilder gson_builder = new GsonBuilder();
        return gson_builder.create().toJson(string_list);
    }

    /**
     * Turn a json string of a InetAdress into a InetAdress
     * @param value the json string of a InetAdress
     * @return the corresponding InetAdress
     */
    @TypeConverter
    public InetAddress from_String_to_InetAddress(String value){
        try {
            return InetAddress.getByName(value);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Turn a InetAdress into the a json string
     * @param value the InetAddress to turn into a json string
     * @return the corresponding json string
     */
    @TypeConverter
    public String from_InetAddress_to_String(InetAddress value) {
        if (value == null) {
            return "127.0.0.1";
        }
        return value.getHostAddress();
    }

    /**
     * Turn a Waypoint-List into the a json string
     * @param value the Waypoint-List to turn into a json string
     * @return the corresponding json string
     */
    @TypeConverter
    public String from_Waypoint_List_to_String(List<Waypoint> value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<Waypoint> serializer = new WaypointJsonGenerator();
        JsonSerializer<GeoPoint> geoPointJsonSerializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(Waypoint.class, serializer);
        gsonBuilder.registerTypeAdapter(GeoPoint.class, geoPointJsonSerializer);

        return gsonBuilder.create().toJson(value);
    }

    /**
     * Turn a json string of a Waypoint-List into a Waypoint-List
     * @param value the json string of a Waypoint-List
     * @return the corresponding Waypoint-List
     */
    @TypeConverter
    public List<Waypoint> from_Waypoint_List_String_to_Waypoint_List(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonDeserializer<Waypoint> deserializer = new WaypointJsonGenerator();
        JsonDeserializer<GeoPoint> geoPointJsonDeserializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(Waypoint.class, deserializer);
        gsonBuilder.registerTypeAdapter(GeoPoint.class, geoPointJsonDeserializer);

        return gsonBuilder.create().fromJson(value, WAYPOINT_LIST_TYPE);
    }

    /**
     * Turn a Polygon into a json string
     * @param value the Polygon to turn into a json string
     * @return the corresponding json string
     */
    @TypeConverter
    public String from_Polygon_to_String(Polygon value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<Polygon> serializer = new PolygonJsonGenerator();
        JsonSerializer<GeoPoint> geoPointJsonSerializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(Polygon.class, serializer);
        gsonBuilder.registerTypeAdapter(GeoPoint.class, geoPointJsonSerializer);

        return gsonBuilder.create().toJson(value);
    }

    /**
     * Turn a json string of a Polygon into a Polygon
     * @param value the json string of a Polygon
     * @return the corresponding Polygon
     */
    @TypeConverter
    public Polygon from_Polygon_String_to_Polygon(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonDeserializer<Polygon> deserializer = new PolygonJsonGenerator();
        JsonDeserializer<GeoPoint> geoPointJsonDeserializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(Polygon.class, deserializer);
        gsonBuilder.registerTypeAdapter(GeoPoint.class, geoPointJsonDeserializer);

        return gsonBuilder.create().fromJson(value, Polygon.class);
    }

    /**
     * Turn a json string of a GeoPoint into a GeoPoint
     * @param value the json string of a GeoPoint
     * @return the corresponding GeoPoint
     */
    @TypeConverter
    public GeoPoint from_GeoPoint_String_to_GeoPoint(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonDeserializer<GeoPoint> deserializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(GeoPoint.class, deserializer);

        return gsonBuilder.create().fromJson(value, GeoPoint.class);
    }

    /**
     * Turn a GeoPoint into a json string
     * @param value the GeoPoint to turn into a json string
     * @return the corresponding json string
     */
    @TypeConverter
    public String from_GeoPoint_to_String(GeoPoint value) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<GeoPoint> serializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(GeoPoint.class, serializer);
        return gsonBuilder.create().toJson(value);
    }

    /**
     * Turn a json string of a GeoPoint-List into a GeoPoint-List
     * @param value the json string of a GeoPoint-List
     * @return the corresponding GeoPoint-List
     */
    @TypeConverter
    public List<GeoPoint> from_GeoPoint_List_String_to_GeoPoint_List(String value) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonDeserializer<GeoPoint> deserializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(GeoPoint.class, deserializer);



        return gsonBuilder.create().fromJson(value, GEOPOINT_LIST_TYPE);
    }

    /**
     * Turn a GeoPoint-List into a json string
     * @param value the GeoPoint-List to turn into a json string
     * @return the corresponding json string
     */
    @TypeConverter
    public String from_GeoPoint_List_to_String(List<GeoPoint> value) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonSerializer<GeoPoint> serializer = new GeoPointJsonGenerator();
        gsonBuilder.registerTypeAdapter(GeoPoint.class, serializer);

        return gsonBuilder.create().toJson(value);
    }

    /**
     * The JsonGenerator used to turn GeoPoints into Json-Strings and back
     */
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

    /**
     * The JsonGenerator used to turn Polygons into Json-Strings and back
     */
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

    /**
     * The JsonGenerator used to turn Waypoints into Json-Strings and back
     */
    private class WaypointJsonGenerator implements JsonSerializer<Waypoint>, JsonDeserializer<Waypoint> {
        @Override
        public Waypoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject waypoint_json = json.getAsJsonObject();
            GeoPoint position = context.deserialize(waypoint_json.getAsJsonObject("position"), GeoPoint.class);
            String corresponding_route_id = waypoint_json.get("corresponding_route_id").getAsString();
            boolean is_navigation_point = waypoint_json.get("is_navigation_point").getAsBoolean();
            boolean milestone_completed = waypoint_json.get("milestone_completed").getAsBoolean();
            int waypoint_number = waypoint_json.get("waypoint_number").getAsInt();
            String mission_id = waypoint_json.get("mission_id").getAsString();

            Waypoint waypoint = new Waypoint(corresponding_route_id, waypoint_number, position,is_navigation_point, mission_id);
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
            waypoint_json.add("mission_id", context.serialize(src.mission_id));

            return waypoint_json;
        }
    }
}
