package com.tda.sentiment;

import java.util.Map;
import java.util.StringTokenizer;

public class GetSentiment {
	
	public Double WhatIsTheSentiment(String statustext, Map<String, Integer> sentiments){
		
		StringTokenizer st = new StringTokenizer(statustext);

        //System.out.println("---- Split by space ------");
        int numberOfSentiments = 0;
        int sum = 0;
        while (st.hasMoreElements()) {
            String term = (String) st.nextElement();

            if (sentiments.containsKey(term)) {
                numberOfSentiments++;
                sum += sentiments.get(term);
            }
        }

        double sentimentValue = 0;
        if (numberOfSentiments > 0)
            sentimentValue = sum / numberOfSentiments;		
		return sentimentValue;		
	}
}
