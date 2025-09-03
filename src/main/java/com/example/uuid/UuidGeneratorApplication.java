package com.example.uuid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@SpringBootApplication
public class UuidGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(UuidGeneratorApplication.class, args);
	}

}

@Entity
@Table(name = "uuid_entity")
class UUIDEntity {
    @Id
    private String uuid;
    private String data;

    public UUIDEntity() {}
    public UUIDEntity(String uuid, String data) {
        this.uuid = uuid;
        this.data = data;
    }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}

interface UUIDRepository extends JpaRepository<UUIDEntity, String> {}

@RestController
@RequestMapping("/pdm/uuid")
class UuidController {
    @Autowired
    private UUIDRepository uuidRepository;

    // GET: Create and return a new random UUID (version 4)
    @GetMapping
    public Map<String, String> getSeriesUuid() {
        String uuid = UUID.randomUUID().toString();
        uuidRepository.save(new UUIDEntity(uuid, ""));
        return Collections.singletonMap("uuid", uuid);
    }

    // POST: Generate a new UUID
    @PostMapping
    public Map<String, String> createUuid() {
        String uuid = UUID.randomUUID().toString();
        uuidRepository.save(new UUIDEntity(uuid, ""));
        return Collections.singletonMap("uuid", uuid);
    }

    // GET: Retrieve info about a UUID
    @GetMapping("/{uuid}")
    public ResponseEntity<Map<String, String>> getUuid(@PathVariable String uuid) {
        return uuidRepository.findById(uuid)
            .map(entity -> ResponseEntity.ok(Collections.singletonMap("uuid", entity.getUuid())))
            .orElse(ResponseEntity.notFound().build());
    }

    // PUT: Update info for a UUID (replace data)
    @PutMapping("/{uuid}")
    public ResponseEntity<Map<String, String>> updateUuid(@PathVariable String uuid, @RequestBody Map<String, String> body) {
        return uuidRepository.findById(uuid)
            .map(entity -> {
                entity.setData(body.getOrDefault("data", ""));
                uuidRepository.save(entity);
                return ResponseEntity.ok(Collections.singletonMap("uuid", uuid));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // PATCH: Partially update info for a UUID
    @PatchMapping("/{uuid}")
    public ResponseEntity<Map<String, String>> patchUuid(@PathVariable String uuid, @RequestBody Map<String, String> body) {
        return uuidRepository.findById(uuid)
            .map(entity -> {
                String update = body.getOrDefault("data", "");
                entity.setData(entity.getData() + update);
                uuidRepository.save(entity);
                return ResponseEntity.ok(Collections.singletonMap("uuid", uuid));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Remove a UUID
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Map<String, String>> deleteUuid(@PathVariable String uuid) {
        if (!uuidRepository.existsById(uuid)) {
            return ResponseEntity.notFound().build();
        }
        uuidRepository.deleteById(uuid);
        return ResponseEntity.ok(Collections.singletonMap("deleted", uuid));
    }
}
