package dev.sakurakooi.easywatermark.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.MagicConstant;

import java.awt.*;

@NoArgsConstructor
@Data
public class Configuration {
    @JSONField(name = "text")
    private String text = "EasyWatermark";
    @JSONField(name = "font")
    private String font = "Microsoft YaHei";
    @JSONField(name = "fontSize")
    private Integer fontSize = 12;
    @JSONField(name = "gapX")
    private Integer gapX = 16;
    @JSONField(name = "gapY")
    private Integer gapY = 16;
    @JSONField(name = "rotate")
    private Integer rotate = 30;
    @JSONField(name = "fontStyle")
    @MagicConstant(flags = {Font. PLAIN,Font. BOLD,Font. ITALIC})
    private Integer fontStyle = Font.PLAIN;
    @JSONField(name = "transparency")
    private Integer transparency = 128;
    @JSONField
    private Long color = 0xFFFFFFFFL;
}
