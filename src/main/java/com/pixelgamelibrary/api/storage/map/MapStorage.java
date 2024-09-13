///////////////////////////////////////////////////////////////////////////////////////////////
// Pixel: Game library.
// Copyright (C) 2024 the original author or authors.
//
// This program is free software: you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation, either version 3
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see
// <https://www.gnu.org/licenses/> or write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
///////////////////////////////////////////////////////////////////////////////////////////////

package com.pixelgamelibrary.api.storage.map;

import com.badlogic.gdx.Gdx;
import com.pixelgamelibrary.api.Pixel;
import com.pixelgamelibrary.api.Platform;
import com.pixelgamelibrary.api.storage.StorageException;
import com.pixelgamelibrary.api.storage.Storage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the Storage interface for managing a map-based file system.
 * Provides methods to interact with files and directories stored in a map.
 * 
 * @author robertvokac
 */
public class MapStorage implements Storage {

    private final SimpleMap map;
    private final MapStorageCompression mapStorageCompression;

    /**
     * Constructs a MapStorage instance with the specified map and default compression.
     * 
     * @param mapIn the map to be used for storage
     */
    public MapStorage(SimpleMap mapIn) {
        this(mapIn, MapStorageCompression.NONE);
    }

    /**
     * Constructs a MapStorage instance with the specified map and compression.
     * 
     * @param mapIn the map to be used for storage
     * @param mapStorageCompressionIn the compression method to be used
     */
    public MapStorage(SimpleMap mapIn, MapStorageCompression mapStorageCompressionIn) {
        this.map = mapIn;
        this.mapStorageCompression = mapStorageCompressionIn;
        mkdir("/");  // Initialize the root directory
    }

    private String workingDirectory = "/";

    @Override
    public Platform getPlatform() {
        // Returns null as this implementation does not specify a platform
        return null;
    }

    /**
     * Converts a path to an absolute path if it is not already absolute.
     * 
     * @param path the path to convert
     * @return the absolute path
     */
    private String convertToAbsolutePathIfNeeded(String path) {
        if (path.startsWith(SLASH)) {
            return path;
        }
        return workingDirectory + (workingDirectory.equals("/") ? "" : SLASH) + path;
    }
    
    private static final String TWO_DOTS = "..";
    private static final String SLASH = "/";
    private static final String EIGHT_COLONS = "::::::::";
    private static final String BINARYFILE = "BINARYFILE";

    @Override
    public String cd(String path) {
        // Change directory to the specified path
        String absolutePath = path.equals(TWO_DOTS) ? getParentPath(workingDirectory) : convertToAbsolutePathIfNeeded(path);

        if (!exists(absolutePath)) {
            final String msg = "Path does not exist: " + absolutePath;
            logError(msg);
            return msg;
        }
        if (!isdir(absolutePath)) {
            final String msg = "Path is not directory: " + absolutePath;
            logError(msg);
            return msg;
        }

        workingDirectory = absolutePath;
        return "";
    }

    @Override
    public String mkdir(String path) {
        // Create a new directory at the specified path
        if(path.isEmpty()) {
            String msg = "Missing argument";
            logError(msg);
            return msg;
        }
        String absolutePath = convertToAbsolutePathIfNeeded(path);
        final String parentPath = getParentPath(absolutePath);
        if (!path.equals(SLASH) && !exists(parentPath)) {
            var msg = "Cannot create new directory, because parent path does not exist: " + parentPath;
            logError(msg);
            return msg;
        }
        if (!path.equals(SLASH) && !isdir(parentPath)) {
            var msg = "Cannot create new directory, because parent path is not directory: " + parentPath;
            logError(msg);
            return msg;
        }
        if (exists(absolutePath)) {
            var msg = "Cannot create new directory, because path already exists: " + absolutePath;
            logError(msg);
            return msg;
        }
        map.putString(absolutePath, MapFileType.DIRECTORY + EIGHT_COLONS);
        return "";
    }

    /**
     * Retrieves the parent path of the given path.
     * 
     * @param path the path to get the parent of
     * @return the parent path
     * @throws StorageException if the path is null or empty
     */
    private static String getParentPath(String path) {
        if (path == null) {
            throw new StorageException("Path is null");
        }
        if (path.trim().isEmpty()) {
            throw new StorageException("Path is empty");
        }

        if (path.equals("/")) {
            return path;
        }
        String[] array = path.split(SLASH);
        if (array.length == 2) {
            return SLASH;
        }
        return path.substring(0, path.length() - 1 - array[array.length - 1].length());
    }

    @Override
    public String pwd() {
        // Return the current working directory
        return workingDirectory;
    }

    @Override
    public int depth(String path) {
        // Return the depth of the given path
        String absolutePath = convertToAbsolutePathIfNeeded(path);
        if (absolutePath.equals(SLASH)) {
            return 0;
        }
        String[] array = absolutePath.split(SLASH);
        return array.length - 1;
    }

    @Override
    public List<String> ls(String path) {
        // List all files and directories at the specified path
        int currentDepth = depth(path);
        return map
                .keyList()
                .stream()
                .filter(key -> key.startsWith(workingDirectory))
                .filter(key -> depth(key) == (currentDepth + 1))
                .collect(Collectors.toList());
    }

    @Override
    public String touch(String path) {
        return touch(path, "");
    }

    public String touch(String path, String content) {
        // Create a new file at the specified path with optional content
        String absolutePath = convertToAbsolutePathIfNeeded(path);
        final String parentPath = getParentPath(absolutePath);
        if (!exists(parentPath)) {
            var msg = "Cannot create new file, because parent path does not exist: " + parentPath;
            logError(msg);
            return msg;
        }
        if (!isdir(parentPath)) {
            var msg = "Cannot create new file, because parent path is not directory: " + parentPath;
            logError(msg);
            return msg;
        }
        if (exists(absolutePath)) {
            var msg = "Cannot create new file, because path already exists: " + absolutePath;
            logError(msg);
            return msg;
        }
        map.putString(absolutePath, MapFileType.FILE + EIGHT_COLONS + content);
        return "";
    }

    @Override
    public boolean rm(String path) {
        // Remove the file or directory at the specified path
        String absolutePath = convertToAbsolutePathIfNeeded(path);

        if (!map.contains(absolutePath)) {
            logError("Cannot remove file, because it does not exist: " + absolutePath);
            return false;
        }
        map.remove(absolutePath);
        return true;
    }

    @Override
    public String cp(String source, String target) {
        return moveOrCp(source, target, false, true);
    }

    @Override
    public String mv(String source, String target) {
        return moveOrCp(source, target, true, false);
    }

    /**
     * Moves or copies a file from the source path to the target path.
     * 
     * @param source the source path
     * @param target the target path
     * @param move whether to move the file (true) or copy it (false)
     * @param cp whether to copy the file (true) or move it (false)
     * @return an empty string if successful or an error message
     */
    private String moveOrCp(String source, String target, boolean move, boolean cp) {
        if (move && cp) {
            throw new StorageException("move == true && cp == true");
        }
        if (!move && !cp) {
            throw new StorageException("move != true && cp != true");
        }
        String absolutePathSource = convertToAbsolutePathIfNeeded(source);
        String absolutePathTarget = convertToAbsolutePathIfNeeded(target);
        String targetParentPath = getParentPath(absolutePathTarget);

        if (!exists(absolutePathSource)) {
            final String msg = "absolutePathSource does not exist: " + absolutePathSource;
            logError(msg);
            return msg;
        }
        if (isdir(absolutePathSource)) {
            final String msg = "absolutePathSource is directory: " + absolutePathSource;
            logError(msg);
            return msg;
        }
        if (!exists(targetParentPath)) {
            final String msg = "targetParentPath does not exist: " + absolutePathSource;
            logError(msg);
            return msg;
        }
        if (!isdir(targetParentPath)) {
            final String msg = "targetParentPath is not directory: " + absolutePathSource;
            logError(msg);
            return msg;
        }
        String contentOfSourceFile = map.getString(absolutePathSource);
        String result = touch(absolutePathTarget);
        if (!result.isEmpty()) {
            var msg = "Creating new file failed: " + absolutePathTarget;
            logError(msg);
            return msg;
        }
        map.remove(absolutePathTarget);
        map.putString(absolutePathTarget, contentOfSourceFile);
        if (move) {
            map.remove(absolutePathSource);
        }
        return "";
    }

    @Override
    public String readtext(String path) {
        // Read the text content of a file at the specified path
        String absolutePath = convertToAbsolutePathIfNeeded(path);
        if (!exists(absolutePath)) {
            logError("absolutePathSource does not exist: " + absolutePath);
            return null;
        }
        if (isdir(absolutePath)) {
            logError("absolutePathSource is directory: " + absolutePath);
            return null;
        }
        String value = map.getString(absolutePath);
        return value.split(EIGHT_COLONS)[1];
    }

    @Override
    public byte[] readbin(String path) {
        // Read binary data from a file at the specified path
        String absolutePath = convertToAbsolutePathIfNeeded(path);
        String text = readtext(absolutePath);
        if (!text.startsWith(BINARYFILE)) {
            logError("File is not binary: " + absolutePath);
            return null;
        }
        text = text.substring(BINARYFILE.length());
        return Pixel.utils().decodeBase64AsByteArray(text);
    }

    @Override
    public String savetext(String name, String text) {
        return touch(name, text);
    }

    @Override
    public String savebin(String name, byte[] data) {
        return savetext(name, BINARYFILE + Pixel.utils().encodeToBase64(data));
    }

    @Override
    public boolean exists(String name) {
        // Check if the path exists in the map
        return map.contains(convertToAbsolutePathIfNeeded(name));
    }

    @Override
    public boolean isfile(String name) {
        // Check if the path is a file
        return filetype(name) == MapFileType.FILE;
    }

    @Override
    public boolean isdir(String name) {
        // Check if the path is a directory
        return filetype(name) == MapFileType.DIRECTORY;
    }

    public MapFileType filetype(String name) {
        // Get the file type for the given path
        return MapFileType.ofKey(convertToAbsolutePathIfNeeded(name), map);
    }

    @Override
    public String debug() {
        // Return a debug string of all keys and their values
        StringBuilder sb = new StringBuilder();
        for(String key: map.keyList()) {
            sb
                    .append(key)
                    .append("=")
                    .append(map.getString(key))
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    public void flush() {
        // Flush the map to persist changes
        map.flush();
    }

    @Override
    public boolean rmdir(String dirname) {
        // Remove directory is not supported
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    /**
     * Logs an error message using the Pixel application logging mechanism.
     * 
     * @param msg the error message to log
     */
    private void logError(String msg) {
        Pixel.app().error(msg);
    }
}
