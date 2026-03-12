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
        float goldenRatio = 0.618033988749895f; // Golden ratio — guarantees max distance between consecutive colors
        float hue = 0.0f;
        // Use HSB to asure colors well distributed
        for (int i = 0; i < total; i++) {
            hue = (hue + goldenRatio) % 1.0f; // Jump by golden ratio — never repeats pattern
            float saturation = 0.75f;
            float brightness = 0.85f;
            colors.add(Color.getHSBColor(hue, saturation, brightness));
        }
    }

    public synchronized Color getNextColor() {
        Color color = colors.get(index);
        index = (index + 1) % colors.size();
        return color;
    }
}