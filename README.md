# image-color-quantization
A JavaFX application for image color quantization using machine learning algorithms (Self-Organizing Map and Mini Batch K-Means). This application was developed as my Computer Science senior project at AUBG.

## Overview
Image color quantization is a technique that reduces the number of colors in a given image without much loss of quality and important information. The project supports two machine learning algorithms for the color quantization process: **Self-Organizing Map (SOM)** and **Mini Batch K-Means Clustering**. Both algorithms were implemented from scratch and are optimized for speed and output quality.

## Features
- Upload an image in **PNG** or **JPEG** format.
- Choose a quantization algorithm (either SOM or Mini Batch K-Means) and configure its parameters.
- Click a button to color-quantize the input image.
- Edit the learned color palette of the color-quantized image.
- Download the color-quantized image. 8-bit PNG, 24-bit PNG, or JPEG formats are supported.

## Installation
Requirements:
- **Java Development Kit (JDK)** 17 or later. For best compatibility, download the official [Oracle JDK](https://www.oracle.com/java/technologies/downloads/).
- **JavaFX Runtime** compatible with your JDK version. [Link to the **Gluon** website to install JavaFX]( https://gluonhq.com/products/javafx/). 
- (Optional) **Scene Builder** for modifying the UI. [Link to the **Scene Builder** download page](https://gluonhq.com/products/scene-builder/).
   
### Setup:
1. Clone this repository.
2. Ensure JavaFX is configured correctly in your IDE. You should add the SDK to your IDE of choice and also configure the VM options:

```
--module-path "path_to_javafx_lib" --add-modules javafx.controls,javafx.fxml
```

Where the "path_to_javafx_lib" is the path to the lib folder of your JavaFX Runtime installation.

## Running the Program
- You must build and run the project using your preferred IDE. 
- Start the application by running **src/main/java/views/QuantizerView.java**

## Acknowledgments
Supervisor: Vladimir Georgiev, Department of Computer Science, AUBG
