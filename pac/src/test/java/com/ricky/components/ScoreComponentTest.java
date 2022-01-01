package com.ricky.components;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ScoreComponentTest {
    
    @Test
    public void TestScore() {
        ScoreComponent sc = new ScoreComponent();
        assertEquals(0, sc.getScore());
        sc.inc(50);
        assertEquals(50, sc.getScore());
    }
}
