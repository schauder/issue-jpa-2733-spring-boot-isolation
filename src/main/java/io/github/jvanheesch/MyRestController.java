package io.github.jvanheesch;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyRestController {
    private final MyService myService;

    MyRestController(MyService myService) {
        this.myService = myService;
    }

    @GetMapping("/read_uncommitted_data")
    public String readUncommittedData() {
        return myService.readUncommittedData();
    }

    @GetMapping("/read_committed_data")
    public String readCommittedData() {
        return myService.readCommittedData();
    }

    @PostMapping("/update")
    public void update() {
        myService.update();
    }
}
