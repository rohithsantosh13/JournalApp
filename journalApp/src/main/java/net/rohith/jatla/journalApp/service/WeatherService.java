package net.rohith.jatla.journalApp.service;

import net.rohith.jatla.journalApp.api.response.WeatherResponse;
import net.rohith.jatla.journalApp.cache.AppCache;
import net.rohith.jatla.journalApp.constants.PlaceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {
    @Value("${weather.api.key}")
    private String apiKey;
//
//    private static final String API = "http://api.weatherstack.com/current?access_key=API_KEY&query=CITY";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppCache appCache;

    @Autowired
    private RedisService redisService;

    public WeatherResponse getWeather(String city){
        WeatherResponse weatherResponse =  redisService.get("weather_of_"+city,WeatherResponse.class);
        if(weatherResponse !=null){
            return weatherResponse;
        }
        else{
            String finalApi = appCache.appCache.get(AppCache.keys.WEATHER_API.toString()).replace(PlaceHolder.API_KEY,apiKey).replace(PlaceHolder.CITY,city);
            ResponseEntity<WeatherResponse> response = restTemplate.exchange(finalApi, HttpMethod.GET, null, WeatherResponse.class);
            WeatherResponse body = response.getBody();
            if(body != null){
                redisService.set("weather_of_"+city,body, (long) 45l);
            }
            return body;
        }

    }

}

