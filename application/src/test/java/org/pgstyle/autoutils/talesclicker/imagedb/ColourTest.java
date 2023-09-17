package org.pgstyle.autoutils.talesclicker.imagedb;

import java.awt.Color;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ColourTest {
    
    @Test
    public void colourDifferent() {
        Assertions.assertEquals(1f / 256, Capture.colourDifferent(new Color(0x4685e5), new Color(0x4686e5)));
    }
}
