package com.nowcoder.wenda.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class SensitiveService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

    /**
     * 默认敏感词替换符
     */
    private static final String DEFAULT_REPLACEMENT = "***";


    private class TrieNode {

        /**
         * true 关键词的终结 ； false 继续
         */
        private boolean end = false;

        /**
         * key下一个字符，value是对应的节点
         */
        //节点树的构造,表示当前节点下所有的子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();  //这里节点为一个map,字符与树节点对应

        /**
         * 向指定位置添加节点树
         */
        void addSubNode(Character key, TrieNode node) {
            subNodes.put(key, node);
        }

        /**
         * 获取下个节点
         */
        TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }

        boolean isKeywordEnd() {
            return end;
        }

        void setKeywordEnd(boolean end) {
            this.end = end;
        }

        public int getSubNodeCount() {
            return subNodes.size();
        }


    }


    /**
     * 根节点
     */
    //定义初始的根节点,添加都是subNode(下一个字符节点)
    private TrieNode rootNode = new TrieNode();


    /**
     * 判断是否是一个符号
     */
    //不是东亚文字也不是英文.则过滤掉,true代表为符号
    private boolean isSymbol(char c) {
        int ic = (int) c;
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }


    /**
     * 过滤敏感词
     */
    //过滤部分,三个指针的算法部分
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {  //文本为空,则无意义
            return text;
        }
        String replacement = DEFAULT_REPLACEMENT;
        StringBuilder result = new StringBuilder();   //最后能够显现的字符缓冲串

        TrieNode tempNode = rootNode;   //根节点不包含任何字符
        int begin = 0; // 回滚数,指的是下面的指针
        int position = 0; // 当前比较的位置,指的是上面的指针

        while (position < text.length()) {
            char c = text.charAt(position);
            // 空格这样的字符直接跳过,如果是根节点,也就是最初出现符号还是会添加但是在前缀树中间的字符,则全部忽略都不会进入最后字符串中
            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    result.append(c);
                    ++begin;
                }
                ++position;
                continue;
            }

            tempNode = tempNode.getSubNode(c);

            // 当前位置的匹配结束
            if (tempNode == null) {
                // 以begin开始的字符串不存在敏感词
                result.append(text.charAt(begin));  //只要不是敏感词的开端就把该字符填进去
                // 跳到下一个字符开始测试
                position = begin + 1;
                begin = position;
                // 回到树初始节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词， 从begin到position的位置用replacement替换掉
                result.append(replacement);    //这里####注释掉则就把敏感词删除
                position = position + 1;  //发现敏感词结尾的后面直接更新位置.前面的敏感词替换成replacement
                begin = position;
                tempNode = rootNode;
            } else {
                ++position;
            }
        }

        result.append(text.substring(begin));  //添加最后一个敏感词后面的字符串

        return result.toString();
    }

    //把敏感词汇转变成前缀树并添加标记尾部end,把去掉前后空格的敏感词txt变成前缀树
    private void addWord(String lineTxt) {
        TrieNode tempNode = rootNode;   //先把当前节点指向根节点
        // 循环每个字符串,看当前字符是否存在
        for (int i = 0; i < lineTxt.length(); ++i) {
            Character c = lineTxt.charAt(i);
            // 过滤空格,如果在敏感词中出现非正常字符,构造前缀树时则不会添加,比如  色###情;
            if (isSymbol(c)) {
                continue;
            }
            TrieNode node = tempNode.getSubNode(c);

            if (node == null) { // 没初始化
                node = new TrieNode();
                tempNode.addSubNode(c, node);
            }

            tempNode = node;

            if (i == lineTxt.length() - 1) {
                // 关键词结束， 设置结束标志
                tempNode.setKeywordEnd(true);
            }
        }
    }


    //实现InitializingBean,要读取敏感词文本,把文本中的词汇添加进前缀树
    @Override
    public void afterPropertiesSet() throws Exception {
        rootNode = new TrieNode();

        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;  //每一行为敏感词,这里是作为字符串进行添加词汇(在树中是进行前缀树连接字符)
            while ((lineTxt = bufferedReader.readLine() )!= null){
                addWord(lineTxt.trim());   //处理下去掉前后空格

            }
            read.close();


        }catch (Exception e){
            logger.info("读取敏感词文件失败" + e.getMessage());
        }
//        try {
//            InputStream is = Thread.currentThread().getContextClassLoader()
//                    .getResourceAsStream("SensitiveWords.txt");
//            InputStreamReader read = new InputStreamReader(is);
//            BufferedReader bufferedReader = new BufferedReader(read);
//            String lineTxt;
//            while ((lineTxt = bufferedReader.readLine()) != null) {
//                lineTxt = lineTxt.trim();
//                addWord(lineTxt);
//            }
//            read.close();
//        } catch (Exception e) {
//            logger.error("读取敏感词文件失败" + e.getMessage());
//        }
    }

    public static void main(String[] argv) {
        SensitiveService s = new SensitiveService();   //这个service进行ioc作为bean对象
        s.addWord("色##@@情");
        s.addWord("好!!!色");
        System.out.print(s.filter("   ### 你好###    色##情XX"));   //XX属于正常文字类型,不会过滤掉,而像$%#^和空格这种符号都被上面isSymbol先过滤掉啦,trim()方法是去掉敏感词前后的空格,对内部不影响
    }
}
