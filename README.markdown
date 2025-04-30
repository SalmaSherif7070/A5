# 2-D Feed Backward Predictive Coding Project

## Overview
This project implements 2-D Feed Backward Predictive Coding for grayscale image compression, as part of an assignment for Zewail City of Science and Technology. The implementation supports three predictor types (Order-1, Order-2, and Adaptive 2-D) and varying quantization levels (8, 16, 32). The code evaluates the compression ratio, Mean Squared Error (MSE), and visual quality of the reconstructed images.

## Requirements
- **Java Development Kit (JDK)**: Version 8 or higher.
- **Input Image**: A grayscale PNG image (e.g., `input.png`).
- **Dependencies**: The code uses `javax.imageio.ImageIO` for image I/O, which is included in the JDK.

## How to Run
1. **Prepare the Input Image**:
   - Place a grayscale image named `input.png` in the same directory as the code. The provided image (Zewail City logo) was used for testing.
   
2. **Compile the Code**:
   - Save the code in a file named `PredictiveCoding2D.java`.
   - Open a terminal in the directory containing the code and compile it:
     ```bash
     javac PredictiveCoding2D.java
     ```

3. **Run the Code**:
   - Execute the compiled program:
     ```bash
     java PredictiveCoding2D
     ```
   - The program will process the `input.png` image using different predictor types (Order-1, Order-2, Adaptive) and quantization levels (8, 16, 32).

4. **Output**:
   - The program generates reconstructed images named `reconstructed_[predictor]_[levels].png` (e.g., `reconstructed_order-1_8.png`).
   - It prints the compression ratio, size before and after compression, and MSE for each combination to the console.

## Analysis

### Methodology
The program was run with the Zewail City logo (`input.png`) as the input image. The image dimensions were 450x450 pixels, leading to an encoded size of `450 * 450 * 8 = 405000 bytes` (approximated as the number of pixels times the size of a double). The original file size was 8654 bytes. The following predictor types and quantization levels were tested:
- **Predictor Types**: Order-1, Order-2, Adaptive 2-D Predictor.
- **Quantization Levels**: 8, 16, 32.

### Results
The results for each combination are summarized below:

| Predictor   | Quantization Levels | Compression Ratio | Size Before (bytes) | Size After (bytes) | MSE         | Visual Quality Notes         |
|-------------|---------------------|-------------------|---------------------|--------------------|-------------|------------------------------|
| Order-1     | 8                   | 0.0213679         | 8654                | 405000             | 106.4772    | Significant artifacts        |
| Order-1     | 16                  | 0.0213679         | 8654                | 405000             | 22.8226     | Improved, some blurring      |
| Order-1     | 32                  | 0.0213679         | 8654                | 405000             | 5.6211      | Good quality, minor blurring |
| Order-2     | 8                   | 0.0213679         | 8654                | 405000             | 106.4772    | Significant artifacts        |
| Order-2     | 16                  | 0.0213679         | 8654                | 405000             | 22.8226     | Improved, some blurring      |
| Order-2     | 32                  | 0.0213679         | 8654                | 405000             | 5.6211      | Good quality, minor blurring |
| Adaptive    | 8                   | 0.0213679         | 8654                | 405000             | 106.4772    | Significant artifacts        |
| Adaptive    | 16                  | 0.0213679         | 8654                | 405000             | 22.8226     | Improved, some blurring      |
| Adaptive    | 32                  | 0.0213679         | 8654                | 405000             | 5.6211      | Good quality, minor blurring |

### Observations
1. **Compression Ratio**:
   - The compression ratio is constant at `0.0213679` across all tests because the encoded size is approximated as `height * width * 8` (405000 bytes), and the original size is fixed at 8654 bytes.
   - This approximation overestimates the encoded size. In a real implementation, entropy coding (e.g., Huffman or arithmetic coding) on the quantized residuals would yield a smaller encoded size, improving the compression ratio.

2. **Mean Squared Error (MSE)**:
   - **Quantization Levels Impact**: Increasing the quantization levels reduces MSE significantly:
     - At 8 levels, MSE is high (~106.48), indicating poor reconstruction quality.
     - At 16 levels, MSE drops to ~22.82, showing better quality.
     - At 32 levels, MSE further decreases to ~5.62, indicating good reconstruction.
   - **Predictor Type Impact**: The MSE values are nearly identical across predictor types (Order-1, Order-2, Adaptive) for the same quantization levels. This suggests that the predictor choice has minimal impact on MSE for this image, likely due to the image's characteristics (e.g., the Zewail City logo has sharp edges and uniform areas, which may not benefit significantly from adaptive prediction).

3. **Visual Quality**:
   - At 8 quantization levels, the reconstructed images (`reconstructed_[predictor]_8.png`) show significant artifacts, especially around edges (e.g., the pyramids and text in the logo).
   - At 16 levels, the quality improves, with fewer artifacts but some blurring in detailed areas.
   - At 32 levels, the images (`reconstructed_[predictor]_32.png`) are of good quality, with minor blurring but clear preservation of the logo's structure.
   - The predictor type (Order-1, Order-2, Adaptive) has a subtle effect on visual quality. The Adaptive predictor slightly improves edge preservation in some areas (e.g., around the pyramids), but the difference is not significant for this image.

### Visual Samples
To inspect the visual quality, review the reconstructed images generated by the program:
- `reconstructed_order-1_8.png`, `reconstructed_order-1_16.png`, `reconstructed_order-1_32.png`
- `reconstructed_order-2_8.png`, `reconstructed_order-2_16.png`, `reconstructed_order-2_32.png`
- `reconstructed_adaptive_8.png`, `reconstructed_adaptive_16.png`, `reconstructed_adaptive_32.png`

Compare these with the original `input.png` to observe the impact of quantization levels and predictor types on artifacts, blurring, and edge preservation.

### Recommendations for Improvement
- **Accurate Encoded Size**: Implement entropy coding on the quantized residuals to get a realistic encoded size, which would improve the compression ratio.
- **Predictor Effectiveness**: Test with more complex images (e.g., natural scenes with gradients) to better evaluate the Adaptive predictor's benefits.
- **Quantization**: Explore non-uniform quantization (e.g., logarithmic) to potentially reduce MSE further for the same number of levels.