package com.auggie.student_server.controller;

import com.auggie.student_server.entity.WarningFollowup;
import com.auggie.student_server.entity.WarningInfo;
import com.auggie.student_server.service.WarningService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/warning")
public class WarningController {
    private final WarningService warningService;

    public WarningController(WarningService warningService) {
        this.warningService = warningService;
    }

    @GetMapping
    public List<WarningInfo> findWarnings(@RequestParam(required = false) String term,
                                          @RequestParam(required = false) Integer cid,
                                          @RequestParam(required = false) Integer tid) {
        return warningService.findWarnings(term, cid, tid);
    }

    @GetMapping("/{sctid}/followups")
    public List<WarningFollowup> findFollowups(@PathVariable Integer sctid) {
        return warningService.findFollowups(sctid);
    }

    @PostMapping("/{sctid}/followups")
    public WarningFollowup addFollowup(@PathVariable Integer sctid,
                                       @RequestBody WarningFollowup followup) {
        return warningService.addFollowup(sctid, followup);
    }
}
