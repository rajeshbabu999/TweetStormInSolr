package com.tda.topology;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tda.solr.SolrBolt;
import com.tda.spout.TweetSpout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

/**
 * @author Rajesh Babu
 *
 */
public class TweetTopology {

	private static final Logger logger = LoggerFactory.getLogger(TweetTopology.class);

	public static void main(String[] args) throws Exception {

		 /*String consumerKey = "Zh3YNV6xr0fA4VFDiFmgCBaiR";
		 String consumerSecret = "PT12so9xLrxbLTEU1mzpY1uJ6iugrg9FrwVJW7WSuF2B0zfYSp";
		 String accessToken = "512215931-Xhr0Ofe3ajHMp3Fw8c5YvIDOLYtdlRSLnWsMwTnh";
		 String accessTokenSecret = "GmFPgD6ziEZAU9NDNRfslZltSVAqbEvO4jYl0IT8gAZ53";*/

		 /*String consumerKey = "u6OIkxrlNQn0CqbFgPJZMmQDa"; 
		 String consumerSecret = "cxrxEYvVh3136UsbY8z9vlSNWKRHO1n80CEFpbKZ9wci14XgfP"; 
		 String accessToken = "2564269056-q5TngQbM3G2yAjqpEPIRwZe6I7jKA0iF729UGP1";
		 String accessTokenSecret = "r9FHHVKRBYBXmVk8HfFsxypMEpiSszsbGAHyRO3OjaaTD";*/
		 
		 String consumerKey = "xlQPZXQjc78hqHLmjzaATO30j";
		 String consumerSecret = "4oBQSkIM62bTvgpQqSxNwRKJCYMbghr5Zvg6AhzQ13MCwKSCbG";
		 String accessToken = "499663420-UPx1f5cWTb2rJ5fyD0IjKmuePVMGMUNKVYyEostv";
		 String accessTokenSecret = "IhelPU92SGAJOevFH5pWLVANEIDCn4gIjyr1UwXA9GYq3";
	
		TopologyBuilder builder = new TopologyBuilder();
		
		//Filter directly to hdfs
		builder.setSpout("word", new TweetSpout(consumerKey, consumerSecret, accessToken, accessTokenSecret), 1);
		//builder.setBolt("matched", new TweetHbaseFilterBolt(), 3).shuffleGrouping("word");
		builder.setBolt("solr", new SolrBolt(), 4).shuffleGrouping("word");

		Config conf = new Config();
		//conf.put(Config.WORKER_CHILDOPTS, "-Xmx1G");
		conf.setDebug(true);
		
		if (args != null && args.length > 0) {
			conf.setNumWorkers(8);
			StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
		} else {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("TweetTopology", conf,builder.createTopology());
			//Utils.sleep(100000);
			//cluster.killTopology("TweetTopology");
			//cluster.shutdown();
		}
	}
}
