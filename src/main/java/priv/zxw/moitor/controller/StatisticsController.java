package priv.zxw.moitor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import priv.zxw.moitor.service.MonitorService;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/pv")
@RestController
public class StatisticsController {

    @Autowired
    private MonitorService monitorService;

    @PostMapping("/i")
    public boolean incr(HttpServletRequest request) {
        String remoteHost = request.getRemoteHost();
        return monitorService.incrementVisitorCount(remoteHost);
    }

    @RequestMapping("/s")
    public Integer statistics() {
        return monitorService.getTotalCount();
    }
}
