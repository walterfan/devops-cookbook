package com.github.walterfan.hello;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.github.walterfan.config.HelloConfig;
import com.github.walterfan.dto.CityWeather;
import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by yafan on 11/10/2017.
 *
 * refer to api http://lbsyun.baidu.com/index.php?title=car/api/weather
 */


@Component
public class HelloCacheLoader extends CacheLoader<String, CityWeather> {

    private static Logger logger = LoggerFactory.getLogger(HelloCacheLoader.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private String appToken;

    @Autowired
    private LoadingCache<String, CityWeather> cityWeatherCache;

    @Autowired
    private MetricRegistry metricRegistry;

    @DurationTimer(name="getCityWeather")
    public CityWeather getCityWeather(String city) throws ExecutionException {
        return this.cityWeatherCache.get(city);
    }


    @Override
    public CityWeather load(String city) throws Exception {

        String url = "http://api.map.baidu.com/telematics/v3/weather";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("location", city)
                .queryParam("output", "json")
                .queryParam("ak", appToken);

        ResponseEntity<CityWeather> resp = restTemplate.getForEntity(builder.toUriString(), CityWeather.class);

        logger.debug("response status: " + resp.getStatusCode());
        return resp.getBody();
    }

    public static void main(String[] args) throws ExecutionException {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HelloCacheConfig.class)) {
            HelloCacheLoader helloCacheLoder = (HelloCacheLoader) context.getBean("helloCacheLoader");

            CityWeather cityWeather = helloCacheLoder.getCityWeather("hefei");
            for(int i=0;i<10;++i) {
                cityWeather = helloCacheLoder.getCityWeather("hefei");
            }

            logger.info("----- weather -----");
            logger.info(cityWeather.toString());

            MetricRegistry metricRegistry = (MetricRegistry) context.getBean("metricRegistry");


            SortedMap<String, Histogram> histograms =  metricRegistry.getHistograms();

            for(Map.Entry<String, Histogram> entry: histograms.entrySet()) {
                Snapshot snapshot = entry.getValue().getSnapshot();
                logger.info("{}: size={},values: {}",  entry.getKey(), snapshot.size(), snapshot.getValues());
                logger.info(" max={}, min={}, mean={}, median={}",
                        snapshot.getMax(), snapshot.getMin(), snapshot.getMean(), snapshot.getMedian());
            }
        }
    }


}
