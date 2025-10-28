package dev.sakurakooi.easywatermark;

import dev.sakurakooi.easywatermark.pojo.Configuration;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class WatermarkRenderer {
    private static final double REFERENCE_DIAGONAL = Math.sqrt(1920.0 * 1920.0 + 1080.0 * 1080.0);
    private static final RenderingHints[] WATERMARK_RENDERING_HINTS = {
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON),
        new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON),
        new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    };
    private static final RenderingHints[] RESULT_RENDERING_HINTS = {
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON),
        new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR),
        new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    };

    public static BufferedImage renderWatermark(BufferedImage processingImage, Configuration configuration) {
        if (configuration.getTransparency() == 0) {
            return processingImage;
        }

        String watermarkText = configuration.getText();
        int rotate = configuration.getRotate();
        int transparency = configuration.getTransparency();

        int imageWidth = processingImage.getWidth();
        int imageHeight = processingImage.getHeight();
        double imageDiagonal = Math.sqrt(imageWidth * imageWidth + imageHeight * imageHeight);
        double scaleFactor = imageDiagonal / REFERENCE_DIAGONAL;

        int scaledFontSize = Math.max(4, (int) Math.round(configuration.getFontSize() * scaleFactor));
        int scaledGapX = Math.max(2, (int) Math.round(configuration.getGapX() * scaleFactor));
        int scaledGapY = Math.max(2, (int) Math.round(configuration.getGapY() * scaleFactor));

        double rotationRadians = Math.abs(Math.toRadians(rotate));
        int sizeMultiplier = (rotate == 0) ? 1 : (int) Math.ceil(1.0 + Math.sin(rotationRadians) + Math.cos(rotationRadians));
        int watermarkWidth = imageWidth * sizeMultiplier;
        int watermarkHeight = imageHeight * sizeMultiplier;

        BufferedImage watermarkImage = new BufferedImage(watermarkWidth, watermarkHeight, BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D watermarkGraphics = watermarkImage.createGraphics();

        for (RenderingHints hint : WATERMARK_RENDERING_HINTS) {
            watermarkGraphics.addRenderingHints(hint);
        }

        Font font = new Font(configuration.getFont(), configuration.getFontStyle(), scaledFontSize);
        watermarkGraphics.setFont(font);
        Color textColor = new Color((int) (configuration.getColor() & 0xFFFFFFFFL), true);
        watermarkGraphics.setColor(textColor);

        FontMetrics fontMetrics = watermarkGraphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(watermarkText);
        int textHeight = fontMetrics.getHeight();
        int horizontalSpacing = textWidth + scaledGapX;
        int verticalSpacing = textHeight + scaledGapY;

        int row = 0;
        int startXOffset = horizontalSpacing / 2;
        
        for (int y = 0; y < watermarkHeight + textHeight; y += verticalSpacing) {
            int startX = (row % 2 == 1) ? startXOffset : 0;

            int instancesNeeded = (watermarkWidth + textWidth - startX + horizontalSpacing - 1) / horizontalSpacing;
            
            for (int i = 0; i < instancesNeeded; i++) {
                int x = startX + i * horizontalSpacing;
                watermarkGraphics.drawString(watermarkText, x, y);
            }
            row++;
        }
        
        watermarkGraphics.dispose();

        BufferedImage resultImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D resultGraphics = resultImage.createGraphics();

        for (RenderingHints hint : RESULT_RENDERING_HINTS) {
            resultGraphics.addRenderingHints(hint);
        }

        resultGraphics.drawImage(processingImage, 0, 0, null);

        if (rotate != 0) {
            double centerX = imageWidth / 2.0;
            double centerY = imageHeight / 2.0;

            AffineTransform transform = new AffineTransform();
            transform.translate(centerX, centerY);
            transform.rotate(Math.toRadians(rotate));
            transform.translate(-centerX, -centerY);
            
            resultGraphics.setTransform(transform);
        }

        float alpha = transparency / 255.0f;
        resultGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int drawX = -(watermarkWidth - imageWidth) / 2;
        int drawY = -(watermarkHeight - imageHeight) / 2;
        resultGraphics.drawImage(watermarkImage, drawX, drawY, null);
        
        resultGraphics.dispose();
        
        return resultImage;
    }
}
