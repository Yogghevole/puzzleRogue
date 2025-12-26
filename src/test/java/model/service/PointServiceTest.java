package model.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Unit tests for PointService.
 * Verifies score calculation logic, including base points, multipliers, and victory bonuses.
 */
public class PointServiceTest {

    @Test
    public void testScoreBreakdown_Basic() {
        PointService service = new PointService();
        
        PointService.ScoreBreakdown bd = service.buildBreakdown(5, 2, 3, 4, false, 0, 0, 1.0, false);
        
        assertNotNull(bd);
        assertEquals(275, bd.getTotal());
    }
    
    @Test
    public void testScoreBreakdown_VictoryAndMultipliers() {
        PointService service = new PointService();
        
        PointService.ScoreBreakdown bd = service.buildBreakdown(10, 0, 0, 0, true, 100, 1, 1.5, false);
        
        assertEquals(1275, bd.getTotal());
        assertEquals(850, bd.getSubtotal());
        assertEquals(425, bd.getMultiplierAdded());
    }
}
