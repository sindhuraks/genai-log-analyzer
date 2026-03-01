package com.amex.wicse.log_analyzer.service;

import com.amex.wicse.log_analyzer.model.ApacheLog;
import com.amex.wicse.log_analyzer.model.HDFSLog;
import com.amex.wicse.log_analyzer.model.ZookeeperLog;
import com.amex.wicse.log_analyzer.repo.ApacheLogRepo;
import com.amex.wicse.log_analyzer.repo.HDFSLogRepo;
import com.amex.wicse.log_analyzer.repo.ZookeeperLogRepo;
import com.fasterxml.jackson.datatype.jsr310.deser.key.ZoneOffsetKeyDeserializer;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogIngestionService {

    private final ApacheLogRepo apacheLogRepo;
    private final HDFSLogRepo hdfsLogRepo;
    private final ZookeeperLogRepo zookeeperLogRepo;

    public LogIngestionService(ApacheLogRepo apacheLogRepo, HDFSLogRepo hdfsLogRepo, ZookeeperLogRepo zookeeperLogRepo) {
        this.apacheLogRepo = apacheLogRepo;
        this.hdfsLogRepo = hdfsLogRepo;
        this.zookeeperLogRepo =zookeeperLogRepo;
    }

    @Transactional
    public int save(MultipartFile file) {

        int line_cnt = 0;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line = reader.readLine();

            if (line == null) {
                return 0;
            }

            while((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) {
                    continue;
                }

                ApacheLog log = new ApacheLog();
                log.setLine_id(Long.parseLong(parts[0].trim()));
                log.setTime(parts[1].trim());
                log.setLevel(parts[2].trim());
                log.setContent(parts[3].trim());
                log.setEvent_id(parts[4].trim());
                log.setEvent_template(parts[5].trim());
                apacheLogRepo.save(log);
                line_cnt++;
            }

        } catch (IOException e) {
            throw new RuntimeException("fail to store csv data: " + e.getMessage());
        }
        return line_cnt;
    }

    @Transactional
    public int saveHDFS(MultipartFile file) {

        int line_cnt = 0;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line = reader.readLine();

            if (line == null) {
                return 0;
            }

            while((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 9) {
                    continue;
                }

                HDFSLog log = new HDFSLog();
                log.setLine_id(Long.parseLong(parts[0].trim()));
                log.setDate(parts[1].trim());
                log.setTime(parts[2].trim());
                log.setPid(Integer.parseInt(parts[3].trim()));
                log.setLevel(parts[4].trim());
                log.setComponent(parts[5].trim());
                log.setContent(parts[6].trim());
                log.setEvent_id(parts[7].trim());
                log.setEvent_template(parts[8].trim());
                hdfsLogRepo.save(log);
                line_cnt++;
            }

        } catch (IOException e) {
            throw new RuntimeException("fail to store csv data: " + e.getMessage());
        }
        return line_cnt;
    }

    @Transactional
    public int saveZookeeper(MultipartFile file) {

        int line_cnt = 0;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line = reader.readLine();

            if (line == null) {
                return 0;
            }

            while((line = reader.readLine()) != null) {
                System.out.println(line);
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 10) {
                    continue;
                }

                ZookeeperLog log = new ZookeeperLog();
                log.setLine_id(Long.parseLong(parts[0].trim()));
                log.setDate(parts[1].trim());
                log.setTime(parts[2].trim());
                log.setLevel(parts[3].trim());
                log.setNode(parts[4].trim());
                log.setComponent(parts[5].trim());
                log.setPid(Integer.parseInt(parts[6].trim()));
                log.setContent(parts[7].trim());
                log.setEvent_id(parts[8].trim());
                log.setEvent_template(parts[9].trim());
                zookeeperLogRepo.save(log);
                line_cnt++;
            }

        } catch (IOException e) {
            throw new RuntimeException("fail to store csv data: " + e.getMessage());
        }
        return line_cnt;
    }
}
