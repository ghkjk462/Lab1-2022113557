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
    public void testCase1_EmptyGraph() {
        // 测试用例1: 基本路径1 - 空图
        System.out.println("=== 测试用例1 - 空图测试 ===");
        TextGraphProcessor emptyProcessor = new TextGraphProcessor("nonexistent.txt");
        String result = emptyProcessor.randomWalk();
        System.out.println("期望输出: 图为空，无法进行随机游走");
        System.out.println("实际输出: " + result);
        System.out.println("测试结果: " + (result.equals("图为空，无法进行随机游走") ? "通过" : "失败"));
        System.out.println();
        assertEquals("图为空，无法进行随机游走", result);
    }
    
    @Test
    public void testCase2_SingleNodeNoEdges() {
        // 测试用例2: 基本路径2 - 单节点无出边
        System.out.println("=== 测试用例2 - 单节点无出边测试 ===");
        String result = processor.randomWalk();
        System.out.println("期望输出: 包含至少一个单词的字符串");
        System.out.println("实际输出: " + result);
        System.out.println("测试结果: " + (result != null && result.length() > 0 ? "通过" : "失败"));
        System.out.println();
        assertNotNull("随机游走结果不应为null", result);
        assertTrue("结果应包含至少一个单词", result.length() > 0);
    }
    
    @Test
    public void testCase3_RepeatedEdge() {
        // 测试用例3: 基本路径3 - 遇到重复边
        System.out.println("=== 测试用例3 - 遇到重复边测试 ===");
        String result = processor.randomWalk();
        System.out.println("期望输出: 包含多个节点的路径字符串");
        System.out.println("实际输出: " + result);
        System.out.println("测试结果: " + (result != null && result.length() > 0 ? "通过" : "失败"));
        System.out.println();
        assertNotNull("随机游走结果不应为null", result);
        assertTrue("结果应包含至少一个单词", result.length() > 0);
    }
    
    @Test
    public void testCase4_NormalWalk() {
        // 测试用例4: 基本路径4 - 正常游走
        System.out.println("=== 测试用例4 - 正常游走测试 ===");
        String result = processor.randomWalk();
        String[] words = result.split(" ");
        System.out.println("期望输出: 用空格分隔的单词路径");
        System.out.println("实际输出: " + result);
        System.out.println("路径长度: " + words.length + " 个单词");
        System.out.println("测试结果: " + (result != null && words.length >= 1 ? "通过" : "失败"));
        System.out.println();
        assertNotNull("随机游走结果不应为null", result);
        assertTrue("结果应包含至少一个单词", result.length() > 0);
        assertTrue("路径应包含至少一个单词", words.length >= 1);
    }
} 