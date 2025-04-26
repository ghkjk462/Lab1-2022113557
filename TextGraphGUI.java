import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class TextGraphGUI extends JFrame {
    private TextGraphProcessor processor;
    private JTextArea outputArea;
    private JTextField word1Field, word2Field, textInputField;
    private JButton loadFileButton, showGraphButton, queryBridgeButton, 
                  generateTextButton, shortestPathButton, pageRankButton, randomWalkButton;
    private GraphPanel graphPanel;
    private JTabbedPane tabbedPane;
    private JTextField filePathField;
    
    public TextGraphGUI() {
        super("文本图处理器");
        setSize(1200, 900); // 进一步增加窗口大小
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 创建界面组件
        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel fileLabel = new JLabel("文件路径：");
        filePathField = new JTextField(30);
        loadFileButton = new JButton("加载文件");
        JButton browseButton = new JButton("浏览...");
        
        topPanel.add(fileLabel);
        topPanel.add(filePathField);
        topPanel.add(loadFileButton);
        topPanel.add(browseButton);
        
        // 创建功能按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 5, 5));
        showGraphButton = new JButton("1. 显示有向图");
        
        // 查询桥接词面板
        JPanel bridgePanel = new JPanel(new FlowLayout());
        bridgePanel.add(new JLabel("单词1:"));
        word1Field = new JTextField(10);
        bridgePanel.add(word1Field);
        bridgePanel.add(new JLabel("单词2:"));
        word2Field = new JTextField(10);
        bridgePanel.add(word2Field);
        queryBridgeButton = new JButton("2. 查询桥接词");
        bridgePanel.add(queryBridgeButton);
        
        // 生成新文本面板
        JPanel genTextPanel = new JPanel(new FlowLayout());
        genTextPanel.add(new JLabel("输入文本:"));
        textInputField = new JTextField(30);
        genTextPanel.add(textInputField);
        generateTextButton = new JButton("3. 生成新文本");
        genTextPanel.add(generateTextButton);
        
        // 最短路径按钮复用word1Field和word2Field
        shortestPathButton = new JButton("4. 计算最短路径");
        
        // PageRank按钮复用word1Field
        pageRankButton = new JButton("5. 计算PageRank");
        
        // 随机游走按钮
        randomWalkButton = new JButton("6. 随机游走");
        
        buttonPanel.add(showGraphButton);
        buttonPanel.add(bridgePanel);
        buttonPanel.add(genTextPanel);
        buttonPanel.add(shortestPathButton);
        buttonPanel.add(pageRankButton);
        buttonPanel.add(randomWalkButton);
        
        // 创建输出区域
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane textScrollPane = new JScrollPane(outputArea);
        
        // 创建图形面板
        graphPanel = new GraphPanel();
        JScrollPane graphScrollPane = new JScrollPane(graphPanel);
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("文本视图", textScrollPane);
        tabbedPane.addTab("图形视图", graphScrollPane);
        
        // 选项卡切换监听器，用于在切换到图形视图时刷新图形
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1 && processor != null) {
                // 触发布局重新计算
                graphPanel.setGraph(processor.getGraph(), processor.getWordSequence());
                graphPanel.repaint();
            }
        });
        
        // 设置布局
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.WEST);
        add(tabbedPane, BorderLayout.CENTER);
        
        // 窗口大小变化监听器，用于重新布局图形
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (processor != null) {
                    // 窗口大小变化时，重新计算布局
                    graphPanel.setGraph(processor.getGraph(), processor.getWordSequence());
                }
            }
        });
        
        // 浏览按钮动作
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(TextGraphGUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        // 加载文件按钮动作 - 直接使用ActionListener而不是lambda
        loadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });
        
        showGraphButton.addActionListener(e -> {
            if (processor == null) {
                outputArea.setText("请先加载文件");
                return;
            }
            
            if (tabbedPane.getSelectedIndex() == 0) {
                // 文本视图
                outputArea.setText("");
                processor.showDirectedGraph(outputArea);
            } else {
                // 图形视图 - 刷新图
                graphPanel.setGraph(processor.getGraph(), processor.getWordSequence());
                graphPanel.repaint();
            }
        });
        
        queryBridgeButton.addActionListener(e -> {
            if (processor == null) {
                outputArea.setText("请先加载文件");
                return;
            }
            
            String word1 = word1Field.getText().toLowerCase();
            String word2 = word2Field.getText().toLowerCase();
            if (word1.isEmpty() || word2.isEmpty()) {
                outputArea.setText("请输入两个单词");
                return;
            }
            
            String result = processor.queryBridgeWords(word1, word2);
            outputArea.setText(result);
            tabbedPane.setSelectedIndex(0); // 切换到文本视图
        });
        
        generateTextButton.addActionListener(e -> {
            if (processor == null) {
                outputArea.setText("请先加载文件");
                return;
            }
            
            String inputText = textInputField.getText();
            if (inputText.isEmpty()) {
                outputArea.setText("请输入文本");
                return;
            }
            
            String newText = processor.generateNewText(inputText);
            outputArea.setText("原文本: " + inputText + "\n生成的新文本: " + newText);
            tabbedPane.setSelectedIndex(0); // 切换到文本视图
        });
        
        shortestPathButton.addActionListener(e -> {
            if (processor == null) {
                outputArea.setText("请先加载文件");
                return;
            }
            
            String word1 = word1Field.getText().toLowerCase();
            String word2 = word2Field.getText().toLowerCase();
            if (word1.isEmpty() || word2.isEmpty()) {
                outputArea.setText("请输入两个单词");
                return;
            }
            
            String result = processor.calcShortestPath(word1, word2);
            outputArea.setText(result);
            tabbedPane.setSelectedIndex(0); // 切换到文本视图
        });
        
        pageRankButton.addActionListener(e -> {
            if (processor == null) {
                outputArea.setText("请先加载文件");
                return;
            }
            
            String word = word1Field.getText().toLowerCase();
            if (word.isEmpty()) {
                outputArea.setText("请在单词1输入框中输入单词");
                return;
            }
            
            String result = processor.calPageRank(word);
            outputArea.setText(result);
            tabbedPane.setSelectedIndex(0); // 切换到文本视图
        });
        
        randomWalkButton.addActionListener(e -> {
            if (processor == null) {
                outputArea.setText("请先加载文件");
                return;
            }
            
            String result = processor.randomWalk();
            outputArea.setText("随机游走结果：\n" + result);
            tabbedPane.setSelectedIndex(0); // 切换到文本视图
        });
        
        // 初始禁用功能按钮，等待文件加载
        enableButtons(false);
        
        // 设置窗口在屏幕中央显示
        setLocationRelativeTo(null);
    }
    
    // 将文件加载逻辑提取为单独的方法
    private void loadFile() {
        System.out.println("加载文件按钮被点击");
        
        String filePath = filePathField.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入文件路径或使用浏览按钮选择文件", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            JOptionPane.showMessageDialog(this, "文件不存在或路径不正确", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // 清空之前的图形数据
            graphPanel.setGraph(new HashMap<>());
            graphPanel.repaint();
            
            // 创建新的处理器实例
            processor = new TextGraphProcessor(filePath);
            processor.readTextAndBuildGraph();
            outputArea.setText("文件已加载：" + filePath + "\n图已创建成功！");
            
            // 更新图形面板，传递单词序列
            graphPanel.setGraph(processor.getGraph(), processor.getWordSequence());
            
            // 自动切换到图形视图
            tabbedPane.setSelectedIndex(1);
            
            enableButtons(true);
        } catch (IOException ex) {
            outputArea.setText("错误：" + ex.getMessage());
            JOptionPane.showMessageDialog(this, "加载文件时出错: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            enableButtons(false);
        }
    }
    
    private void enableButtons(boolean enable) {
        showGraphButton.setEnabled(enable);
        queryBridgeButton.setEnabled(enable);
        generateTextButton.setEnabled(enable);
        shortestPathButton.setEnabled(enable);
        pageRankButton.setEnabled(enable);
        randomWalkButton.setEnabled(enable);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TextGraphGUI gui = new TextGraphGUI();
            gui.setVisible(true);
        });
    }
} 