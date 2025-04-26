import java.io.*;
import java.util.*;
import javax.swing.JTextArea;

public class TextGraphProcessor {
    // 图的数据结构
    private Map<String, Set<Edge>> graph = new HashMap<>();
    private String inputFileName;
    private List<String> wordSequence = new ArrayList<>();

    // 边类
    static class Edge {
        String source;
        String target;
        int weight;

        public Edge(String source, String target, int weight) {
            this.source = source;
            this.target = target;
            this.weight = weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return Objects.equals(source, edge.source) && Objects.equals(target, edge.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target);
        }
    }

    public TextGraphProcessor(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    // 获取图结构
    public Map<String, Set<Edge>> getGraph() {
        return graph;
    }

    // 添加一个方法来获取单词序列
    public List<String> getWordSequence() {
        return wordSequence;
    }

    // 主方法
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String fileName;

        // 允许用户选择或输入文件名
        System.out.println("请输入文本文件路径：");
        fileName = scanner.nextLine();

        TextGraphProcessor processor = new TextGraphProcessor(fileName);
        
        try {
            // 读取文本并生成有向图
            processor.readTextAndBuildGraph();
            
            // 显示菜单，允许用户选择功能
            boolean running = true;
            while (running) {
                System.out.println("\n请选择功能：");
                System.out.println("1. 显示有向图");
                System.out.println("2. 查询桥接词");
                System.out.println("3. 根据桥接词生成新文本");
                System.out.println("4. 计算两个单词之间的最短路径");
                System.out.println("5. 计算PageRank值");
                System.out.println("6. 随机游走");
                System.out.println("0. 退出");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); // 清除换行符
                
                switch (choice) {
                    case 1:
                        processor.showDirectedGraph();
                        break;
                    case 2:
                        System.out.println("请输入第一个单词：");
                        String word1 = scanner.nextLine().toLowerCase();
                        System.out.println("请输入第二个单词：");
                        String word2 = scanner.nextLine().toLowerCase();
                        String result = processor.queryBridgeWords(word1, word2);
                        System.out.println(result);
                        break;
                    case 3:
                        System.out.println("请输入文本：");
                        String inputText = scanner.nextLine();
                        String newText = processor.generateNewText(inputText);
                        System.out.println("生成的新文本：" + newText);
                        break;
                    case 4:
                        System.out.println("请输入第一个单词：");
                        word1 = scanner.nextLine().toLowerCase();
                        System.out.println("请输入第二个单词：");
                        word2 = scanner.nextLine().toLowerCase();
                        String path = processor.calcShortestPath(word1, word2);
                        System.out.println(path);
                        break;
                    case 5:
                        System.out.println("请输入要计算PageRank的单词：");
                        String word = scanner.nextLine().toLowerCase();
                        String pr = processor.calPageRank(word);
                        System.out.println(pr);
                        break;
                    case 6:
                        String walkResult = processor.randomWalk();
                        System.out.println("随机游走结果：\n" + walkResult);
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("无效选择，请重试。");
                }
            }
        } catch (IOException e) {
            System.err.println("读取文件错误: " + e.getMessage());
        }
        
        scanner.close();
    }

    // 读取文本并生成有向图
    public void readTextAndBuildGraph() throws IOException {
        // 清空图和单词序列
        graph.clear();
        wordSequence.clear();
        
        BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
        String line;
        List<String> words = new ArrayList<>();
        
        // 读取所有文本行
        while ((line = reader.readLine()) != null) {
            // 将文本转换为小写并替换所有非字母为空格
            line = line.toLowerCase().replaceAll("[^a-z]", " ");
            // 分割单词并添加到列表中
            String[] lineWords = line.trim().split("\\s+");
            for (String word : lineWords) {
                if (!word.isEmpty()) {
                    words.add(word);
                    // 如果单词序列中还没有这个单词，添加它
                    if (!wordSequence.contains(word)) {
                        wordSequence.add(word);
                    }
                }
            }
        }
        reader.close();
        
        // 构建有向图
        for (int i = 0; i < words.size() - 1; i++) {
            String current = words.get(i);
            String next = words.get(i + 1);
            
            if (!graph.containsKey(current)) {
                graph.put(current, new HashSet<>());
            }
            
            // 检查是否已存在这条边
            boolean edgeExists = false;
            for (Edge edge : graph.get(current)) {
                if (edge.target.equals(next)) {
                    edge.weight++;
                    edgeExists = true;
                    break;
                }
            }
            
            // 如果边不存在，添加新边
            if (!edgeExists) {
                graph.get(current).add(new Edge(current, next, 1));
            }
            
            // 确保目标单词也作为一个节点存在于图中
            if (!graph.containsKey(next)) {
                graph.put(next, new HashSet<>());
            }
        }
    }

    // 显示有向图 (控制台版本)
    public void showDirectedGraph() {
        System.out.println("有向图的边：");
        
        // 根据单词在文本中的出现顺序显示边
        for (String source : wordSequence) {
            if (graph.containsKey(source)) {
                for (Edge edge : graph.get(source)) {
                    System.out.println(source + " -> " + edge.target + " (权重: " + edge.weight + ")");
                }
            }
        }
    }
    
    // 显示有向图 (GUI版本)
    public void showDirectedGraph(JTextArea textArea) {
        textArea.append("有向图的边：\n");
        
        // 根据单词在文本中的出现顺序显示边
        for (String source : wordSequence) {
            if (graph.containsKey(source)) {
                for (Edge edge : graph.get(source)) {
                    textArea.append(source + " -> " + edge.target + " (权重: " + edge.weight + ")\n");
                }
            }
        }
    }

    // 查询桥接词
    public String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        
        List<String> bridges = new ArrayList<>();
        
        // 查找所有从word1出发的边
        for (Edge edge1 : graph.get(word1)) {
            String bridge = edge1.target;
            // 查找从bridge出发到word2的边
            if (graph.containsKey(bridge)) {
                for (Edge edge2 : graph.get(bridge)) {
                    if (edge2.target.equals(word2)) {
                        bridges.add(bridge);
                        break;
                    }
                }
            }
        }
        
        if (bridges.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            StringBuilder result = new StringBuilder("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ");
            for (int i = 0; i < bridges.size(); i++) {
                result.append("\"").append(bridges.get(i)).append("\"");
                if (i < bridges.size() - 1) {
                    result.append(", ");
                }
                if (i == bridges.size() - 2) {
                    result.append("and ");
                }
            }
            return result.toString();
        }
    }

    // 根据桥接词生成新文本
    public String generateNewText(String inputText) {
        // 将输入文本转换为小写并替换所有非字母为空格
        inputText = inputText.toLowerCase().replaceAll("[^a-z]", " ");
        String[] words = inputText.trim().split("\\s+");
        
        if (words.length <= 1) {
            return inputText; // 如果只有0或1个单词，直接返回
        }
        
        StringBuilder result = new StringBuilder();
        result.append(words[0]);
        
        for (int i = 0; i < words.length - 1; i++) {
            String current = words[i];
            String next = words[i + 1];
            
            // 查找桥接词
            String bridgeResult = queryBridgeWords(current, next);
            if (!bridgeResult.startsWith("No")) {
                // 提取桥接词
                String[] parts = bridgeResult.split("are: ");
                if (parts.length > 1) {
                    String bridgesStr = parts[1].replaceAll("\"", "");
                    String[] bridges = bridgesStr.split(", |and ");
                    
                    // 随机选择一个桥接词
                    if (bridges.length > 0) {
                        Random random = new Random();
                        String selectedBridge = bridges[random.nextInt(bridges.length)];
                        result.append(" ").append(selectedBridge);
                    }
                }
            }
            
            result.append(" ").append(next);
        }
        
        return result.toString();
    }

    // 计算两个单词之间的最短路径
    public String calcShortestPath(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        
        // 使用Dijkstra算法计算最短路径
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<Map.Entry<String, Integer>> queue = new PriorityQueue<>(
                Comparator.comparingInt(Map.Entry::getValue));
        Set<String> visited = new HashSet<>();
        
        // 初始化距离
        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(word1, 0);
        queue.add(new AbstractMap.SimpleEntry<>(word1, 0));
        
        while (!queue.isEmpty()) {
            String current = queue.poll().getKey();
            
            if (current.equals(word2)) {
                break; // 找到目标节点
            }
            
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            
            // 检查邻居节点
            for (Edge edge : graph.get(current)) {
                String neighbor = edge.target;
                int newDist = distances.get(current) + edge.weight;
                
                if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.add(new AbstractMap.SimpleEntry<>(neighbor, newDist));
                }
            }
        }
        
        // 构建路径
        if (distances.get(word2) == Integer.MAX_VALUE) {
            return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
        }
        
        List<String> path = new ArrayList<>();
        String current = word2;
        
        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }
        
        Collections.reverse(path);
        
        StringBuilder result = new StringBuilder("The shortest path from \"" + word1 + "\" to \"" + word2 + "\" is: ");
        for (int i = 0; i < path.size(); i++) {
            result.append(path.get(i));
            if (i < path.size() - 1) {
                result.append(" -> ");
            }
        }
        result.append(" (距离: ").append(distances.get(word2)).append(")");
        
        return result.toString();
    }

    // 计算PageRank值
    public String calPageRank(String word) {
        if (!graph.containsKey(word)) {
            return word + "不在图中，PageRank值为0.0";
        }
        
        // 使用迭代法计算PageRank
        int nodeCount = graph.keySet().size();
        double d = 0.85; // 阻尼因子
        double epsilon = 1e-6; // 收敛阈值
        int maxIterations = 100; // 最大迭代次数，防止无限循环
        
        Map<String, Double> ranks = new HashMap<>();
        Map<String, Double> newRanks = new HashMap<>();
        
        // 初始化所有节点的PR值为1.0/N
        for (String node : graph.keySet()) {
            ranks.put(node, 1.0 / nodeCount);
        }
        
        // 迭代计算PageRank直到收敛或达到最大迭代次数
        boolean converged = false;
        int iterations = 0;
        
        while (!converged && iterations < maxIterations) {
            iterations++;
            
            // 计算新的PageRank值
            for (String node : graph.keySet()) {
                double sum = 0.0;
                
                // 找出所有指向该节点的边
                for (String source : graph.keySet()) {
                    for (Edge edge : graph.get(source)) {
                        if (edge.target.equals(node)) {
                            // 计算该源节点的出度
                            int outDegree = graph.get(source).size();
                            sum += ranks.get(source) / outDegree;
                        }
                    }
                }
                
                newRanks.put(node, (1.0 - d) / nodeCount + d * sum);
            }
            
            // 检查是否收敛
            double diff = 0.0;
            for (String node : graph.keySet()) {
                diff += Math.abs(newRanks.get(node) - ranks.get(node));
            }
            
            // 如果差值小于阈值，认为已收敛
            if (diff < epsilon) {
                converged = true;
            }
            
            // 更新ranks
            ranks = new HashMap<>(newRanks);
        }
        
        String convergenceStatus = converged ? "收敛" : "达到最大迭代次数";
        return word + "的PageRank值为：" + ranks.get(word) + " (迭代" + iterations + "次, " + convergenceStatus + ")";
    }

    // 随机游走
    public String randomWalk() {
        if (graph.isEmpty()) {
            return "图为空，无法进行随机游走";
        }
        
        // 随机选择一个起始节点
        Random random = new Random();
        List<String> nodes = new ArrayList<>(graph.keySet());
        String current = nodes.get(random.nextInt(nodes.size()));
        
        StringBuilder path = new StringBuilder(current);
        Set<String> visitedEdges = new HashSet<>();
        
        while (true) {
            // 获取当前节点的所有出边
            Set<Edge> edges = graph.get(current);
            if (edges.isEmpty()) {
                break; // 如果没有出边，结束游走
            }
            
            // 转换为列表以便随机选择
            List<Edge> edgeList = new ArrayList<>(edges);
            Edge selectedEdge = edgeList.get(random.nextInt(edgeList.size()));
            
            // 构造边的唯一标识符
            String edgeId = selectedEdge.source + "->" + selectedEdge.target;
            
            // 如果边已经访问过，结束游走
            if (visitedEdges.contains(edgeId)) {
                break;
            }
            
            visitedEdges.add(edgeId);
            current = selectedEdge.target;
            path.append(" ").append(current);
        }
        
        return path.toString();
    }
} 