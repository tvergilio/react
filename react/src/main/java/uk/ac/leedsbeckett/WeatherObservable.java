package uk.ac.leedsbeckett;


import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

/**
 * Created by Thalita on 03-Nov-16.
 */
public class WeatherObservable {
    public static final String API_KEY = "33cd58f339fc44d5be0c16f7cae463ef";

    static Observable fetchWeatherInfo() throws IOException {
        URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/city?id=524901&APPID=" + API_KEY);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        Scheduler scheduler = Schedulers.newThread();

        return Observable.create(subscriber -> {
            Action0 action0 = () -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    if (subscriber.isUnsubscribed() || !reader.ready()) {
                        return;
                    }
                    subscriber.onNext(reader.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            scheduler.createWorker().schedulePeriodically(action0, 1, 60, TimeUnit.SECONDS);
        });
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Path p = Paths.get("weather.txt");

        fetchWeatherInfo().subscribe((x -> {
            try (BufferedWriter writer = Files.newBufferedWriter(p, Charset.defaultCharset(), StandardOpenOption.APPEND)) {
                writer.append(x.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        Thread.sleep(500000);

    }


}
