package com.procoder.gui;

import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Created by reneb_000 on 14-4-2015.
 */
public class Selfie {

    Webcam webcam = Webcam.getDefault();
    webcam.open(true);
}
