package util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorManager {

    private final List<Color> colors = new ArrayList<>();
    private int index = 0;
    private static final int TOTAL_COLORS = 50;

    public ColorManager() {
        generateColors(TOTAL_COLORS);
    }

    private void generateColors(int total) {
        // Genereate colors by RGB uniformly
        for (int i = 0; i < total; i++) {
            // Use HSB to asure colors well distributed
            float hue = (float) i / total;  // 0.0 a 1.0
            float saturation = 0.7f + (i % 5) * 0.06f; // a bit of variety
            float brightness = 0.7f + (i % 3) * 0.1f;  // a bit more bright
            colors.add(Color.getHSBColor(hue, (float)Math.min(saturation, 1f), (float)Math.min(brightness, 1f)));
        }
    }

    public synchronized Color getNextColor() {
        Color color = colors.get(index);
        index = (index + 1) % colors.size();
        return color;
    }
}