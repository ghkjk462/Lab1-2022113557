import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;

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
    public void testCase2_SingleNodeNoEdges() throws IOException {
        // 测试用例2: 基本路径2 - 遇到无出边节点，结束游走
        System.out.println("=== 测试用例2 - 无出边节点测试 ===");
        
        // 创建一个图：start -> end，其中end节点无出边
        String testFileName = "no_outgoing_test.txt";
        FileWriter writer = new FileWriter(testFileName);
        writer.write("start end");  // start指向end，end无出边
        writer.close();
        
        TextGraphProcessor noOutgoingProcessor = new TextGraphProcessor(testFileName);
        noOutgoingProcessor.readTextAndBuildGraph();
        
        // 多次运行以增加遇到end节点的概率
        String result = null;
        for (int i = 0; i < 10; i++) {
            result = noOutgoingProcessor.randomWalk();
            if (result.equals("end") || result.equals("start end")) {
                break; // 找到了预期的结果
            }
        }
        
        System.out.println("期望输出: start end 或 end（遇到无出边节点时停止）");
        System.out.println("实际输出: " + result);
        System.out.println("测试结果: " + (result != null && (result.equals("end") || result.equals("start end")) ? "通过" : "失败"));
        System.out.println();
        
        // 清理测试文件
        new File(testFileName).delete();
        
        assertTrue("结果应该是end或start end", result != null && (result.equals("end") || result.equals("start end")));
    }
    
    @Test
    public void testCase3_RepeatedEdge() throws IOException {
        // 测试用例3: 基本路径3 - 遇到重复边
        System.out.println("=== 测试用例3 - 遇到重复边测试 ===");
        
        // 创建会产生重复边的测试文件
        String testFileName = "repeated_edge_test.txt";
        FileWriter writer = new FileWriter(testFileName);
        writer.write("a b a b a b");
        writer.close();
        
        TextGraphProcessor repeatedEdgeProcessor = new TextGraphProcessor(testFileName);
        repeatedEdgeProcessor.readTextAndBuildGraph();
        String result = repeatedEdgeProcessor.randomWalk();
        
        System.out.println("期望输出: 包含a和b的路径，遇到重复边时停止");
        System.out.println("实际输出: " + result);
        System.out.println("测试结果: " + (result.contains("a") && result.contains("b") ? "通过" : "失败"));
        System.out.println();
        
        // 清理测试文件
        new File(testFileName).delete();
        
        assertTrue("结果应包含a和b", result.contains("a") && result.contains("b"));
    }
    
    @Test
    public void testCase4_NormalWalk() {
        // 测试用例4: 基本路径4 - 正常游走（使用原始测试文件）
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