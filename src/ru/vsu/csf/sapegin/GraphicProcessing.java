package ru.vsu.csf.sapegin;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphicProcessing {

    public static Point[] getPointsForGraphicCubicSplineMethod(String graphicAsString, double width, double height, double sizeOfGrid, double moveX, double moveY, int N) {
        if (graphicAsString.equals("")) {
            return null;
        }
        Expression mainExpression = new ExpressionBuilder(graphicAsString).variables("x").build();
        Point[] boundaryPoints = getBoundaryPoints(mainExpression, width, sizeOfGrid, moveX);
        if (boundaryPoints == null) {
            return null;
        }
        double domain = boundaryPoints[1].getX() - boundaryPoints[0].getX();
        if (domain == 0) {
            return null;
        }
        double x = boundaryPoints[0].getX();
        double h = domain / (N-1); //длина одного сегмента в единичных отрезках
        Point[] mainPoints = new Point[N]; //опорные точки
        for (int i = 0; i < mainPoints.length; i++) {
            Double y = y(mainExpression, x);
            if (y == null || Double.isNaN(y)) {
                continue;
            }
            mainPoints[i] = new Point(x, y);
            x += h;
        }
        double coefficientXInPixels = 1 / sizeOfGrid; //смещение икс для одного пиксела
        int numOfPointsForOneSegment = (int) ((mainPoints[1].getX() - mainPoints[0].getX()) / coefficientXInPixels);
        List<Point> points = new ArrayList<>(numOfPointsForOneSegment * (N-1));
        for (int i = 0; i < N - 1; i++) { //цикл по всем сегментам
            String polynomial = getPolynomialForI(mainPoints, i, h);
            Expression polynomialExpression = new ExpressionBuilder(polynomial).variables("x").build();
            points.add(mainPoints[i]);
            for (double intermediateX = mainPoints[i].getX() + coefficientXInPixels; intermediateX < mainPoints[i+1].getX(); intermediateX += coefficientXInPixels) { //(i+1 == mainPoints.length-1 && (i+iterator) < intermediatePoints.length)
                Double y = y(polynomialExpression, intermediateX);
                if (y == null || Double.isNaN(y)) {
                    continue;
                }
                points.add(new Point(intermediateX, y));
            }
        }
        Point[] intermediatePoints = new Point[points.size()];
        int i = 0;
        for (Point p : points) {
            intermediatePoints[i] = p;
            i++;
        }
        return intermediatePoints;
    }

    private static Point[] getBoundaryPoints(Expression e, double width, double sizeOfGrid, double moveX) {
        double xLeft = 0;
        Double y = y(e, xLeft);
        while ((y == null || Double.isNaN(y)) && (Math.abs(Math.abs(xLeft * sizeOfGrid) + moveX) < width / 2 + Math.abs(moveX))) {
            xLeft -= 0.1;
            y = y(e, xLeft);
        }
        if (y == null || Double.isNaN(y)) { //POSITIVE_INFINITY
            while ((y == null || Double.isNaN(y)) && (Math.abs(Math.abs(xLeft * sizeOfGrid) + moveX) < width / 2 + Math.abs(moveX))) {
                xLeft += 0.1;
                y = y(e, xLeft);
            }
        }
        if (y == null || Double.isNaN(y)) {
            return null;
        }
        double xRight = xLeft + 0.1;
        y = y(e, xRight);
        while ((y == null || Double.isNaN(y)) && (Math.abs(Math.abs(xLeft * sizeOfGrid) + moveX) < width / 2 + Math.abs(moveX))) {
            xRight += 0.1;
            y = y(e, xRight);
        }
        y = y(e, xLeft);
        //не предусмотрены точки разрыва
        while (Math.abs(Math.abs(xLeft * sizeOfGrid) + moveX) < width / 2 + Math.abs(moveX)) { //Math.abs(Math.abs(y * sizeOfGrid) - Math.abs(moveY)) < height / 2 + Math.abs(moveY) &&
            xLeft -= 0.1;
        }
        while (Math.abs(Math.abs(xRight * sizeOfGrid) - Math.abs(moveX)) < width / 2 + Math.abs(moveX)) { //Math.abs(Math.abs(y * sizeOfGrid) - Math.abs(moveY)) < height / 2 + Math.abs(moveY) &&
            xRight += 0.1;
        }
        return new Point[]{new Point(xLeft, e.setVariable("x", xLeft).evaluate()), new Point(xRight, e.setVariable("x", xRight).evaluate())};
    }

    private static Double y(Expression expression, double x) { //иф инфинити
        try {
            return expression.setVariable("x", x).evaluate();
        } catch (Throwable cause) {
            if (cause instanceof ArithmeticException && "Division by zero!".equals(cause.getMessage())) {
                return null;
//                return Double.POSITIVE_INFINITY;
            } else {
                return Double.NaN;
            }
        }
    }

    private static double getMi(Point[] points, int i, double h) { //название Mi используется в формуле (производные)
        if (i == 0) {
            return (4 * points[1].getY() - points[2].getY() - 3 * points[0].getY()) / 2 / h;
        }
        if (i == points.length - 1) {
            return (3 * points[i].getY() - points[i-2].getY() - 3 * points[i-1].getY()) / 2 / h;
        }
        return (points[i+1].getY() - points[i-1].getY()) / 2 / h;
    }

    public static String getPolynomialForI(Point[] points, int i, double h) {
        return "((" + points[i+1].getX() + "-x)^2 * (2 * (x-" + points[i].getX() + ") + " + h + ")) / " + h * h * h + " * " + points[i].getY() + " + " +
                "((x-" + points[i].getX() + ")^2 * (2 * (" + points[i+1].getX() + "-x) + " + h + ")) / " + h * h * h + " * " + points[i+1].getY() + " + " +
                "((" + points[i+1].getX() + "-x)^2 * (x-" + points[i].getX() + ")) / " + h * h + " * " + getMi(points, i, h) + " + " +
                "((x-" + points[i].getX() + ")^2 * (x-" + points[i+1].getX() + ")) / " + h * h + " * " + getMi(points, i + 1, h);
    }
}
