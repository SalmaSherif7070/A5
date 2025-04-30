import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PredictiveCoding2D {
    private final int quantizationLevels;
    private final double quantizationStep;
    private final String predictorType; // order-1, order-2, or adaptive

    public PredictiveCoding2D(String predictorType, int quantizationLevels) {
        this.predictorType = predictorType;
        this.quantizationLevels = quantizationLevels;
        this.quantizationStep = 510.0 / (quantizationLevels - 1); 
    }

    private double[][] loadImage(String imagePath) throws IOException {
        BufferedImage img = ImageIO.read(new File(imagePath));
        double[][] imageArray = new double[img.getHeight()][img.getWidth()];
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                imageArray[i][j] = (img.getRGB(j, i) >> 16) & 0xFF;
            }
        }
        return imageArray;
    }

    private void saveImage(double[][] imageArray, String outputPath) throws IOException {
        BufferedImage img = new BufferedImage(imageArray[0].length, imageArray.length, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < imageArray.length; i++) {
            for (int j = 0; j < imageArray[0].length; j++) {
                int gray = (int) Math.max(0, Math.min(255, imageArray[i][j]));
                img.setRGB(j, i, (gray << 16) | (gray << 8) | gray);
            }
        }
        ImageIO.write(img, "png", new File(outputPath));
    }

    // I have made the quantization step dynmic based on the quantization levels 
    // why? (to deal with different quantization levels)
    private double quantize(double diff) {
        return Math.round(diff / quantizationStep) * quantizationStep;
    }

    private double predict(int i, int j, double[][] decodedImage) {
        double A = (j > 0) ? decodedImage[i][j - 1] : 0; // Left 
        double B = (i > 0 && j > 0) ? decodedImage[i - 1][j - 1] : 0; // Diagonal
        double C = (i > 0) ? decodedImage[i - 1][j] : 0; // Top

        switch (predictorType.toLowerCase()) {
            case "order-1":
                return A; // left
            case "order-2":
                return (j > 0 && i > 0) ? (A + C - B) : (j > 0 ? A : C); // left + top - diagonal
            case "adaptive":
                double minAC = Math.min(A, C);
                double maxAC = Math.max(A, C);
                if (B < minAC) return maxAC;
                if (B >= maxAC) return minAC;
                return A + C - B;
            default:
                throw new IllegalArgumentException("Unknown predictor type: " + predictorType);
        }
    }

    private double[][] predictiveCodingEncoder(double[][] img) {
        double[][] residuals = new double[img.length][img[0].length];
        double[][] decodedImage = new double[img.length][img[0].length];

        // Initialize first row and first column
        residuals[0][0] = img[0][0];
        for (int j = 1; j < img[0].length; j++) {
            decodedImage[0][j] = img[0][j];
            residuals[0][j] = img[0][j];
        }
        for (int i = 1; i < img.length; i++) {
            decodedImage[i][0] = img[i][0];
            residuals[i][0] = img[i][0];
        }

        // remaining pixels
        for (int i = 1; i < img.length; i++) {
            for (int j = 1; j < img[0].length; j++) {
                double predicted = predict(i, j, decodedImage);
                residuals[i][j] = quantize(img[i][j] - predicted);
                decodedImage[i][j] = predicted + residuals[i][j];
            }
        }
        return residuals;
    }

    private double[][] predictiveCodingDecoder(double[][] quantizedResiduals) {
        double[][] reconstructed = new double[quantizedResiduals.length][quantizedResiduals[0].length];

        // first row and first column
        reconstructed[0][0] = quantizedResiduals[0][0];
        for (int j = 1; j < quantizedResiduals[0].length; j++) {
            reconstructed[0][j] = quantizedResiduals[0][j];
        }
        for (int i = 1; i < quantizedResiduals.length; i++) {
            reconstructed[i][0] = quantizedResiduals[i][0];
        }

        // remaining pixels
        for (int i = 1; i < quantizedResiduals.length; i++) {
            for (int j = 1; j < quantizedResiduals[0].length; j++) {
                double predicted = predict(i, j, reconstructed);
                reconstructed[i][j] = predicted + quantizedResiduals[i][j];
            }
        }
        return reconstructed;
    }

    private double calculateMSE(double[][] original, double[][] reconstructed) {
        double mse = 0;
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[0].length; j++) {
                double diff = original[i][j] - reconstructed[i][j];
                mse += diff * diff;
            }
        }
        return mse / (original.length * original[0].length);
    }

    private double calculateCompressionRatio(long originalSize, long encodedSize) {
        return (double) originalSize / encodedSize;
    }

    private long calculateEncodedSize(int height, int width) {
        int bitsPerResidual = (int) Math.ceil(Math.log(quantizationLevels) / Math.log(2));
        long totalBits = (long) height * width * bitsPerResidual;
        return (int) Math.ceil(totalBits / 8.0);
    }

    public void process(String imagePath, String outputPath) throws IOException {
        double[][] img = loadImage(imagePath);
        long originalSize = new File(imagePath).length();
        long encodedSize = calculateEncodedSize(img.length, img[0].length);

        double[][] quantizedResiduals = predictiveCodingEncoder(img);
        double[][] reconstructed = predictiveCodingDecoder(quantizedResiduals);

        double mse = calculateMSE(img, reconstructed);
        double compressionRatio = calculateCompressionRatio(originalSize, encodedSize);

        saveImage(reconstructed, outputPath);

        System.out.println("Compression Ratio: " + compressionRatio);
        System.out.println("Size Before Compression (bytes): " + originalSize);
        System.out.println("Size After Compression (bytes): " + encodedSize);
        System.out.println("MSE: " + mse);
    }


    public static void main(String[] args) {
        String[] predictors = {"order-1", "order-2", "adaptive"};
        int[] quantLevels = {8, 16, 32};
        String imagePath = "input.png";

        for (String predictor : predictors) {
            for (int levels : quantLevels) {
                PredictiveCoding2D pc = new PredictiveCoding2D(predictor, levels);
                String outputPath = "reconstructed_" + predictor + "_" + levels + ".png";
                try {
                    System.out.println("\nPredictor: " + predictor + ", Quantization Levels: " + levels);
                    pc.process(imagePath, outputPath);
                } catch (IOException e) {
                    System.err.println("Error processing image: " + e.getMessage());
                }
            }
        }
    }
}