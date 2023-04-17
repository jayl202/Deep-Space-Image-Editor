package com.example.deepspaceimageeditor;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import static jdk.internal.org.jline.utils.AttributedStyle.BLACK;
import static jdk.internal.org.jline.utils.AttributedStyle.WHITE;

public class EditorController implements Initializable {

    public WritableImage newBlackAndWhiteImage;
    public int width;
    public int height;
    public int[][] imageArray;
    public UnionFind unionFind;

    FileChooser fileChooser = new FileChooser();

    HashMap<Integer, ArrayList<Integer>> starMap;
    @FXML
    public ImageView origImage;
    @FXML
    public ImageView newImage;
    @FXML
    public Button chooseFileButton;
    @FXML
    public Button blackAndWhiteButton;
    @FXML
    public TextField numberOfStarsTextField;
    @FXML
    public Button circleStarsButton;
    @FXML
    public Button labelStarsButton;

    EditorController eccon;

    public int[] root;

    private int[] size;

    public void getFile(ActionEvent event) {
        fileChooser.setTitle("Select an image to show");
        File selectedFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());
        if (selectedFile != null) {
            // create image from selected file
            Image image = new Image(selectedFile.toURI().toString());
            // display the image in the imageView
            origImage.setImage(image);
            newImage.setImage(image);
        }
    }

    public WritableImage blackAndWhite(ActionEvent event) {
        Image image = newImage.getImage();
        PixelReader pixelReader = image.getPixelReader();

        height = (int) image.getHeight();
        width = (int) image.getWidth();

        imageArray = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color color = pixelReader.getColor(col, row);
                int red = (int) (color.getRed() * 255);
                int green = (int) (color.getGreen() * 255);
                int blue = (int) (color.getBlue() * 255);
                int luminance = (red + green + blue) / 3;

                if (luminance > 40) {
                    imageArray[row][col] = WHITE;
                } else {
                    imageArray[row][col] = BLACK;
                }
            }
        }

        // Create a UnionFind object with the number of pixels in the image as size
        unionFind = new UnionFind(height * width);

        // Iterate over the imageArray and union adjacent white pixels
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int currentPixel = imageArray[row][col];
                int currentIndex = row * width + col;

                if (currentPixel == WHITE) {
                    // Check if the pixel to the right is white
                    if (col + 1 < width && imageArray[row][col + 1] == WHITE) {
                        int rightIndex = row * width + col + 1;
                        unionFind.unify(currentIndex, rightIndex);
                    }

                    // Check if the pixel below is white
                    if (row + 1 < height && imageArray[row + 1][col] == WHITE) {
                        int belowIndex = (row + 1) * width + col;
                        unionFind.unify(currentIndex, belowIndex);
                    }
                }
            }
        }

        // Create a new image from the processed image array
        WritableImage processedImage = new WritableImage(width, height);
        PixelWriter pixelWriter = processedImage.getPixelWriter();
        newImage.setImage(processedImage);

        // Create a hashmap to track each star
        HashMap<Integer, ArrayList<Integer>> starMap = new HashMap<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int color = imageArray[row][col];

                if (color == WHITE) {
                    int index = row * width + col;
                    int root = unionFind.find(index);

                    // Add the pixel to the appropriate ArrayList in the hashmap
                    if (!starMap.containsKey(root)) {
                        starMap.put(root, new ArrayList<>());
                    }
                    starMap.get(root).add(index); //new int[]{col, row});
                }
                // Set the pixel color in the processed image
                if (color == WHITE) {
                    pixelWriter.setColor(col, row, Color.WHITE);
                } else {
                    pixelWriter.setColor(col, row, Color.BLACK);
                }
            }
        }

        //for test
        int largestRoot= Collections.max(starMap.keySet(),(a, b)->starMap.get(a).size()-starMap.get(b).size());
        List lis=starMap.get(largestRoot);


        return processedImage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileChooser.setInitialDirectory(new File("C:\\Users\\jayla\\Desktop\\Year 2\\Semester 2\\Data Structures & Algorithms 2\\DeepSpaceImageFolder"));

        eccon=this;
    }

    public void reset(ActionEvent event) {
        newImage.setImage(origImage.getImage());
    }



    public void type(ActionEvent event) {
    }

    public void circleStars(ActionEvent event) {
        WritableImage blackAndWhite = newBlackAndWhiteImage;

        Canvas canvas = new Canvas(blackAndWhite.getWidth(), blackAndWhite.getHeight());
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.drawImage(blackAndWhite,0,0);

        for(int row = 0; row < height; row++){
            for(int column = 0; column < width; column++){
                int colour = imageArray[row*width][column];

                if(colour == WHITE){
                    int index = row*width+column;
                    int root = unionFind.find(index);

                    if (!starMap.containsKey(root)){
                        starMap.put(root, new ArrayList<>());
                    }
                    starMap.get(root).add(index);
                }
            }
        }
        for(List<Integer> set : starMap.values()){
            graphicsContext.setStroke(Color.BLUE);
            graphicsContext.setLineWidth(2);
            graphicsContext.setFill(Color.TRANSPARENT);

            int radius = (int) Math.sqrt(set.size());

            int centreX = 0, centreY = 0;
            for(int index : set){
                int x = index % width;
                int y = index / width;
                centreX += x;
                centreY += y;
            }
            centreX /= set.size();
            centreY /= set.size();

            graphicsContext.strokeOval(centreX - radius, centreY - radius, radius* 2, radius*2);
        }

        WritableImage circleImage = new WritableImage((int)canvas.getWidth(),(int)canvas.getHeight());
        canvas.snapshot(null, circleImage);

        newImage.setImage(circleImage);



    }


    public void labelStars(ActionEvent event) {
       /* // Get the starMap from the setBlackAndWhite method
        HashMap<Integer, ArrayList<Integer>> starMap = getStarMap();

        // Find the largest star
        int largestRoot = Collections.max(starMap.keySet(), Comparator.comparingInt(a -> starMap.get(a).size()));
        List<Integer> pixels = starMap.get(largestRoot);

        // Sort the pixels in reverse order by their size
        pixels.sort(Comparator.comparingInt(pixel -> pixel % newImage.getImage().getWidth() * pixel / newImage.getImage().getWidth()).reversed());

        // Create a copy of the newImage
        WritableImage labeledImage = new WritableImage(newImage.getImage().getPixelReader(), (int)newImage.getFitWidth(), (int)newImage.getFitHeight());
        PixelWriter pixelWriter = labeledImage.getPixelWriter();
        GraphicsContext gc = newImage.getGraphicsContext2D();

        // Label the pixels in the copy of the newImage
        for (int pixel : pixels) {
            int x = pixel % (int)newImage.getFitWidth();
            int y = pixel / (int)newImage.getFitWidth();
            pixelWriter.setColor(x, y, Color.WHITE);
            gc.setStroke(Color.BLACK);
            gc.strokeText(Integer.toString(pixels.indexOf(pixel) + 1), x, y);
        }

        // Set the newImage to be the labeledImage
        newImage.setImage(labeledImage);

        */

    }

    public void getRandomColour(ActionEvent event) {

    }
}





