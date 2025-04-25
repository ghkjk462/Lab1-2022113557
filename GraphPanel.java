import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * 用于绘制有向图的自定义面板
 */
public class GraphPanel extends JPanel {
    private Map<String, Point> nodePositions = new HashMap<>();
    private Map<String, Set<TextGraphProcessor.Edge>> graph;
    private List<String> wordSequence;
    private Map<String, Color> nodeColors = new HashMap<>();
    private static final int NODE_RADIUS = 28;
    private static final Color NODE_COLOR = new Color(100, 180, 255);
    private static final Color EDGE_COLOR = new Color(80, 80, 80);
    private static final Color TEXT_COLOR = new Color(0, 0, 0);
    private static final Stroke EDGE_STROKE = new BasicStroke(1.0f);
    
    
    public GraphPanel() {
        setBackground(Color.WHITE);
    }
    
    public void setGraph(Map<String, Set<TextGraphProcessor.Edge>> graph) {
        this.graph = graph;
        calculateNodePositions();
        repaint();
    }
    
    public void setGraph(Map<String, Set<TextGraphProcessor.Edge>> graph, List<String> wordSequence) {
        this.graph = graph;
        this.wordSequence = wordSequence;
        calculateNodePositions();
        repaint();
    }
    
    private void calculateNodePositions() {
        nodePositions.clear();
        
        if (graph == null || graph.isEmpty()) {
            return;
        }
        
        // 获取图中所有节点
        List<String> allNodes = new ArrayList<>(graph.keySet());
        
        // 随机分配节点颜色
        Random rand = new Random(123); // 固定种子使颜色保持一致
        for (String node : allNodes) {
            // 为节点分配随机颜色
            float h = rand.nextFloat();
            float s = 0.5f + rand.nextFloat() * 0.3f;
            float b = 0.7f + rand.nextFloat() * 0.2f;
            nodeColors.put(node, Color.getHSBColor(h, s, b));
        }
        
        // 如果有单词序列，则使用它进行布局
        if (wordSequence != null && !wordSequence.isEmpty()) {
            layoutBySequence();
        } else {
            // 否则使用层次布局
            layoutByLevels(allNodes, rand);
        }
        
        // 最后检查确保所有节点都有位置
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        for (String node : allNodes) {
            if (!nodePositions.containsKey(node)) {
                // 为漏掉的节点随机分配位置
                int x = rand.nextInt(panelWidth - 100) + 50;
                int y = rand.nextInt(panelHeight - 100) + 50;
                nodePositions.put(node, new Point(x, y));
            }
        }
    }
    
    private void layoutBySequence() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        // 计算序列中实际出现在图中的单词数量
        List<String> validSequenceWords = new ArrayList<>();
        for (String word : wordSequence) {
            if (graph.containsKey(word)) {
                validSequenceWords.add(word);
            }
        }
        
        int totalWords = validSequenceWords.size();
        if (totalWords == 0) return;
        
        // 确定树的层数和每层最大节点数
        int maxNodesPerRow = (int) Math.ceil(Math.sqrt(totalWords));
        int numRows = (int) Math.ceil((double) totalWords / maxNodesPerRow);
        
        // 计算节点间距
        int verticalSpacing = panelHeight / (numRows + 1);
        int horizontalSpacing = panelWidth / (maxNodesPerRow + 1);
        
        // 布局节点
        for (int i = 0; i < validSequenceWords.size(); i++) {
            String word = validSequenceWords.get(i);
            int row = i / maxNodesPerRow;
            int col = i % maxNodesPerRow;
            
            int x = horizontalSpacing * (col + 1);
            int y = verticalSpacing * (row + 1);
            
            // 随机添加小偏移量以避免重叠
            Random rand = new Random(word.hashCode()); // 使用单词作为种子保持一致性
            x += rand.nextInt(horizontalSpacing / 8) - horizontalSpacing / 16;
            y += rand.nextInt(verticalSpacing / 8) - verticalSpacing / 16;
            
            nodePositions.put(word, new Point(x, y));
        }
    }
    
    private void layoutByLevels(List<String> allNodes, Random rand) {
        // 查找所有没有入边的节点作为起始节点
        List<String> startNodes = new ArrayList<>();
        Set<String> hasIncomingEdge = new HashSet<>();
        
        // 找出有入边的节点
        for (String source : graph.keySet()) {
            for (TextGraphProcessor.Edge edge : graph.get(source)) {
                hasIncomingEdge.add(edge.target);
            }
        }
        
        // 找出没有入边的节点
        for (String node : allNodes) {
            if (!hasIncomingEdge.contains(node)) {
                startNodes.add(node);
            }
        }
        
        // 如果没有找到起始节点，则选择第一个节点作为起始节点
        if (startNodes.isEmpty() && !allNodes.isEmpty()) {
            startNodes.add(allNodes.get(0));
        }
        
        // 创建已访问节点集合和层级映射
        Set<String> visited = new HashSet<>();
        Map<String, Integer> levelMap = new HashMap<>();
        
        // 使用BFS为每个节点分配层级
        for (String startNode : startNodes) {
            Queue<String> queue = new LinkedList<>();
            queue.add(startNode);
            levelMap.put(startNode, 0);
            
            while (!queue.isEmpty()) {
                String current = queue.poll();
                visited.add(current);
                int currentLevel = levelMap.get(current);
                
                // 处理所有邻居
                if (graph.containsKey(current)) {
                    for (TextGraphProcessor.Edge edge : graph.get(current)) {
                        String neighbor = edge.target;
                        
                        // 如果邻居还没有层级或新层级更深，则更新
                        if (!levelMap.containsKey(neighbor) || levelMap.get(neighbor) <= currentLevel) {
                            levelMap.put(neighbor, currentLevel + 1);
                            
                            if (!visited.contains(neighbor)) {
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
        
        // 处理孤立节点，给它们分配层级0
        for (String node : allNodes) {
            if (!levelMap.containsKey(node)) {
                levelMap.put(node, 0);
            }
        }
        
        // 计算每个层级有多少节点
        Map<Integer, List<String>> levelToNodes = new HashMap<>();
        for (Map.Entry<String, Integer> entry : levelMap.entrySet()) {
            levelToNodes.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }
        
        // 计算最大层级
        int maxLevel = 0;
        for (Integer level : levelMap.values()) {
            maxLevel = Math.max(maxLevel, level);
        }
        
        // 计算节点位置，层级作为Y坐标
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int verticalSpacing = panelHeight / (maxLevel + 2);
        
        for (int level = 0; level <= maxLevel; level++) {
            List<String> nodesInLevel = levelToNodes.getOrDefault(level, Collections.emptyList());
            
            if (!nodesInLevel.isEmpty()) {
                int horizontalSpacing = panelWidth / (nodesInLevel.size() + 1);
                
                for (int i = 0; i < nodesInLevel.size(); i++) {
                    String node = nodesInLevel.get(i);
                    int x = horizontalSpacing * (i + 1);
                    int y = verticalSpacing * (level + 1);
                    
                    // 对于节点较多的层级，增加随机偏移以避免节点重叠
                    if (nodesInLevel.size() > 3) {
                        x += rand.nextInt(horizontalSpacing / 4) - horizontalSpacing / 8;
                        y += rand.nextInt(verticalSpacing / 4) - verticalSpacing / 8;
                    }
                    
                    nodePositions.put(node, new Point(x, y));
                }
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        if (graph == null || graph.isEmpty() || nodePositions.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.drawString("无图数据或节点位置未计算", getWidth() / 2 - 80, getHeight() / 2);
            return;
        }
        
        // 首先绘制所有边
        for (String source : graph.keySet()) {
            Point sourcePos = nodePositions.get(source);
            if (sourcePos == null) continue;
            
            for (TextGraphProcessor.Edge edge : graph.get(source)) {
                Point targetPos = nodePositions.get(edge.target);
                if (targetPos == null) continue;
                
                // 绘制边和权重
                drawEdge(g2d, sourcePos, targetPos, edge.weight);
            }
        }
        
        // 然后绘制所有节点
        for (String node : graph.keySet()) {
            Point pos = nodePositions.get(node);
            if (pos == null) continue;
            
            // 绘制节点
            drawNode(g2d, pos, node);
        }
    }
    
    private void drawNode(Graphics2D g2d, Point pos, String label) {
        // 绘制节点圆形
        Color nodeColor = nodeColors.getOrDefault(label, NODE_COLOR);
        g2d.setColor(nodeColor);
        Ellipse2D.Double circle = new Ellipse2D.Double(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS, 
                                                      2 * NODE_RADIUS, 2 * NODE_RADIUS);
        g2d.fill(circle);
        
        // 绘制边框
        g2d.setColor(nodeColor.darker());
        g2d.draw(circle);
        
        // 使用更大、更清晰的字体
        Font originalFont = g2d.getFont();
        Font boldFont = new Font(originalFont.getName(), Font.BOLD, 13);
        g2d.setFont(boldFont);
        
        // 绘制标签
        g2d.setColor(TEXT_COLOR);
        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(label);
        int textHeight = metrics.getHeight();
        g2d.drawString(label, pos.x - textWidth / 2, pos.y + textHeight / 4);
        
        // 恢复原来的字体
        g2d.setFont(originalFont);
    }
    
    private void drawEdge(Graphics2D g2d, Point source, Point target, int weight) {
        // 计算方向向量
        double dx = target.x - source.x;
        double dy = target.y - source.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        // 归一化
        double unitDx = dx / length;
        double unitDy = dy / length;
        
        // 调整起点和终点，使边不与节点圆形重叠
        Point adjustedSource = new Point(
            (int) (source.x + unitDx * NODE_RADIUS),
            (int) (source.y + unitDy * NODE_RADIUS)
        );
        
        Point adjustedTarget = new Point(
            (int) (target.x - unitDx * NODE_RADIUS),
            (int) (target.y - unitDy * NODE_RADIUS)
        );
        
        // 确定是否需要绘制曲线（当两点之间的直线距离足够大时）
        boolean drawCurve = length > NODE_RADIUS * 4;
        
        // 绘制边（曲线或直线）
        g2d.setColor(EDGE_COLOR);
        g2d.setStroke(EDGE_STROKE);
        
        if (drawCurve) {
            // 计算控制点，用于创建二次贝塞尔曲线
            // 控制点垂直于连线，距离根据连线长度按比例计算
            double controlOffsetRatio = 0.2; // 控制曲线弯曲程度
            double perpDx = -unitDy; // 垂直方向向量
            double perpDy = unitDx;
            
            // 为同一对节点间的多条边设置不同的曲线
            // 使用边的权重来区分曲线
            double offsetMultiplier = Math.min(weight, 4) * 0.5;
            
            // 计算控制点位置
            int controlX = (adjustedSource.x + adjustedTarget.x) / 2 + (int)(perpDx * length * controlOffsetRatio * offsetMultiplier);
            int controlY = (adjustedSource.y + adjustedTarget.y) / 2 + (int)(perpDy * length * controlOffsetRatio * offsetMultiplier);
            
            // 创建并绘制二次贝塞尔曲线
            QuadCurve2D curve = new QuadCurve2D.Double(
                adjustedSource.x, adjustedSource.y,
                controlX, controlY,
                adjustedTarget.x, adjustedTarget.y
            );
            g2d.draw(curve);
            
            // 计算曲线尾部附近的点和方向用于绘制箭头
            double t = 0.95; // 接近终点但不是终点
            
            // 计算曲线上的点
            double arrowX = Math.pow(1-t, 2) * adjustedSource.x + 
                           2 * (1-t) * t * controlX + 
                           Math.pow(t, 2) * adjustedTarget.x;
            double arrowY = Math.pow(1-t, 2) * adjustedSource.y + 
                           2 * (1-t) * t * controlY + 
                           Math.pow(t, 2) * adjustedTarget.y;
            
            // 计算该点处的切线方向
            double tangentX = 2 * (1-t) * (controlX - adjustedSource.x) + 2 * t * (adjustedTarget.x - controlX);
            double tangentY = 2 * (1-t) * (controlY - adjustedSource.y) + 2 * t * (adjustedTarget.y - controlY);
            
            // 归一化方向向量
            double norm = Math.sqrt(tangentX * tangentX + tangentY * tangentY);
            if (norm > 0) {
                tangentX /= norm;
                tangentY /= norm;
            }
            
            // 绘制箭头
            drawArrow(g2d, new Point((int)arrowX, (int)arrowY), adjustedTarget);
            
            // 计算权重标签的位置（曲线中点）
            double labelT = 0.5; // 在曲线中点位置
            double labelX = Math.pow(1-labelT, 2) * adjustedSource.x + 
                           2 * (1-labelT) * labelT * controlX + 
                           Math.pow(labelT, 2) * adjustedTarget.x;
            double labelY = Math.pow(1-labelT, 2) * adjustedSource.y + 
                           2 * (1-labelT) * labelT * controlY + 
                           Math.pow(labelT, 2) * adjustedTarget.y;
            
            // 在曲线上绘制权重标签（不再偏移）
            drawWeightLabel(g2d, (int)labelX, (int)labelY, weight);
        } else {
            // 直线情况
            g2d.drawLine(adjustedSource.x, adjustedSource.y, adjustedTarget.x, adjustedTarget.y);
            
            // 绘制箭头
            drawArrow(g2d, adjustedSource, adjustedTarget);
            
            // 计算边的中点，权重标签直接放在线上
            int midX = (adjustedSource.x + adjustedTarget.x) / 2;
            int midY = (adjustedSource.y + adjustedTarget.y) / 2;
            
            // 在线的中点绘制权重标签
            drawWeightLabel(g2d, midX, midY, weight);
        }
    }
    
    // 绘制权重标签的辅助方法
    private void drawWeightLabel(Graphics2D g2d, int x, int y, int weight) {
        String weightStr = Integer.toString(weight);
        
        // 添加背景使权重更容易阅读
        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(weightStr);
        int textHeight = metrics.getHeight();
        
        // 使用不透明的白色背景
        g2d.setColor(new Color(255, 255, 255, 240));  // 增加不透明度
        g2d.fillRect(x - textWidth/2 - 3, y - textHeight/2, textWidth + 6, textHeight);
        
        // 绘制黑色边框使其与背景更加区分
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRect(x - textWidth/2 - 3, y - textHeight/2, textWidth + 6, textHeight);
        
        g2d.setColor(Color.RED);
        g2d.drawString(weightStr, x - textWidth/2, y + textHeight/4);
    }
    
    // 为直线绘制箭头的方法
    private void drawArrow(Graphics2D g2d, Point from, Point to) {
        int arrowSize = 8; // 箭头大小
        
        // 计算方向向量
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        // 归一化
        double unitDx = dx / length;
        double unitDy = dy / length;
        
        // 计算箭头两个顶点
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        // 箭头顶点
        xPoints[0] = to.x;
        yPoints[0] = to.y;
        
        // 箭头底边两个顶点
        double angle = Math.PI / 6; // 30度
        double backDX1 = unitDx * Math.cos(angle) - unitDy * Math.sin(angle);
        double backDY1 = unitDx * Math.sin(angle) + unitDy * Math.cos(angle);
        
        double backDX2 = unitDx * Math.cos(-angle) - unitDy * Math.sin(-angle);
        double backDY2 = unitDx * Math.sin(-angle) + unitDy * Math.cos(-angle);
        
        xPoints[1] = (int) (to.x - arrowSize * backDX1);
        yPoints[1] = (int) (to.y - arrowSize * backDY1);
        
        xPoints[2] = (int) (to.x - arrowSize * backDX2);
        yPoints[2] = (int) (to.y - arrowSize * backDY2);
        
        g2d.setColor(EDGE_COLOR);
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1000, 800); // 增加面板的首选大小
    }
} 