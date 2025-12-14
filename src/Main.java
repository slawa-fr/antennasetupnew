import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("sample.fxml"));
            //System.out.println("root:" + getClass().getResource("sample.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("Настройка приёмной спутниковой антенны по азимуту (АЗ) и углу места (УМ) на выбранный космический аппарат (КА)");
        primaryStage.setScene(new Scene(root, 1024, 650));
// Чтобы нельзя было изменять размеры окна
        //primaryStage.setResizable(false);


// Автомасштабирование окна
// How to AutoSize Components in JavaFX - Responsive Design, Scene Builder
// https://www.youtube.com/watch?v=4JSaH0dNK5M

// в ScenEBuilder - Preview - посмотреть как ведёт себя окно при масштабировании
// выделить тот элемент, который нужно чтобы не масштабировался - вторая закладка справа Layout
// Там будет такой квадратик - в поле Anchor Pane Constrains
// Нажать полосочку той стороны, которую нужно масштабировать

// предотвратить слишком маленькое окно
        primaryStage.setMinWidth(primaryStage.getWidth());
        primaryStage.setMinHeight(primaryStage.getHeight());


        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
