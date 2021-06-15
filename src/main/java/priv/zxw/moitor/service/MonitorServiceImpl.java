package priv.zxw.moitor.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class MonitorServiceImpl implements MonitorService {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String TOTAL_KEY_NAME = "TOTAL";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean incrementVisitorCount(String ip) {
        String date = DATE_FORMATTER.format(LocalDateTime.now());
        String time = TIME_FORMATTER.format(LocalDateTime.now());

        try {
            synchronized (this) {
                redisTemplate.opsForHash().increment(TOTAL_KEY_NAME, date, 1);
                if (!StringUtils.isEmpty(ip)) {
                    try {
                        redisTemplate.opsForList().rightPush(ip2Number(ip), time);
                    } catch (Exception e) {
                        redisTemplate.opsForList().rightPush(ip, time);
                    }
                }
            }
        } catch (Exception e) {
            log.error("保存 {} 访问请求信息失败", ip, e);
        }

        return true;
    }

    private static int ip2Number(String ip) {
        String[] split = ip.split("\\.");

        int num = 0;
        for (int i = 0; i < split.length; i++) {
            num = Integer.parseInt(split[i]) << (i * 8) | num;
        }

        return num;
    }

    @Override
    public String getSnapshotAsJson() {
        HashOperations<String, String, Integer> hashOperations = redisTemplate.opsForHash();
        Set<String> keys = hashOperations.keys(TOTAL_KEY_NAME);

        Map<String, Integer> map = new HashMap<>(keys.size(), 1);
        for (String key : keys) {
            Integer count = hashOperations.get(TOTAL_KEY_NAME, key);
            map.put(key, count);
        }

        return JSON.toJSONString(map);
    }

    @Override
    public Integer getTotalCount() {
        HashOperations<String, String, Integer> hashOperations = redisTemplate.opsForHash();
        Set<String> keys = hashOperations.keys(TOTAL_KEY_NAME);

        int total = 0;
        for (String key : keys) {
            total += hashOperations.get(TOTAL_KEY_NAME, key);
        }

        return total;
    }
}
