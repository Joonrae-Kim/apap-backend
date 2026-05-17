package com.apap.backend.video;

import com.apap.backend.common.ApiResponse;
import com.apap.backend.user.User;
import com.apap.backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoSourceRepository videoSourceRepository;
    private final UserRepository userRepository;
    private final Path uploadDir;

    public VideoController(
            VideoSourceRepository videoSourceRepository,
            UserRepository userRepository,
            @Value("${apap.upload-dir}") String uploadDir
    ) {
        this.videoSourceRepository = videoSourceRepository;
        this.userRepository = userRepository;
        this.uploadDir = Path.of(uploadDir);
    }

    @PostMapping
    public ApiResponse<VideoResponse> create(@Valid @RequestBody VideoRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        VideoSource videoSource = new VideoSource(
                user,
                request.type(),
                request.name(),
                request.sourceUrl()
        );
        return ApiResponse.ok(VideoResponse.from(videoSourceRepository.save(videoSource)));
    }

    @PostMapping("/upload")
    public ApiResponse<VideoResponse> upload(@RequestParam Long userId, @RequestParam MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Files.createDirectories(uploadDir);
        String originalName = file.getOriginalFilename() == null ? "video" : file.getOriginalFilename();
        String savedName = UUID.randomUUID() + "-" + originalName;
        Path savedPath = uploadDir.resolve(savedName);
        file.transferTo(savedPath);

        VideoSource videoSource = new VideoSource(
                user,
                VideoSourceType.UPLOAD,
                originalName,
                savedPath.toString()
        );
        return ApiResponse.ok(VideoResponse.from(videoSourceRepository.save(videoSource)));
    }

    @GetMapping
    public ApiResponse<List<VideoResponse>> list(@RequestParam Long userId) {
        List<VideoResponse> videos = videoSourceRepository.findAllByUserIdOrderByIdDesc(userId)
                .stream()
                .map(VideoResponse::from)
                .toList();
        return ApiResponse.ok(videos);
    }

    public record VideoRequest(
            Long userId,
            VideoSourceType type,
            @NotBlank String name,
            @NotBlank String sourceUrl
    ) {
    }

    public record VideoResponse(
            Long id,
            Long userId,
            VideoSourceType type,
            String name,
            String sourceUrl,
            VideoSourceStatus status
    ) {
        static VideoResponse from(VideoSource videoSource) {
            return new VideoResponse(
                    videoSource.getId(),
                    videoSource.getUser().getId(),
                    videoSource.getType(),
                    videoSource.getName(),
                    videoSource.getSourceUrl(),
                    videoSource.getStatus()
            );
        }
    }
}
