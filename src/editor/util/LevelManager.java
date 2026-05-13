package editor.util;

import editor.components.EditorComponent;
import framework.json.JsonArray;
import framework.json.JsonPair;
import framework.json.JsonReader;
import framework.json.JsonValue;

import javax.swing.JFileChooser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class LevelManager {
    private static final String LEVELS_DIR = "res/rooms";

    public static void saveGrid(int[][] grid, String filename) throws IOException {
        Path dir = Path.of(LEVELS_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    \"width\": ").append(grid[0].length).append(",\n");
        sb.append("    \"height\": ").append(grid.length).append(",\n");
        sb.append("    \"tiles\": [\n");

        for (int y = 0; y < grid.length; y++) {
            sb.append("        [");
            for (int x = 0; x < grid[y].length; x++) {
                sb.append(grid[y][x]);
                if (x < grid[y].length - 1) sb.append(", ");
            }
            sb.append("]");
            if (y < grid.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    ]\n");
        sb.append("}\n");

        Files.writeString(Path.of(LEVELS_DIR, filename), sb.toString());
    }

    public static String showSaveDialog() {
        JFileChooser chooser = new JFileChooser(LEVELS_DIR);
        chooser.setDialogTitle("Save Level");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String name = chooser.getSelectedFile().getName();
            if (!name.endsWith(".json")) {
                name = name + ".json";
            }
            return name;
        }
        return null;
    }

    public static String showLoadDialog() {
        JFileChooser chooser = new JFileChooser(LEVELS_DIR);
        chooser.setDialogTitle("Load Level");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String name = chooser.getSelectedFile().getName();
            if (!name.endsWith(".json")) {
                name = name + ".json";
            }
            return name;
        }
        return null;
    }

    public static int[][] loadGrid(String filename) {
        try (JsonReader reader = new JsonReader(LEVELS_DIR + "/" + filename)) {
            int width = 0;
            int height = 0;
            int[][] grid = null;
            
            int currentRow = 0;
            int currentCol = 0;

            Optional<JsonPair> pair;
            while (true) {
                pair = reader.getPair();
                if (pair.isEmpty()) break;

                String key = pair.get().key();
                JsonValue value = pair.get().value();

                switch (key) {
                    case "width" -> width = value.getInt();
                    case "height" -> {
                        height = value.getInt();
                        grid = new int[height][width];
                    }
                    case "tiles" -> {
                        JsonArray outerArr = value.getArray();
                        while (true) {
                            Optional<JsonValue> rowItem = outerArr.getItem();
                            if (rowItem.isEmpty()) {
                                break;
                            }

                            // open the inner array for the current row
                            JsonArray innerArr = rowItem.get().getArray();
                            while (true) {
                                Optional<JsonValue> cellItem = innerArr.getItem();
                                if (cellItem.isEmpty()) {
                                    break;
                                }

                                // now we can finally get the integer
                                int cellValue = cellItem.get().getInt();
                                if (currentRow < height && currentCol < width) {
                                    assert grid != null;
                                    grid[currentRow][currentCol] = cellValue;
                                }

                                currentCol++;
                                if (currentCol >= width) {
                                    currentCol = 0;
                                    currentRow++;
                                }
                            }
                        }
                    }
                }
            }
            return grid;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}