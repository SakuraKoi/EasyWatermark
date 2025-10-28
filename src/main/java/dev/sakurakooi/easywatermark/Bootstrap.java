package dev.sakurakooi.easywatermark;

import com.formdev.flatlaf.FlatLightLaf;
import dev.sakurakooi.easywatermark.ui.MainForm;

public class Bootstrap {
    public static void main(String[] args) {
        if (args.length == 0) {
            FlatLightLaf.setup();
            MainForm mainForm = new MainForm();
            mainForm.setVisible(true);
            return;
        }
    }
}
