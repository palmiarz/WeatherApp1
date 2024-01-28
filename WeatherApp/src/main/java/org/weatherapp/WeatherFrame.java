package org.weatherapp;

import com.google.gson.Gson;
import com.squareup.okhttp.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class WeatherFrame extends JFrame {
    private JLabel cityLabel;
    private JTextField cityField;
    private JButton searchButton;
    private JLabel descriptionLabel;
    private JLabel temperatureLabel;
    private JLabel windLabel;
    private JLabel humidityLabel;
    private JList<String> historyList;
    private DefaultListModel<String> historyListModel;
    private JScrollPane historyScrollPane;

    private final String API_KEY = "e85cff64b30a0753f8097ac58717ae4b";

    public WeatherFrame() {
        setTitle("Aplikacja pogodowa");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cityLabel = new JLabel("Wpisz miasto:");
        cityField = new JTextField(20);
        searchButton = new JButton("Szukaj");

        descriptionLabel = new JLabel();
        temperatureLabel = new JLabel();
        windLabel = new JLabel();
        humidityLabel = new JLabel();

        // tworzenie listy i jej wyświetlanie
        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        JScrollPane historyScrollPane = new JScrollPane(historyList);

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(cityLabel);
        topPanel.add(cityField);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        centerPanel.add(descriptionLabel);
        centerPanel.add(temperatureLabel);
        centerPanel.add(windLabel);
        centerPanel.add(humidityLabel);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(new JLabel("Historia wyszukiwania:"), BorderLayout.NORTH);
        bottomPanel.add(historyScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // zamiana tekstu z cityField na zmienna String
                String city = cityField.getText();

                if (!isValidCity(city)) {
                    JOptionPane.showMessageDialog(WeatherFrame.this,
                            "Wprowadź poprawną nazwę miasta.", "Błędna nazwa miasta",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // tworzymy nowy wątek dla metody getWeatherData
                Thread weatherThread = new Thread(() -> {
                    WeatherData data = getWeatherData(city);
                    SwingUtilities.invokeLater(() -> handleWeatherData(data, city));
                });
                weatherThread.start();
            }
        });

        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    //  Pobiera źródło zdarzenia myszy (e.getSource()),
                    //  które powinno być obiektem JList, a następnie rzutuje je na JList<String>
                    JList<String> list = (JList<String>) e.getSource();
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        // Pobiera wybrany element z modelu listy na podstawie indeksu
                        String selectedCity = list.getModel().getElementAt(index);
                        // Ustawia wybrane miasto w polu tekstowym
                        cityField.setText(selectedCity);
                    }
                }
            }
        });

        //można to dać wyżej bez większezgo znaczenia :)
        String defaultCity = getCityByIp();
        cityField.setText(defaultCity);
    }

    private boolean isValidCity(String city) {
        // Prosta walidacja: sprawdza czy nazwa miasta składa się z liter
        return city.matches("[a-zA-Z]+");
    }

    // przetwarzanie i aktualizacja danych pogodowych
    private void handleWeatherData(WeatherData data, String city) {
        if (data != null && data.getWeather() != null && data.getWeather().length > 0) {
            WeatherData.MainData mainData = data.getMain();
            WeatherData.WindData windData = data.getWind();
            WeatherData.WeatherInfo[] weatherInfoArray = data.getWeather();

            // Pobiera opis pogody (description) z pierwszego elementu tablicy weatherInfoArray
            String weatherDescription = weatherInfoArray[0].getDescription();

            descriptionLabel.setText("Opis pogody: " + weatherDescription);
            temperatureLabel.setText("Temperatura: " + mainData.getTemp() + "°C");
            windLabel.setText("Wiatr: " + windData.getSpeed() + " km/h");
            humidityLabel.setText("Wilgotność: " + mainData.getHumidity() + "%");

            // Sprawdź, czy miasto jest już w historii
            if (!historyListModel.contains(city)) {
                // Dodaj wybrane miasto do historii na początek listy
                historyListModel.insertElementAt(city, 0);
            }
        } else {
            clearWeatherLabels();
            JOptionPane.showMessageDialog(WeatherFrame.this,
                    "Nie udało się pobrać informacji pogodowych dla miasta " + city,
                    "Błąd pobierania danych", JOptionPane.ERROR_MESSAGE);
        }
    }

    // pobieranie danych pogodowych dla konkretnego miasta
    private WeatherData getWeatherData(String city) {
        // klasa do wykonywania żądań z http
        OkHttpClient client = new OkHttpClient();
        // tworzenie URL do zapytania do API OpenWeatherMap.
        // parametr units=metric oznacza, że dane pogodowe mają być zwracane w jednostkach metrycznych(km/h itp.).
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric";
        // tworzenie obiektu żądania
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            // wykonanie żądania HTTP do serwera za pomocą OkHttpClient i Response
            Response response = client.newCall(request).execute();
            // odczytanie treści odpowiedzi HTTP jako łańcuch znaków (JSON)
            // przy użyciu metody string() na obiekcie ResponseBody
            String json = response.body().string();
            //Gson służy do przekształcania danych JSON na obiekty Java
            Gson gson = new Gson();
            // wykorzystanie Gson do przekształcenia łańcucha znaków JSON (json)
            // na obiekt klasy WeatherData.
            WeatherData data = gson.fromJson(json, WeatherData.class);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            // wrazie błędu dla bezpieczeństwa zwraca po brostu null :p
            return null;
        }
    }

    private String getCityByIp() {
        // te kilka linijek to ta sama bajka jak w metodzie wyżej
        OkHttpClient client = new OkHttpClient();
        String url = "https://ipinfo.io/json";
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String jsonResponse = response.body().string();
            Gson gson = new Gson();
            IpInfo ipInfo = gson.fromJson(jsonResponse, IpInfo.class);

            // pobranie nazwy miasta z obiektu IpInfo.
            // jeśli obiekt IpInfo nie jest null, używana jest metoda getCity() (metoda z tej klasy IpInfo),
            // w przeciwnym razie zwracany jest "".
            // BTW ciekawy zapis nigdy czegoś takiego nie widziałem xd
            String city = ipInfo != null ? ipInfo.getCity() : "";
            return city;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void clearWeatherLabels() {
        descriptionLabel.setText("");
        temperatureLabel.setText("");
        windLabel.setText("");
        humidityLabel.setText("");
    }

    // to w sumie bez sensu bo i tak odpalamy apke z WeatherApp.java a nie z tej klasy
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherFrame frame = new WeatherFrame();
            frame.setVisible(true);
        });
    }
}
