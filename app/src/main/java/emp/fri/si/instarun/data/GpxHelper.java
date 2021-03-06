package emp.fri.si.instarun.data;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import emp.fri.si.instarun.InstarunApp;
import emp.fri.si.instarun.util.IsoDateHelper;
import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import org.joda.time.DateTime;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GpxHelper {

    /**
     * Saves Gpx to file. Only supports single track and single segment.
     * @param path Path to the Gpx file.
     * @param name Name of the track.
     * @param track Gpx instance.
     * @return True if successful.
     */
    public static boolean writeToFile(String path, String name, Gpx track) {

        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        String nameHeader = "<name>" + name + "</name>";

        String segments = "<trkseg>";
        for (TrackPoint p : track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints()) {
            String date = IsoDateHelper.dateToIsoString(p.getTime().toDate());
            segments += "<trkpt lat=\"" + p.getLatitude() + "\" "+
                               "lon=\"" + p.getLongitude() + "\">"+
                            "<ele>" + p.getElevation() + "</ele>"+
                            "<time>" + date + "</time>"+
                        "</trkpt>";
        }
        segments += "</trkseg>";

        String footer = "</trk></gpx>";

        try {
            FileOutputStream fos = InstarunApp.getInstance().openFileOutput(path, Context.MODE_PRIVATE);
            fos.write(header.getBytes());
            fos.write(nameHeader.getBytes());
            fos.write(segments.getBytes());
            fos.write(footer.getBytes());
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Reads Gpx from file.
     * @param path
     * @return Gpx instance or null if unsuccessful.
     */
    public static Gpx readFromFile(String path){
        GPXParser gpxParser = new GPXParser();
        try {
            FileInputStream stream = InstarunApp.getInstance().openFileInput(path);
            Gpx gpx = gpxParser.parse(stream);
            stream.close();
            return gpx;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates Gpx instance from a list of locations.
     * @param track
     * @return
     */
    public static Gpx buildGpx(List<Location> track){
        Gpx.Builder gpxBuilder = new Gpx.Builder();
        Track.Builder trackBuilder = new Track.Builder();
        TrackSegment.Builder tsBuilder = new TrackSegment.Builder();
        TrackPoint[] points = new TrackPoint[track.size()];
        for (int i = 0; i < points.length; i++){
            Location loc = track.get(i);
            TrackPoint.Builder pointBuilder = new TrackPoint.Builder();
            pointBuilder.setLatitude(loc.getLatitude());
            pointBuilder.setLongitude(loc.getLongitude());
            pointBuilder.setElevation(loc.getAltitude());
            pointBuilder.setTime(new DateTime(loc.getTime()).toDateTimeISO());
            points[i] = pointBuilder.build();
        }
        tsBuilder.setTrackPoints(Arrays.asList(points));
        trackBuilder.setTrackSegments(Collections.singletonList(tsBuilder.build()));
        gpxBuilder.setTracks(Collections.singletonList(trackBuilder.build()));
        return gpxBuilder.build();
    }
}
