package dev.sakurakooi.easywatermark;

import dev.sakurakooi.easywatermark.pojo.Configuration;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class WatermarkRenderer {
    public static BufferedImage renderWatermark(BufferedImage processingImage, Configuration configuration) {
        // 1. create a new transparent image with the double size as processingImage
        int watermarkWidth = processingImage.getWidth() * 2;
        int watermarkHeight = processingImage.getHeight() * 2;
        BufferedImage watermarkImage = new BufferedImage(watermarkWidth, watermarkHeight, BufferedImage.TYPE_INT_ARGB);
        
        // 2. render pattern brick text on the new image according to configuration(without rotate and transparency)
        Graphics2D watermarkGraphics = watermarkImage.createGraphics();
        
        // Enable anti-aliasing for better text quality
        watermarkGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        watermarkGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        watermarkGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        // Set up font from configuration
        Font font = new Font(configuration.getFont(), configuration.getFontStyle(), configuration.getFontSize());
        watermarkGraphics.setFont(font);
        
        // Set color from configuration (convert long to Color)
        Color textColor = new Color((int) (configuration.getColor() & 0xFFFFFFFFL), true);
        watermarkGraphics.setColor(textColor);
        
        // Get font metrics for text measurement
        FontMetrics fontMetrics = watermarkGraphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(configuration.getText());
        int textHeight = fontMetrics.getHeight();
        
        // Create brick pattern with configured gaps (staggered/brick layout)
        int row = 0;
        int horizontalSpacing = textWidth + configuration.getGapX();
        
        for (int y = 0; y < watermarkHeight + textHeight; y += textHeight + configuration.getGapY()) {
            // Calculate starting X position for staggered pattern
            int startX = (row % 2 == 1) ? (horizontalSpacing / 2) : 0;
            
            for (int x = startX; x < watermarkWidth + textWidth; x += horizontalSpacing) {
                watermarkGraphics.drawString(configuration.getText(), x, y);
            }
            row++;
        }
        
        watermarkGraphics.dispose();
        
        // 3. rotate the new image according to configuration and overlay it on processingImage with configured alpha
        BufferedImage resultImage = new BufferedImage(processingImage.getWidth(), processingImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D resultGraphics = resultImage.createGraphics();
        
        // Enable anti-aliasing for the result
        resultGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        resultGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        resultGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw the original image first
        resultGraphics.drawImage(processingImage, 0, 0, null);
        
        // Apply rotation and transparency
        if (configuration.getRotate() != 0) {
            // Calculate center point for rotation
            double centerX = processingImage.getWidth() / 2.0;
            double centerY = processingImage.getHeight() / 2.0;
            
            // Create transform for rotation
            AffineTransform transform = new AffineTransform();
            transform.translate(centerX, centerY);
            transform.rotate(Math.toRadians(configuration.getRotate()));
            transform.translate(-centerX, -centerY);
            
            resultGraphics.setTransform(transform);
        }
        
        // Set transparency (alpha composite)
        float alpha = configuration.getTransparency() / 255.0f;
        resultGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Draw the rotated watermark pattern
        // Position the watermark to cover the entire image when rotated
        int drawX = -(watermarkWidth - processingImage.getWidth()) / 2;
        int drawY = -(watermarkHeight - processingImage.getHeight()) / 2;
        resultGraphics.drawImage(watermarkImage, drawX, drawY, null);
        
        resultGraphics.dispose();
        
        return resultImage;
    }
}
