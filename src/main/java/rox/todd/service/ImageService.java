package rox.todd.service;

import static java.nio.file.Files.isRegularFile;
import static java.util.stream.Collectors.toList;
import static rox.todd.common.util.ClojureUtil.ClojureFunction.Search;
import static rox.todd.common.util.Enums.AllowedImageType.toAllowedImageType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import rox.todd.common.cache.AutoRefreshCache;
import rox.todd.common.util.ClojureUtil;
import rox.todd.common.util.Config;
import rox.todd.common.util.Enums;
import rox.todd.common.util.Const.ConfigPropery;
import rox.todd.common.util.Enums.AllowedImageType;
import rox.todd.common.util.Enums.PathSort;
import rox.todd.model.DirectoryData;

/**
 * A class of image related services.
 */
@Service
public class ImageService {
  private static final Logger log = LoggerFactory.getLogger(ImageService.class);
  private static final Pattern pathDelimPattern = Pattern.compile("[\\\\\\/]+");
  private static final Pattern yearPattern = Pattern.compile("\\d{4}");
  private Config config;
  private Path baseImageDir;
  private Path baseThumbnailDir;
  private AutoRefreshCache<List<DirectoryData>> dirDataCache = new AutoRefreshCache<>(args->getDirectoryData(false));
  
  @Autowired
  public ImageService(Config config) {
    this.config = config;
  }
  
  @PostConstruct
  public void initialize() {
    
    if(!config.isActive(Enums.Service.IMAGE)) {
      log.info("Skipping initialization for: " + Enums.Service.IMAGE);
      return;
    }
    
    baseImageDir = getExistingDirFromConfig(ConfigPropery.baseImageDir);
    baseThumbnailDir = getExistingDirFromConfig(ConfigPropery.baseThumbnailDir);
  }
  
  private Path getExistingDirFromConfig(String configPropertyName) {
    Path dir = Path.of(config.getOrException(configPropertyName));
    
    if(!dir.toFile().exists()) {
      throw new IllegalStateException("The '" + configPropertyName + "' config value is not a valid directory: " + dir);
    }
    
    return dir;
  }
  
  /**
   * Get information about the image directories.
   * @param useCache Indicates if a previously cached result should be used.
   * @return 
   */
  @SuppressWarnings("rawtypes")
  public List<DirectoryData> getDirectoryData(boolean useCache) {
    if(useCache) return dirDataCache.get();
    
    List<DirectoryData> result = new ArrayList<>();
    List<Object> list = Search.run(baseImageDir.toFile(), true, ClojureUtil.funct(args->false));
    if(list.size() == 1) return result;
    
    //Ideally you'd scan the file system in one go, but I'm doing an initial dir
    //scan to practice Clojure. Then I will count the files in each dir. 

    List<Integer> years = list.stream()
        .skip(1) //Skip the root dirs name
        .filter(obj->(obj instanceof List) && ((List)obj).size() > 1)
        .map(obj->((File)((List)obj).get(0)).getName())
        .filter(n->yearPattern.matcher(n).matches())
        .map(Integer::parseInt)
        .collect(toList());
    
    return years.stream().map(year ->{
      Path dir = baseImageDir.resolve(year+"");
      
      try(var stream = Files.list(dir)){
        long count = stream.filter(p->isRegularFile(p) && toAllowedImageType(p.getFileName().toString()) != null).count();
        return new DirectoryData(year, count);
      }
      catch(Exception e) {
        throw new IllegalStateException("Error while search for files in: " + dir);
      }

    }).collect(toList());
  }
  

  /**
   * @param year The year of the image.
   * @param page The page.
   * @param pageSize The page size.
   * @param sort The PathSort.
   * @return Image names contained in directory (year).
   * @throws IOException
   */
  public List<String> getImageNames(int year, int page, int pageSize, PathSort sort) throws IOException{
    Path dir = baseImageDir.resolve(year+"");
    
    try (Stream<Path> stream = Files.list(dir)) {
      return stream
              .filter(p->isRegularFile(p) && toAllowedImageType(p.getFileName().toString()) != null)
              .sorted(sort.getComparator())
              .skip((long) page * pageSize)
              .limit(pageSize)
              .map(p->p.toFile().getName())
              .collect(Collectors.toList());
    }
  }

  /**
   * @param year The year
   * @param name The name
   * @param asThumbnail Indicates if the image should be the thumbnail version
   * @return The Path of an image.
   */
  public Path getImage(int year, String name, boolean asThumbnail) {
    return validateAndGet(asThumbnail ? baseThumbnailDir : baseImageDir, year, name);
  }
  
  /**
   * @param imageName The name of the image
   * @return The mime type of the image
   */
  public String getMimeType(String imageName) {
    AllowedImageType type = AllowedImageType.toAllowedImageType(imageName);
    if(type == null) throw new IllegalArgumentException("Disallowed image type: " + imageName);
    return type.getMimeType();
  }
  
  private Path validateAndGet(Path baseDir, int year, String name) {
    if(year < 2000 || year > 2050) throw new IllegalArgumentException("Invalid year: " + year);
    if(pathDelimPattern.matcher(name).matches()) throw new IllegalArgumentException("Invalid file: " + name);
    
    Path file = baseDir.resolve(year+"").resolve(name);
    if(!Files.exists(file)) throw new IllegalArgumentException("File doesn't exist: " + file);
    return file;
  }

}
