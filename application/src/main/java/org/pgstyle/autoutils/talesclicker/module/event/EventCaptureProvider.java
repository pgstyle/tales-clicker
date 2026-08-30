package org.pgstyle.autoutils.talesclicker.module.event;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.pgstyle.autoutils.talesclicker.application.AppUtils;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.imagedb.Capture;
import org.pgstyle.autoutils.talesclicker.imagedb.Stencil;
import org.pgstyle.autoutils.talesclicker.module.running.RunningCapture;

public class EventCaptureProvider {

    public class EventCapture extends Capture {

        protected EventCapture(BufferedImage image) {
            super(image);
        }

        public Point findAnchor() {
            return this.getPointsOffset(EventCaptureProvider.this.anchorStencil, 6 / 256f);
        }
        
    }

    /**
     * Create a capture provider object from anchor image.
     *
     * @param image am image
     * @return a capture provider object
     */
    public static EventCaptureProvider anchor(BufferedImage image) {
        return new EventCaptureProvider(image);
    }

    private EventCaptureProvider(BufferedImage image) {
        this.anchorStencil = Collections.unmodifiableMap(Stencil.fromImage(image));
    }

    private final Map<Point, Color> anchorStencil;

    /**
     * Find the specified point reference in the underlying image.
     *
     * @param image a screenshot image
     * @return a capture object
     */
    public EventCapture capture(BufferedImage image) {
        return new EventCapture(image);
    }
}
