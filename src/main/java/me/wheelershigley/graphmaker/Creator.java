package me.wheelershigley.graphmaker;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Creator {
    private static String TAB = "  ";

    private static String titleize(String title) {
        StringBuilder title_builder = new StringBuilder();

        boolean capitalize_next = true;
        for(char current : title.toCharArray() ) {
            if(current == ' ' || current == '_') {
                title_builder.append("\\n");
                capitalize_next = true;
                continue;
            }

            if(capitalize_next) {
                title_builder.append( Character.toUpperCase(current) );
            } else {
                title_builder.append( Character.toLowerCase(current) );
            }

            if(current == '-' || current == '\n') {
                capitalize_next = true;
                continue;
            }

            capitalize_next = false;
        }

        return title_builder.toString();
    }
    public static void makeGraph(SlimefunItem item) throws IOException {
        String id = item.getId();

        //ensure folder exists
        Path data_folder = Paths.get( GraphMaker.instance.getDataFolder().getAbsolutePath() );
        if(Files.notExists(data_folder) ) {
            //create folder
            new File( String.valueOf(data_folder) ).mkdir();
        }

        //check if file already exists
        data_folder = Paths.get( String.valueOf(data_folder)+ "/"+item.getId()+".dot" );
        File digraph;
        if(Files.notExists(data_folder) ) {
            //create file
            digraph = new File( String.valueOf(data_folder) );
            try {
                digraph.createNewFile();
            } catch(IOException io_exception) {
                GraphMaker.instance.getLogger().warning(GraphMaker.instance.PLUGIN_NAME_PREFIX+" §cUnable to create director, \"§4§o"+digraph.getAbsolutePath()+"§r§4\".");
                return;
            }
        }

        //construct String for file
        StringBuilder graph_code = new StringBuilder(); {
            graph_code.append("digraph " + item.getId() + " {\n");

            //body
            List<String> ids = new ArrayList<>();

                graph_code.append(TAB+id+"\n");
                ids.add(id);

                List<SlimefunItem> valid_items = Slimefun.getRegistry().getAllSlimefunItems();
                String current_id;
                HashMap<String,Integer> parts = new HashMap<>();
                for(ItemStack part : SlimefunItem.getById(id).getRecipe() ) {
                    current_id = PlainTextComponentSerializer.plainText().serialize( part.displayName() ); current_id = current_id.substring(1,current_id.length()-1).toUpperCase().replace(' ','_');
                    if( parts.containsKey(current_id) ) {
                        parts.put( current_id, parts.get(current_id)+part.getAmount() );
                    } else {
                        parts.put(current_id,part.getAmount() );
                    }
                }
                for(Map.Entry<String,Integer> part : parts.entrySet() ) {
                    current_id = part.getKey();
                    graph_code.append(TAB+TAB+current_id+"->"+id+"[label=\""+ part.getValue() +"\"]\n");
                    ids.add(current_id);
                }

            graph_code.append("\n");

                for(String untitled : ids.toArray(new String[0]) ) {
                    graph_code.append(TAB+untitled +"[label=\""+titleize(untitled)+"\"]"+ "\n");
                }

            graph_code.append("}");
        }

        //set file filled with constructed data
        List<String> lines = new ArrayList<>(); {
            StringBuilder current_line = new StringBuilder();
            for(char current : graph_code.toString().toCharArray() ) {
                if(current == '\n') {
                    lines.add( current_line.toString() );
                    current_line.setLength(0);
                } else {
                    current_line.append(current);
                }
            }
            if(0 < current_line.length() ) {
                lines.add( current_line.toString() );
            }
        }
        Files.write(data_folder, lines, StandardCharsets.UTF_8);
    }

}
