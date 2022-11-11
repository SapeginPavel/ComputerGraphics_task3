package ru.vsu.csf.sapegin;

public class Main {
    //интерполяция кубическими сплайнами
     /*
    Можно:
    1. добавить рисовку графиков с точками разрыва (тогда будет двумерный массив точек, для каждого непрерывного участка
       функции будет свой массив
    2. Использовать какие-то структуры данных, чтобы мгновенно получать сведения о параметрах и инструментах управления ими
    3. ax^4+bcx^3+hx-0.2
      */

    public static void main(String[] args) {
        GraphicApp app = new GraphicApp();
        app.show();
    }
}