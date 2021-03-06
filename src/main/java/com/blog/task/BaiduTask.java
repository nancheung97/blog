package com.blog.task;

import com.blog.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * 百度推送的工具类
 * application.yml文件中配置
 *
 * @author NanCheung
 */
@Component
public class BaiduTask {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("${baidu.task.postUrl}")
    public String postUrl;
    
    @Value("${url}")
    public String baseUrl;
    
    @Autowired
    private ArticleService articleService;
    
    /**
     * 初始化HttpURLConnection
     *
     * @return
     * @throws IOException
     */
    private HttpURLConnection initConnect() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(postUrl).openConnection();
        conn.setRequestProperty("Host", "data.zz.baidu.com");
        conn.setRequestProperty("User-Agent", "curl/7.12.1");
        conn.setRequestProperty("Content-Length", "83");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        return conn;
    }
    
    /**
     * 自动推送任务
     *
     * @throws IOException
     */
    @Scheduled(fixedDelay = 200000)
    public void postArticleUrl() throws IOException {
        String[] ids = articleService.getArticleId();
        writerUrl(initConnect(), ids);
        
    }
    
    /**
     * 重构推送文章的write方法
     *
     * @param conn
     * @param ids
     * @throws IOException
     */
    private void writerUrl(HttpURLConnection conn, String... ids) throws IOException {
        
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            sb.append(baseUrl).append("/article/details/").append(id).append("\n");
        }
        logger.info("百度推送的url为:{}", sb.toString());
        conn.connect();
        
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8))) {
            out.print(sb.toString().trim());
        }
        
        int code = conn.getResponseCode();
        if (code == HTTP_OK) {
            String line;
            StringBuilder r = new StringBuilder();
            
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                while ((line = in.readLine()) != null) {
                    r.append(line);
                }
            }
            
            logger.info("向百度推送返回内容：{}", r.toString());
            logger.info("the article url push success");
        } else {
            logger.info("the article url push back");
        }
    }
    
    /**
     * 新增添加文章推送功能
     *
     * @param articleId 文章id
     */
    public void pushOneArticle(String articleId) throws IOException {
        writerUrl(initConnect(), articleId);
    }
}
