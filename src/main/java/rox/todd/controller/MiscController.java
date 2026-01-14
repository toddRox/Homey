package rox.todd.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rox.todd.service.WifiService;

/**
 * A class to initiate miscellaneous services.
 */
@RestController
public class MiscController {
  @Autowired
  private WifiService ws;


  @GetMapping("/ping")
  private String ping() {
    return "pong";
  }

  @GetMapping("/wifi/{up}")
  private String home(@PathVariable boolean up, @RequestParam(name = "dr") Optional<Integer> durationMinutes) {
    ws.changeWifi(up, durationMinutes.orElse(-1));

    return "";
  }
}
