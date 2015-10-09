package com.tda.spout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Rajesh Babu
 *
 */
public class TweetSpout extends BaseRichSpout {

	private static final Logger logger = LoggerFactory.getLogger(TweetSpout.class);

	SpoutOutputCollector _collector;
	LinkedBlockingQueue<Status> queue = null;
	TwitterStream _twitterStream;
	String _custkey;
	String _custsecret;
	String _accesstoken;
	String _accesssecret;
	String[] keywords = { "Bahubali","Rudhramadevi","Rajamouli" };
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	Map<String, String> TweetMap = new HashMap<String, String>();
	JSONObject json = new JSONObject();
	String CreatedAtDate;
	String StatusId;
	String StatusText;
	String USerID;
	String UserName;
	String UserScreenName;
	String UserLocation;
	String UserDescription;
	String UserFollowers;
	String UserFriends;
	Double Latitude;
	Double Longitude;
	String Location;
	String ProfileImageUrl;

	public TweetSpout(String key, String secret) {
		_custkey = key;
		_custsecret = secret;
	}

	public TweetSpout(String key, String secret, String token, String tokensecret) {
		_custkey = key;
		_custsecret = secret;
		_accesstoken = token;
		_accesssecret = tokensecret;
	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		queue = new LinkedBlockingQueue<Status>(1000);
		_collector = collector;
		
		formatter.setTimeZone(TimeZone.getTimeZone("CST"));
		
		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {
				queue.offer(status);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice sdn) {
			}

			@Override
			public void onTrackLimitationNotice(int i) {
			}

			@Override
			public void onScrubGeo(long l, long l1) {
			}

			@Override
			public void onStallWarning(StallWarning warning) {
			}

			@Override
			public void onException(Exception e) {
				e.printStackTrace();
			}
		};

		ConfigurationBuilder config = new ConfigurationBuilder().setOAuthConsumerKey(_custkey).setOAuthConsumerSecret(_custsecret).setOAuthAccessToken(_accesstoken).setOAuthAccessTokenSecret(_accesssecret);
		TwitterStreamFactory fact = new TwitterStreamFactory(config.build());

		_twitterStream = fact.getInstance();
		_twitterStream.addListener(listener);
		// _twitterStream.sample();
		FilterQuery query = new FilterQuery().track(keywords);
		_twitterStream.filter(query);
	}

	@Override
	public void nextTuple() {
		Status ret = queue.poll();
		if (ret == null) {
			Utils.sleep(500);
		} else {
			CreatedAtDate = formatter.format(ret.getCreatedAt());
			StatusId = String.valueOf(ret.getId());
			StatusText = ret.getText();
			USerID = String.valueOf(ret.getUser().getId());
			UserName = ret.getUser().getName();
			UserScreenName = ret.getUser().getScreenName();
			UserLocation = ret.getUser().getLocation();
			UserDescription = ret.getUser().getDescription();
			UserFollowers = String.valueOf(ret.getUser().getFollowersCount());
			UserFriends = String.valueOf(ret.getUser().getFriendsCount());
			ProfileImageUrl = String.valueOf(ret.getUser().getProfileImageURL());
			GeoLocation location = ret.getGeoLocation();
			JSONObject obj = new JSONObject();
			if (location != null) {
				obj.put("lon", ret.getGeoLocation().getLongitude());
				obj.put("lat", ret.getGeoLocation().getLatitude());
			}
			Location = obj.get("lon") + "," + obj.get("lat");

			TweetMap.put("statusid", StatusId);
			TweetMap.put("userfollowers", UserFollowers);
			TweetMap.put("userscreenname", UserScreenName);
			TweetMap.put("userfriends", UserFriends);
			TweetMap.put("statustext", StatusText);
			TweetMap.put("userlocation", UserLocation);
			TweetMap.put("userdescription", UserDescription);
			TweetMap.put("location", Location);
			TweetMap.put("username", UserName);
			TweetMap.put("userid", USerID);
			TweetMap.put("createdatdate", CreatedAtDate);
			TweetMap.put("imageurl", ProfileImageUrl);

			_collector.emit(new Values(TweetMap));

		}
	}

	@Override
	public void close() {
		_twitterStream.shutdown();
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		Config ret = new Config();
		ret.setMaxTaskParallelism(1);
		return ret;
	}

	@Override
	public void ack(Object id) {
	}

	@Override
	public void fail(Object id) {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("tweet"));
	}
}
