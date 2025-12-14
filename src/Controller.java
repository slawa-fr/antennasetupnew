import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.sqlite.JDBC;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

public class Controller extends Component  {

    @FXML
    private Button button1, button2, button3, button4, button5, button6, button7, button8, button9;

    @FXML
    private TableView<ObservableList> TableView1, TableView2;

    @FXML
    private Label label1, label2, label3, label4, label7, label8, label9, label10, label11, label12, label13, label14, label15, label16;

    @FXML
    private TextField textField1, textField2, textField3, textField4, textField5, textField6, textField7, textField8, textField9, textField10, textField11;

    @FXML
    private RadioButton radio1, radio2;

    private ObservableList<ObservableList> data;

    public String SQL = "SELECT * FROM satellite";

    private String nameKA;
    private String namestation;
    private String id;
    private String freq;
    private String pol;
    private String azreal;
    private String umreal;
    private String comment;
    private double lat;   // 54.84509 Смоленск
    private double lon;   // 32.09363 Смоленск
    private double pt;

// Константа, в которой хранится адрес подключения
    private String CON_STR = "jdbc:sqlite:Z:/database_sat.db";
    // Объект, в котором будет храниться соединение с БД
    private Connection connection;
    private  static final String CURRENTDIRECTORY = "user.dir";

    private String pathToDatabase;

    @FXML
    void initialize() {

// Проверка существования каталога database
        File theDir0 = new File(System.getProperty(CURRENTDIRECTORY),"database");
        if (!theDir0.exists())
            new File(System.getProperty(CURRENTDIRECTORY), "database").mkdir();

// Проверка существования всех нужных файлов в папке database
        File theDir1 = new File(System.getProperty(CURRENTDIRECTORY),"database/database_sat.db");
        File theDir2 = new File(System.getProperty(CURRENTDIRECTORY),"database/satellite.csv");
        File theDir3 = new File(System.getProperty(CURRENTDIRECTORY),"database/satellite.xlsx");
        File theDir4 = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");

        if (!theDir1.exists())
            createFile1();
        if (!theDir2.exists())
            createFile2();
        if (!theDir3.exists())
            createFile3();
        if (!theDir4.exists())
            createFile4();

// Проверим есть ли файл setting.properties  в папке database
        checkingFile();

// Загружаем  setting.properties из папки database
        File theDir = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(theDir));
        } catch (IOException e) {
            e.printStackTrace();
        }

// Получить значения nameKA, ПТ, широты - lat и долготы - lon из database/setting.properties"
        nameKA = appProps.getProperty("nameKA", "Express AM7");
        id = appProps.getProperty("id", "1");
        namestation = appProps.getProperty("namestation", "Merlino");
        label11.setText(namestation);
        pt = Double.parseDouble(appProps.getProperty("pt", "40.0"));
        lat = Double.parseDouble(appProps.getProperty("lat", "54"));
        lon = Double.parseDouble(appProps.getProperty("lon", "32"));

// Установить их в textField
        textField10.setText(String.valueOf(id));
        textField11.setText(String.valueOf(namestation));
        textField1.setText(String.valueOf(nameKA));
        textField6.setText(String.valueOf(pt));
        textField2.setText(String.valueOf(lat));
        textField3.setText(String.valueOf(lon));
        textField7.setText("");
        textField8.setText("");
        pathToDatabase = theDir1.getPath();
        CON_STR = "jdbc:sqlite:" + pathToDatabase;

// активные поля в зависимости от радиокнопки
        textField1.setEditable(true);
        textField1.setDisable(false);
        textField6.setEditable(false);
        textField6.setDisable(true);

        if(textField1.getText().length() == 0 || textField6.getText().length() == 0){
            // Поле пустое
            System.out.println("Поле Имя КА или ПТ пустое");
        }else {
            // Поле заполнено
            if(radio1.isSelected()){
                // Если выбрана кнопка 1
                nameKA = textField1.getText().trim();
                SQL = "SELECT * FROM satellite WHERE SATELLITE_NAME like '%" + nameKA + "%'";
                TableView1.getItems().clear();
                TableView1.getColumns().clear();
                TableView1.refresh();
                label1.setText("SQL = " + SQL);
                label2.setTextFill(Color.web("#FF0000"));
                label2.setText("Выберите КА из Таблицы");
                label7.setTextFill(Color.web("#000000"));
                label7.setText("Азимут = ");
                label8.setTextFill(Color.web("#000000"));
                label8.setText("Угол места = ");
                sql();
            }else{
                // Если выбрана кнопка 2
                pt = Double.parseDouble(textField6.getText().trim());
                SQL = "SELECT * FROM satellite WHERE PT = " + pt + " ";
                TableView1.getItems().clear();
                TableView1.getColumns().clear();
                TableView1.refresh();
                label1.setText("SQL = " + SQL);
                label2.setTextFill(Color.web("#FF0000"));
                label2.setText("Выберите КА из Таблицы");
                label7.setText("Азимут = ");
                label8.setText("Угол места = ");
                sql();
                calculation();
            }
        }

//Нажатие на кнопку 1 - Вывести все данные из БД в таблицу - начало
        button1.setOnAction(event -> {
            data = FXCollections.observableArrayList();
            TableView1.getItems().clear();
            TableView1.getColumns().clear();
            TableView1.refresh();
            SQL = "SELECT * FROM satellite";
            label1.setText("SQL = " + SQL);
            textField1.setText("");
            label2.setTextFill(Color.web("#FF0000"));
            label2.setText("Выберите КА из Таблицы");
            textField6.setText("");
            textField7.setText("");
            textField8.setText("");
            label7.setTextFill(Color.web("#000000"));
            label7.setText("Азимут = ");
            label8.setTextFill(Color.web("#000000"));
            label8.setText("Угол места = ");
            sql();
            select();
        });
//Нажатие на кнопку 1 - Вывести все данные из БД в таблицу - конец


//Нажатие на кнопку 2 - Очистить таблицу - начало
        button2.setOnAction(event -> {
            TableView1.getItems().clear();
            TableView1.getColumns().clear();
            TableView1.refresh();
            textField1.setText("");
            textField4.setText("");
            textField5.setText("");
            textField7.setText("");
            textField8.setText("");
            textField9.setText("");
            label2.setTextFill(Color.web("#000000"));
            label2.setText("Введите имя КА или ПТ для расчёта");
            label1.setText("");
            textField6.setText("");
            label7.setTextFill(Color.web("#000000"));
            label7.setText("Азимут = ");
            label8.setTextFill(Color.web("#000000"));
            label8.setText("Угол места = ");
            label4.setTextFill(Color.web("#000000"));
            label4.setText("Введите координаты вашей антенны");
            label9.setTextFill(Color.web("#000000"));
            label9.setText("Частота (FREQ) *");
            label10.setTextFill(Color.web("#000000"));
            label10.setText("Поляризация (POL) *");
        });
//Нажатие на кнопку 2 - Очистить таблицу - конец


//Нажатие на кнопку 3 - Занести частоту, поляризацию и подспутниковую точку в Базу данных - начало
        button3.setOnAction(event -> {
// Проверка за заполненность полей Имя КА, частота, поляризация
                    if (textField1.getText().length() == 0 || textField6.getText().length() == 0) {
                        label2.setTextFill(Color.web("#FF0000"));
                        label2.setText("Введите имя КА или ПТ для расчёта");

                    }else if (textField4.getText().length() == 0){
                        label9.setTextFill(Color.web("#FF0000"));
                        label9.setText("Частота (FREQ) *");

                    }else if (textField5.getText().length() == 0){
                        label10.setTextFill(Color.web("#FF0000"));
                        label10.setText("Поляризация (POL) *");

                    }else if (textField7.getText().length() == 0){
                        label14.setTextFill(Color.web("#FF0000"));
                        label14.setText("AZ_REAL *");

                    }else if (textField8.getText().length() == 0){
                        label15.setTextFill(Color.web("#FF0000"));
                        label15.setText("UM_REAL *");

                    }else if (textField9.getText().length() == 0){
                        label16.setTextFill(Color.web("#FF0000"));
                        label16.setText("COMMENT *");

                    }else {
                        label2.setTextFill(Color.web("#000000"));
                        label2.setText("Введите имя КА или ПТ для расчёта");
                        label9.setTextFill(Color.web("#000000"));
                        label9.setText("Частота (FREQ) *");
                        label10.setTextFill(Color.web("#000000"));
                        label10.setText("Поляризация (POL) *");
                        label14.setTextFill(Color.web("#000000"));
                        label14.setText("AZ_REAL *");
                        label15.setTextFill(Color.web("#000000"));
                        label15.setText("UM_REAL *");
                        label16.setTextFill(Color.web("#000000"));
                        label16.setText("COMMENT *");
                        nameKA = textField1.getText().trim().replace(",", ".");
                        freq = textField4.getText().trim().replace(",", ".");
                        pol = textField5.getText().trim().replace(",", ".");
                        azreal = textField7.getText().trim().replace(",", ".");
                        umreal = textField8.getText().trim().replace(",", ".");
                        comment = textField9.getText().trim().replace(",", ".");
                        pt = Double.parseDouble(textField6.getText().trim().replace(",", "."));
                        SQL = "UPDATE satellite SET FREQ = " + freq + ", POL = '" + pol + "'" +   ", PT = '" + pt + "'" + ", AZ_REAL = '" + azreal + "'" + ", UM_REAL = '" + umreal + "'" + ", COMMENT = '" + comment + "'" + " WHERE SATELLITE_NAME = '" + nameKA + "'"; // OK
                        sql2();
                        label1.setText("SQL = " + SQL);
                    }
        });
//Нажатие на кнопку 3 - Занести частоту, поляризацию и подспутниковую точку в Базу данных - конец

//Нажатие на кнопку 4 - рассчитать Азимут и Угол места - начало
        button4.setOnAction(event -> {

            if(radio1.isSelected()){
                // Если выбрана кнопка 1
                nameKA = textField1.getText().trim();
                SQL = "SELECT * FROM satellite WHERE SATELLITE_NAME like '%" + nameKA + "%'";
                TableView1.getItems().clear();
                TableView1.getColumns().clear();
                TableView1.refresh();
                label1.setText("SQL = " + SQL);
                label2.setTextFill(Color.web("#FF0000"));
                label2.setText("Выберите КА из Таблицы");
                label7.setTextFill(Color.web("#000000"));
                label7.setText("Азимут = ");
                label8.setTextFill(Color.web("#000000"));
                label8.setText("Угол места = ");
                sql();
            }else{
                // Если выбрана кнопка 2
                pt = Double.parseDouble(textField6.getText().trim());
                SQL = "SELECT * FROM satellite WHERE PT = " + pt + " ";
                TableView1.getItems().clear();
                TableView1.getColumns().clear();
                TableView1.refresh();
                label1.setText("SQL = " + SQL);
                label2.setTextFill(Color.web("#FF0000"));
                label2.setText("Выберите КА из Таблицы");
                label7.setTextFill(Color.web("#000000"));
                label7.setText("Азимут = ");
                label8.setTextFill(Color.web("#000000"));
                label8.setText("Угол места = ");
                sql();
                calculation();
            }
        });
//Нажатие на кнопку 4 - рассчитать Азимут и Угол места - конец


//Нажатие на кнопку 5 - Описание программы - начало
        button5.setOnAction(event -> {
// Справка по программе через поток - Potok1
            Potok1.main();
        });
//Нажатие на кнопку 5 - Описание программы  - конец

//Нажатие на кнопку 6 - Значёк + - начало
        button6.setOnAction(event -> {
            textField7.setText(label7.getText().replace("АЗ = ", ""));
            textField8.setText(label8.getText().replace("УМ = ", ""));
        });
//Нажатие на кнопку 6 - Значёк +   - конец


//Нажатие на кнопку 7 - Найти - начало
        button7.setOnAction(event -> {

            if(radio1.isSelected()){
                // Если выбрана кнопка 1
                nameKA = textField1.getText().trim();
                SQL = "SELECT * FROM satellite WHERE SATELLITE_NAME like '%" + nameKA + "%'";
                TableView1.getItems().clear();
                TableView1.getColumns().clear();
                TableView1.refresh();
                label1.setText("SQL = " + SQL);
                label2.setTextFill(Color.web("#FF0000"));
                label2.setText("Выберите КА из Таблицы");
                label7.setTextFill(Color.web("#000000"));
                label7.setText("Азимут = ");
                label8.setTextFill(Color.web("#000000"));
                label8.setText("Угол места = ");
                sql();
            }else{
                // Если выбрана кнопка 2
                pt = Double.parseDouble(textField6.getText().trim());
                SQL = "SELECT * FROM satellite WHERE PT = " + pt + " ";
                TableView1.getItems().clear();
                TableView1.getColumns().clear();
                TableView1.refresh();
                label1.setText("SQL = " + SQL);
                label2.setTextFill(Color.web("#FF0000"));
                label2.setText("Выберите КА из Таблицы");
                label7.setTextFill(Color.web("#000000"));
                label7.setText("Азимут = ");
                label8.setTextFill(Color.web("#000000"));
                label8.setText("Угол места = ");
                sql();
            }
        });
//Нажатие на кнопку  7 - Найти - конец


//Нажатие на кнопку 8
        button8.setOnAction(event -> {
            System.out.println("Нажата кнопка 8");
            data = FXCollections.observableArrayList();
//            valueSelect = true;
            TableView2.getItems().clear();
            TableView2.getColumns().clear();
            TableView2.refresh();
            SQL = "SELECT * FROM station";
            label1.setText("SQL = " + SQL);
            label11.setText(namestation);
//            label2.setText("Выберите основной КА из таблицы");
//            label2.setTextFill(Color.web("#FF0000"));
            sql3();
            select2();
        });
//Нажатие на кнопку 8


//Нажатие на кнопку 9 - Занести  в Базу данных - начало
        button9.setOnAction(event -> {
// Проверка за заполненность полей Имя КА, частота, поляризация
            if (textField10.getText().length() == 0 || textField11.getText().length() == 0 || textField2.getText().length() == 0 || textField3.getText().length() == 0) {
                label4.setTextFill(Color.web("#FF0000"));
                label4.setText("ЗАПОЛНИТЕ ВСЕ ПОЛЯ");

            }else {
                label4.setTextFill(Color.web("#000000"));
                label4.setText("координаты антенны");
                id = textField10.getText().trim().replace(",", ".");
                namestation = textField11.getText().trim().replace(",", ".");
                lat = Double.parseDouble(textField2.getText().trim().replace(",", "."));
                lon = Double.parseDouble(textField3.getText().trim().replace(",", "."));
// Образец
//                SQL = "UPDATE satellite SET FREQ = " + freq + ", POL = '" + pol + "'" +   ", PT = '" + pt + "'" + ", AZ_REAL = '" + azreal + "'" + ", UM_REAL = '" + umreal + "'" + ", COMMENT = '" + comment + "'" + " WHERE SATELLITE_NAME = '" + nameKA + "'"; // OK



                SQL = "UPDATE station SET NAMESTATION = '" + namestation + "'" + ", LATITUDE = '" + lat + "'" +   ", LONGITUDE = '" + lon  + "'" + " WHERE ID = '" + id + "'"; // OK
                System.out.println(" SQL = " + SQL);
                sql4();
                label1.setText("SQL = " + SQL);
            }
        });
//Нажатие на кнопку 9 - Занести  в Базу данных - конец

// Этот метод нужен если не была нажата кнопка "Вывести все данные из Базы данных в таблицу" а сразу введен КА и нажата кнопка "Найти и рассчитать АЗ и УМ"
        select();
    }

// Радиокнопка выбора режима ввода частоты - 5
    public void onRadio1(javafx.event.ActionEvent actionEvent) {
        System.out.println("onRadio1");
        textField1.setEditable(true);
        textField1.setDisable(false);
        textField6.setEditable(false);
        textField6.setDisable(true);
    }

// Радиокнопка выбора режима ввода частоты - 6
    public void onRadio2(javafx.event.ActionEvent actionEvent) {
        System.out.println("onRadio2");
        textField1.setEditable(false);
        textField1.setDisable(true);
        textField6.setEditable(true);
        textField6.setDisable(false);
    }

// Метод сохранения в properties
    void saveToPropertiesSetting() {
// Загружаем  setting.properties из папки database
        File theDir17 = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");
        Properties appProps = new Properties();
        try {
            appProps.load(new BufferedReader(new InputStreamReader(new FileInputStream(theDir17), "UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        nameKA = textField1.getText();
        id = textField10.getText();
        namestation = textField11.getText();
        pt = Double.parseDouble(textField6.getText());
        lat = Double.parseDouble(textField2.getText().replace(",", "."));
        lon = Double.parseDouble(textField3.getText().replace(",", "."));

        appProps.setProperty("nameKA", String.valueOf(nameKA));
        appProps.setProperty("id", String.valueOf(id));
        appProps.setProperty("namestation", String.valueOf(namestation));
        appProps.setProperty("pt", String.valueOf(pt));
        appProps.setProperty("lat", String.valueOf(lat));
        appProps.setProperty("lon", String.valueOf(lon));

// Сохраним в setting.properties внесенные изменения из текстовых полей
        String newAppProps = "database/setting.properties";
        try {
            appProps.store(new FileWriter(newAppProps), "store");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// Округление чисел: Метод Math.round()
// https://www.internet-technologies.ru/articles/kak-v-java-okruglit-chislo-do-n-znakov-posle-zapyatoy.html

    public static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

// Метод расчета азимута и угла места
    public void calculation (){
//условие: пока не введены координаты расчет не будет производиться
        if (textField2.getText().length() == 0 | textField3.getText().length() == 0) {
            label4.setTextFill(Color.web("#FF0000"));
            label4.setText("координаты не введены");
        } else {
            // Координаты введены
            if (textField6.getText().length() == 0) {
                // Не введена подспутниковая точка
                label2.setTextFill(Color.web("#FF0000"));
                label2.setText("Выберите КА из Таблицы");
            }else {
                // подспутниковая точка ведена
                lat = Double.parseDouble(textField2.getText().replace(",", "."));
                lon = Double.parseDouble(textField3.getText().replace(",", "."));
// округление до 3-х знаков используя метод roundAvoid
                double lat2 = roundAvoid(lat, 3);
                double lon2 = roundAvoid(lon, 3);
                double v = lat2; // работает с дробными числами; int a = Integer.valueOf(editText.getText().toString()); - работает только с целыми числами
                double g2 = lon2;
                double g1 = Double.valueOf(textField6.getText());
                //Формула для вычисления азимута
                //ф= 180° + arctg{tg(g2 - g1)/sin(v)}
                //где g1 - долгота спутника, g2 - долгота места приема, v - широта места приема.
                // Math.tan(c) - тангенс; Math.toRadians(90) - перевести 90 градусов в радианы; Math.toDegrees - в градусы;  Math.round - округление
                // старый но рабочий вариант формулы
                //textView.setText("Азимут (АЗ) = " + Math.round((180 + Math.toDegrees(Math.atan(Math.tan(Math.toRadians(g2) - Math.toRadians(g1)) / Math.sin(Math.toRadians(v)))))) + " градусов");
                double tanG = Math.tan(Math.toRadians(g2) - Math.toRadians(g1));
                double sinV = Math.sin(Math.toRadians(v));
                double znachenie1 = 180 + Math.toDegrees(Math.atan(tanG / sinV));
                label7.setTextFill(Color.web("#0000FF"));
                label7.setText("АЗ = " + String.format("%.1f", znachenie1).replace(",", "."));

                //Формула для вычисления угла места
                //F=arctg{[Cos(g2 - g1) x Cos(v) - 0.151]/sqrt(1 - Соs2(g2 - g1) х Cos2(v)]}
                //где g1 - долгота спутника, g2 - долгота места приема, v - широта места приема
                // Math.tan(c) - тангенс; Math.toRadians(90) - перевести 90 градусов в радианы; Math.toDegrees - в градусы;  Math.round - округление
                // старый но рабочий вариант формулы
                //textView5.setText("Угол места (УМ) = " + Math.round(Math.toDegrees(Math.atan((Math.cos(Math.toRadians(g2) - Math.toRadians(g1)) * Math.cos(Math.toRadians(v)) - 0.151) / Math.sqrt(1 - Math.cos(Math.toRadians(g2) - Math.toRadians(g1)) * Math.cos(Math.toRadians(v)))))) + " градусов");
                double cosG = Math.cos(Math.toRadians(g2) - Math.toRadians(g1)); //переменная для хранения части формулы: Соs2(g2 - g1)
                double cosV = Math.cos(Math.toRadians(v)); //переменная для хранения части формулы: Cos2(v)
                double znachenie2 = Math.toDegrees(Math.atan(((cosG * cosV) - 0.151) / Math.sqrt(1 - cosG * cosG * cosV * cosV)));
                label8.setTextFill(Color.web("#0000FF"));
                label8.setText("УМ = " + String.format("%.1f", znachenie2).replace(",", "."));
                label4.setTextFill(Color.web("#000000"));
                label4.setText("Введите координаты вашей антенны");
            }
        }
        saveToPropertiesSetting();
    }

    public void select(){
// Получить выбранное значение - одну строку
// https://metanit.com/java/javafx/4.13.php
        TableView.TableViewSelectionModel<ObservableList> selectionModel = TableView1.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(new ChangeListener<ObservableList>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList> observable, ObservableList oldValue, ObservableList newValue) {
// Проверка newValue на null, если нулевое, то ничего не делаем (иначе выскакивала ошибка)
                    if (newValue == null) {
                        // do something
                        //System.out.println("newValue = " + newValue);
                    }else {
                        label1.setText("Selected: " + newValue);
// Получаем строку со всеми значениями, разделенными запятыми
                        String text = newValue.toString();
                        //System.out.println("text = " + text);
// Выбранную строку со всеми значениями занесем в массив строк, разделенными запятыми
                        String[] words = text.split(",");
// Выберем только нужные значения, т.е. столбцы и выведем их в textField1 в зависимости от того какая была нажата кнопка и соответсвенно было значение переменной valueSelect
                        for (int i = 0; i < words.length; i++) {
                            //System.out.println(words[i]);
                            label2.setTextFill(Color.web("#000000"));
                            label2.setText("Выберите КА из Таблицы");
                            label9.setTextFill(Color.web("#000000"));
                            label9.setText("Частота (FREQ) *");
                            label10.setTextFill(Color.web("#000000"));
                            label10.setText("Поляризация (POL) *");
                            pt = Double.parseDouble(words[1].trim());
                            textField6.setText(words[1].trim()); // PT
                            textField1.setText(words[2].trim()); // Имя КА
                            textField4.setText(words[3].trim()); // Частота
                            freq = textField4.getText();
                            textField5.setText(words[4].trim()); // Поляризация
                            pol = textField5.getText();
                            textField7.setText(words[5].trim()); // Азимут
                            textField8.setText(words[6].trim()); // Угол места
                            textField9.setText(words[7].replace(']', ' ').trim()); // Комментарий
                            calculation();
                            saveToPropertiesSetting();
                    }

                }
            }
        });
    }


    public void select2(){
// Получить выбранное значение - одну строку
// https://metanit.com/java/javafx/4.13.php
        TableView.TableViewSelectionModel<ObservableList> selectionModel = TableView2.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(new ChangeListener<ObservableList>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList> observable, ObservableList oldValue, ObservableList newValue) {
// Проверка newValue на null, если нулевое, то ничего не делаем (иначе выскакивала ошибка)
                if (newValue == null) {
                    // do something
                    //System.out.println("newValue = " + newValue);
                }else {
                    label1.setText("Selected: " + newValue);
// Получаем строку со всеми значениями, разделенными запятыми
                    String text = newValue.toString();
                    System.out.println("text = " + text);
// Выбранную строку со всеми значениями занесем в массив строк, разделенными запятыми
                    String[] words = text.split(",");
// Выберем только нужные значения, т.е. столбцы и выведем их в textField1 в зависимости от того какая была нажата кнопка и соответсвенно было значение переменной valueSelect
                    for (int i = 0; i < words.length; i++) {
                        //System.out.println(words[i]);
//                        label2.setTextFill(Color.web("#000000"));
//                        label2.setText("Выберите КА из Таблицы");
//                        label9.setTextFill(Color.web("#000000"));
//                        label9.setText("Частота (FREQ) *");
//                        label10.setTextFill(Color.web("#000000"));
//                        label10.setText("Поляризация (POL) *");
//                        pt = Double.parseDouble(words[1].trim());
                        textField10.setText(words[0].replace('[', ' ').trim()); // ID
                        textField11.setText(words[1].trim()); // NAMESTATION
                        textField2.setText(words[2].trim()); // latitude
                        textField3.setText(words[3].replace(']', ' ').trim()); // longitude

                        lat = Double.parseDouble(textField2.getText());
                        lon = Double.parseDouble(textField3.getText());
                        id = textField10.getText();
                        namestation = textField11.getText();
                        label11.setText(namestation);
                        System.out.println(" id = "  + id + " namestation = " + namestation + " lat = " + lat + " lon = " + lon);


//                        freq = textField4.getText();
//                        textField5.setText(words[4].trim()); // Поляризация
//                        pol = textField5.getText();
//                        textField7.setText(words[5].trim()); // Азимут
//                        textField8.setText(words[6].trim()); // Угол места
//                        textField9.setText(words[7].replace(']', ' ').trim()); // Комментарий
//                        calculation();
                        saveToPropertiesSetting();
                    }

                }
            }
        });
    }


    public void sql (){
        data = FXCollections.observableArrayList();

        try {
            DriverManager.registerDriver(new JDBC());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
// Выполняем подключение к базе данных
        try {
            this.connection = DriverManager.getConnection(CON_STR);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

// Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery(SQL);
// Пример заполнения TableView из БД
// https://github.com/seifallah/Dynamic-TableView--Java-Fx-2.0-/blob/master/DynamicTable.java
            for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                TableView1.getColumns().addAll(col);
            }

            while(rs.next()){
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

//FINALLY ADDED TO TableView
            TableView1.setItems(data);
            connection.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }

// Запрос в БД на внесение частоты и поляризации
    public void sql2 (){

//условие: пока не введены часта и поляризация расчет не будет производиться
        if (textField1.getText().length() == 0 || textField4.getText().length() == 0 || textField5.getText().length() == 0) {
            label2.setTextFill(Color.web("#FF0000"));
            label2.setText("Введите имя КА или ПТ для расчёта");
            label9.setTextFill(Color.web("#FF0000"));
            label9.setText("Частота (FREQ) *");
            label10.setTextFill(Color.web("#FF0000"));
            label10.setText("Поляризация (POL) *");
        } else {
            label2.setTextFill(Color.web("#000000"));
            label2.setText("Введите имя КА или ПТ для расчёта");
            label9.setTextFill(Color.web("#000000"));
            label9.setText("Частота (FREQ) *");
            label10.setTextFill(Color.web("#000000"));
            label10.setText("Поляризация (POL) *");

            data = FXCollections.observableArrayList();
            try {
                DriverManager.registerDriver(new JDBC());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
// Выполняем подключение к базе данных
            try {
                this.connection = DriverManager.getConnection(CON_STR);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

// Statement используется для того, чтобы выполнить sql-запрос
            try (Statement statement = this.connection.createStatement()) {
                statement.executeUpdate(SQL);
                connection.close();
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Error on Building Data");
            }
        }
        System.out.println(" SQL = " + SQL);
    }

    // Запрос в БД на внесение частоты и поляризации
    public void sql4 (){
        data = FXCollections.observableArrayList();
        try {
            DriverManager.registerDriver(new JDBC());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
// Выполняем подключение к базе данных
        try {
            this.connection = DriverManager.getConnection(CON_STR);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

// Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(SQL);
            connection.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }

        System.out.println(" SQL = " + SQL);
    }


    public void sql3 (){
        data = FXCollections.observableArrayList();

        try {
            DriverManager.registerDriver(new JDBC());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
// Выполняем подключение к базе данных
        try {
            this.connection = DriverManager.getConnection(CON_STR);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

// Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery(SQL);
// Пример заполнения TableView из БД
// https://github.com/seifallah/Dynamic-TableView--Java-Fx-2.0-/blob/master/DynamicTable.java
            for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                TableView2.getColumns().addAll(col);
            }

            while(rs.next()){
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

//FINALLY ADDED TO TableView
            TableView2.setItems(data);
            connection.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }


// Метод создания файла database_sat.db из папки с ресурсами в рабочую папку с программой
    void createFile1(){
        File file1 = null;
        String resource = "/database_sat.db";
        URL res = getClass().getResource(resource);
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file1 = File.createTempFile("sat", ".db");
                OutputStream out = new FileOutputStream(file1);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file1.deleteOnExit();
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
                System.out.println("Exceptions.printStackTrace(ex);");
            }
        } else {
            file1 = new File(res.getFile());
        }

        if (file1 != null && !file1.exists()) {
            throw new RuntimeException("Error: File " + file1 + " not found!");
        }

// OK
        File file2 = new File(System.getProperty(CURRENTDIRECTORY),"database/database_sat.db");
        try {
            copyFileUsingStream(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void createFile2(){
        File file3 = null;
        String resource = "/satellite.csv";
        URL res = getClass().getResource(resource);
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file3 = File.createTempFile("satellite", ".csv");
                OutputStream out = new FileOutputStream(file3);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file3.deleteOnExit();
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
                System.out.println("Exceptions.printStackTrace(ex);");
            }
        } else {
            file3 = new File(res.getFile());
        }

        if (file3 != null && !file3.exists()) {
            throw new RuntimeException("Error: File " + file3 + " not found!");
        }

        File file4 = new File(System.getProperty(CURRENTDIRECTORY),"database/satellite.csv");
        try {
            copyFileUsingStream(file3, file4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void createFile3(){
        File file1 = null;
        String resource = "/satellite.xlsx";
        URL res = getClass().getResource(resource);
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file1 = File.createTempFile("satellite", ".xlsx");
                OutputStream out = new FileOutputStream(file1);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file1.deleteOnExit();
            } catch (IOException ex) {
                System.out.println("Exceptions.printStackTrace(ex);");
            }
        } else {
            file1 = new File(res.getFile());
        }

        if (file1 != null && !file1.exists()) {
            throw new RuntimeException("Error: File " + file1 + " not found!");
        }

        File file2 = new File(System.getProperty(CURRENTDIRECTORY),"database/satellite.xlsx");
        try {
            copyFileUsingStream(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void createFile4(){
        File file1 = null;
        String resource = "/setting.properties";
        URL res = getClass().getResource(resource);
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file1 = File.createTempFile("setting", ".properties");
                OutputStream out = new FileOutputStream(file1);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file1.deleteOnExit();
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
                System.out.println("Exceptions.printStackTrace(ex);");
            }
        } else {
            file1 = new File(res.getFile());
        }

        if (file1 != null && !file1.exists()) {
            throw new RuntimeException("Error: File " + file1 + " not found!");
        }

        File file2 = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");
        try {
            copyFileUsingStream(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// Проверка существования файла setting.properties
    void checkingFile(){
        File theDir = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");
        if (!theDir.exists())
            createFileAppProperties();
    }

// Метод создания файла setting.properties из папки с ресурсами в рабочую папку с программой
    void createFileAppProperties(){
// Создаем файл setting.properties в папке с программой
        File dest = new File(System.getProperty(CURRENTDIRECTORY),"setting.properties");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("setting.properties");
             OutputStream out = new FileOutputStream(dest)) {
            int data;
            while ((data = in.read()) != -1) {
                out.write(data);
            }
        }
        catch (IOException exc) {
            exc.printStackTrace();
        }
    }

 // Как скопировать файл в Java? 4 способа — примеры и код
// Способ 1: Используем потоки для копирования файла
// https://javadevblog.com/kak-skopirovat-fajl-v-java-4-sposoba-primery-i-kod.html
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}

// Всплывающее окно
// https://fooobar.com/questions/102466/popup-message-boxes
class ClassNameHere {
    public static void infoBox(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(null, infoMessage, " Описание программы " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
}

// Всплывающее окно со справкой по программе запускаем в другом потоке чтобы UI интерфейс не стопорился
// Работа с потоками
// https://www.youtube.com/watch?v=Jr6L2f5BACM&list=PL0lO_mIqDDFUzG5WOCUVmqx4CBW2qIulV&index=5
// 1 Создаем класс SomePotok1, который унаследуется от класса Thread
class SomePotok1 extends Thread{
    // 2 Создаем метод run, который позволяет создавать потоки
    public void run(){
// Код, который будет выполняться в другом потоке
        try {
            //new PrimerFiles().main();
// Всплывающее окно - справка по программе
            ClassNameHere.infoBox("Программа предназначена для настройки спутниковой антенны по азимуту (АЗ) и углу места (УМ) для выбранного космического аппарата (КА).\n\n" +
                    "В программе используется база данных (БД) SQLite, которая хранится в файле database_sat.db.\n" +
                    "Этот файл был создан на основе файла satellite.csv в программе \"DB Browser (SQLite)\".\n" +
                    "Файл satellite.csv был создан из satellite.xlsx путем сохранения его в: CSV (Разделители - запятые).\n" +
                    " Очень важно, чтобы в данных в качестве разделителя целой части от дробной в числовых значениях\n" +
                    " была обязательно точка, а не запятая и все поля таблицы были: формат ячеек - текстовый.\n" +
                    " При первом запуске программы эти файлы будут созданы автоматически в папке database в том месте, где была запущена эта программа.\n\n" +
                    " Работа с программой интуитивно понятна:\n" +
                    " 1. При нажатии на кнопку \"Вывести все КА из БД в таблицу\" - выводятся все данные хранящиеся в БД.\n" +
                    " 2. При выборе из таблицы нужного КА, азимут и угол места для настройки на этот КА отобразятся в окне \"Рссчётные значения\".\n" +
                    " 3. В программе имеется кнопка \"Найти\" и переключатель \"по имени КА\" и \"по ПТ\".\n" +
                    " 4. Перед началом работы необходимо ввести координаты места установки спутниковой антенны, которую нужно настраивать на КА.\n" +
                    " 5. Координаты сохраняются в программе (в файле setting.properties) при нажатии на кнопку \"Рассчитать АЗ и УМ\".\n\n" +
                    " Вся работа в программе осуществляется в основном путём SQL-запросов к БД, которые отображаются вверху программы.\n" +
                    "            Формула для вычисления азимута:\n" +
                    "            АЗ = 180° + arctg{tg(g2 - g1)/sin(v)}\n" +
                    "            где g1 - долгота спутника, g2 - долгота места приема, v - широта места приема.\n\n" +
                    "            Формула для вычисления угла места:\n" +
                    "            УМ = arctg{[Cos(g2 - g1) x Cos(v) - 0.151]/sqrt(1 - Соs2(g2 - g1) х Cos2(v)]}\n" +
                    "            где g1 - долгота спутника, g2 - долгота места приема, v - широта места приема.\n\n" +

                    "При настройке спутниковой антенны на КА рассчётные значения азимута и угла места могут незначительно отличаться от реальных.\n" +
                    "При нажатии на кнопку \" + \" рассчётные значения копируются в поля AZ_REAL и UM_REAL, которые можно исправить и\n" +
                    "сохранить в БД при нажатии на кнопку \" Сохранить в БД поля отмеченные * \".\n" +
                    "Так же имеется возможность сохранить в БД частоту, поляризацию и комментарий в соответствующих полях,\n" +
                    "которые можно использовать для удобства в работе. \n\n" +

                    "Удачи!", " ");
            //System.out.println(" Запущен ClassNameHere.infoBox через поток1 - SomePotok1");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}

class Potok1 {
    public static void main() {
// 3 Создаем объект на основе класса SomePotok1
        SomePotok1 potok1 = new SomePotok1();
// 4 Вызываем метод run - обязательно  через метод start()
        potok1.start();
    }
}







