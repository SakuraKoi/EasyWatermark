package dev.sakurakooi.easywatermark;

import dev.sakurakooi.easywatermark.pojo.Configuration;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class WatermarkRenderer {
    public static BufferedImage renderWatermark(BufferedImage processingImage, Configuration configuration) {
        double imageDiagonal = Math.sqrt(
            processingImage.getWidth() * processingImage.getWidth() + 
            processingImage.getHeight() * processingImage.getHeight()
        );
        double referenceDiagonal = Math.sqrt(1920.0 * 1920.0 + 1080.0 * 1080.0); // Reference: 1920x1080
        double scaleFactor = imageDiagonal / referenceDiagonal;

        int scaledFontSize = Math.max(4, (int) Math.round(configuration.getFontSize() * scaleFactor));
        int scaledGapX = Math.max(2, (int) Math.round(configuration.getGapX() * scaleFactor));
        int scaledGapY = Math.max(2, (int) Math.round(configuration.getGapY() * scaleFactor));

        int watermarkWidth = processingImage.getWidth() * 2;
        int watermarkHeight = processingImage.getHeight() * 2;
        BufferedImage watermarkImage = new BufferedImage(watermarkWidth, watermarkHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D watermarkGraphics = watermarkImage.createGraphics();

        watermarkGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        watermarkGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        watermarkGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        Font font = new Font(configuration.getFont(), configuration.getFontStyle(), scaledFontSize);
        watermarkGraphics.setFont(font);

        Color textColor = new Color((int) (configuration.getColor() & 0xFFFFFFFFL), true);
        watermarkGraphics.setColor(textColor);

        FontMetrics fontMetrics = watermarkGraphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(configuration.getText());
        int textHeight = fontMetrics.getHeight();

        int row = 0;
        int horizontalSpacing = textWidth + scaledGapX;
        
        for (int y = 0; y < watermarkHeight + textHeight; y += textHeight + scaledGapY) {
            int startX = (row % 2 == 1) ? (horizontalSpacing / 2) : 0;
            
            for (int x = startX; x < watermarkWidth + textWidth; x += horizontalSpacing) {
                watermarkGraphics.drawString(configuration.getText(), x, y);
            }
            row++;
        }
        
        watermarkGraphics.dispose();

        BufferedImage resultImage = new BufferedImage(processingImage.getWidth(), processingImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D resultGraphics = resultImage.createGraphics();

        resultGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        resultGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        resultGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        resultGraphics.drawImage(processingImage, 0, 0, null);

        if (configuration.getRotate() != 0) {
            double centerX = processingImage.getWidth() / 2.0;
            double centerY = processingImage.getHeight() / 2.0;

            AffineTransform transform = new AffineTransform();
            transform.translate(centerX, centerY);
            transform.rotate(Math.toRadians(configuration.getRotate()));
            transform.translate(-centerX, -centerY);
            
            resultGraphics.setTransform(transform);
        }

        float alpha = configuration.getTransparency() / 255.0f;
        resultGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        int drawX = -(watermarkWidth - processingImage.getWidth()) / 2;
        int drawY = -(watermarkHeight - processingImage.getHeight()) / 2;
        resultGraphics.drawImage(watermarkImage, drawX, drawY, null);
        
        resultGraphics.dispose();
        
        return resultImage;
    }
}
