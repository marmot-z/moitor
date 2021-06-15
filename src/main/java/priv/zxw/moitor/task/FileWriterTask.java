package priv.zxw.moitor.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import priv.zxw.moitor.service.MonitorService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class FileWriterTask {

    @Autowired
    private MonitorService monitorService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String FILE_NAME = "snapshot";

    @Scheduled(cron = "0 0 0/12 * * ? ")
    public void write2File() throws IOException {
        String snapshot = monitorService.getSnapshotAsJson();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(FORMATTER.format(LocalDateTime.now()));
            writer.write(" ------------------------------------------------");
            writer.newLine();
            writer.write(snapshot);
            writer.newLine();
            writer.newLine();
        } catch (IOException e) {
            log.error("记录信息失败", e);
        }
    }
}
