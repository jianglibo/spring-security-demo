package com.example.springsecurity.app;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class EditingWatcher {

	@Autowired
	AppProperties appProperties;

	public static Mono<Flux<WatchEvent<Path>>> watchDirectoryRecursively(List<Path> toWatch,
			List<String> excludedDirectories)
			throws IOException {
		return Mono.fromCallable(() -> {
			WatchService watchService = FileSystems.getDefault().newWatchService();

			for (Path rootPath : toWatch) {
				Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						if (!excludedDirectories.contains(dir.getFileName().toString())) {
							dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
									StandardWatchEventKinds.ENTRY_MODIFY);
						}
						return FileVisitResult.CONTINUE;
					}
				});
			}
			return Flux.<WatchEvent<Path>>create(fluxSink -> {
				while (true) {
					WatchKey key;
					try {
						key = watchService.take(); // Block until an event is available.
					} catch (InterruptedException e) {
						fluxSink.error(e);
						return;
					}

					for (WatchEvent<?> event : key.pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
							continue;
						}
						WatchEvent<Path> pe = (WatchEvent<Path>) event;
						fluxSink.next(pe);
					}

					boolean valid = key.reset();
					if (!valid) {
						fluxSink.complete();
						return;
					}
				}
			}).subscribeOn(Schedulers.boundedElastic());
		}).publishOn(Schedulers.boundedElastic());
	}

	@PostConstruct
	void post() throws IOException {
		Path projectRoot = appProperties.playground().baseDir().resolve(appProperties.playground().instanceUuid());
		List<Path> toWatch = appProperties.playground().watchIncludes().stream().map(projectRoot::resolve).toList();
		List<String> excludedDirs = appProperties.playground().watchExcludes(); // Directories to exclude
		Mono<Flux<WatchEvent<Path>>> watchFlux = watchDirectoryRecursively(toWatch, excludedDirs);
		watchFlux.flatMapMany(fx -> fx).subscribe(event -> {
			System.out.println("Event: " + event.context() + " | Kind: " + event.kind());
		});
	}
}
