package rox.todd.controller;

import static org.springframework.http.MediaType.parseMediaType;
import static rox.todd.common.util.Enums.PathSort.DATE_ASC;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rox.todd.common.util.Enums.PathSort;
import rox.todd.model.DirectoryData;
import rox.todd.service.ImageService;

/**
 * A class to initiate image related services.
 */
@RestController
public class ImageController {
  
  @Autowired
  private ImageService is;

  
  @GetMapping("/directory-data")
  private List<DirectoryData> getDirectoryData() throws Exception {
    return is.getDirectoryData(true);
  }
  
  @GetMapping("/image-names/{year}")
  private List<String> getImageNames(
      @PathVariable int year,
      @RequestParam(name = "pg") Optional<Integer> page,
      @RequestParam(name = "ps") Optional<Integer> pageSize,
      @RequestParam(name = "st") Optional<PathSort> sort
      ) throws Exception {
    
    return is.getImageNames(year, page.orElse(0), pageSize.orElse(30), sort.orElse(DATE_ASC));
  }

  @GetMapping("/image/{year}/{name}")
  private ResponseEntity<Resource> getImage(
      @PathVariable int year,
      @PathVariable String name,
      @RequestParam(name="tn") Optional<Boolean> asThumbnail) throws Exception {
    
    var file = is.getImage(year, name, asThumbnail.orElse(false));
    var mimeType = is.getMimeType(name);
    var builder = ResponseEntity.ok().contentType(parseMediaType(mimeType));
    
    return builder.contentLength(Files.size(file)).body(new UrlResource(file.toUri()));
  }
}
