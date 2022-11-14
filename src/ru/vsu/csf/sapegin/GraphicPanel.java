package ru.vsu.csf.sapegin;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class GraphicPanel extends JPanel {
    private int width;
    private int height;

    private int moveX = 0;
    private int moveY = 0;

    //можно дописать, чтобы менялся единичный отрезок при приближении или удалении
    private int singleSegment = 2;

    private int[] xCoordsOfGraphic;
    private int[] yCoordsOfGraphic;

    private String functionAsStr;

    private static final int DEFAULT_SIZE_OF_GRID = 40;
    private double sizeOfGrid = DEFAULT_SIZE_OF_GRID;
    private static final Font DEFAULT_FONT = new Font("Times New Roman", Font.PLAIN, 14);

    private int N = 10;

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        width = getWidth();
        height = getHeight();

//        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid((Graphics2D) g);
        drawAxis((Graphics2D) g);
        drawCoordinates((Graphics2D) g);
        drawGraphic((Graphics2D) g);
    }

    private void drawGrid(Graphics2D graphics) {
        for (double x = width / 2 - moveX; x < width; x += sizeOfGrid) {
            graphics.drawLine((int) x, 0, (int) x, height);
        }
        for (double x = width / 2 - moveX; x > 0; x -= sizeOfGrid) {
            graphics.drawLine((int) x, 0, (int) x, height);
        }
        for (double y = height / 2 - moveY; y < height; y += sizeOfGrid) {
            graphics.drawLine(0, (int) y, width, (int) y);
        }
        for (double y = height / 2 - moveY; y > 0; y -= sizeOfGrid) {
            graphics.drawLine(0, (int) y, width, (int) y);
        }
    }

    private void drawAxis(Graphics2D graphics) {
        Color color = graphics.getColor();
        Stroke stroke = graphics.getStroke();

        graphics.setColor(Color.GRAY);
        graphics.setStroke(new BasicStroke(3));

        graphics.drawLine(0, height / 2 - moveY, width, height / 2 - moveY); //когда прибавляем, ось уползает вниз
        graphics.drawLine(width / 2 - moveX, 0, width / 2 - moveX, height);

        graphics.setColor(color);
        graphics.setStroke(stroke);
    }

    private void drawCoordinates(Graphics2D graphics) {
        Color color = graphics.getColor();
        Stroke stroke = graphics.getStroke();
        Font font = graphics.getFont();

        graphics.setColor(Color.DARK_GRAY);
        graphics.setStroke(new BasicStroke(3));
        graphics.setFont(DEFAULT_FONT);
        double n = singleSegment;

        for (double i = width / 2 - moveX + (singleSegment * sizeOfGrid); i <= width; i += singleSegment * sizeOfGrid) {
            String coordinate = convertNumberToString(n);
            graphics.drawString(coordinate, (int) i - getStringSizeInPixels(coordinate) / 2, height / 2 + 15 - moveY);
            n += singleSegment;
        }
        n = -singleSegment;
        for (double i = width / 2 - moveX - (singleSegment * sizeOfGrid); i >= - singleSegment * sizeOfGrid; i -= singleSegment * sizeOfGrid) {
            String coordinate = convertNumberToString(n);
            graphics.drawString(coordinate, (int) i - getStringSizeInPixels(coordinate) / 2, height / 2 + 15 - moveY);
            n -= singleSegment;
        }
        n = singleSegment;
        for (double i = height / 2 - moveY - sizeOfGrid * singleSegment; i >= - singleSegment * sizeOfGrid; i -= singleSegment * sizeOfGrid) {
            String coordinate = convertNumberToString(n);
            graphics.drawString(coordinate, width / 2 - moveX + 5, (int) (i + font.getSize() / 2));
            n += singleSegment;
        }
        n = -singleSegment;
        for (double i = height / 2 - moveY + (singleSegment * sizeOfGrid); i <= height; i += singleSegment * sizeOfGrid) {
            String coordinate = convertNumberToString(n);
            graphics.drawString(coordinate, width / 2 - moveX + 5, (int) (i + font.getSize() / 2));
            n -= singleSegment;
        }

        graphics.setColor(color);
        graphics.setStroke(stroke);
        graphics.setFont(font);
    }

    private String convertNumberToString(double n) {
        DecimalFormat format = new DecimalFormat("#.####");
        return format.format(n);
    }

    private int getStringSizeInPixels(String str) {
        return str.length() * DEFAULT_FONT.getSize() / 2;
    }

    public void setNewMove(int dx, int dy) {
        moveX += dx;
        moveY -= dy;
        repaint();
    }

    public void setNewScale(double coefficientOfScale) {
        sizeOfGrid = sizeOfGrid * coefficientOfScale;
        moveX *= coefficientOfScale;
        moveY *= coefficientOfScale;
        repaint();
    }

    public void setStartScale() {
        sizeOfGrid = DEFAULT_SIZE_OF_GRID;
        moveX = 0;
        moveY = 0;
        repaint();
    }

    public void setFunction(String function) { //если что, все функции можно хранить в списке
        functionAsStr = function;
        repaint();
    }

    public void setNumOfPoints(int n) {
        N = n;
    }

    private void drawGraphic(Graphics2D graphics) {
        if (functionAsStr == null) {
            return;
        }
        Point[] pixPoints = GraphicProcessing.getPointsForGraphicCubicSplineMethod(functionAsStr, width, height, sizeOfGrid, moveX, moveY, N);
        if (pixPoints == null) {
            System.out.println("Нет точек");
            return;
        }
        Point[] resPixPoints = new Point[pixPoints.length];
        for (int i = 0; i < pixPoints.length; i++) {
            resPixPoints[i] = new Point(pixPoints[i].getX() * sizeOfGrid + width / 2, (pixPoints[i].getY() * sizeOfGrid - height / 2));
        }

        double[] xCoordsForBresenham = new double[resPixPoints.length];
        double[] yCoordsForBresenham = new double[resPixPoints.length];
        for (int i = 0; i < resPixPoints.length; i++) {
            xCoordsForBresenham[i] = (resPixPoints[i].getX()) - moveX;
            yCoordsForBresenham[i] = -(resPixPoints[i].getY()) - moveY;
        }

        fillArraysOfCoordinates(resPixPoints);

        Color color = graphics.getColor();
        Stroke stroke = graphics.getStroke();
        Font font = graphics.getFont();

        graphics.setStroke(new BasicStroke(2));
        graphics.setFont(DEFAULT_FONT);
        for (int i = 0; i < xCoordsOfGraphic.length - 1; i++) {
//            drawBresenhamLine(xCoords[i], yCoords[i], xCoords[i+1], yCoords[i+1], graphics);
//            drawBresenhamLine(xCoordsOfGraphic[i], yCoordsOfGraphic[i], xCoordsOfGraphic[i+1], yCoordsOfGraphic[i+1], graphics);
            graphics.drawLine(xCoordsOfGraphic[i], yCoordsOfGraphic[i], xCoordsOfGraphic[i+1], yCoordsOfGraphic[i+1]);
        }

        graphics.setColor(color);
        graphics.setStroke(stroke);
        graphics.setFont(font);
    }

    private void fillArraysOfCoordinates(Point[] points) {
        xCoordsOfGraphic = new int[points.length];
        yCoordsOfGraphic = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            xCoordsOfGraphic[i] = (int) (points[i].getX()) - moveX;
            yCoordsOfGraphic[i] = -(int) (points[i].getY()) - moveY;
        }
    }

    // Этот код "рисует" все 9 видов отрезков. Наклонные (из начала в конец и из конца в начало каждый), вертикальный и горизонтальный - тоже из начала в конец и из конца в начало, и точку.
    private int sign (double x) {
        return (x > 0) ? 1 : (x < 0) ? -1 : 0;
        //возвращает 0, если аргумент (x) равен нулю; -1, если x < 0 и 1, если x > 0.
    }

    public void drawBresenhamLine (double xstart, double ystart, double xend, double yend, Graphics g)
    /**
     * xstart, ystart - начало;
     * xend, yend - конец;
     * "g.drawLine (x, y, x, y);" используем в качестве "setPixel (x, y);"
     * Можно писать что-нибудь вроде g.fillRect (x, y, 1, 1);
     */
    {
        double x, y, dx, dy, incx, incy, pdx, pdy, es, el, err;

        dx = xend - xstart;//проекция на ось икс
        dy = yend - ystart;//проекция на ось игрек

        incx = sign(dx);
        /*
         * Определяем, в какую сторону нужно будет сдвигаться. Если dx < 0, т.е. отрезок идёт
         * справа налево по иксу, то incx будет равен -1.
         * Это будет использоваться в цикле постороения.
         */
        incy = sign(dy);
        /*
         * Аналогично. Если рисуем отрезок снизу вверх -
         * это будет отрицательный сдвиг для y (иначе - положительный).
         */

        if (dx < 0) dx = -dx;//далее мы будем сравнивать: "if (dx < dy)"
        if (dy < 0) dy = -dy;//поэтому необходимо сделать dx = |dx|; dy = |dy|
        //эти две строчки можно записать и так: dx = Math.abs(dx); dy = Math.abs(dy);

        if (dx > dy)
        //определяем наклон отрезка:
        {
            /*
             * Если dx > dy, то значит отрезок "вытянут" вдоль оси икс, т.е. он скорее длинный, чем высокий.
             * Значит в цикле нужно будет идти по икс (строчка el = dx;), значит "протягивать" прямую по иксу
             * надо в соответствии с тем, слева направо и справа налево она идёт (pdx = incx;), при этом
             * по y сдвиг такой отсутствует.
             */
            pdx = incx;	pdy = 0;
            es = dy;	el = dx;
        }
        else//случай, когда прямая скорее "высокая", чем длинная, т.е. вытянута по оси y
        {
            pdx = 0;	pdy = incy;
            es = dx;	el = dy;//тогда в цикле будем двигаться по y
        }

        x = xstart;
        y = ystart;
        err = el/2;
        g.drawLine ((int) x, (int) y, (int) x, (int) y);//ставим первую точку
        //все последующие точки возможно надо сдвигать, поэтому первую ставим вне цикла

        for (int t = 0; t < el; t++)//идём по всем точкам, начиная со второй и до последней
        {
            err -= es;
            if (err < 0)
            {
                err += el;
                x += incx;//сдвинуть прямую (сместить вверх или вниз, если цикл проходит по иксам)
                y += incy;//или сместить влево-вправо, если цикл проходит по y
            }
            else
            {
                x += pdx;//продолжить тянуть прямую дальше, т.е. сдвинуть влево или вправо, если
                y += pdy;//цикл идёт по иксу; сдвинуть вверх или вниз, если по y
            }

            g.drawLine ((int) x, (int) y, (int) x, (int) y);
        }
    }

}
