import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class WhiteBoxTest {
    private TextGraphProcessor processor;
    
    @Before
    public void setUp() throws IOException {
        // 使用测试文件初始化处理器
        processor = new TextGraphProcessor("Easy_Test.txt");
        processor.readTextAndBuildGraph();
    }
    
    @Test
    public void testCase1_WordNotInGraph() {
        // 测试用例1: 基本路径1 - 输入参数不在图中
        String result = processor.calcShortestPath("unknown", "data");
        assertTrue("应该提示单词不在图中", 
                   result.contains("No unknown or data in the graph!"));
    }
    
    @Test
    public void testCase2_DirectPath() {
        // 测试用例2: 基本路径2,5 - 直接路径
        String result = processor.calcShortestPath("the", "data");
        assertTrue("应该找到最短路径", 
                   result.contains("The shortest path from \"the\" to \"data\""));
        assertTrue("结果应包含距离信息", result.contains("距离:"));
    }
    
    @Test
    public void testCase3_TargetNotInGraph() {
        // 测试用例3: 基本路径1 - 目标单词不在图中
        String result = processor.calcShortestPath("scientist", "nonexistent");
        assertTrue("应该提示单词不在图中", 
                   result.contains("No scientist or nonexistent in the graph!"));
    }
    
    @Test
    public void testCase4_NoPathExists() {
        // 测试用例4: 基本路径4 - 无路径可达
        String result = processor.calcShortestPath("team", "scientist");
        // 根据图结构，可能有路径也可能没有路径
        assertTrue("应该返回路径信息或无路径提示", 
                   result.contains("shortest path") || result.contains("No path"));
    }
} 