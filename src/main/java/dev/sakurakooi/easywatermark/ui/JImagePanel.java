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

    // For dragging
    private Point dragStartPoint;
    private int dragStartPosX;
    private int dragStartPosY;

    public JImagePanel() {
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                handleMouseWheel(e);
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
        });
    }

    private void handleMousePressed(MouseEvent e) {
        dragStartPoint = e.getPoint();
        dragStartPosX = dragPositionX;
        dragStartPosY = dragPositionY;
    }

    private void handleMouseDragged(MouseEvent e) {
        if (dragStartPoint != null) {
            int dx = e.getX() - dragStartPoint.x;
            int dy = e.getY() - dragStartPoint.y;
            dragPositionX = dragStartPosX + dx;
            dragPositionY = dragStartPosY + dy;
            repaint();
        }
    }

    private void handleMouseWheel(MouseWheelEvent e) {
        if (image == null) return;

        int oldZoomLevel = zoomLevel;
        // Zoom in or out
        int notches = e.getWheelRotation();
        if (notches < 0) {
            zoomLevel = (int) Math.round(zoomLevel * 1.1);
        } else {
            zoomLevel = (int) Math.round(zoomLevel / 1.1);
        }

        // Constrain zoom level
        zoomLevel = Math.max(5, Math.min(100, zoomLevel));

        if (oldZoomLevel != zoomLevel) {
            // Get mouse position
            Point mousePos = e.getPoint();
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            // Calculate image dimensions before zoom
            double oldScale = oldZoomLevel / 100.0;
            int oldScaledWidth = (int) (image.getWidth() * oldScale);
            int oldScaledHeight = (int) (image.getHeight() * oldScale);

            // Calculate image dimensions after zoom
            double newScale = zoomLevel / 100.0;
            int newScaledWidth = (int) (image.getWidth() * newScale);
            int newScaledHeight = (int) (image.getHeight() * newScale);

            // Calculate the image coordinate under the mouse before zoom
            // (mousePos.x - dragPositionX - (panelWidth - oldScaledWidth) / 2) / oldScale
            double imageX = (mousePos.x - dragPositionX - (panelWidth - oldScaledWidth) / 2.0) / oldScale;
            double imageY = (mousePos.y - dragPositionY - (panelHeight - oldScaledHeight) / 2.0) / oldScale;

            // Calculate the new drag position to keep the image point under the mouse
            dragPositionX = mousePos.x - (int) (imageX * newScale) - (panelWidth - newScaledWidth) / 2;
            dragPositionY = mousePos.y - (int) (imageY * newScale) - (panelHeight - newScaledHeight) / 2;

            repaint();
        }
    }

    public void resetView() {
        if (image == null) {
            this.zoomLevel = 100;
            this.dragPositionX = 0;
            this.dragPositionY = 0;
            repaint();
            return;
        }

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        // Calculate scale ratios
        double scaleX = (double) panelWidth / imageWidth;
        double scaleY = (double) panelHeight / imageHeight;

        // Choose the smaller scale to fit entire image
        double fitScale = Math.min(scaleX, scaleY);

        // Convert to zoom percentage and apply constraints
        this.zoomLevel = (int) (fitScale * 100);
        this.zoomLevel = Math.max(5, Math.min(100, this.zoomLevel));

        // Center the image
        this.dragPositionX = 0;
        this.dragPositionY = 0;

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g;

            int panelWidth = getWidth();
            int panelHeight = getHeight();

            // Calculate scaled dimensions based on zoom level
            double scale = zoomLevel / 100.0;
            int scaledWidth = (int) (image.getWidth() * scale);
            int scaledHeight = (int) (image.getHeight() * scale);

            // Calculate the top-left corner of the image, considering drag position and centering if smaller than panel
            int x = dragPositionX + (panelWidth - scaledWidth) / 2;
            int y = dragPositionY + (panelHeight - scaledHeight) / 2;

            // For better rendering quality
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(image, x, y, scaledWidth, scaledHeight, this);
        }
    }
}
