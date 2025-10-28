package dev.sakurakooi.easywatermark.ui;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class JImagePanel extends JPanel {
    @Getter @Setter
    private BufferedImage image;

    private int zoomLevel = 100; // percentage
    private int dragPositionX = 0;
    private int dragPositionY = 0;

    public JImagePanel() {
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }
        });
    }

    public void resetView() {
        this.zoomLevel = 100;
        this.dragPositionX = 0;
        this.dragPositionY = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            // Calculate scaled dimensions to maintain aspect ratio
            double imageAspectRatio = (double) image.getWidth() / image.getHeight();
            double panelAspectRatio = (double) panelWidth / panelHeight;

            int scaledWidth;
            int scaledHeight;

            if (imageAspectRatio > panelAspectRatio) {
                scaledWidth = panelWidth;
                scaledHeight = (int) (panelWidth / imageAspectRatio);
            } else {
                scaledHeight = panelHeight;
                scaledWidth = (int) (panelHeight * imageAspectRatio);
            }

            // Center the image if it doesn't fill the panel completely
            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - scaledHeight) / 2;

            g.drawImage(image, x, y, scaledWidth, scaledHeight, this);
        }
    }
}