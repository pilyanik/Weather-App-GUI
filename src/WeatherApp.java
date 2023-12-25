import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.IIOException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// сбор данных с API из внешних источников.
// GUI показывает

public class WeatherApp {
    // сбор данных о погоде по заданной локации
    public static JSONObject getWeatherData(String locationName) {
        // получение координат используя API геолокации
        JSONArray locationData = getLocationData(locationName);

        // извлечение долготы и широты
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longtitude = (double) location.get("longitude");

        // создание API запроса URL с координатами
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                            "latitude=" + latitude + "&longitude=" + longtitude +
                            "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Europe%2FMoscow";

        try {
            // вызов API и получение ответа
            HttpURLConnection conn = fetchApiResponse(urlString);

            // проверка ответа
            // 200 все ок
            if (conn.getResponseCode() != 200){
                System.out.println("Ошибка: Не могу соединится с API и получить данные по температуре");
                return null;
            }

            // сборка json данных
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()){
                // считаывать данные в StringBuilder
                resultJson.append(scanner.nextLine());
            }

            // закрыть сканер
            scanner.close();

            //закрыть url коннект
            conn.disconnect();

            // парсить через наши данные
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // сборка данных каждый час
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // для сборки текущих данные нам нужно получить индекс текущего времени
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // получение данных о температуре
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // получение данных о погоде
            JSONArray weathercode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // получение данных о влажности
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // получение данных о скорости ветра
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windspeedData.get(index);

            // создание JSON данные которые будут использоваться для frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;


        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // сбор географических координат по имени локации
    public static JSONArray getLocationData(String locationName){
        // заменяет пробелы на + в имени локации для добавления в формате API запроса
        locationName = locationName.replaceAll(" ", "+");

        // создание API url с параметрами локации
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try {
            // вызов api и получение ответа
            HttpURLConnection conn = fetchApiResponse(urlString);

            // проверка статуса ответа
            // 200 удачно
            if (conn.getResponseCode() != 200){
                System.out.println("Ошибка: Не могу соединится с API и получить данные по городу");
                return null;
            } else {
                // сбор API результата
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // собрать данные в StringBuilder
                while (scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                // закрываем сканер
                scanner.close();

                // закрываем url соединение
                conn.disconnect();

                // парсим JSON строки в JSON объект
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // получаем список данных о локации из API по имени
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        // не может найти локацию
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try {
            // попытки для создания подключения
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // установка GET запроса
            conn.setRequestMethod("GET");

            // соединение с API

            conn.connect();
            return conn;
        }catch (IIOException e){
            e.printStackTrace();
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // не может соединится
        return  null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();

        // поиск совпадения через список данных по времени
        for (int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                // возврат индекса
                return i;
            }
        }

        return 0;
    }
    public static String getCurrentTime() {
        // получение текущей даты и времени
        LocalDateTime currentDateTime = LocalDateTime.now();

        // формат даты 2023-09-02Т00:00 (это как считывается в API)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // форматирование и вывод текущей даты и времени
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    // конвертировать код погоды в читабельный формат
    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if (weathercode == 0L){
            weatherCondition = "Clear";
        } else if (weathercode <= 3L && weathercode > 0L){
            weatherCondition = "Cloudy";
        } else if ((weathercode >= 51L && weathercode <= 67L)
                    || (weathercode >= 80L && weathercode <= 99L)){
            weatherCondition = "Rain";
        } else if (weathercode >= 71L && weathercode <= 77L) {
            weatherCondition = "Snow";
        }
        return weatherCondition;
    }
}
