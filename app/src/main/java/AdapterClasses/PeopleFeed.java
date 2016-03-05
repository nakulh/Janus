package AdapterClasses;

import com.android.bike.janus.R;

import java.util.ArrayList;
import java.util.List;

public class PeopleFeed {

    int photo;
    String name;

    public PeopleFeed(){}

    public PeopleFeed(int photo, String name){
        this.photo = photo;
        this.name = name;
    }

    public List<PeopleFeed> createFeeds () {
        List<PeopleFeed> peopleFeeds = new ArrayList<>();

        for(int i = 0; i < 10; i++){
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Gandalf the White"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "James T. Kirk"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Luke Skywalker"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Light Yagami"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Yoda"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Bilbo Baggins"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Bruce Wayne"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Tony Stark"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Imhotep"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Bruce Banner"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Captain America"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Green Lantern"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Clark Kent"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Han Solo"));
            peopleFeeds.add(new PeopleFeed(R.drawable.ic_person, "Mr. Spock"));
        }

        return peopleFeeds;
    }
}
