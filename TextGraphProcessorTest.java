import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class TextGraphProcessorTest {
    private TextGraphProcessor processor;
    
    @Before
    public void setUp() throws IOException {
        // 使用测试文件初始化处理器
        processor = new TextGraphProcessor("Easy_Test.txt");
        processor.readTextAndBuildGraph();
    }
    
    @Test
    public void testCase1_ValidEquivalenceClass() {
        // 测试用例1: 覆盖所有有效等价类 (1)(2)(3)
        // word1="analyzed", word2="data"
        String result = processor.queryBridgeWords("analyzed", "data");
        
        // 期望输出: The bridge words from "analyzed" to "data" are: "the"
        assertTrue("结果应该包含桥接词信息", 
                   result.contains("The bridge words from \"analyzed\" to \"data\" are: \"the\""));
    }
    
    @Test
    public void testCase2_InvalidEquivalenceClass_Word1NotInGraph() {
        // 测试用例2: 无效等价类 (4) - word1不存在于图中
        String result = processor.queryBridgeWords("unknown", "data");
        
        // 断言：应该提示word1不在图中
        assertTrue("应该提示单词不在图中", result.contains("No unknown or data in the graph!"));
    }
    
    @Test
    public void testCase3_InvalidEquivalenceClass_Word2NotInGraph() {
        // 测试用例3: 无效等价类 (5) - word2不存在于图中
        String result = processor.queryBridgeWords("the", "unknown");
        
        // 断言：应该提示word2不在图中
        assertTrue("应该提示单词不在图中", result.contains("No the or unknown in the graph!"));
    }
    
    @Test
    public void testCase4_InvalidEquivalenceClass_NoBridgeWords() {
        // 测试用例4: 无效等价类 (6) - 两个单词都在图中但没有桥接词
        // word1="the", word2="team"
        String result = processor.queryBridgeWords("the", "team");
        
        // 期望输出: No bridge words from "the" to "team"!
        assertTrue("应该提示没有桥接词", 
                   result.contains("No bridge words from \"the\" to \"team\"!"));
    }
} 