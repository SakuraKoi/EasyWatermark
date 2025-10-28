/*
 * Created by JFormDesigner on Tue Oct 28 09:09:08 CST 2025
 */

package dev.sakurakooi.easywatermark.ui;

import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.event.*;
import com.alibaba.fastjson.JSON;
import com.intellij.uiDesigner.core.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import dev.sakurakooi.easywatermark.WatermarkRenderer;
import dev.sakurakooi.easywatermark.pojo.Configuration;
import io.github.bhowell2.debouncer.Debouncer;
import lombok.SneakyThrows;
import org.drjekyll.fontchooser.FontDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author sakura
 */
public class MainForm extends JFrame {
    private Debouncer debouncer = new Debouncer(1);
    private Configuration configuration;

    private File processingFile;
    private BufferedImage processingImage;
    private BufferedImage watermarkedImage;

    public MainForm() {
        initComponents();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1366, 768));
        initialize();
        this.pack();
        this.setLocationRelativeTo(null);
        loadConfig();
    }

    @SneakyThrows
    private void loadConfig() {
        File configFile = new File("config.json");
        if (!configFile.exists()) {
            configuration = new Configuration();
            saveConfig();
        } else {
            String configContent = Files.readString(configFile.toPath());
            configuration = JSON.parseObject(configContent, Configuration.class);
        }

        editWatermark.setText(configuration.getText());
        editFont.setText(configuration.getFont());
        editFont.setFont(new Font(configuration.getFont(), configuration.getFontStyle(), configuration.getFontSize()));
        editColor.setText("#" + Long.toHexString(configuration.getColor()).toUpperCase());
        editColor.setBackground(new Color((int) (configuration.getColor() & 0xFFFFFFFFL), true));
        editColor.setForeground(new Color((int) (~configuration.getColor() & 0x00FFFFFFL) | 0xFF000000, true));
        inputGapX.setValue(configuration.getGapX());
        inputGapY.setValue(configuration.getGapY());
        inputRotate.setValue(configuration.getRotate());
        inputTransparency.setValue(configuration.getTransparency());
    }

    private void saveConfig() {
        //noinspection unchecked
        debouncer.addRunLast(15, TimeUnit.MILLISECONDS, "save", k -> {
            File configFile = new File("config.json");
            try {
                Files.writeString(configFile.toPath(), JSON.toJSONString(configuration));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() {
        imgPreviewer.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    //noinspection unchecked
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if(!droppedFiles.isEmpty()) {
                        processingFile = droppedFiles.get(0);
                        processingImage = javax.imageio.ImageIO.read(processingFile);
                        renderImage();
                    }
                    evt.dropComplete(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        registerFocusListenerForSpinner(inputGapX);
        registerFocusListenerForSpinner(inputGapY);
        registerFocusListenerForSpinner(inputRotate);
        registerFocusListenerForSpinner(inputTransparency);
    }

    private void registerFocusListenerForSpinner(JSpinner spinner) {
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                applyFocusSpinner(spinner);
            }
        });
    }

    private void renderImage() {
        if (processingImage != null) {
            watermarkedImage = WatermarkRenderer.renderWatermark(processingImage, configuration);
            displayPreview();
        }
    }

    private void displayPreview() {
        if (watermarkedImage != null) {
            imgPreviewer.setImage(watermarkedImage);
            imgPreviewer.repaint();
        }
    }

    private void editFontMouseClicked(MouseEvent e) {
        FontDialog dialog = new FontDialog(this, "Choose Font", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        if (!dialog.isCancelSelected()) {
            configuration.setFont(dialog.getSelectedFont().getFamily());
            configuration.setFontSize(dialog.getSelectedFont().getSize());
            configuration.setFontStyle(dialog.getSelectedFont().getStyle());
            renderImage();

            editFont.setText(configuration.getFont());
            editFont.setFont(dialog.getSelectedFont());
            saveConfig();
        }
    }

    private void editWatermarkCaretUpdate(CaretEvent e) {
        configuration.setText(editWatermark.getText());
        renderImage();
        saveConfig();
    }

    private void inputGapXStateChanged(ChangeEvent e) {
        configuration.setGapX((Integer) inputGapX.getValue());
        renderImage();
        saveConfig();
    }

    private void inputGapYStateChanged(ChangeEvent e) {
        configuration.setGapY((Integer) inputGapY.getValue());
        renderImage();
        saveConfig();
    }

    private void inputRotateStateChanged(ChangeEvent e) {
        configuration.setRotate((Integer) inputRotate.getValue());
        renderImage();
        saveConfig();
    }

    private void inputTransparencyStateChanged(ChangeEvent e) {
        configuration.setTransparency((Integer) inputTransparency.getValue());
        renderImage();
        saveConfig();
    }

    private JSpinner focusedSpinner = null;

    private void applyFocusSpinner(JSpinner spinner) {
        focusedSpinner = spinner;
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        valueSlider.setModel(new DefaultBoundedRangeModel((Integer) spinner.getValue(), 0, (Integer) model.getMinimum(), Objects.requireNonNullElse((Integer) model.getMaximum(), 255)));
        inputGapX.setBackground(spinner == inputGapX ? Color.YELLOW : Color.WHITE);
        inputGapY.setBackground(spinner == inputGapY ? Color.YELLOW : Color.WHITE);
        inputRotate.setBackground(spinner == inputRotate ? Color.YELLOW : Color.WHITE);
        inputTransparency.setBackground(spinner == inputTransparency ? Color.YELLOW : Color.WHITE);
        valueSlider.setEnabled(true);
    }

    private void valueSliderStateChanged(ChangeEvent e) {
        if (focusedSpinner != null) {
            focusedSpinner.setValue(valueSlider.getValue());
            //renderImage();
           // saveConfig();
        }
    }

    private void editColorMouseClicked(MouseEvent e) {
        Color newColor = JColorChooser.showDialog(this, "Choose a color", editColor.getBackground());
        if (newColor != null) {
            configuration.setColor(((long) newColor.getAlpha() << 24) | ((long) newColor.getRed() << 16) | ((long) newColor.getGreen() << 8) | (long) newColor.getBlue());
            renderImage();
            editColor.setText("#" + Long.toHexString(configuration.getColor()).toUpperCase());
            editColor.setBackground(newColor);
            editColor.setForeground(new Color((int) (~configuration.getColor() & 0x00FFFFFFL) | 0xFF000000, true));
            saveConfig();
        }
    }

    private void imgPreviewerComponentResized(ComponentEvent e) {
        imgPreviewer.repaint();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        panel1 = new JPanel();
        imgPreviewer = new JImagePanel();
        valueSlider = new JSlider();
        panel3 = new JPanel();
        panel2 = new JPanel();
        label1 = new JLabel();
        editWatermark = new JTextField();
        panel4 = new JPanel();
        label2 = new JLabel();
        editFont = new JTextField();
        panel9 = new JPanel();
        label7 = new JLabel();
        editColor = new JTextField();
        panel5 = new JPanel();
        label3 = new JLabel();
        inputGapX = new JSpinner();
        panel6 = new JPanel();
        label4 = new JLabel();
        inputGapY = new JSpinner();
        panel7 = new JPanel();
        label5 = new JLabel();
        inputRotate = new JSpinner();
        panel8 = new JPanel();
        label6 = new JLabel();
        inputTransparency = new JSpinner();
        var vSpacer1 = new Spacer();
        btnSave = new JButton();

        //======== this ========
        setTitle("EasyWatermark");
        var contentPane = getContentPane();
        contentPane.setLayout(new GridLayoutManager(1, 2, new Insets(8, 8, 8, 8), 8, 8));

        //======== panel1 ========
        {
            panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), 8, 8));

            //---- imgPreviewer ----
            imgPreviewer.setBorder(new LineBorder(Color.lightGray));
            imgPreviewer.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    imgPreviewerComponentResized(e);
                }
            });
            panel1.add(imgPreviewer, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));

            //---- valueSlider ----
            valueSlider.setEnabled(false);
            valueSlider.addChangeListener(e -> valueSliderStateChanged(e));
            panel1.add(valueSlider, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, new Dimension(0, 16), new Dimension(9999, 24)));
        }
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1,
            GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
            null, null, null));

        //======== panel3 ========
        {
            panel3.setLayout(new GridLayoutManager(9, 1, new Insets(0, 0, 0, 0), -1, 8));

            //======== panel2 ========
            {
                panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));

                //---- label1 ----
                label1.setText("Watermark");
                panel2.add(label1, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- editWatermark ----
                editWatermark.addCaretListener(e -> editWatermarkCaretUpdate(e));
                panel2.add(editWatermark, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    new Dimension(256, 16), null, null));
            }
            panel3.add(panel2, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //======== panel4 ========
            {
                panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));

                //---- label2 ----
                label2.setText("Font");
                panel4.add(label2, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- editFont ----
                editFont.setEditable(false);
                editFont.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        editFontMouseClicked(e);
                    }
                });
                panel4.add(editFont, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    new Dimension(256, 64), null, new Dimension(256, 64)));
            }
            panel3.add(panel4, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //======== panel9 ========
            {
                panel9.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));

                //---- label7 ----
                label7.setText("Color");
                panel9.add(label7, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- editColor ----
                editColor.setEditable(false);
                editColor.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        editColorMouseClicked(e);
                    }
                });
                panel9.add(editColor, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            }
            panel3.add(panel9, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //======== panel5 ========
            {
                panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));

                //---- label3 ----
                label3.setText("Gap X");
                panel5.add(label3, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- inputGapX ----
                inputGapX.setModel(new SpinnerNumberModel(0, 0, null, 1));
                inputGapX.addChangeListener(e -> inputGapXStateChanged(e));
                panel5.add(inputGapX, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            }
            panel3.add(panel5, new GridConstraints(3, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //======== panel6 ========
            {
                panel6.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));

                //---- label4 ----
                label4.setText("Gap Y");
                panel6.add(label4, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- inputGapY ----
                inputGapY.setModel(new SpinnerNumberModel(0, 0, null, 1));
                inputGapY.addChangeListener(e -> inputGapYStateChanged(e));
                panel6.add(inputGapY, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            }
            panel3.add(panel6, new GridConstraints(4, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //======== panel7 ========
            {
                panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));

                //---- label5 ----
                label5.setText("Rotate");
                panel7.add(label5, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- inputRotate ----
                inputRotate.setModel(new SpinnerNumberModel(0, 0, 360, 1));
                inputRotate.addChangeListener(e -> inputRotateStateChanged(e));
                panel7.add(inputRotate, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            }
            panel3.add(panel7, new GridConstraints(5, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //======== panel8 ========
            {
                panel8.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));

                //---- label6 ----
                label6.setText("Transparency");
                panel8.add(label6, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- inputTransparency ----
                inputTransparency.setModel(new SpinnerNumberModel(128, 0, 255, 1));
                inputTransparency.addChangeListener(e -> inputTransparencyStateChanged(e));
                panel8.add(inputTransparency, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            }
            panel3.add(panel8, new GridConstraints(6, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
            panel3.add(vSpacer1, new GridConstraints(7, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));

            //---- btnSave ----
            btnSave.setText("Save");
            panel3.add(btnSave, new GridConstraints(8, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                new Dimension(10, 64), null, null));
        }
        contentPane.add(panel3, new GridConstraints(0, 1, 1, 1,
            GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            null, null, null));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel panel1;
    private JImagePanel imgPreviewer;
    private JSlider valueSlider;
    private JPanel panel3;
    private JPanel panel2;
    private JLabel label1;
    private JTextField editWatermark;
    private JPanel panel4;
    private JLabel label2;
    private JTextField editFont;
    private JPanel panel9;
    private JLabel label7;
    private JTextField editColor;
    private JPanel panel5;
    private JLabel label3;
    private JSpinner inputGapX;
    private JPanel panel6;
    private JLabel label4;
    private JSpinner inputGapY;
    private JPanel panel7;
    private JLabel label5;
    private JSpinner inputRotate;
    private JPanel panel8;
    private JLabel label6;
    private JSpinner inputTransparency;
    private JButton btnSave;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
