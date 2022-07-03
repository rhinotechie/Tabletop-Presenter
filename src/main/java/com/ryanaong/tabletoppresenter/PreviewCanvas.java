package com.ryanaong.tabletoppresenter;

import javafx.scene.canvas.Canvas;

public class PreviewCanvas extends Canvas {
    public PreviewCanvas(double v, double v1) {
        super(v, v1);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double minWidth(double v) {
        return 300D;
    }

    @Override
    public double minHeight(double v) {
        return 300D;
    }

    @Override
    public double maxWidth(double v) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double maxHeight(double v) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public void resize(double v, double v1) {
        super.setWidth(v);
        super.setHeight(v1);

    }
}
