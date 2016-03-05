package AdapterClasses;

import java.util.ArrayList;
import java.util.List;

public class RoutesFeed {

    String startLocationText;
    String endLocationText;
    String dateTimeText;
    String distanceText;
    String timeTravelledText;

    public RoutesFeed(){}

    public RoutesFeed( String startLocationText, String endLocationText, String dateTimeText,
                       String distanceText, String timeTravelledText){
        this.startLocationText = startLocationText;
        this.endLocationText = endLocationText;
        this.dateTimeText = dateTimeText;
        this.distanceText = distanceText;
        this.timeTravelledText = timeTravelledText;
    }

    public List<RoutesFeed> createFeeds () {
        List<RoutesFeed> routesFeed = new ArrayList<>();

        routesFeed.add(new RoutesFeed("Hazratganj", "Rajajipuram", "23 October 2015 18:17", "5 km", "20 mins"));
        routesFeed.add(new RoutesFeed("Aliganj", "Indiranagar", "23 October 2015 13:34", "12 km", "38 mins"));
        routesFeed.add(new RoutesFeed("Gomtinagar", "Chowk", "22 October 2015 15:56", "7 km", "27 mins"));
        routesFeed.add(new RoutesFeed("Alambagh", "Jankipuram", "22 October 2015 12:23", "11 km", "33 mins"));
        routesFeed.add(new RoutesFeed("Mahanagar", "Aminabad", "22 October 2015 08:32", "19 km", "58 mins"));

        return routesFeed;
    }
}
