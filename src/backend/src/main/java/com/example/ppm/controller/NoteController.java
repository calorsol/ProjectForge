package com.example.ppm.controller;

import com.example.ppm.common.R;
import com.example.ppm.dto.note.NoteRequest;
import com.example.ppm.dto.note.NoteResponse;
import com.example.ppm.security.CurrentUserContext;
import com.example.ppm.security.JwtUser;
import com.example.ppm.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NoteController {
    private final NoteService service;
    public NoteController(NoteService service) { this.service = service; }
    private JwtUser me() { return CurrentUserContext.require(); }

    @GetMapping("/api/tasks/{id}/notes")
    public R<List<NoteResponse>> list(@PathVariable Long id) { return R.ok(service.list(me().userId(), id)); }

    @PostMapping("/api/tasks/{id}/notes")
    public R<NoteResponse> add(@PathVariable Long id, @Valid @RequestBody NoteRequest r) {
        JwtUser u = me();
        return R.ok(service.add(u.userId(), u.username(), id, r.content()));
    }

    @DeleteMapping("/api/notes/{id}")
    public R<Void> remove(@PathVariable Long id) { service.remove(me().userId(), id); return R.ok(); }
}
