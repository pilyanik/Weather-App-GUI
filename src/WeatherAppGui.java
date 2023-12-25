import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;
    public WeatherAppGui() {
        //установка нашего gui и добавление title
        super("Weather App");

        // конфиг gui как только процесс будет закрыт
        // Устанавливает операцию, которая будет происходить по умолчанию, когда пользователь инициирует «закрытие» этого кадра.
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // установка размера в пикселях
        setSize(450, 650);

        // загрузка нашего GUI в центре экрана
        setLocationRelativeTo(null);

        //сделайте наш менеджер компоновки нулевым, чтобы вручную расположить наши компоненты в графическом интерфейсе.
        setLayout(null);

        //предотвращает изменение размера нашего GUI
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents(){
        // поле Поиска
        JTextField searchTextField = new JTextField();

        // установить место и размер наших компонентов
        searchTextField.setBounds(15, 15, 351, 45);

        // изменить фон и размер
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);

        // изображения погоды
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0,125,450,217);
        add(weatherConditionImage);

        // значение температуры
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0,350,450,54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        // текст в центре
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // описание погоды
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0,405,450,36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // влажность изображение
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15,500, 74, 66);
        add(humidityImage);

        // влажность текст
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90,500, 85,55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // скорость ветра изображение
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500,74,66);
        add(windspeedImage);

        // скорость ветра текст
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km</html>");
        windspeedText.setBounds(310,500,85,55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        // кнопка Поиска
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        // измените курсор на курсор в виде руки при наведении курсора на эту кнопку
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // получение локации от пользователя
                String userInput = searchTextField.getText();

                // валидация ввода - удалить пробелы и убедится что строка не пустая
                if (userInput.replaceAll("\\s", "").length() <= 0){
                    return;
                }

                // забрать данные о погоде
                weatherData = WeatherApp.getWeatherData(userInput);

                // обновление GUI

                // обновление изображение погоды
                String weatherCondition = (String) weatherData.get("weather_condition");

                // в зависимости от состояния, обновится изображение погоды которая соотвествует текущему состоянию
                switch(weatherCondition) {
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }

                // обновление текста о температуре
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                // обновление текста о состоянии погоды
                weatherConditionDesc.setText(weatherCondition);

                // обновление текста о влажности
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                // обновление текста о скорости ветра
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "km/h</html>");
            }
        });
        add(searchButton);
    }


    //используется для добавления картинки на нашем gui
    private ImageIcon loadImage(String resourcePath){
        try {
            // просмотреть картинку по пути
            BufferedImage image = ImageIO.read(new File(resourcePath));

            // вернуть картинку чтобы наше приложение отрендерило
            return new ImageIcon(image);
        }catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("Не нахожу путь к изображению");
        return null;
    }
}
