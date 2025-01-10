import java.io.*;
import java.util.*;

/* Write a program that can parse a file containing flow log data
and maps each row to a tag based on a lookup table. The lookup table
is defined as a csv file, and it has 3 columns, dstport,protocol,tag.
The dstport and protocol combination decide what tag can be applied. */
/* Author Shengqi Zhou */
public class Program {
    static String flowLog = "src/main/resources/flow-log.csv";
    static String lookUpTable = "src/main/resources/lookup-table.csv";
    static String protocolNumbers = "src/main/resources/protocol-numbers.csv";
    static String tagCount = "src/main/resources/tag-count.txt";
    static String combCount = "src/main/resources/combination-count.txt";

    // read from csv files
    private static List<List<String>> readFile(String filePath) {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    // create map for lookup table
    public static Map<String, String> lookUpMap() {
        List<List<String>> look_up = readFile(lookUpTable);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < look_up.size(); i++) {
            List<String> list = look_up.get(i);
            String key = list.get(0).toLowerCase() + "," + list.get(1).toLowerCase();
            map.put(key, list.get(2).toLowerCase());
        }
        return map;
    }

    // get the map for protocol and its number
    public static Map<String, String> protocolNum() {
        List<List<String>> protocol_num = readFile(protocolNumbers);
        Map<String, String> pro_map = new HashMap<>();
        for (int i = 0; i < protocol_num.size(); i++) {
            List<String> list = protocol_num.get(i);
            pro_map.put(list.get(0), list.get(1).toLowerCase());
        }
        return pro_map;
    }

    // create <desport, protocol> list from flow log and protocol numbers files
    public static List<List<String>> logData() {
        List<List<String>> flow_log = readFile(flowLog);
        List<List<String>> log_list = new ArrayList<>();
        Map<String, String> pro_map = protocolNum();
        for (int i = 0; i < flow_log.size(); i++) {
            List<String> list = flow_log.get(i);
            List<String> row = new ArrayList<>();
            row.add(list.get(6).toLowerCase());
            row.add(pro_map.get(list.get(7)).toLowerCase());
            log_list.add(row);
        }
        return log_list;
    }

    // write gathered output data into file
    public static void writeToFile(String filePath, Map<String, Integer> map, String type) {
        File file = new File(filePath);
        BufferedWriter bf = null;
        try {
            bf = new BufferedWriter(new FileWriter(file));
            if (type.equals("tag")) {
                bf.write("Tag,Count");
            } else if (type.equals("comb")) {
                bf.write("Port,Protocol,Count");
            }
            bf.newLine();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                bf.write(entry.getKey() + "," + entry.getValue());
                bf.newLine();
            }
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        List<List<String>> log_list = logData();
        Map<String, String> look_up = lookUpMap();

        Map<String, Integer> tag_count = new HashMap<>();
        Map<String, Integer> comb_count = new HashMap<>();

        for (int i = 0; i < log_list.size(); i++) {
            List<String> row = log_list.get(i);
            String key = row.get(0) + "," + row.get(1);
            if (look_up.containsKey(key)) {
                tag_count.put(look_up.get(key), tag_count.getOrDefault(look_up.get(key), 0) + 1);
            }
            String comb = row.get(0) + "," + row.get(1);
            comb_count.put(comb, comb_count.getOrDefault(comb, 0) + 1);
        }

        writeToFile(tagCount, tag_count, "tag");
        writeToFile(combCount, comb_count, "comb");
    }
}
