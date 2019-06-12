import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;


public class StvorcekovaSiet extends Application {

    private Image slide = new Image("imgs/slide.png");
    private Image pin = new Image("imgs/pin.png");

    private boolean gameIsOn = false;
    private boolean shapeFinished = false;

    private double width = 500;
    private double height = 500;
    private PlayGround tmpRoot;
    private PlayGround root = new PlayGround();
    private Scene settingScene;
    private Scene gameScene;

    private Color[] cuteColors = {Color.AQUA, Color.DARKRED, Color.BLUEVIOLET, Color.DARKOLIVEGREEN,
    Color.PINK, Color.TOMATO, Color.PEACHPUFF, Color.CHOCOLATE, Color.MAGENTA, Color.FORESTGREEN,
    Color.YELLOW, Color.DEEPPINK, Color.DARKSALMON, Color.MEDIUMPURPLE};
    private Color chosenColor;

    private double[][] pinsCoords = {{width / 2 - pin.getWidth()/2, height * 0.33 - pin.getHeight()/2},
            {width / 2 - pin.getWidth()/2, height * 0.70 - pin.getHeight()/2}};
    private double[][] slideCoords = {{(width / 2) - slide.getWidth()/2, height * 0.33},
            {width / 2 - slide.getWidth()/2, height * 0.70}};

    private int newWidth = 10;
    private int newHeight = 10;
    private int squareSize = 50;

    private List<List<Double>> verticesCoords = new ArrayList<>();
    private List<List<Double>> shapeCoords = new ArrayList<>();

    private List<MyTriangle> allDrawnTriangles = new ArrayList<>();
    private List<MyPolygon> allDrawnPolygons = new ArrayList<>();



    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start
     * Starts application and calls functions to set keyboard controls
     * Calls repaint function for Pane
     * @param stage the game Stage
     * @throws Exception if something goes wrong
     */
    @Override
    public void start(Stage stage) throws Exception {
        tmpRoot = new PlayGround();
        stage.setTitle("Customize your settings");
        settingScene = new Scene(tmpRoot, width, height);
        stage.setScene(settingScene);
        stage.show();

        Timeline paintPlayGround = new Timeline(new KeyFrame(new Duration(30), ee -> {
            if (!gameIsOn) {
                tmpRoot.paintSettings();
            }
            else {
                root.paint(stage);
            }
        }));

        paintPlayGround.setCycleCount(Timeline.INDEFINITE);
        paintPlayGround.play();

        mouseSettingsControl();
        keyboardSettingsControl(stage);

        int indexColor = new Random().nextInt(cuteColors.length);
        chosenColor = cuteColors[indexColor];
    }


    /**
     *Keyboard controls for settings
     * after pressing Enter, calls a function to set keyboard and mouse controls for the game
     * @param stage the game Stage
     */
    private void keyboardSettingsControl(Stage stage) {
        settingScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                width = squareSize * newWidth;
                height = squareSize * newHeight;

                PlayGround playGround = new PlayGround();
                gameScene = new Scene(playGround, width, height);
                root = playGround;

                stage.setScene(gameScene);
                stage.setTitle("Play!");
                gameIsOn = true;
                keyboardGameControl(stage);
                mouseGameControl();
            }
        });
    }

    /**
     *Keyboard controls for the game
     * after  pressing Escape, game resets and Settings screen is back on
     * @param stage the game Stage
     */
    private void keyboardGameControl(Stage stage) {
        gameScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                gameIsOn = false;
                allDrawnTriangles.clear();
                allDrawnPolygons.clear();
                shapeCoords.clear();
                width = 500;
                height = 500;
                stage.setTitle("Start again by adjusting your settings");
                stage.setScene(settingScene);
            }
        });
    }


    /**
     * Mouse controls for settings
     * this function enables dragging pins by mouse
     */
    private void mouseSettingsControl(){
        settingScene.setOnMouseDragged(e -> {
            double mx = e.getX();
            double my = e.getY();

            if (pinHit(0, mx, my) && inSlideRange(0, my)){
                newWidth = ((int)pinsCoords[0][0]/35 + 4);
                pinsCoords[0][0] = mx - pin.getWidth()/2;
            }
            else if (pinHit(1, mx, my) && inSlideRange(1, my)) {
                newHeight = ((int)pinsCoords[1][0]/35 + 4);
                pinsCoords[1][0] = mx - pin.getWidth()/2;
            }
        });
    }


    /**
     * Mouse controls for the game
     * by clicking on a vertex it creates a circle
     * if a player clicks on a vertex 2 times and all clicked vertices
     * create together a triangle or a 4-edged polygon - this function remembers it
     * and chooses a random color for it
     */
    private void mouseGameControl() {
        gameScene.setOnMousePressed(e -> {
            double mx = e.getX();
            double my = e.getY();

            if (shapeFinished){
                if (shapeCoords.size() == 3) {
                    MyTriangle myTriangle = createUniqueTriangle();
                    if (myTriangle != null) {
                        allDrawnTriangles.add(myTriangle);
                    }
                }
                else if (shapeCoords.size() == 4){
                    MyPolygon myPolygon = createUniquePolygon();
                    if (myPolygon != null){
                        allDrawnPolygons.add(myPolygon);
                    }
                }
                shapeFinished = false;
                shapeCoords.clear();
                int indexColor = new Random().nextInt(cuteColors.length);
                chosenColor = cuteColors[indexColor];
                return;
            }

            for (List<Double> vc : verticesCoords) {
                if(hitVertex(mx, my, vc)){
                    if (shapeCoords.size() > 5){
                        shapeCoords.clear();
                    }
                    else if (!shapeCoords.contains(vc)){
                        shapeCoords.add(vc);
                    }
                    else {
                        shapeFinished = true;
                    }
                }
            }

        });
    }


    /**
     * If player clicked on a vertex
     * @param mx  x coordinate of the mouse
     * @param my  y coordinate of the mouse
     * @param vc  a vertex coordinates
     * @return true if the vertex was clicked on, false if not
     */
    private boolean hitVertex(double mx, double my, List<Double> vc) {
        return (mx + squareSize * 0.2 >= vc.get(0) &&
                mx - squareSize * 0.2 <= vc.get(0) &&
                my + squareSize * 0.2 >= vc.get(1) &&
                my - squareSize * 0.2 <= vc.get(1));
    }


    /**
     * If player clicked on a pin (from Settings)
     * @param i  (0 or 1) -  0 = the first pin , 1 = the second pin
     * @param mx   x coordinate of the mouse
     * @param my   y coordinate of the mouse
     * @return true if the pin was clicked on, false if not
     */
    private boolean pinHit(int i, double mx, double my) {
        return (mx >= pinsCoords[i][0] - 5 && mx <= pinsCoords[i][0] + pin.getWidth() + 5 &&
                my >= pinsCoords[i][1] && my <= pinsCoords[i][1] + pin.getHeight());
    }


    /**
     * If player moves pin in slides range (from Settings)
     * @param i  (0 or 1) -  0 = the first slide , 1 = the second slide
     * @param mx   x coordinate of the mouse
     * @return true if mouse is inside the slide, false if not
     */
    private boolean inSlideRange(int i, double mx) {
        return (mx >= slideCoords[i][0] && mx <= slideCoords[i][0] + slide.getWidth());
    }


    /**
     * Playground
     * paints settings or the game
     */
    public class PlayGround extends Pane {
        /**
         *Paints Settings
         */
        void paintSettings() {
            this.getChildren().clear();
            Rectangle background = new Rectangle(0, 0, width, height);
            background.setFill(Color.DARKOLIVEGREEN);
            this.getChildren().add(background);

            drawImage(slide, slideCoords[0][0], slideCoords[0][1]);
            drawImage(pin, pinsCoords[0][0], pinsCoords[0][1]);

            drawImage(slide, slideCoords[1][0], slideCoords[1][1]);
            drawImage(pin, pinsCoords[1][0], pinsCoords[1][1]);

            makeText("width", width / 2 - 30, slideCoords[0][1] - 80,
                    Font.font("Bookman Old Style", 30), false);

            makeText("height", width / 2 - 30, slideCoords[1][1] - 80,
                    Font.font("Bookman Old Style", 30), false);

            makeText(newWidth + "", pinsCoords[0][0] + pin.getWidth() / 2, pinsCoords[0][1] - 15,
                    Font.font("Bookman Old Style", 20), true);

            makeText(newHeight + "", pinsCoords[1][0] + pin.getWidth() / 2, pinsCoords[1][1] - 15,
                    Font.font("Bookman Old Style", 20), true);

            makeText("press ->Enter<- \n when you're satisfied \n with your settings", width / 2 - 100,
                    height * 0.85, Font.font("Bookman Old Style", 20), false);
        }


        /**
         * Creates a new Text
         * @param text  what text
         * @param x  x coordinate
         * @param y  y coordinate
         * @param font  which font to use
         * @param underline  if the text is supposed to be underlined
         */
        private void makeText(String text, double x, double y, Font font, boolean underline) {
            Text t = new Text(text);
            t.setX(x);
            t.setY(y);
            t.setFont(font);
            t.setFill(Color.WHITE);
            t.setUnderline(underline);
            t.setTextAlignment(TextAlignment.CENTER);
            this.getChildren().add(t);
        }

        /**
         *Paints the Playground
         * @param stage the game Stage
         */
        void paint(Stage stage) {
            this.getChildren().clear();
            for (int i = 0; i < newHeight; i++) {
                for (int j = 0; j < newWidth; j++) {
                    Rectangle r = new Rectangle(j * squareSize, i * squareSize, squareSize, squareSize);
                    List<Double> xy = new ArrayList<>();
                    xy.add((double) (j * squareSize));
                    xy.add((double) (i * squareSize));
                    if (!verticesCoords.contains(xy)) {
                        verticesCoords.add(xy);
                    }
                    r.setFill(Color.WHITE);
                    r.setStroke(Color.GRAY);
                    this.getChildren().add(r);
                }
            }

            if (shapeFinished) {
                if (shapeCoords.size() == 3 && triangleCoordsTest()) {
                    Polygon triangle = new Polygon();
                    for (List<Double> sc : shapeCoords) {
                        Double[] bar = new Double[2];
                        sc.toArray(bar);
                        triangle.getPoints().addAll(bar);
                    }
                    triangle.setFill(chosenColor);
                    this.getChildren().add(triangle);
                }
                else if (shapeCoords.size() == 4 && polygonCoordsTest()) {
                    Polygon polygon = new Polygon();
                    MyPolygon myPolygon = new MyPolygon(shapeCoords.get(0), shapeCoords.get(1),
                            shapeCoords.get(2), shapeCoords.get(3));
                    for (List<Double> sc : myPolygon.vertexInOrder) {
                        Double[] bar = new Double[2];
                        sc.toArray(bar);
                        polygon.getPoints().addAll(bar);
                    }
                    polygon.setFill(chosenColor);
                    this.getChildren().add(polygon);
                }

            } else {
                for (List<Double> sc : shapeCoords) {
                    Circle c = new Circle(sc.get(0), sc.get(1), squareSize * 0.2);
                    c.setFill(chosenColor);
                    this.getChildren().add(c);
                }
            }

            stage.setTitle("your score : " + (allDrawnTriangles.size() + allDrawnPolygons.size()));
        }

        /**
         * Creates a new ImageView - shows an image
         * @param image which image
         * @param x  x coordinate
         * @param y  y coordinate
         */
        void drawImage(Image image, double x, double y) {
            ImageView imageView = new ImageView(image);
            imageView.setX(x);
            imageView.setY(y);

            this.getChildren().add(imageView);
        }
    }

    /**
     * Creates coordinates for a new Vector
     * based on two given points
     * @param A  point A coordinates
     * @param B  point B coordinates
     * @return Vector coordinates
     */
    private List<Double> createVector(List<Double> A, List<Double> B){
        return Arrays.asList(B.get(0) - A.get(0), B.get(1) - A.get(1));
    }

    /**
     * Counts a vector length based on two given points
     * @param A  point A coordinates
     * @param B  point B coordinates
     * @return Vector length
     */
    private double vectorLength(List<Double> A, List<Double> B){
        return Math.sqrt((Math.pow(B.get(0) - A.get(0),2))+ ((Math.pow(B.get(1) - A.get(1),2))));
    }

    /**
     * Calculates a cosine between two vectors (AB, AC)
     * @param A  point A coordinates
     * @param B  point B coordinates
     * @param C  point C coordinates
     * @param AB  vector AB coordinates
     * @param AC  vector AC coordinates
     * @return cosine value
     */
    private double cosTwoVectors(List<Double> A, List<Double> B, List<Double> C, Double[] AB, Double[] AC){
        double numerator = AB[0] * AC[0] + AB[1] * AC[1];
        double denominator = vectorLength(A,B) * vectorLength(A,C);
        return numerator / denominator;
    }

    /**
     * creates a triangle that is unique
     * @return new myPolygon (if not unique returns null)
     */
    private MyTriangle createUniqueTriangle() {
        MyTriangle myTriangle = new MyTriangle(shapeCoords.get(0), shapeCoords.get(1), shapeCoords.get(2));
        for (MyTriangle mt : allDrawnTriangles) {
            if (myTriangle.equals(mt)){
                return null;                 //if not unique
            }
        }
        return myTriangle;
    }

    /**
     * creates a 4-edged polygon that is unique
     * @return new myPolygon (if not unique returns null)
     */
    private MyPolygon createUniquePolygon() {
        MyPolygon myPolygon = new MyPolygon(shapeCoords.get(0), shapeCoords.get(1),
                shapeCoords.get(2), shapeCoords.get(3));
        for (MyPolygon mp : allDrawnPolygons) {
            if (myPolygon.equals(mp)){
                return null;                 //if not unique
            }
        }
        return myPolygon;
    }

    /**
     * a test if its possible to create a triangle from given coordinates
     * @return true if it is, false if its not
     */
    private boolean triangleCoordsTest() {
        List<Double> A = shapeCoords.get(0);
        List<Double> B = shapeCoords.get(1);
        List<Double> C = shapeCoords.get(2);

        Double[] AB = new Double[2];
        createVector(A,B).toArray(AB);
        Double[] AC = new Double[2];
        createVector(A,C).toArray(AC);

        return (AC[0] / AB[0] != AC[1] / AB[1]);
    }

    /**
     * a test if its possible to create a 4-edged polygon from given coordinates
     * @return true if it is, false if its not
     */
    private boolean polygonCoordsTest() {
        List<Double> A = shapeCoords.get(0);
        List<Double> B = shapeCoords.get(1);
        List<Double> C = shapeCoords.get(2);
        List<Double> D = shapeCoords.get(3);

        List<List<Double>> coords = Arrays.asList(A, B, C, D);
        Map<String, Integer> xCoords = new HashMap<>();
        Map<String, Integer> yCoords = new HashMap<>();

        for (List<Double> c : coords) {
            String x = "" + c.get(0);
            String y = "" + c.get(1);
            if (xCoords.containsKey(x)){
                xCoords.put(x, xCoords.get(x) + 1);
            }
            else {
                xCoords.put(x, 1);
            }
            if (yCoords.containsKey(y)){
                yCoords.put(y, yCoords.get(y) + 1);
            }
            else {
                yCoords.put(y, 1);
            }
        }


        for (String k : xCoords.keySet()) {
            if (xCoords.get(k) > 2){
                return false;
            }
        }

        for (String k : yCoords.keySet()) {
            if (yCoords.get(k) > 2){
                return false;
            }
        }

        Double[] AB = new Double[2];
        createVector(A,B).toArray(AB);
        Double[] AC = new Double[2];
        createVector(A,C).toArray(AC);
        Double[] AD = new Double[2];
        createVector(A,D).toArray(AD);
        Double[] DA = new Double[2];
        createVector(D,A).toArray(DA);
        Double[] DB = new Double[2];
        createVector(D,B).toArray(DB);
        Double[] DC = new Double[2];
        createVector(D,C).toArray(DC);


        return (AB[0] / AC[0] != AB[1] / AC[1] &&
                AB[0] / AD[0] != AB[1] / AD[1] &&
                AD[0] / AC[0] != AD[1] / AC[1] &&
                DB[0] / DA[0] != DB[1] / DA[1] &&
                DC[0] / DA[0] != DC[1] / DA[1] &&
                DC[0] / DB[0] != DC[1] / DB[1]);
    }


    /**
     * MyTriangle - holds all the things you need to know about a triangle
     * its vertices and edges
     */
    private class MyTriangle {
        private List<Double> A;
        private List<Double> B;
        private List<Double> C;
        private double a;
        private double b;
        private double c;


        /**
         * Creates a new MyTriangle
         * and calculates vector lengths as edges
         * @param vertex1  the first vertex
         * @param vertex2  the second vertex
         * @param vertex3  the third vertex
         */
        private MyTriangle(List<Double> vertex1, List<Double> vertex2, List<Double> vertex3) {
            this.A = vertex1;
            this.B = vertex2;
            this.C = vertex3;
            this.a = vectorLength(B,C);
            this.b = vectorLength(A,C);
            this.c = vectorLength(A,B);
        }

        /**
         * a test if two MyTriangles are identical or symmetrically identical
         * @param other other MyTriangle
         * @return true if they are identical, false if not
         */
        private boolean equals(MyTriangle other) {
            Set<Double> thisEdges = new HashSet<>(Arrays.asList(this.a, this.b, this.c));
            Set<Double> otherEdges = new HashSet<>(Arrays.asList(other.a, other.b, other.c));
            return thisEdges.containsAll(otherEdges);
        }
    }

    /**
     * MyPolygon
     * holds all the things you need to know about a 4-edged polygon
     * its vertices, vectors, lengths of edges, diagonals, lengths of diagonals
     */
    private class MyPolygon {
        private List<Double> A;
        private List<Double> B;
        private List<Double> C;
        private List<Double> D;

        private List<List<Double>> vertexInOrder = new ArrayList<>();

        private Double[] AB = new Double[2];
        private Double[] AC = new Double[2];
        private Double[] AD = new Double[2];
        private Double[] BC = new Double[2];
        private Double[] BD = new Double[2];
        private Double[] CD = new Double[2];

        private Set<Double> edgesLength = new TreeSet<>();

        private Double[] diagonal1;
        private Double[] diagonal2;
        private Set<Double> diagonalsLength = new TreeSet<>();


        /**
         * Creates a new MyPolygon
         * and creates vectors
         * @param a  the first vertex
         * @param b  the second vertex
         * @param c  the third vertex
         * @param d  the fourth vertex
         */
        private MyPolygon(List<Double> a, List<Double> b, List<Double> c, List<Double> d) {
            this.A = a;
            this.B = b;
            this.C = c;
            this.D = d;
            createVector(A,B).toArray(AB);
            createVector(A,C).toArray(AC);
            createVector(A,D).toArray(AD);
            createVector(B,C).toArray(BC);
            createVector(B,D).toArray(BD);
            createVector(C,D).toArray(CD);
            this.findEdgesAndDiagonals();
        }

        /**
         * Determines which vectors are edges and which are diagonals,
         * based on the result calls a function to order vertexes
         */
        private void findEdgesAndDiagonals() {
            List<Double[]> allVectors = Arrays.asList(AB, AC, AD, BC, BD, CD);

            for (Double[] vect : allVectors) {
                if (!Arrays.equals(vect, diagonal1) && !Arrays.equals(vect, diagonal2)){
                    compareAndAdd(AB, AC, AD, vect, A, B, C, vectorLength(A, D));
                    compareAndAdd(BC, BD, CD, vect, B, C, D, vectorLength(C, D));
                }
            }

            double cos_AB_AC = cosTwoVectors(A, B, C, AB, AC);
            double cos_AB_AD = cosTwoVectors(A, B, D, AB, AD);
            double cos_AC_AD = cosTwoVectors(A, C, D, AC, AD);

            double min = cos_AB_AC;
            if (min > cos_AB_AD) { min = cos_AB_AD; }
            if (min > cos_AC_AD) { min = cos_AC_AD; }

            if (min == cos_AB_AD){
                orderVertexes(AC, BD, B, C, D);
                diagonalsLength.add(vectorLength(A,C));
                diagonalsLength.add(vectorLength(B,D));

            }
            else if (min == cos_AB_AC){
                orderVertexes(AD, BC, B, D, C);
                diagonalsLength.add(vectorLength(A,D));
                diagonalsLength.add(vectorLength(B,C));
            }
            else if (min == cos_AC_AD){
                orderVertexes(AB, CD, C, B, D);
                diagonalsLength.add(vectorLength(A,B));
                diagonalsLength.add(vectorLength(C,D));
            }
        }


        /**
         * a test if two MyPolygon are identical or symmetrically identical
         * @param other  other MyPolygon
         * @return true if they are identical, false if not
         */
        boolean equals(MyPolygon other){
            if (this.edgesLength.containsAll(other.edgesLength)){
                return true;
            }
            return (this.diagonalsLength.containsAll(other.diagonalsLength)) ;
        }

        /**
         * Compares vectors to an "unknown" vector
         * based on which vector is equal to which one, this function calculates their length
         * and adds this length to edgesLength
         * @param vector1  the first vector
         * @param vector2  the second vector
         * @param vector3  the third vector
         * @param vect     an "unknown" vector from allVectors
         * @param vertex1  the first vertex
         * @param vertex2  the second vertex
         * @param vertex3  the third vertex
         * @param length   vector3 length
         */
        private void compareAndAdd(Double[] vector1, Double[] vector2, Double[] vector3, Double[] vect,
                                   List<Double> vertex1, List<Double> vertex2, List<Double> vertex3, double length) {
            if (Arrays.equals(vect, vector1)){
                this.edgesLength.add(vectorLength(vertex1, vertex2));
            }
            if (Arrays.equals(vect, vector2)){
                this.edgesLength.add(vectorLength(vertex1, vertex3));
            }
            if (Arrays.equals(vect, vector3)){
                this.edgesLength.add(length);
            }
        }

        /**
         * Orders Vertexes
         * @param first  the first vector
         * @param second  the second vector
         * // v1 can be always vertex A
         * @param v2  the second vertex
         * @param v3  the third vertex
         * @param v4  the fourth vertex
         */
        private void orderVertexes(Double[] first, Double[] second, List<Double> v2, List<Double> v3, List<Double> v4) {
            diagonal1 = first;
            diagonal2 = second;
            this.vertexInOrder.add(A);
            this.vertexInOrder.add(v2);
            this.vertexInOrder.add(v3);
            this.vertexInOrder.add(v4);
        }
    }
}
